/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.local.player

import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.toBitmap
import dev.zacsweers.metro.Inject
import io.element.android.libraries.architecture.bindings
import io.element.android.libraries.core.data.tryOrNull
import io.element.android.libraries.matrix.api.MatrixClientProvider
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.ui.media.MediaRequestData
import io.element.android.libraries.mediaviewer.api.local.LocalMediaFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

@OptIn(UnstableApi::class)
class MediaPlaybackService : MediaSessionService() {
    @Inject lateinit var matrixClientProvider: MatrixClientProvider
    @Inject lateinit var localMediaFactory: LocalMediaFactory

    private var mediaSession: MediaSession? = null
    private var exoPlayer: ExoPlayer? = null
    private var forwardingPlayer: SkipEnabledForwardingPlayer? = null
    private var playlistManager: MediaPlaylistManager? = null
    private var serviceScope: CoroutineScope? = null

    override fun onCreate() {
        super.onCreate()
        bindings<MediaPlaybackServiceBindings>().inject(this)

        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        serviceScope = scope

        val player = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                // handleAudioFocus
                true
            )
            .setHandleAudioBecomingNoisy(true)
            .build()
        exoPlayer = player

        val skipPlayer = SkipEnabledForwardingPlayer(
            player = player,
            onSkipToNext = { scope.launch { handleSkipToNext() } },
            onSkipToPrevious = { scope.launch { handleSkipToPrevious() } },
        )
        forwardingPlayer = skipPlayer

        val manager = MediaPlaylistManager(
            matrixClientProvider = matrixClientProvider,
            localMediaFactory = localMediaFactory,
            coroutineScope = scope,
            onPlayableItemsChanged = { updateSkipButtonState() },
        )
        playlistManager = manager

        mediaSession = MediaSession.Builder(this, skipPlayer).build()

        player.addListener(object : Player.Listener {
            private var hasInjectedNotificationMetadata = false

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    scope.launch { handlePlaybackEnded() }
                }
                // If no embedded metadata was extracted (bare file), inject notification metadata when playback starts
                if (playbackState == Player.STATE_READY && !hasInjectedNotificationMetadata) {
                    val currentMediaItem = player.currentMediaItem ?: return
                    val extras = currentMediaItem.mediaMetadata.extras
                    // If we have our extras but no title was extracted, we need to inject notification metadata
                    val hasOurExtras = extras?.containsKey("notificationTitle") == true
                    val hasExtractedTitle = player.mediaMetadata.title?.isNotEmpty() == true
                    if (hasOurExtras && !hasExtractedTitle) {
                        hasInjectedNotificationMetadata = true
                        scope.launch { injectNotificationMetadataFromExtras(currentMediaItem) }
                    }
                }
            }

            override fun onMediaMetadataChanged(metadata: MediaMetadata) {
                updateSessionActivity(metadata)
                initializePlaylistFromMetadata(metadata)
                // If embedded metadata was extracted (has title but no custom sessionId in the metadata EXTRAS),
                // inject notification metadata for the MediaStyle notification.
                // Note: We check metadata.extras (not currentMediaItem.extras) because this is the parsed metadata.
                val extras = metadata.extras
                val hasOurExtras = extras?.containsKey("sessionId") == true
                if (!hasOurExtras && metadata.title?.isNotEmpty() == true && !hasInjectedNotificationMetadata) {
                    hasInjectedNotificationMetadata = true
                    scope.launch { injectNotificationMetadataFromExtras(player.currentMediaItem) }
                }
            }
        })
    }

    private fun initializePlaylistFromMetadata(metadata: MediaMetadata) {
        val extras = metadata.extras ?: return
        val sessionId = extras.getString("sessionId") ?: return
        val roomId = extras.getString("roomId") ?: return
        val eventId = extras.getString("eventId") ?: return

        playlistManager?.initialize(SessionId(sessionId), RoomId(roomId), EventId(eventId))
        updateSkipButtonState()
    }

    private suspend fun injectNotificationMetadataFromExtras(currentMediaItem: MediaItem?) {
        val player = exoPlayer ?: return
        val mediaItem = currentMediaItem ?: player.currentMediaItem ?: return
        // Get notification metadata from the extras we sent from UI
        val originalExtras = mediaItem.mediaMetadata.extras ?: return
        val sessionId = originalExtras.getString("sessionId") ?: return
        val roomId = originalExtras.getString("roomId") ?: return
        val eventId = originalExtras.getString("eventId") ?: return
        val notificationTitle = originalExtras.getString("notificationTitle") ?: return
        val notificationArtist = originalExtras.getString("notificationArtist")

        // Load artwork - either from notificationArtwork (room avatar for audio) or notificationThumbnailUrl (video thumbnail)
        val artworkUrl = originalExtras.getString("notificationArtwork")
            ?: originalExtras.getString("notificationThumbnailUrl")
        val artworkBytes = artworkUrl?.let { url ->
            tryOrNull {
                val source = MediaSource(url)
                val request = ImageRequest.Builder(this)
                    .data(MediaRequestData(source, MediaRequestData.Kind.Thumbnail(256, 256)))
                    .build()
                val result = imageLoader.execute(request)
                result.image?.toBitmap()?.let { bitmap ->
                    ByteArrayOutputStream().use { stream ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                        stream.toByteArray()
                    }
                }
            }
        }

        // Build extras with navigation context (for tap intent)
        val extras = Bundle().apply {
            putString("sessionId", sessionId)
            putString("roomId", roomId)
            putString("eventId", eventId)
            // Marker to distinguish injected notification metadata from embedded metadata
            putString("isInjectedNotification", "true")
        }

        // Build notification metadata - this goes to the notification only, not to ExoPlayer's embedded
        val notificationMetadata = MediaMetadata.Builder()
            .setTitle(notificationTitle)
            .apply { notificationArtist?.let { setArtist(it) } }
            .apply {
                if (artworkBytes != null) {
                    setArtworkData(artworkBytes, MediaMetadata.PICTURE_TYPE_FRONT_COVER)
                }
            }
            .setExtras(extras)
            .build()

        // Create new MediaItem with notification metadata
        val newMediaItem = MediaItem.Builder()
            .setMediaId(mediaItem.mediaId)
            .setUri(mediaItem.localConfiguration?.uri ?: return)
            .setMediaMetadata(notificationMetadata)
            .build()

        // Replace the current item - this updates the notification but NOT the embedded metadata shown in UI
        player.setMediaItem(newMediaItem)
        player.prepare()
        // updateSessionActivity will be called again via onMediaMetadataChanged, now with our custom extras
    }

    private fun updateSkipButtonState() {
        val manager = playlistManager ?: return
        forwardingPlayer?.canSkipNext = manager.hasNext
        forwardingPlayer?.canSkipPrev = manager.hasPrevious
    }

    private suspend fun handleSkipToNext() {
        val result = playlistManager?.skipToNext()
        if (result != null) {
            applySkipResult(result)
        }
    }

    private suspend fun handleSkipToPrevious() {
        val result = playlistManager?.skipToPrevious()
        if (result != null) {
            applySkipResult(result)
        }
    }

    private fun applySkipResult(result: MediaPlaylistManager.SkipResult) {
        val player = exoPlayer ?: return
        player.setMediaItem(result.mediaItem)
        player.prepare()
        player.play()
        updateSkipButtonState()
    }

    private fun handlePlaybackEnded() {
        stopSelf()
    }

    @OptIn(UnstableApi::class)
    private fun updateSessionActivity(metadata: MediaMetadata) {
        val extras = metadata.extras ?: return
        val sessionId = extras.getString("sessionId") ?: return
        val roomId = extras.getString("roomId") ?: return
        val eventId = extras.getString("eventId") ?: return

        val deepLinkUri = Uri.parse("elementx://open/$sessionId/$roomId//$eventId?media=true")
        val intent = Intent(Intent.ACTION_VIEW, deepLinkUri).apply {
            setPackage(packageName)
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        mediaSession?.setSessionActivity(pendingIntent)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (player == null || !player.playWhenReady) {
            stopSelf()
        }
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        playlistManager?.release()
        playlistManager = null
        mediaSession?.run {
            player.release()
            release()
        }
        mediaSession = null
        exoPlayer = null
        forwardingPlayer = null
        serviceScope?.cancel()
        serviceScope = null
        super.onDestroy()
    }
}

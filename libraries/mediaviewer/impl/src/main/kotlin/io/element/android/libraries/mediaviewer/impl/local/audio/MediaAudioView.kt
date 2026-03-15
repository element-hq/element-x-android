/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.local.audio

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.isVisible
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.bumble.appyx.core.node.LocalNodeTargetVisibility
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.components.media.WaveformPlaybackView
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.text.toDp
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.mediaviewer.api.MediaInfo
import io.element.android.libraries.mediaviewer.api.helper.formatFileExtensionAndSize
import io.element.android.libraries.mediaviewer.api.local.LocalMedia
import io.element.android.libraries.mediaviewer.impl.local.LocalMediaViewState
import io.element.android.libraries.mediaviewer.impl.local.PlayableState
import io.element.android.libraries.mediaviewer.impl.local.player.LocalMediaPlaybackContext
import io.element.android.libraries.mediaviewer.impl.local.player.MediaPlayerControllerState
import io.element.android.libraries.mediaviewer.impl.local.player.MediaPlayerControllerView
import io.element.android.libraries.mediaviewer.impl.local.player.rememberMediaServicePlayer
import io.element.android.libraries.mediaviewer.impl.local.player.seekToEnsurePlaying
import io.element.android.libraries.mediaviewer.impl.local.player.togglePlay
import io.element.android.libraries.mediaviewer.impl.local.rememberLocalMediaViewState
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay

@SuppressLint("UnsafeOptInUsageError")
@Composable
fun MediaAudioView(
    localMediaViewState: LocalMediaViewState,
    bottomPaddingInPixels: Int,
    localMedia: LocalMedia?,
    info: MediaInfo?,
    modifier: Modifier = Modifier,
    isDisplayed: Boolean = true,
) {
    val player = rememberMediaServicePlayer()
    if (player != null) {
        ServicePlayerMediaAudioView(
            isDisplayed = isDisplayed,
            localMediaViewState = localMediaViewState,
            bottomPaddingInPixels = bottomPaddingInPixels,
            player = player,
            localMedia = localMedia,
            info = info,
            modifier = modifier,
        )
    }
}

@SuppressLint("UnsafeOptInUsageError")
@Composable
private fun ServicePlayerMediaAudioView(
    isDisplayed: Boolean,
    localMediaViewState: LocalMediaViewState,
    bottomPaddingInPixels: Int,
    player: Player,
    localMedia: LocalMedia?,
    info: MediaInfo?,
    modifier: Modifier = Modifier,
) {
    var mediaPlayerControllerState: MediaPlayerControllerState by remember {
        mutableStateOf(
            MediaPlayerControllerState(
                isVisible = true,
                isPlaying = player.isPlaying,
                isReady = player.playbackState == Player.STATE_READY,
                progressInMillis = player.currentPosition,
                durationInMillis = player.duration.takeIf { it >= 0 } ?: 0L,
                canMute = false,
                isMuted = player.volume == 0f,
                seekingToMillis = null,
            )
        )
    }
    // Track when playback is requested for a specific media ID
    var pendingPlaybackMediaId by remember { mutableStateOf<String?>(null) }

    // Track the displayed filename — updated when service skips to a different file
    var displayFilename: String? by remember { mutableStateOf(info?.filename) }
    var displayFileExtension: String? by remember { mutableStateOf(info?.fileExtension) }
    // Reset displayed info when the page-level info changes (user navigated)
    LaunchedEffect(info?.filename) {
        displayFilename = info?.filename
        displayFileExtension = info?.fileExtension
    }

    var metadata: MediaMetadata? by remember {
        mutableStateOf(null)
    }

    val isTargetVisible = LocalNodeTargetVisibility.current

    val playableState: PlayableState.Playable by remember {
        derivedStateOf {
            PlayableState.Playable(
                isShowingControls = mediaPlayerControllerState.isVisible,
            )
        }
    }

    localMediaViewState.playableState = playableState

    val playerListener = remember {
        object : Player.Listener {
            override fun onRenderedFirstFrame() {
                localMediaViewState.isReady = true
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                val currentMediaId = player.currentMediaItem?.mediaId
                // Show Playing if: actually playing, OR we're transitioning to expected new media
                val isExpectedMedia = currentMediaId == pendingPlaybackMediaId

                val shouldShowPlaying = isPlaying || isExpectedMedia

                mediaPlayerControllerState = mediaPlayerControllerState.copy(
                    isPlaying = shouldShowPlaying,
                )

                if (isPlaying && isExpectedMedia) {
                    pendingPlaybackMediaId = null
                }
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                pendingPlaybackMediaId = null
                mediaPlayerControllerState = mediaPlayerControllerState.copy(
                    isPlaying = false,
                    isReady = false,
                )
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                mediaPlayerControllerState = mediaPlayerControllerState.copy(
                    isReady = playbackState == Player.STATE_READY,
                )
            }

            override fun onTimelineChanged(timeline: Timeline, reason: Int) {
                if (reason == Player.TIMELINE_CHANGE_REASON_SOURCE_UPDATE) {
                    player.duration.takeIf { it >= 0 }
                        ?.let {
                            mediaPlayerControllerState = mediaPlayerControllerState.copy(
                                durationInMillis = it,
                            )
                        }
                }
            }

            override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                // Only update UI metadata from file-embedded metadata (e.g. ID3 tags),
                // not from our injected notification metadata which has "isInjectedNotification" marker.
                if (mediaMetadata.extras?.containsKey("isInjectedNotification") != true) {
                    metadata = mediaMetadata
                }
            }

            override fun onMediaItemTransition(mediaItem: androidx.media3.common.MediaItem?, reason: Int) {
                val title = mediaItem?.mediaMetadata?.title?.toString()
                // Only update displayFilename if the title matches the current page's expected filename,
                // or if info is null (we don't have an expected filename).
                // This prevents stale media item titles from overwriting the correct filename when swiping.
                val expectedFilename = info?.filename
                if (title != null && (expectedFilename == null || title == expectedFilename)) {
                    displayFilename = title
                    displayFileExtension = title.substringAfterLast('.', "")
                }
            }
        }
    }

    LaunchedEffect(player.isPlaying, isDisplayed) {
        if (player.isPlaying) {
            while (true) {
                val position = player.currentPosition
                val seekingTo = mediaPlayerControllerState.seekingToMillis
                mediaPlayerControllerState = mediaPlayerControllerState.copy(
                    progressInMillis = position,
                    seekingToMillis = if (seekingTo != null && position >= seekingTo) null else seekingTo,
                )
                delay(200)
            }
        } else {
            // Ensure we render the final state
            val position = player.currentPosition
            val seekingTo = mediaPlayerControllerState.seekingToMillis
            mediaPlayerControllerState = mediaPlayerControllerState.copy(
                progressInMillis = position,
                seekingToMillis = if (seekingTo != null && position >= seekingTo) null else seekingTo,
            )
        }
    }
    LaunchedEffect(isDisplayed) {
        // If not displayed, make sure to pause the audio
        if (!isDisplayed) {
            player.pause()
        }
    }
    LaunchedEffect(isTargetVisible) {
        if (!isTargetVisible) {
            player.pause()
        }
    }
    val playbackContext = LocalMediaPlaybackContext.current
    val context = LocalContext.current
    // Use localMedia.uri and isDisplayed as keys - ensures metadata loads when page becomes visible after settling
    if (localMedia?.uri != null) {
        // Sender's avatar for audio notification artwork (not room avatar)
        val senderAvatarUrl = localMedia.info.senderAvatar
        LaunchedEffect(localMedia.uri, isDisplayed, senderAvatarUrl) {
            if (!isDisplayed) return@LaunchedEffect
            // Step 1: Send bare MediaItem with ONLY extras - let ExoPlayer extract embedded metadata
            // (title/artist/artwork). Service will inject notification metadata after embedded is extracted.
            val hasValidContext = playbackContext.sessionId.isNotEmpty()
            val extras = if (hasValidContext) {
                Bundle().apply {
                    putString("sessionId", playbackContext.sessionId)
                    putString("roomId", playbackContext.roomId)
                    putString("eventId", playbackContext.eventId)
                    // Signal that notification metadata should be injected by the service
                    putString("notificationTitle", info?.filename ?: localMedia.info.filename)
                    putString("notificationArtist", info?.senderName ?: localMedia.info.senderName)
                    senderAvatarUrl?.let { putString("notificationArtwork", it) }
                }
            } else {
                null
            }
            // Send minimal MediaItem - no title/artist/artwork so ExoPlayer extracts embedded
            val mediaMetadata = extras?.let { MediaMetadata.Builder().setExtras(it).build() }
                ?: MediaMetadata.EMPTY
            val mediaId = if (hasValidContext) playbackContext.eventId else localMedia.uri.toString()
            val mediaItem = MediaItem.Builder()
                .setMediaId(mediaId)
                .setUri(localMedia.uri)
                .setMediaMetadata(mediaMetadata)
                .build()
            if (player.currentMediaItem?.mediaId == mediaId) {
                // Same item already loaded — don't reset
                // Sync UI state with actual player state
                mediaPlayerControllerState = mediaPlayerControllerState.copy(
                    isPlaying = player.isPlaying,
                    isReady = player.playbackState == Player.STATE_READY,
                )
            } else {
                // Set pending playback BEFORE changing media to prevent flicker
                pendingPlaybackMediaId = mediaId
                player.setMediaItem(mediaItem)
                player.prepare()
                // Reset progress when opening a new file
                mediaPlayerControllerState = mediaPlayerControllerState.copy(
                    progressInMillis = 0L,
                    durationInMillis = 0L,
                )
            }
        }
    } else if (!isDisplayed) {
        // Don't clear media items when not displayed
    } else {
        // Don't clear media items when localMedia is null - they may still be playing in background
    }
    val waveform = info?.waveform
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ElementTheme.colors.bgSubtlePrimary),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                if (LocalInspectionMode.current) {
                    Text(
                        modifier = Modifier
                            .padding(16.dp)
                            .width(240.dp),
                        text = "An audio Player may render an image here if the audio file contains some artwork.",
                        textAlign = TextAlign.Center,
                        color = ElementTheme.colors.textPrimary,
                    )
                } else {
                    AndroidView(
                        modifier = Modifier
                            .clip(shape = RoundedCornerShape(12.dp))
                            .clipToBounds()
                            .width(240.dp),
                        factory = {
                            PlayerView(context).apply {
                                this.player = player
                                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                                layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
                                useController = false
                            }
                        },
                        update = { playerView ->
                            playerView.isVisible = metadata.hasArtwork() && isTargetVisible
                        },
                        onRelease = { playerView ->
                            playerView.player = null
                        },
                    )
                }
                if (waveform != null) {
                    WaveformPlaybackView(
                        modifier = Modifier
                            .height(48.dp),
                        playbackProgress = mediaPlayerControllerState.progressAsFloat,
                        showCursor = true,
                        waveform = waveform.toImmutableList(),
                        onSeek = {
                            player.seekToEnsurePlaying((it * player.duration).toLong())
                        },
                        seekEnabled = true,
                    )
                } else {
                    if (!metadata.hasArtwork()) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(ElementTheme.colors.iconPrimary),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = CompoundIcons.Audio(),
                                contentDescription = null,
                                tint = ElementTheme.colors.iconOnSolidPrimary,
                                modifier = Modifier
                                    .size(32.dp),
                            )
                        }
                    }
                }
            }
            if (waveform == null) {
                // Display the info below the player
                AudioInfoView(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    info = info,
                    metadata = metadata,
                    displayFilename = displayFilename,
                    displayFileExtension = displayFileExtension,
                )
            }
        }
        MediaPlayerControllerView(
            state = mediaPlayerControllerState,
            onTogglePlay = {
                player.togglePlay()
            },
            onSeekChange = {
                mediaPlayerControllerState = mediaPlayerControllerState.copy(
                    seekingToMillis = it.toLong(),
                )
                player.seekToEnsurePlaying(it.toLong())
            },
            onToggleMute = {
                // Cannot happen for audio files
            },
            // Pass null: the service's ExoPlayer handles audio focus via handleAudioFocus=true.
            // Passing audioFocus here would cause a second AudioManager.requestAudioFocus() call
            // that conflicts with ExoPlayer's internal focus request, instantly pausing playback.
            audioFocus = null,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = bottomPaddingInPixels.toDp()),
        )
    }

    DisposableEffect(player) {
        player.addListener(playerListener)
        onDispose {
            player.removeListener(playerListener)
            player.release()
        }
    }
}

@Composable
private fun AudioInfoView(
    info: MediaInfo?,
    metadata: MediaMetadata?,
    displayFilename: String?,
    displayFileExtension: String?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Render the info about the file and from the metadata
        val metaDataInfo = metadata.buildInfo()
        if (metaDataInfo.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = metaDataInfo,
                style = ElementTheme.typography.fontBodyMdRegular,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = ElementTheme.colors.textPrimary
            )
        }
        val filename = displayFilename ?: info?.filename
        val fileExtension = displayFileExtension ?: info?.fileExtension
        if (filename != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = filename,
                maxLines = 2,
                style = ElementTheme.typography.fontBodyLgRegular,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                color = ElementTheme.colors.textPrimary
            )
            if (fileExtension != null) {
                val formattedFileSize = info?.formattedFileSize ?: ""
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatFileExtensionAndSize(fileExtension, formattedFileSize),
                    style = ElementTheme.typography.fontBodyMdRegular,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = ElementTheme.colors.textPrimary
                )
            }
        }
    }
}

@PreviewsDayNight
@Composable
internal fun MediaAudioViewPreview(
    @PreviewParameter(MediaInfoAudioProvider::class) info: MediaInfo
) = ElementPreview {
    MediaAudioView(
        modifier = Modifier.fillMaxSize(),
        bottomPaddingInPixels = 0,
        localMediaViewState = rememberLocalMediaViewState(),
        info = info,
        localMedia = null,
    )
}

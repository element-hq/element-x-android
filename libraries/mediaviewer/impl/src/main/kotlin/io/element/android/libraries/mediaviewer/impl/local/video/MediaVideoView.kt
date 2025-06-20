/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.local.video

import android.annotation.SuppressLint
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Player.STATE_READY
import androidx.media3.common.Timeline
import androidx.media3.common.VideoSize
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.audio.api.AudioFocus
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.text.toDp
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.utils.KeepScreenOn
import io.element.android.libraries.designsystem.utils.OnLifecycleEvent
import io.element.android.libraries.mediaviewer.api.local.LocalMedia
import io.element.android.libraries.mediaviewer.impl.local.LocalMediaViewState
import io.element.android.libraries.mediaviewer.impl.local.PlayableState
import io.element.android.libraries.mediaviewer.impl.local.player.MediaPlayerControllerState
import io.element.android.libraries.mediaviewer.impl.local.player.MediaPlayerControllerView
import io.element.android.libraries.mediaviewer.impl.local.player.rememberExoPlayer
import io.element.android.libraries.mediaviewer.impl.local.player.seekToEnsurePlaying
import io.element.android.libraries.mediaviewer.impl.local.player.togglePlay
import io.element.android.libraries.mediaviewer.impl.local.rememberLocalMediaViewState
import kotlinx.coroutines.delay
import me.saket.telephoto.zoomable.ZoomableContentLocation
import me.saket.telephoto.zoomable.zoomable
import kotlin.time.Duration.Companion.seconds

@SuppressLint("UnsafeOptInUsageError")
@Composable
fun MediaVideoView(
    isDisplayed: Boolean,
    localMediaViewState: LocalMediaViewState,
    bottomPaddingInPixels: Int,
    localMedia: LocalMedia?,
    autoplay: Boolean,
    audioFocus: AudioFocus?,
    modifier: Modifier = Modifier,
) {
    val exoPlayer = rememberExoPlayer()
    ExoPlayerMediaVideoView(
        isDisplayed = isDisplayed,
        localMediaViewState = localMediaViewState,
        bottomPaddingInPixels = bottomPaddingInPixels,
        exoPlayer = exoPlayer,
        localMedia = localMedia,
        autoplay = autoplay,
        audioFocus = audioFocus,
        modifier = modifier,
    )
}

@SuppressLint("UnsafeOptInUsageError")
@Composable
private fun ExoPlayerMediaVideoView(
    isDisplayed: Boolean,
    localMediaViewState: LocalMediaViewState,
    bottomPaddingInPixels: Int,
    exoPlayer: ExoPlayer,
    localMedia: LocalMedia?,
    autoplay: Boolean,
    audioFocus: AudioFocus?,
    modifier: Modifier = Modifier,
) {
    var mediaPlayerControllerState: MediaPlayerControllerState by remember {
        mutableStateOf(
            MediaPlayerControllerState(
                isVisible = true,
                isPlaying = false,
                isReady = false,
                progressInMillis = 0,
                durationInMillis = 0,
                canMute = true,
                isMuted = false,
            )
        )
    }

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
                mediaPlayerControllerState = mediaPlayerControllerState.copy(
                    isPlaying = isPlaying,
                )
            }

            override fun onVolumeChanged(volume: Float) {
                mediaPlayerControllerState = mediaPlayerControllerState.copy(
                    isMuted = volume == 0f,
                )
            }

            override fun onTimelineChanged(timeline: Timeline, reason: Int) {
                if (reason == Player.TIMELINE_CHANGE_REASON_SOURCE_UPDATE) {
                    exoPlayer.duration.takeIf { it >= 0 }
                        ?.let {
                            mediaPlayerControllerState = mediaPlayerControllerState.copy(
                                durationInMillis = it,
                            )
                        }
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                mediaPlayerControllerState = mediaPlayerControllerState.copy(
                    isReady = playbackState == STATE_READY,
                )
            }

            override fun onVideoSizeChanged(videoSize: VideoSize) {
                // Ensure that the user cannot zoom/move outside of the video bounds
                localMediaViewState.zoomableState.setContentLocation(
                    ZoomableContentLocation.scaledInsideAndCenterAligned(
                        Size(
                            videoSize.width.toFloat(),
                            videoSize.height.toFloat(),
                        )
                    )
                )
            }
        }
    }

    var autoHideController by remember { mutableIntStateOf(0) }

    LaunchedEffect(autoHideController) {
        delay(5.seconds)
        if (exoPlayer.isPlaying) {
            mediaPlayerControllerState = mediaPlayerControllerState.copy(
                isVisible = false,
            )
        }
    }

    LaunchedEffect(exoPlayer.isPlaying) {
        if (exoPlayer.isPlaying) {
            while (true) {
                mediaPlayerControllerState = mediaPlayerControllerState.copy(
                    progressInMillis = exoPlayer.currentPosition,
                )
                delay(200)
            }
        } else {
            // Ensure we render the final state
            mediaPlayerControllerState = mediaPlayerControllerState.copy(
                progressInMillis = exoPlayer.currentPosition,
            )
        }
    }

    var needsAutoPlay by remember { mutableStateOf(autoplay) }

    LaunchedEffect(needsAutoPlay, isDisplayed, mediaPlayerControllerState.isReady) {
        val isReadyAndNotPlaying = mediaPlayerControllerState.isReady && !mediaPlayerControllerState.isPlaying
        if (needsAutoPlay && isDisplayed && isReadyAndNotPlaying) {
            // When displayed, start autoplaying
            exoPlayer.play()
            needsAutoPlay = false
        } else if (!isDisplayed && mediaPlayerControllerState.isPlaying) {
            // If not displayed, make sure to pause the video
            exoPlayer.pause()
        }
    }
    if (localMedia?.uri != null) {
        LaunchedEffect(localMedia.uri) {
            val mediaItem = MediaItem.fromUri(localMedia.uri)
            exoPlayer.setMediaItem(mediaItem)
        }
    } else {
        exoPlayer.setMediaItems(emptyList())
    }
    KeepScreenOn(mediaPlayerControllerState.isPlaying)
    Box(
        modifier = modifier
            .background(ElementTheme.colors.bgSubtlePrimary),
    ) {
        val context = LocalContext.current
        if (LocalInspectionMode.current) {
            Text(
                modifier = Modifier
                    .background(ElementTheme.colors.bgSubtlePrimary)
                    .align(Alignment.Center),
                text = "A Video Player will render here",
            )
        } else {
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .zoomable(
                        state = localMediaViewState.zoomableState,
                        onClick = {
                            autoHideController++
                            mediaPlayerControllerState = mediaPlayerControllerState.copy(
                                isVisible = !mediaPlayerControllerState.isVisible,
                            )
                        }
                    ),
                factory = {
                    PlayerView(context).apply {
                        player = exoPlayer
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                        layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                        useController = false
                    }
                },
                onRelease = { playerView ->
                    playerView.player = null
                },
            )
        }
        MediaPlayerControllerView(
            state = mediaPlayerControllerState,
            onTogglePlay = {
                autoHideController++
                exoPlayer.togglePlay()
            },
            onSeekChange = {
                autoHideController++
                exoPlayer.seekToEnsurePlaying(it.toLong())
            },
            onToggleMute = {
                autoHideController++
                exoPlayer.volume = if (exoPlayer.volume == 1f) 0f else 1f
            },
            audioFocus = audioFocus,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = bottomPaddingInPixels.toDp()),
        )
    }

    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_CREATE -> exoPlayer.addListener(playerListener)
            Lifecycle.Event.ON_RESUME -> exoPlayer.prepare()
            Lifecycle.Event.ON_PAUSE -> exoPlayer.pause()
            Lifecycle.Event.ON_DESTROY -> {
                exoPlayer.release()
                exoPlayer.removeListener(playerListener)
            }
            else -> Unit
        }
    }
}

@PreviewsDayNight
@Composable
internal fun MediaVideoViewPreview() = ElementPreview {
    MediaVideoView(
        isDisplayed = true,
        modifier = Modifier.fillMaxSize(),
        bottomPaddingInPixels = 0,
        localMediaViewState = rememberLocalMediaViewState(),
        localMedia = null,
        audioFocus = null,
        autoplay = false,
    )
}

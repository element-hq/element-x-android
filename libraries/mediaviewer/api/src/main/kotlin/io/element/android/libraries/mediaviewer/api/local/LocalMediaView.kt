/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.api.local

import android.annotation.SuppressLint
import android.net.Uri
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.core.bool.orFalse
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.core.mimetype.MimeTypes.isMimeTypeAudio
import io.element.android.libraries.core.mimetype.MimeTypes.isMimeTypeImage
import io.element.android.libraries.core.mimetype.MimeTypes.isMimeTypeVideo
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.utils.CommonDrawables
import io.element.android.libraries.designsystem.utils.KeepScreenOn
import io.element.android.libraries.designsystem.utils.OnLifecycleEvent
import io.element.android.libraries.mediaviewer.api.helper.formatFileExtensionAndSize
import io.element.android.libraries.mediaviewer.api.local.exoplayer.ExoPlayerWrapper
import io.element.android.libraries.mediaviewer.api.local.pdf.PdfViewer
import io.element.android.libraries.mediaviewer.api.local.pdf.rememberPdfViewerState
import io.element.android.libraries.mediaviewer.api.player.MediaPlayerControllerState
import io.element.android.libraries.mediaviewer.api.player.MediaPlayerControllerView
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.coroutines.delay
import me.saket.telephoto.zoomable.coil.ZoomableAsyncImage
import me.saket.telephoto.zoomable.rememberZoomableImageState
import kotlin.time.Duration.Companion.seconds

@Composable
fun LocalMediaView(
    localMedia: LocalMedia?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    localMediaViewState: LocalMediaViewState = rememberLocalMediaViewState(),
    mediaInfo: MediaInfo? = localMedia?.info,
) {
    val mimeType = mediaInfo?.mimeType
    when {
        mimeType.isMimeTypeImage() -> MediaImageView(
            localMediaViewState = localMediaViewState,
            localMedia = localMedia,
            modifier = modifier,
            onClick = onClick,
        )
        mimeType.isMimeTypeVideo() -> MediaVideoView(
            localMediaViewState = localMediaViewState,
            localMedia = localMedia,
            modifier = modifier,
        )
        mimeType == MimeTypes.Pdf -> MediaPDFView(
            localMediaViewState = localMediaViewState,
            localMedia = localMedia,
            modifier = modifier,
            onClick = onClick,
        )
        // TODO handle audio with exoplayer
        else -> MediaFileView(
            localMediaViewState = localMediaViewState,
            uri = localMedia?.uri,
            info = mediaInfo,
            modifier = modifier,
            onClick = onClick,
        )
    }
}

@Composable
private fun MediaImageView(
    localMediaViewState: LocalMediaViewState,
    localMedia: LocalMedia?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (LocalInspectionMode.current) {
        Image(
            painter = painterResource(id = CommonDrawables.sample_background),
            modifier = modifier,
            contentDescription = null,
        )
    } else {
        val zoomableImageState = rememberZoomableImageState(localMediaViewState.zoomableState)
        localMediaViewState.isReady = zoomableImageState.isImageDisplayed
        ZoomableAsyncImage(
            modifier = modifier,
            state = zoomableImageState,
            model = localMedia?.uri,
            contentDescription = stringResource(id = CommonStrings.common_image),
            contentScale = ContentScale.Fit,
            onClick = { onClick() }
        )
    }
}

@Composable
private fun MediaVideoView(
    localMediaViewState: LocalMediaViewState,
    localMedia: LocalMedia?,
    modifier: Modifier = Modifier,
) {
    if (LocalInspectionMode.current) {
        Text(
            modifier = modifier
                .background(ElementTheme.colors.bgSubtlePrimary)
                .wrapContentSize(),
            text = "A Video Player will render here",
        )
    } else {
        ExoPlayerMediaVideoView(
            localMediaViewState = localMediaViewState,
            localMedia = localMedia,
            modifier = modifier,
        )
    }
}

@SuppressLint("UnsafeOptInUsageError")
@Composable
private fun ExoPlayerMediaVideoView(
    localMediaViewState: LocalMediaViewState,
    localMedia: LocalMedia?,
    modifier: Modifier = Modifier,
) {
    var playableState: PlayableState.Playable by remember {
        mutableStateOf(
            PlayableState.Playable(
                isPlaying = false,
                progressInMillis = 0,
                durationInMillis = 0,
                isShowingControls = false,
                isMuted = false,
            )
        )
    }
    localMediaViewState.playableState = playableState

    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayerWrapper.create(context)
    }
    val playerListener = object : Player.Listener {
        override fun onRenderedFirstFrame() {
            localMediaViewState.isReady = true
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            playableState = playableState.copy(isPlaying = isPlaying)
        }

        override fun onVolumeChanged(volume: Float) {
            playableState = playableState.copy(isMuted = volume == 0f)
        }

        override fun onTimelineChanged(timeline: Timeline, reason: Int) {
            if (reason == Player.TIMELINE_CHANGE_REASON_SOURCE_UPDATE) {
                playableState = playableState.copy(
                    durationInMillis = exoPlayer.duration,
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        exoPlayer.addListener(playerListener)
        exoPlayer.prepare()
    }

    var autoHideController by remember { mutableIntStateOf(0) }

    LaunchedEffect(autoHideController) {
        delay(5.seconds)
        if (exoPlayer.isPlaying) {
            playableState = playableState.copy(isShowingControls = false)
        }
    }

    LaunchedEffect(exoPlayer.isPlaying) {
        if (exoPlayer.isPlaying) {
            while (true) {
                playableState = playableState.copy(
                    progressInMillis = exoPlayer.currentPosition,
                )
                delay(200)
            }
        } else {
            // Ensure we render the final state
            playableState = playableState.copy(
                progressInMillis = exoPlayer.currentPosition,
            )
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
    KeepScreenOn(playableState.isPlaying)
    Box(
        modifier = modifier
            .background(ElementTheme.colors.bgSubtlePrimary)
            .wrapContentSize(),
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                PlayerView(context).apply {
                    player = exoPlayer
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                    setOnClickListener {
                        autoHideController++
                        playableState = playableState.copy(isShowingControls = !playableState.isShowingControls)
                    }
                    useController = false
                }
            },
            onRelease = { playerView ->
                playerView.setOnClickListener(null)
                playerView.setControllerVisibilityListener(null as PlayerView.ControllerVisibilityListener?)
                playerView.player = null
            },
        )
        MediaPlayerControllerView(
            state = MediaPlayerControllerState(
                isVisible = playableState.isShowingControls,
                playableState = playableState,
            ),
            onTogglePlay = {
                autoHideController++
                if (exoPlayer.isPlaying) {
                    exoPlayer.pause()
                } else {
                    if (exoPlayer.playbackState == Player.STATE_ENDED) {
                        exoPlayer.seekTo(0)
                    } else {
                        exoPlayer.play()
                    }
                }
            },
            onSeekChange = {
                autoHideController++
                if (exoPlayer.isPlaying.not()) {
                    exoPlayer.play()
                }
                exoPlayer.seekTo(it.toLong())
            },
            onToggleMute = {
                autoHideController++
                exoPlayer.volume = if (exoPlayer.volume == 1f) 0f else 1f
            },
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
        )
    }

    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> exoPlayer.play()
            Lifecycle.Event.ON_PAUSE -> exoPlayer.pause()
            Lifecycle.Event.ON_DESTROY -> {
                exoPlayer.release()
                exoPlayer.removeListener(playerListener)
            }
            else -> Unit
        }
    }
}

@Composable
private fun MediaPDFView(
    localMediaViewState: LocalMediaViewState,
    localMedia: LocalMedia?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pdfViewerState = rememberPdfViewerState(
        model = localMedia?.uri,
        zoomableState = localMediaViewState.zoomableState,
    )
    localMediaViewState.isReady = pdfViewerState.isLoaded
    PdfViewer(
        pdfViewerState = pdfViewerState,
        onClick = onClick,
        modifier = modifier,
    )
}

@Composable
private fun MediaFileView(
    localMediaViewState: LocalMediaViewState,
    uri: Uri?,
    info: MediaInfo?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isAudio = info?.mimeType.isMimeTypeAudio().orFalse()
    localMediaViewState.isReady = uri != null

    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .padding(horizontal = 8.dp)
            .clickable(
                onClick = onClick,
                interactionSource = interactionSource,
                indication = null
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onBackground),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (isAudio) Icons.Outlined.GraphicEq else CompoundIcons.Attachment(),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.background,
                    modifier = Modifier
                        .size(32.dp)
                        .rotate(if (isAudio) 0f else -45f),
                )
            }
            if (info != null) {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = info.filename,
                    maxLines = 2,
                    style = ElementTheme.typography.fontBodyLgRegular,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatFileExtensionAndSize(info.fileExtension, info.formattedFileSize),
                    style = ElementTheme.typography.fontBodyMdRegular,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

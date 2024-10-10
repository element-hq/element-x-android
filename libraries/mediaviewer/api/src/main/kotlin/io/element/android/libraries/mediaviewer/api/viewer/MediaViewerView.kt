/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

@file:OptIn(ExperimentalMaterial3Api::class)

package io.element.android.libraries.mediaviewer.api.viewer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.components.dialogs.RetryDialog
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarHost
import io.element.android.libraries.designsystem.utils.snackbar.rememberSnackbarHostState
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.ui.media.MediaRequestData
import io.element.android.libraries.mediaviewer.api.R
import io.element.android.libraries.mediaviewer.api.local.LocalMedia
import io.element.android.libraries.mediaviewer.api.local.LocalMediaView
import io.element.android.libraries.mediaviewer.api.local.MediaInfo
import io.element.android.libraries.mediaviewer.api.local.PlayableState
import io.element.android.libraries.mediaviewer.api.local.rememberLocalMediaViewState
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.coroutines.delay
import me.saket.telephoto.flick.FlickToDismiss
import me.saket.telephoto.flick.FlickToDismissState
import me.saket.telephoto.flick.rememberFlickToDismissState
import me.saket.telephoto.zoomable.ZoomSpec
import me.saket.telephoto.zoomable.rememberZoomableState
import kotlin.time.Duration

@Composable
fun MediaViewerView(
    state: MediaViewerState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = rememberSnackbarHostState(snackbarMessage = state.snackbarMessage)
    var showOverlay by remember { mutableStateOf(true) }

    Scaffold(
        modifier,
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) {
        MediaViewerPage(
            showOverlay = showOverlay,
            state = state,
            onDismiss = {
                onBackClick()
            },
            onShowOverlayChange = {
                showOverlay = it
            }
        )
        AnimatedVisibility(visible = showOverlay, enter = fadeIn(), exit = fadeOut()) {
            MediaViewerTopBar(
                actionsEnabled = state.downloadedMedia is AsyncData.Success,
                mimeType = state.mediaInfo.mimeType,
                onBackClick = onBackClick,
                canDownload = state.canDownload,
                canShare = state.canShare,
                eventSink = state.eventSink
            )
        }
    }
}

@Composable
private fun MediaViewerPage(
    showOverlay: Boolean,
    state: MediaViewerState,
    onDismiss: () -> Unit,
    onShowOverlayChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    fun onRetry() {
        state.eventSink(MediaViewerEvents.RetryLoading)
    }

    fun onDismissError() {
        state.eventSink(MediaViewerEvents.ClearLoadingError)
    }

    val currentShowOverlay by rememberUpdatedState(showOverlay)
    val currentOnShowOverlayChange by rememberUpdatedState(onShowOverlayChange)
    val flickState = rememberFlickToDismissState(dismissThresholdRatio = 0.1f, rotateOnDrag = false)

    DismissFlickEffects(
        flickState = flickState,
        onDismissing = { animationDuration ->
            delay(animationDuration / 3)
            onDismiss()
        },
        onDragging = {
            currentOnShowOverlayChange(false)
        }
    )

    FlickToDismiss(
        state = flickState,
        modifier = modifier.background(backgroundColorFor(flickState))
    ) {
        val showProgress = rememberShowProgress(state.downloadedMedia)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            Box(contentAlignment = Alignment.Center) {
                val zoomableState = rememberZoomableState(
                    zoomSpec = ZoomSpec(maxZoomFactor = 4f, preventOverOrUnderZoom = false)
                )
                val localMediaViewState = rememberLocalMediaViewState(zoomableState)
                val showThumbnail = !localMediaViewState.isReady
                val playableState = localMediaViewState.playableState
                val showError = state.downloadedMedia is AsyncData.Failure

                LaunchedEffect(playableState) {
                    if (playableState is PlayableState.Playable) {
                        currentOnShowOverlayChange(playableState.isShowingControls)
                    }
                }

                LocalMediaView(
                    modifier = Modifier.fillMaxSize(),
                    localMediaViewState = localMediaViewState,
                    localMedia = state.downloadedMedia.dataOrNull(),
                    mediaInfo = state.mediaInfo,
                    onClick = {
                        if (playableState is PlayableState.NotPlayable) {
                            currentOnShowOverlayChange(!currentShowOverlay)
                        }
                    },
                )
                ThumbnailView(
                    mediaInfo = state.mediaInfo,
                    thumbnailSource = state.thumbnailSource,
                    isVisible = showThumbnail,
                )
                if (showError) {
                    ErrorView(
                        errorMessage = stringResource(id = CommonStrings.error_unknown),
                        onRetry = ::onRetry,
                        onDismiss = ::onDismissError
                    )
                }
            }
            if (showProgress) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                )
            }
        }
    }
}

@Composable
private fun DismissFlickEffects(
    flickState: FlickToDismissState,
    onDismissing: suspend (Duration) -> Unit,
    onDragging: suspend () -> Unit,
) {
    val currentOnDismissing by rememberUpdatedState(onDismissing)
    val currentOnDragging by rememberUpdatedState(onDragging)

    when (val gestureState = flickState.gestureState) {
        is FlickToDismissState.GestureState.Dismissing -> {
            LaunchedEffect(Unit) {
                currentOnDismissing(gestureState.animationDuration)
            }
        }
        is FlickToDismissState.GestureState.Dragging -> {
            LaunchedEffect(Unit) {
                currentOnDragging()
            }
        }
        else -> Unit
    }
}

@Composable
private fun rememberShowProgress(downloadedMedia: AsyncData<LocalMedia>): Boolean {
    var showProgress by remember {
        mutableStateOf(false)
    }
    if (LocalInspectionMode.current) {
        showProgress = downloadedMedia.isLoading()
    } else {
        // Trick to avoid showing progress indicator if the media is already on disk.
        // When sdk will expose download progress we'll be able to remove this.
        LaunchedEffect(downloadedMedia) {
            showProgress = false
            delay(100)
            if (downloadedMedia.isLoading()) {
                showProgress = true
            }
        }
    }
    return showProgress
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MediaViewerTopBar(
    actionsEnabled: Boolean,
    canDownload: Boolean,
    canShare: Boolean,
    mimeType: String,
    onBackClick: () -> Unit,
    eventSink: (MediaViewerEvents) -> Unit,
) {
    TopAppBar(
        title = {},
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent.copy(0.6f),
        ),
        navigationIcon = { BackButton(onClick = onBackClick) },
        actions = {
            IconButton(
                enabled = actionsEnabled,
                onClick = {
                    eventSink(MediaViewerEvents.OpenWith)
                },
            ) {
                when (mimeType) {
                    MimeTypes.Apk -> Icon(
                        resourceId = R.drawable.ic_apk_install,
                        contentDescription = stringResource(id = CommonStrings.common_install_apk_android)
                    )
                    else -> Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = stringResource(id = CommonStrings.action_open_with)
                    )
                }
            }
            if (canDownload) {
                IconButton(
                    enabled = actionsEnabled,
                    onClick = {
                        eventSink(MediaViewerEvents.SaveOnDisk)
                    },
                ) {
                    Icon(
                        imageVector = CompoundIcons.Download(),
                        contentDescription = stringResource(id = CommonStrings.action_save),
                    )
                }
            }
            if (canShare) {
                IconButton(
                    enabled = actionsEnabled,
                    onClick = {
                        eventSink(MediaViewerEvents.Share)
                    },
                ) {
                    Icon(
                        imageVector = CompoundIcons.ShareAndroid(),
                        contentDescription = stringResource(id = CommonStrings.action_share)
                    )
                }
            }
        }
    )
}

@Composable
private fun ThumbnailView(
    thumbnailSource: MediaSource?,
    isVisible: Boolean,
    mediaInfo: MediaInfo,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (isVisible) {
            val mediaRequestData = MediaRequestData(
                source = thumbnailSource,
                kind = MediaRequestData.Kind.File(mediaInfo.filename, mediaInfo.mimeType)
            )
            AsyncImage(
                modifier = Modifier.fillMaxSize(),
                model = mediaRequestData,
                contentScale = ContentScale.Fit,
                contentDescription = null,
            )
        }
    }
}

@Composable
private fun ErrorView(
    errorMessage: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
) {
    RetryDialog(
        content = errorMessage,
        onRetry = onRetry,
        onDismiss = onDismiss
    )
}

@Composable
private fun backgroundColorFor(flickState: FlickToDismissState): Color {
    val animatedAlpha by animateFloatAsState(
        targetValue = when (flickState.gestureState) {
            is FlickToDismissState.GestureState.Dismissed,
            is FlickToDismissState.GestureState.Dismissing -> 0f
            is FlickToDismissState.GestureState.Dragging,
            is FlickToDismissState.GestureState.Idle,
            is FlickToDismissState.GestureState.Resetting -> 1f - flickState.offsetFraction
        },
        label = "Background alpha",
    )
    return Color.Black.copy(alpha = animatedAlpha)
}

// Only preview in dark, dark theme is forced on the Node.
@Preview
@Composable
internal fun MediaViewerViewPreview(@PreviewParameter(MediaViewerStateProvider::class) state: MediaViewerState) = ElementPreviewDark {
    MediaViewerView(
        state = state,
        onBackClick = {}
    )
}

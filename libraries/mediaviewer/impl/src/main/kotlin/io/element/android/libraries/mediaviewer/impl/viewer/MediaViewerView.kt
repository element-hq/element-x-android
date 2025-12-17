/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalMaterial3Api::class)

package io.element.android.libraries.mediaviewer.impl.viewer

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.viewfolder.api.TextFileViewer
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.audio.api.AudioFocus
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.core.mimetype.MimeTypes.isMimeTypeVideo
import io.element.android.libraries.designsystem.components.async.AsyncFailure
import io.element.android.libraries.designsystem.components.async.AsyncLoading
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.components.dialogs.RetryDialog
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.theme.components.HorizontalDivider
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarHost
import io.element.android.libraries.designsystem.utils.snackbar.rememberSnackbarHostState
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.ui.media.MediaRequestData
import io.element.android.libraries.mediaviewer.api.MediaInfo
import io.element.android.libraries.mediaviewer.api.local.LocalMedia
import io.element.android.libraries.mediaviewer.impl.R
import io.element.android.libraries.mediaviewer.impl.details.MediaBottomSheetState
import io.element.android.libraries.mediaviewer.impl.details.MediaDeleteConfirmationBottomSheet
import io.element.android.libraries.mediaviewer.impl.details.MediaDetailsBottomSheet
import io.element.android.libraries.mediaviewer.impl.local.LocalMediaView
import io.element.android.libraries.mediaviewer.impl.local.PlayableState
import io.element.android.libraries.mediaviewer.impl.local.rememberLocalMediaViewState
import io.element.android.libraries.mediaviewer.impl.util.bgCanvasWithTransparency
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.coroutines.delay
import me.saket.telephoto.zoomable.OverzoomEffect
import me.saket.telephoto.zoomable.ZoomSpec
import me.saket.telephoto.zoomable.rememberZoomableState

val topAppBarHeight = 88.dp

@Composable
fun MediaViewerView(
    state: MediaViewerState,
    textFileViewer: TextFileViewer,
    onBackClick: () -> Unit,
    audioFocus: AudioFocus?,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = rememberSnackbarHostState(snackbarMessage = state.snackbarMessage)
    var showOverlay by remember { mutableStateOf(true) }

    val defaultBottomPaddingInPixels = if (LocalInspectionMode.current) 303 else 0
    val currentData = state.listData.getOrNull(state.currentIndex)
    BackHandler { onBackClick() }
    Scaffold(
        modifier,
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) {
        val pagerState = rememberPagerState(state.currentIndex, 0f) {
            state.listData.size
        }
        LaunchedEffect(pagerState) {
            snapshotFlow { pagerState.currentPage }.collect { page ->
                state.eventSink(MediaViewerEvents.OnNavigateTo(page))
            }
        }
        HorizontalPager(
            state = pagerState,
            modifier = Modifier,
            // Pre-load previous and next pages
            beyondViewportPageCount = 1,
            key = { index -> state.listData[index].pagerKey },
        ) { page ->
            when (val dataForPage = state.listData[page]) {
                is MediaViewerPageData.Failure -> {
                    MediaViewerErrorPage(
                        throwable = dataForPage.throwable,
                        onDismiss = onBackClick,
                    )
                }
                is MediaViewerPageData.Loading -> {
                    LaunchedEffect(dataForPage.timestamp) {
                        state.eventSink(MediaViewerEvents.LoadMore(dataForPage.direction))
                    }
                    MediaViewerLoadingPage(
                        onDismiss = onBackClick,
                    )
                }
                is MediaViewerPageData.MediaViewerData -> {
                    var bottomPaddingInPixels by remember { mutableIntStateOf(defaultBottomPaddingInPixels) }
                    LaunchedEffect(Unit) {
                        state.eventSink(MediaViewerEvents.LoadMedia(dataForPage))
                    }
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val isDisplayed = remember(pagerState.settledPage) {
                            // This 'item provider' lambda will be called when the data source changes with an outdated `settlePage` value
                            // So we need to update this value only when the `settledPage` value changes. It seems like a bug that needs to be fixed in Compose.
                            page == pagerState.settledPage
                        }
                        MediaViewerPage(
                            isDisplayed = isDisplayed,
                            showOverlay = showOverlay,
                            bottomPaddingInPixels = bottomPaddingInPixels,
                            data = dataForPage,
                            textFileViewer = textFileViewer,
                            onDismiss = onBackClick,
                            onRetry = {
                                state.eventSink(MediaViewerEvents.LoadMedia(dataForPage))
                            },
                            onDismissError = {
                                state.eventSink(MediaViewerEvents.ClearLoadingError(dataForPage))
                            },
                            onShowOverlayChange = {
                                showOverlay = it
                            },
                            audioFocus = audioFocus,
                            isUserSelected = (state.listData[page] as? MediaViewerPageData.MediaViewerData)?.eventId == state.initiallySelectedEventId,
                        )
                        // Bottom bar
                        AnimatedVisibility(visible = showOverlay, enter = fadeIn(), exit = fadeOut()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .navigationBarsPadding()
                            ) {
                                MediaViewerBottomBar(
                                    modifier = Modifier.align(Alignment.BottomCenter),
                                    showDivider = dataForPage.mediaInfo.mimeType.isMimeTypeVideo(),
                                    caption = dataForPage.mediaInfo.caption,
                                    onHeightChange = { bottomPaddingInPixels = it },
                                )
                            }
                        }
                    }
                }
            }
        }
        // Top bar
        AnimatedVisibility(visible = showOverlay, enter = fadeIn(), exit = fadeOut()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
            ) {
                when (currentData) {
                    is MediaViewerPageData.MediaViewerData -> {
                        MediaViewerTopBar(
                            data = currentData,
                            canShowInfo = state.canShowInfo,
                            onBackClick = onBackClick,
                            onInfoClick = {
                                state.eventSink(MediaViewerEvents.OpenInfo(currentData))
                            },
                            eventSink = state.eventSink
                        )
                    }
                    else -> {
                        TopAppBar(
                            title = {
                                if (currentData is MediaViewerPageData.Loading) {
                                    Text(
                                        modifier = Modifier.semantics {
                                            heading()
                                        },
                                        text = stringResource(id = CommonStrings.common_loading_more),
                                        style = ElementTheme.typography.fontBodyMdMedium,
                                        color = ElementTheme.colors.textPrimary,
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = bgCanvasWithTransparency,
                            ),
                            navigationIcon = { BackButton(onClick = onBackClick) },
                        )
                    }
                }
            }
        }
    }

    when (val bottomSheetState = state.mediaBottomSheetState) {
        MediaBottomSheetState.Hidden -> Unit
        is MediaBottomSheetState.MediaDetailsBottomSheetState -> {
            MediaDetailsBottomSheet(
                state = bottomSheetState,
                onViewInTimeline = {
                    state.eventSink(MediaViewerEvents.ViewInTimeline(it))
                },
                onShare = {
                    (currentData as? MediaViewerPageData.MediaViewerData)?.let {
                        state.eventSink(MediaViewerEvents.Share(currentData))
                    }
                },
                onForward = {
                    state.eventSink(MediaViewerEvents.Forward(it))
                },
                onDownload = {
                    (currentData as? MediaViewerPageData.MediaViewerData)?.let {
                        state.eventSink(MediaViewerEvents.SaveOnDisk(currentData))
                    }
                },
                onDelete = { eventId ->
                    (currentData as? MediaViewerPageData.MediaViewerData)?.let {
                        state.eventSink(
                            MediaViewerEvents.ConfirmDelete(
                                eventId,
                                currentData,
                            )
                        )
                    }
                },
                onDismiss = {
                    state.eventSink(MediaViewerEvents.CloseBottomSheet)
                },
            )
        }
        is MediaBottomSheetState.MediaDeleteConfirmationState -> {
            MediaDeleteConfirmationBottomSheet(
                state = bottomSheetState,
                onDelete = {
                    state.eventSink(MediaViewerEvents.Delete(it))
                },
                onDismiss = {
                    state.eventSink(MediaViewerEvents.CloseBottomSheet)
                },
            )
        }
    }
}

@Composable
private fun MediaViewerPage(
    isDisplayed: Boolean,
    showOverlay: Boolean,
    bottomPaddingInPixels: Int,
    data: MediaViewerPageData.MediaViewerData,
    textFileViewer: TextFileViewer,
    isUserSelected: Boolean,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
    onDismissError: () -> Unit,
    onShowOverlayChange: (Boolean) -> Unit,
    audioFocus: AudioFocus?,
    modifier: Modifier = Modifier,
) {
    val currentShowOverlay by rememberUpdatedState(showOverlay)
    val currentOnShowOverlayChange by rememberUpdatedState(onShowOverlayChange)

    MediaViewerFlickToDismiss(
        onDismiss = onDismiss,
        onDragging = {
            currentOnShowOverlayChange(false)
        },
        onResetting = {
            currentOnShowOverlayChange(true)
        },
        modifier = modifier,
    ) {
        val downloadedMedia by data.downloadedMedia
        val showProgress = rememberShowProgress(downloadedMedia)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            Box(contentAlignment = Alignment.Center) {
                val zoomableState = rememberZoomableState(
                    zoomSpec = ZoomSpec(maxZoomFactor = 4f, overzoomEffect = OverzoomEffect.NoLimits)
                )
                val localMediaViewState = rememberLocalMediaViewState(zoomableState)
                val showThumbnail = !localMediaViewState.isReady
                val playableState = localMediaViewState.playableState
                val showError = downloadedMedia.isFailure()

                LaunchedEffect(playableState) {
                    if (playableState is PlayableState.Playable) {
                        currentOnShowOverlayChange(playableState.isShowingControls)
                    }
                }

                LocalMediaView(
                    modifier = Modifier.fillMaxSize(),
                    isDisplayed = isDisplayed,
                    bottomPaddingInPixels = bottomPaddingInPixels,
                    localMediaViewState = localMediaViewState,
                    localMedia = downloadedMedia.dataOrNull(),
                    mediaInfo = data.mediaInfo,
                    textFileViewer = textFileViewer,
                    onClick = {
                        if (playableState is PlayableState.NotPlayable) {
                            currentOnShowOverlayChange(!currentShowOverlay)
                        }
                    },
                    isUserSelected = isUserSelected,
                    audioFocus = audioFocus,
                )
                ThumbnailView(
                    mediaInfo = data.mediaInfo,
                    thumbnailSource = data.thumbnailSource,
                    isVisible = showThumbnail,
                )
                if (showError) {
                    ErrorView(
                        errorMessage = stringResource(id = CommonStrings.error_unknown),
                        onRetry = onRetry,
                        onDismiss = onDismissError
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
private fun MediaViewerLoadingPage(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MediaViewerFlickToDismiss(
        onDismiss = onDismiss,
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            AsyncLoading()
        }
    }
}

@Composable
private fun MediaViewerErrorPage(
    throwable: Throwable,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MediaViewerFlickToDismiss(
        onDismiss = onDismiss,
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            AsyncFailure(
                throwable = throwable,
                onRetry = null
            )
        }
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
    data: MediaViewerPageData.MediaViewerData,
    canShowInfo: Boolean,
    onBackClick: () -> Unit,
    onInfoClick: () -> Unit,
    eventSink: (MediaViewerEvents) -> Unit,
) {
    val downloadedMedia by data.downloadedMedia
    val actionsEnabled = downloadedMedia.isSuccess()
    val mimeType = data.mediaInfo.mimeType
    val senderName = data.mediaInfo.senderName
    val dateSent = data.mediaInfo.dateSent
    TopAppBar(
        title = {
            if (senderName != null && dateSent != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        modifier = Modifier.semantics {
                            heading()
                        },
                        text = senderName,
                        style = ElementTheme.typography.fontBodyMdMedium,
                        color = ElementTheme.colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = dateSent,
                        style = ElementTheme.typography.fontBodySmRegular,
                        color = ElementTheme.colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = bgCanvasWithTransparency,
        ),
        navigationIcon = { BackButton(onClick = onBackClick) },
        actions = {
            IconButton(
                enabled = actionsEnabled,
                onClick = {
                    eventSink(MediaViewerEvents.OpenWith(data))
                },
            ) {
                when (mimeType) {
                    MimeTypes.Apk -> Icon(
                        resourceId = R.drawable.ic_apk_install,
                        contentDescription = stringResource(id = CommonStrings.common_install_apk_android)
                    )
                    else -> Icon(
                        imageVector = CompoundIcons.PopOut(),
                        contentDescription = stringResource(id = CommonStrings.action_open_with)
                    )
                }
            }
            if (canShowInfo) {
                IconButton(
                    onClick = onInfoClick,
                    enabled = actionsEnabled,
                ) {
                    Icon(
                        imageVector = CompoundIcons.Info(),
                        contentDescription = stringResource(id = CommonStrings.a11y_view_details),
                    )
                }
            }
        }
    )
}

@Composable
private fun MediaViewerBottomBar(
    caption: String?,
    showDivider: Boolean,
    onHeightChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(bgCanvasWithTransparency)
            .onSizeChanged {
                onHeightChange(it.height)
            },
    ) {
        if (caption != null) {
            if (showDivider) {
                HorizontalDivider()
            }
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                text = caption,
                maxLines = 5,
                overflow = TextOverflow.Ellipsis,
                style = ElementTheme.typography.fontBodyLgRegular,
            )
        }
    }
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
            val alpha = if (LocalInspectionMode.current) 0.1f else 1f
            AsyncImage(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(alpha),
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

// Only preview in dark, dark theme is forced on the Node.
@Preview
@Composable
internal fun MediaViewerViewPreview(@PreviewParameter(MediaViewerStateProvider::class) state: MediaViewerState) = ElementPreviewDark {
    MediaViewerView(
        state = state,
        audioFocus = null,
        textFileViewer = { _, _ -> },
        onBackClick = {},
    )
}

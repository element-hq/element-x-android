/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.gallery

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.components.BigIcon
import io.element.android.libraries.designsystem.components.PageTitle
import io.element.android.libraries.designsystem.components.async.AsyncFailure
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.aliasScreenTitle
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.LinearProgressIndicator
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.SegmentedButton
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarHost
import io.element.android.libraries.designsystem.utils.snackbar.rememberSnackbarHostState
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.mediaviewer.impl.R
import io.element.android.libraries.mediaviewer.impl.details.MediaBottomSheetState
import io.element.android.libraries.mediaviewer.impl.details.MediaDeleteConfirmationBottomSheet
import io.element.android.libraries.mediaviewer.impl.details.MediaDetailsBottomSheet
import io.element.android.libraries.mediaviewer.impl.gallery.ui.AudioItemView
import io.element.android.libraries.mediaviewer.impl.gallery.ui.DateItemView
import io.element.android.libraries.mediaviewer.impl.gallery.ui.FileItemView
import io.element.android.libraries.mediaviewer.impl.gallery.ui.ImageItemView
import io.element.android.libraries.mediaviewer.impl.gallery.ui.VideoItemView
import kotlinx.collections.immutable.ImmutableList
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaGalleryView(
    state: MediaGalleryState,
    onBackClick: () -> Unit,
    onItemClick: (MediaItem.Event) -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = rememberSnackbarHostState(snackbarMessage = state.snackbarMessage)
    BackHandler { onBackClick() }
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.roomName,
                        style = ElementTheme.typography.aliasScreenTitle,
                    )
                },
                navigationIcon = {
                    BackButton(
                        onClick = onBackClick,
                    )
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
                .fillMaxSize()
        ) {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                MediaGalleryMode.entries.forEach { mode ->
                    SegmentedButton(
                        index = mode.ordinal,
                        count = MediaGalleryMode.entries.size,
                        selected = state.mode == mode,
                        onClick = { state.eventSink(MediaGalleryEvents.ChangeMode(mode)) },
                        text = stringResource(mode.stringResource),
                    )
                }
            }
            val pagerState = rememberPagerState(0, 0f) {
                MediaGalleryMode.entries.size
            }
            LaunchedEffect(state.mode) {
                pagerState.scrollToPage(state.mode.ordinal)
            }
            HorizontalPager(
                state = pagerState,
                userScrollEnabled = false,
                modifier = Modifier,
            ) { page ->
                val mode = MediaGalleryMode.entries[page]
                MediaGalleryPage(
                    mode = mode,
                    state = state,
                    onItemClick = onItemClick,
                )
            }
        }
    }
    when (val bottomSheetState = state.mediaBottomSheetState) {
        MediaBottomSheetState.Hidden -> Unit
        is MediaBottomSheetState.MediaDetailsBottomSheetState -> {
            MediaDetailsBottomSheet(
                state = bottomSheetState,
                onViewInTimeline = { eventId ->
                    state.eventSink(MediaGalleryEvents.ViewInTimeline(eventId))
                },
                onDelete = { eventId ->
                    state.eventSink(
                        MediaGalleryEvents.ConfirmDelete(
                            eventId = eventId,
                            mediaInfo = bottomSheetState.mediaInfo,
                            thumbnailSource = bottomSheetState.thumbnailSource,
                        )
                    )
                },
                onDismiss = {
                    state.eventSink(MediaGalleryEvents.CloseBottomSheet)
                },
            )
        }
        is MediaBottomSheetState.MediaDeleteConfirmationState -> {
            MediaDeleteConfirmationBottomSheet(
                state = bottomSheetState,
                onDelete = {
                    state.eventSink(MediaGalleryEvents.Delete(it))
                },
                onDismiss = {
                    state.eventSink(MediaGalleryEvents.CloseBottomSheet)
                },
            )
        }
    }
}

@Composable
private fun MediaGalleryPage(
    mode: MediaGalleryMode,
    state: MediaGalleryState,
    onItemClick: (MediaItem.Event) -> Unit,
) {
    when (val groupedMediaItems = state.groupedMediaItems) {
        AsyncData.Uninitialized,
        is AsyncData.Loading -> {
            LoadingContent(mode)
        }
        is AsyncData.Success -> {
            when (mode) {
                MediaGalleryMode.Images -> MediaGalleryImages(
                    imagesAndVideos = groupedMediaItems.data.imageAndVideoItems,
                    eventSink = state.eventSink,
                    onItemClick = onItemClick,
                )
                MediaGalleryMode.Files -> MediaGalleryFiles(
                    files = groupedMediaItems.data.fileItems,
                    eventSink = state.eventSink,
                    onItemClick = onItemClick,
                )
            }
        }
        is AsyncData.Failure -> {
            ErrorContent(
                error = groupedMediaItems.error,
            )
        }
    }
}

@Composable
private fun MediaGalleryImages(
    imagesAndVideos: ImmutableList<MediaItem>,
    eventSink: (MediaGalleryEvents) -> Unit,
    onItemClick: (MediaItem.Event) -> Unit,
) {
    if (imagesAndVideos.isEmpty()) {
        EmptyContent(
            titleRes = R.string.screen_media_browser_media_empty_state_title,
            subtitleRes = R.string.screen_media_browser_media_empty_state_subtitle,
            icon = CompoundIcons.Image(),
        )
    } else {
        MediaGalleryImageGrid(
            imagesAndVideos = imagesAndVideos,
            eventSink = eventSink,
            onItemClick = onItemClick,
        )
    }
}

@Composable
private fun MediaGalleryFiles(
    files: ImmutableList<MediaItem>,
    eventSink: (MediaGalleryEvents) -> Unit,
    onItemClick: (MediaItem.Event) -> Unit,
) {
    if (files.isEmpty()) {
        EmptyContent(
            titleRes = R.string.screen_media_browser_files_empty_state_title,
            subtitleRes = R.string.screen_media_browser_files_empty_state_subtitle,
            icon = CompoundIcons.Files(),
        )
    } else {
        MediaGalleryFilesList(
            files = files,
            eventSink = eventSink,
            onItemClick = onItemClick,
        )
    }
}

@Composable
private fun MediaGalleryFilesList(
    files: ImmutableList<MediaItem>,
    eventSink: (MediaGalleryEvents) -> Unit,
    onItemClick: (MediaItem.Event) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
    ) {
        items(files) { item ->
            when (item) {
                is MediaItem.File -> FileItemView(
                    item,
                    onClick = { onItemClick(item) },
                    onShareClick = { eventSink(MediaGalleryEvents.Share(item)) },
                    onDownloadClick = { eventSink(MediaGalleryEvents.SaveOnDisk(item)) },
                    onInfoClick = { eventSink(MediaGalleryEvents.OpenInfo(item)) },
                )
                is MediaItem.Audio -> AudioItemView(
                    item,
                    onClick = { onItemClick(item) },
                    onShareClick = { eventSink(MediaGalleryEvents.Share(item)) },
                    onDownloadClick = { eventSink(MediaGalleryEvents.SaveOnDisk(item)) },
                    onInfoClick = { eventSink(MediaGalleryEvents.OpenInfo(item)) },
                )
                is MediaItem.DateSeparator -> DateItemView(item)
                is MediaItem.Image,
                is MediaItem.Video -> {
                    // Should not happen
                }
                is MediaItem.LoadingIndicator -> {
                    LoadingMoreIndicator(item.direction)
                    val latestEventSink by rememberUpdatedState(eventSink)
                    LaunchedEffect(item.timestamp) {
                        latestEventSink(MediaGalleryEvents.LoadMore(item.direction))
                    }
                }
            }
        }
    }
}

@Composable
private fun MediaGalleryImageGrid(
    imagesAndVideos: ImmutableList<MediaItem>,
    eventSink: (MediaGalleryEvents) -> Unit,
    onItemClick: (MediaItem.Event) -> Unit,
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val horizontalPadding = 16.dp
    val itemSpacing = 4.dp
    val availableWidth = screenWidth - horizontalPadding * 2
    val minCellWidth = 80.dp
    // Calculate the number of columns
    val columns = max(1, (availableWidth / (minCellWidth + itemSpacing)).toInt())
    LazyVerticalGrid(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = horizontalPadding),
        columns = GridCells.Fixed(columns),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items(
            imagesAndVideos,
            span = { item ->
                when (item) {
                    is MediaItem.LoadingIndicator,
                    is MediaItem.DateSeparator -> GridItemSpan(columns)
                    is MediaItem.Event -> GridItemSpan(1)
                }
            },
            key = { it.id() },
            contentType = { it::class.java },
        ) { item ->
            when (item) {
                is MediaItem.DateSeparator -> {
                    DateItemView(item)
                }
                is MediaItem.Audio -> {
                    // Should not happen
                }
                is MediaItem.File -> {
                    // Should not happen
                }
                is MediaItem.Image -> {
                    ImageItemView(
                        image = item,
                        onClick = { onItemClick(item) },
                        onLongClick = {
                            eventSink(MediaGalleryEvents.OpenInfo(item))
                        },
                    )
                }
                is MediaItem.Video -> {
                    VideoItemView(
                        video = item,
                        onClick = { onItemClick(item) },
                        onLongClick = {
                            eventSink(MediaGalleryEvents.OpenInfo(item))
                        },
                    )
                }
                is MediaItem.LoadingIndicator -> {
                    LoadingMoreIndicator(item.direction)
                    val latestEventSink by rememberUpdatedState(eventSink)
                    LaunchedEffect(item.timestamp) {
                        latestEventSink(MediaGalleryEvents.LoadMore(item.direction))
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingMoreIndicator(
    direction: Timeline.PaginationDirection,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        when (direction) {
            Timeline.PaginationDirection.FORWARDS -> {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp)
                        .height(1.dp)
                )
            }
            Timeline.PaginationDirection.BACKWARDS -> {
                CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun ErrorContent(error: Throwable) {
    AsyncFailure(
        throwable = error,
        onRetry = null,
        modifier = Modifier.fillMaxSize(),
    )
}

@Composable
fun EmptyContent(
    titleRes: Int,
    subtitleRes: Int,
    icon: ImageVector,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        PageTitle(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 44.dp)
                .padding(24.dp),
            title = stringResource(titleRes),
            iconStyle = BigIcon.Style.Default(icon),
            subtitle = stringResource(subtitleRes),
        )
    }
}

@Composable
private fun LoadingContent(
    mode: MediaGalleryMode,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 48.dp)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
        val res = when (mode) {
            MediaGalleryMode.Images -> R.string.screen_media_browser_list_loading_media
            MediaGalleryMode.Files -> R.string.screen_media_browser_list_loading_files
        }
        Text(
            text = stringResource(res),
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
    }
}

@PreviewsDayNight
@Composable
internal fun MediaGalleryViewPreview(
    @PreviewParameter(MediaGalleryStateProvider::class) state: MediaGalleryState
) = ElementPreview {
    MediaGalleryView(
        state = state,
        onBackClick = {},
        onItemClick = {},
    )
}

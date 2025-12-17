/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.designsystem.atomic.molecules.IconTitleSubtitleMolecule
import io.element.android.libraries.designsystem.background.OnboardingBackground
import io.element.android.libraries.designsystem.components.BigIcon
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
import io.element.android.libraries.mediaviewer.impl.gallery.di.LocalMediaItemPresenterFactories
import io.element.android.libraries.mediaviewer.impl.gallery.di.aFakeMediaItemPresenterFactories
import io.element.android.libraries.mediaviewer.impl.gallery.di.rememberPresenter
import io.element.android.libraries.mediaviewer.impl.gallery.ui.AudioItemView
import io.element.android.libraries.mediaviewer.impl.gallery.ui.DateItemView
import io.element.android.libraries.mediaviewer.impl.gallery.ui.FileItemView
import io.element.android.libraries.mediaviewer.impl.gallery.ui.ImageItemView
import io.element.android.libraries.mediaviewer.impl.gallery.ui.VideoItemView
import io.element.android.libraries.mediaviewer.impl.gallery.ui.VoiceItemView
import io.element.android.libraries.mediaviewer.impl.model.GroupedMediaItems
import io.element.android.libraries.mediaviewer.impl.model.MediaItem
import io.element.android.libraries.mediaviewer.impl.model.id
import io.element.android.libraries.voiceplayer.api.VoiceMessageState
import kotlinx.collections.immutable.ImmutableList

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
                        modifier = Modifier.semantics {
                            heading()
                        },
                        text = state.roomName,
                        style = ElementTheme.typography.aliasScreenTitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
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
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(2.dp),
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
                onShare = { eventId ->
                    state.eventSink(MediaGalleryEvents.Share(eventId))
                },
                onForward = { eventId ->
                    state.eventSink(MediaGalleryEvents.Forward(eventId))
                },
                onDownload = { eventId ->
                    state.eventSink(MediaGalleryEvents.SaveOnDisk(eventId))
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
    val groupedMediaItems = state.groupedMediaItems
    if (groupedMediaItems.isLoadingItems(mode)) {
        // Need to trigger a pagination now if there is only one LoadingIndicator.
        val loadingItem = groupedMediaItems.dataOrNull()?.getItems(mode)?.singleOrNull() as? MediaItem.LoadingIndicator
        if (loadingItem != null) {
            LaunchedEffect(loadingItem.timestamp) {
                state.eventSink(MediaGalleryEvents.LoadMore(loadingItem.direction))
            }
        }
        LoadingContent(mode)
    } else {
        when (groupedMediaItems) {
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
            else -> Unit
        }
    }
}

/**
 * Return true when the timeline is not loaded or if it contains only a single loading item.
 */
private fun AsyncData<GroupedMediaItems>.isLoadingItems(mode: MediaGalleryMode): Boolean {
    return when (this) {
        AsyncData.Uninitialized,
        is AsyncData.Loading -> true
        is AsyncData.Success -> data.getItems(mode).singleOrNull() is MediaItem.LoadingIndicator
        is AsyncData.Failure -> false
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
    val presenterFactories = LocalMediaItemPresenterFactories.current
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
    ) {
        items(
            items = files,
            key = { it.id() },
            contentType = { it::class.java },
        ) { item ->
            when (item) {
                is MediaItem.File -> FileItemView(
                    modifier = Modifier.animateItem(),
                    file = item,
                    onClick = { onItemClick(item) },
                    onLongClick = {
                        eventSink(MediaGalleryEvents.OpenInfo(item))
                    },
                )
                is MediaItem.Audio -> AudioItemView(
                    modifier = Modifier.animateItem(),
                    audio = item,
                    onClick = { onItemClick(item) },
                    onLongClick = {
                        eventSink(MediaGalleryEvents.OpenInfo(item))
                    },
                )
                is MediaItem.Voice -> {
                    val presenter: Presenter<VoiceMessageState> = presenterFactories.rememberPresenter(item)
                    VoiceItemView(
                        modifier = Modifier.animateItem(),
                        state = presenter.present(),
                        voice = item,
                        onLongClick = {
                            eventSink(MediaGalleryEvents.OpenInfo(item))
                        },
                    )
                }
                is MediaItem.DateSeparator -> DateItemView(
                    modifier = Modifier.animateItem(),
                    item = item
                )
                is MediaItem.Image,
                is MediaItem.Video -> {
                    // Should not happen
                }
                is MediaItem.LoadingIndicator -> LoadingMoreIndicator(
                    modifier = Modifier.animateItem(),
                    item = item,
                    eventSink = eventSink,
                )
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
    LazyVerticalGrid(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        columns = GridCells.Adaptive(80.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items(
            items = imagesAndVideos,
            span = { item ->
                when (item) {
                    is MediaItem.LoadingIndicator,
                    is MediaItem.DateSeparator -> GridItemSpan(maxLineSpan)
                    is MediaItem.Event -> GridItemSpan(1)
                }
            },
            key = { it.id() },
            contentType = { it::class.java },
        ) { item ->
            when (item) {
                is MediaItem.DateSeparator -> DateItemView(
                    modifier = Modifier.animateItem(),
                    item = item,
                )
                is MediaItem.Audio -> {
                    // Should not happen
                }
                is MediaItem.Voice -> {
                    // Should not happen
                }
                is MediaItem.File -> {
                    // Should not happen
                }
                is MediaItem.Image -> ImageItemView(
                    modifier = Modifier.animateItem(),
                    image = item,
                    onClick = { onItemClick(item) },
                    onLongClick = {
                        eventSink(MediaGalleryEvents.OpenInfo(item))
                    },
                )
                is MediaItem.Video -> VideoItemView(
                    modifier = Modifier.animateItem(),
                    video = item,
                    onClick = { onItemClick(item) },
                    onLongClick = {
                        eventSink(MediaGalleryEvents.OpenInfo(item))
                    },
                )
                is MediaItem.LoadingIndicator -> LoadingMoreIndicator(
                    modifier = Modifier.animateItem(),
                    item = item,
                    eventSink = eventSink,
                )
            }
        }
    }
}

@Composable
private fun LoadingMoreIndicator(
    item: MediaItem.LoadingIndicator,
    eventSink: (MediaGalleryEvents) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        when (item.direction) {
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
        val latestEventSink by rememberUpdatedState(eventSink)
        LaunchedEffect(item.timestamp) {
            latestEventSink(MediaGalleryEvents.LoadMore(item.direction))
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
private fun EmptyContent(
    titleRes: Int,
    subtitleRes: Int,
    icon: ImageVector,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        OnboardingBackground()
        IconTitleSubtitleMolecule(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 44.dp)
                .padding(24.dp),
            title = stringResource(titleRes),
            iconStyle = BigIcon.Style.Default(icon),
            subTitle = stringResource(subtitleRes),
        )
    }
}

@Composable
private fun LoadingContent(
    mode: MediaGalleryMode,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        OnboardingBackground()
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
}

@PreviewsDayNight
@Composable
internal fun MediaGalleryViewPreview(
    @PreviewParameter(MediaGalleryStateProvider::class) state: MediaGalleryState
) = ElementPreview {
    CompositionLocalProvider(
        LocalMediaItemPresenterFactories provides aFakeMediaItemPresenterFactories(),
    ) {
        MediaGalleryView(
            state = state,
            onBackClick = {},
            onItemClick = {},
        )
    }
}

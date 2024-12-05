/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.mediagallery.impl.gallery

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.timeline.Timeline
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

class MediaGalleryPresenter @Inject constructor(
    private val room: MatrixRoom,
    private val timelineProvider: MediaGalleryTimelineProvider,
    private val timelineMediaItemsFactory: TimelineMediaItemsFactory,
) : Presenter<MediaGalleryState> {
    @Composable
    override fun present(): MediaGalleryState {
        val coroutineScope = rememberCoroutineScope()
        var mode by remember { mutableStateOf(MediaGalleryMode.Images) }

        val roomInfo by room.roomInfoFlow.collectAsState(null)

        var mediaItems by remember {
            mutableStateOf<AsyncData<ImmutableList<MediaItem>>>(AsyncData.Uninitialized)
        }
        val imageItems by remember {
            derivedStateOf {
                mediaItems.filterItems { it is MediaItem.Image || it is MediaItem.Video }
            }
        }
        val fileItems by remember {
            derivedStateOf {
                mediaItems.filterItems { it is MediaItem.File }
            }
        }
        MediaListEffect(
            onItemsChange = { newItems ->
                mediaItems = newItems
            }
        )

        LaunchedEffect(Unit) {
            timelineProvider.launchIn(this)
        }

        fun handleEvents(event: MediaGalleryEvents) {
            when (event) {
                is MediaGalleryEvents.ChangeMode -> {
                    mode = event.mode
                }
                is MediaGalleryEvents.LoadMore -> coroutineScope.launch {
                    timelineProvider.invokeOnTimeline {
                        paginate(event.direction)
                    }
                }
            }
        }

        return MediaGalleryState(
            roomName = roomInfo?.name.orEmpty(),
            mode = mode,
            imageItems = imageItems,
            fileItems = fileItems,
            eventSink = ::handleEvents
        )
    }

    @Composable
    private fun MediaListEffect(onItemsChange: (AsyncData<ImmutableList<MediaItem>>) -> Unit) {
        val updatedOnItemsChange by rememberUpdatedState(onItemsChange)

        val timelineState by timelineProvider.timelineStateFlow.collectAsState()

        LaunchedEffect(timelineState) {
            when (val asyncTimeline = timelineState) {
                AsyncData.Uninitialized -> flowOf(AsyncData.Uninitialized)
                is AsyncData.Failure -> flowOf(AsyncData.Failure(asyncTimeline.error))
                is AsyncData.Loading -> flowOf(AsyncData.Loading())
                is AsyncData.Success -> {
                    asyncTimeline.data.timelineItems
                        .onEach { items ->
                            timelineMediaItemsFactory.replaceWith(
                                timelineItems = items,
                            )
                        }
                        .launchIn(this)

                    asyncTimeline.data.paginationStatus(Timeline.PaginationDirection.BACKWARDS)
                        .onEach { backwardPaginationStatus ->
                            if (backwardPaginationStatus.canPaginate) {
                                timelineMediaItemsFactory.onCanPaginate()
                            }
                        }
                        .launchIn(this)

                    timelineMediaItemsFactory.timelineItems.map { timelineItems ->
                        AsyncData.Success(timelineItems)
                    }
                }
            }
                .onEach { items ->
                    updatedOnItemsChange(items)
                }
                .launchIn(this)
        }
    }
}

private fun AsyncData<ImmutableList<MediaItem>>.filterItems(
    predicate: (MediaItem.Event) -> Boolean,
): AsyncData<ImmutableList<MediaItem>> {
    return when (this) {
        AsyncData.Uninitialized,
        is AsyncData.Loading -> this
        is AsyncData.Success -> {
            val items = buildList {
                val eventList = mutableListOf<MediaItem.Event>()
                for (item in data) {
                    when (item) {
                        is MediaItem.DateSeparator -> {
                            if (eventList.isNotEmpty()) {
                                // Date separator first
                                add(item)
                                // Then events
                                addAll(eventList)
                                eventList.clear()
                            }
                        }
                        is MediaItem.Event -> {
                            if (predicate(item)) {
                                eventList.add(item)
                            }
                        }
                        is MediaItem.LoadingIndicator -> {
                            add(item)
                        }
                    }
                }
            }
                .toPersistentList()
            AsyncData.Success(items)
        }
        is AsyncData.Failure -> this
    }
}

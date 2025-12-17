/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.pinned.banner

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Inject
import io.element.android.features.messages.impl.pinned.DefaultPinnedEventsTimelineProvider
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.room.BaseRoom
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

@Inject
class PinnedMessagesBannerPresenter(
    private val room: BaseRoom,
    private val itemFactory: PinnedMessagesBannerItemFactory,
    private val pinnedEventsTimelineProvider: DefaultPinnedEventsTimelineProvider,
) : Presenter<PinnedMessagesBannerState> {
    private val pinnedItems = mutableStateOf<AsyncData<ImmutableList<PinnedMessagesBannerItem>>>(AsyncData.Uninitialized)

    @Composable
    override fun present(): PinnedMessagesBannerState {
        val expectedPinnedMessagesCount by remember {
            room.roomInfoFlow.map { roomInfo -> roomInfo.pinnedEventIds.size }
        }.collectAsState(initial = 0)

        var currentPinnedMessageIndex by rememberSaveable { mutableIntStateOf(-1) }

        PinnedMessagesBannerItemsEffect(
            onItemsChange = { newItems ->
                val pinnedMessageCount = newItems.dataOrNull().orEmpty().size
                if (currentPinnedMessageIndex >= pinnedMessageCount || currentPinnedMessageIndex < 0) {
                    currentPinnedMessageIndex = pinnedMessageCount - 1
                }
                pinnedItems.value = newItems
            },
        )

        fun handleEvent(event: PinnedMessagesBannerEvents) {
            when (event) {
                is PinnedMessagesBannerEvents.MoveToNextPinned -> {
                    val loadedCount = pinnedItems.value.dataOrNull().orEmpty().size
                    currentPinnedMessageIndex = (currentPinnedMessageIndex - 1).mod(loadedCount)
                }
            }
        }

        return pinnedMessagesBannerState(
            expectedPinnedMessagesCount = expectedPinnedMessagesCount,
            pinnedItems = pinnedItems.value,
            currentPinnedMessageIndex = currentPinnedMessageIndex,
            eventSink = ::handleEvent,
        )
    }

    @Composable
    private fun pinnedMessagesBannerState(
        expectedPinnedMessagesCount: Int,
        pinnedItems: AsyncData<ImmutableList<PinnedMessagesBannerItem>>,
        currentPinnedMessageIndex: Int,
        eventSink: (PinnedMessagesBannerEvents) -> Unit
    ): PinnedMessagesBannerState {
        return when (pinnedItems) {
            is AsyncData.Failure, is AsyncData.Uninitialized -> PinnedMessagesBannerState.Hidden
            is AsyncData.Loading -> {
                if (expectedPinnedMessagesCount == 0) {
                    PinnedMessagesBannerState.Hidden
                } else {
                    PinnedMessagesBannerState.Loading(expectedPinnedMessagesCount = expectedPinnedMessagesCount)
                }
            }
            is AsyncData.Success -> {
                val currentPinnedMessage = pinnedItems.data.getOrNull(currentPinnedMessageIndex)
                if (currentPinnedMessage == null) {
                    PinnedMessagesBannerState.Hidden
                } else {
                    PinnedMessagesBannerState.Loaded(
                        loadedPinnedMessagesCount = pinnedItems.data.size,
                        currentPinnedMessageIndex = currentPinnedMessageIndex,
                        currentPinnedMessage = currentPinnedMessage,
                        eventSink = eventSink
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Composable
    private fun PinnedMessagesBannerItemsEffect(
        onItemsChange: (AsyncData<ImmutableList<PinnedMessagesBannerItem>>) -> Unit,
    ) {
        val updatedOnItemsChange by rememberUpdatedState(onItemsChange)
        LaunchedEffect(Unit) {
            pinnedEventsTimelineProvider.timelineStateFlow
                .flatMapLatest { asyncTimeline ->
                    when (asyncTimeline) {
                        AsyncData.Uninitialized -> flowOf(AsyncData.Uninitialized)
                        is AsyncData.Failure -> flowOf(AsyncData.Failure(asyncTimeline.error))
                        is AsyncData.Loading -> flowOf(AsyncData.Loading())
                        is AsyncData.Success -> {
                            asyncTimeline.data.timelineItems
                                .map { timelineItems ->
                                    val pinnedItems = timelineItems.mapNotNull { timelineItem ->
                                        itemFactory.create(timelineItem)
                                    }.toImmutableList()

                                    AsyncData.Success(pinnedItems)
                                }
                        }
                    }
                }
                .onEach { newItems ->
                    updatedOnItemsChange(newItems)
                }
                .launchIn(this)
        }
    }
}

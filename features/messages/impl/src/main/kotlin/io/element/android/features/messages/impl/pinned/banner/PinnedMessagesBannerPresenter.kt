/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import io.element.android.features.messages.impl.pinned.IsPinnedMessagesFeatureEnabled
import io.element.android.features.networkmonitor.api.NetworkMonitor
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.room.MatrixRoom
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

class PinnedMessagesBannerPresenter @Inject constructor(
    private val room: MatrixRoom,
    private val itemFactory: PinnedMessagesBannerItemFactory,
    private val isFeatureEnabled: IsPinnedMessagesFeatureEnabled,
    private val networkMonitor: NetworkMonitor,
) : Presenter<PinnedMessagesBannerState> {
    private val pinnedItems = mutableStateOf<ImmutableList<PinnedMessagesBannerItem>>(persistentListOf())

    @Composable
    override fun present(): PinnedMessagesBannerState {
        val isFeatureEnabled = isFeatureEnabled()
        val expectedPinnedMessagesCount by remember {
            room.roomInfoFlow.map { roomInfo -> roomInfo.pinnedEventIds.size }
        }.collectAsState(initial = 0)

        var hasTimelineFailedToLoad by rememberSaveable { mutableStateOf(false) }
        var currentPinnedMessageIndex by rememberSaveable { mutableIntStateOf(-1) }

        PinnedMessagesBannerItemsEffect(
            isFeatureEnabled = isFeatureEnabled,
            onItemsChange = { newItems ->
                val pinnedMessageCount = newItems.size
                if (currentPinnedMessageIndex >= pinnedMessageCount || currentPinnedMessageIndex < 0) {
                    currentPinnedMessageIndex = pinnedMessageCount - 1
                }
                pinnedItems.value = newItems
            },
            onTimelineFail = { hasTimelineFailed ->
                hasTimelineFailedToLoad = hasTimelineFailed
            }
        )

        fun handleEvent(event: PinnedMessagesBannerEvents) {
            when (event) {
                is PinnedMessagesBannerEvents.MoveToNextPinned -> {
                    currentPinnedMessageIndex = (currentPinnedMessageIndex - 1).mod(pinnedItems.value.size)
                }
            }
        }

        return pinnedMessagesBannerState(
            isFeatureEnabled = isFeatureEnabled,
            hasTimelineFailed = hasTimelineFailedToLoad,
            expectedPinnedMessagesCount = expectedPinnedMessagesCount,
            pinnedItems = pinnedItems.value,
            currentPinnedMessageIndex = currentPinnedMessageIndex,
            eventSink = ::handleEvent
        )
    }

    @Composable
    private fun pinnedMessagesBannerState(
        isFeatureEnabled: Boolean,
        hasTimelineFailed: Boolean,
        expectedPinnedMessagesCount: Int,
        pinnedItems: ImmutableList<PinnedMessagesBannerItem>,
        currentPinnedMessageIndex: Int,
        eventSink: (PinnedMessagesBannerEvents) -> Unit
    ): PinnedMessagesBannerState {
        val currentPinnedMessage = pinnedItems.getOrNull(currentPinnedMessageIndex)
        return when {
            !isFeatureEnabled -> PinnedMessagesBannerState.Hidden
            hasTimelineFailed -> PinnedMessagesBannerState.Hidden
            currentPinnedMessage != null -> PinnedMessagesBannerState.Loaded(
                currentPinnedMessage = currentPinnedMessage,
                currentPinnedMessageIndex = currentPinnedMessageIndex,
                loadedPinnedMessagesCount = pinnedItems.size,
                eventSink = eventSink
            )
            expectedPinnedMessagesCount == 0 -> PinnedMessagesBannerState.Hidden
            else -> PinnedMessagesBannerState.Loading(expectedPinnedMessagesCount = expectedPinnedMessagesCount)
        }
    }

    @OptIn(FlowPreview::class)
    @Composable
    private fun PinnedMessagesBannerItemsEffect(
        isFeatureEnabled: Boolean,
        onItemsChange: (ImmutableList<PinnedMessagesBannerItem>) -> Unit,
        onTimelineFail: (Boolean) -> Unit,
    ) {
        val updatedOnItemsChange by rememberUpdatedState(onItemsChange)
        val updatedOnTimelineFail by rememberUpdatedState(onTimelineFail)
        val networkStatus by networkMonitor.connectivity.collectAsState()

        LaunchedEffect(isFeatureEnabled, networkStatus) {
            if (!isFeatureEnabled) {
                updatedOnItemsChange(persistentListOf())
                return@LaunchedEffect
            }
            val pinnedEventsTimeline = room.pinnedEventsTimeline()
                .onFailure { updatedOnTimelineFail(true) }
                .onSuccess { updatedOnTimelineFail(false) }
                .getOrNull()
                ?: return@LaunchedEffect

            pinnedEventsTimeline.timelineItems
                .debounce(300.milliseconds)
                .map { timelineItems ->
                    timelineItems.mapNotNull { timelineItem ->
                        itemFactory.create(timelineItem)
                    }.toImmutableList()
                }
                .onEach { newItems ->
                    updatedOnItemsChange(newItems)
                }
                .onCompletion {
                    pinnedEventsTimeline.close()
                }
                .launchIn(this)
        }
    }
}

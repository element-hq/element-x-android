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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.AnnotatedString
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.eventformatter.api.PinnedMessagesBannerFormatter
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

class PinnedMessagesBannerPresenter @Inject constructor(
    private val room: MatrixRoom,
    private val pinnedMessagesBannerFormatter: PinnedMessagesBannerFormatter,
) : Presenter<PinnedMessagesBannerState> {

    @OptIn(FlowPreview::class)
    @Composable
    override fun present(): PinnedMessagesBannerState {
        var pinnedMessages by remember {
            mutableStateOf<List<PinnedMessagesBannerItem>>(emptyList())
        }
        var currentPinnedMessageIndex by rememberSaveable {
            mutableIntStateOf(0)
        }
        LaunchedEffect(pinnedMessages) {
            val pinnedMessageCount = pinnedMessages.size
            if (currentPinnedMessageIndex >= pinnedMessageCount) {
                currentPinnedMessageIndex = (pinnedMessageCount - 1).coerceAtLeast(0)
            }
        }

        LaunchedEffect(Unit) {
            val pinnedEventsTimeline = room.pinnedEventsTimeline().getOrNull() ?: return@LaunchedEffect
            pinnedEventsTimeline.timelineItems
                .debounce(300.milliseconds)
                .map { timelineItems ->
                    timelineItems.mapNotNull { timelineItem ->
                        when (timelineItem) {
                            is MatrixTimelineItem.Event -> {
                                val eventId = timelineItem.eventId ?: return@mapNotNull null
                                val formatted = pinnedMessagesBannerFormatter.format(timelineItem.event)
                                PinnedMessagesBannerItem(
                                    eventId = eventId,
                                    formatted = if (formatted is AnnotatedString) {
                                        formatted
                                    } else {
                                        AnnotatedString(formatted.toString())
                                    },
                                )
                            }
                            else -> null
                        }
                    }
                }
                .flowOn(Dispatchers.Default)
                .onEach { newPinnedMessages ->
                    pinnedMessages = newPinnedMessages
                }.onCompletion {
                    pinnedEventsTimeline.close()
                }
                .launchIn(this)
        }

        fun handleEvent(event: PinnedMessagesBannerEvents) {
            when (event) {
                is PinnedMessagesBannerEvents.MoveToNextPinned -> {
                    if (currentPinnedMessageIndex < pinnedMessages.size - 1) {
                        currentPinnedMessageIndex++
                    } else {
                        currentPinnedMessageIndex = 0
                    }
                }
            }
        }

        return PinnedMessagesBannerState(
            pinnedMessagesCount = pinnedMessages.size,
            currentPinnedMessage = pinnedMessages.getOrNull(currentPinnedMessageIndex),
            currentPinnedMessageIndex = currentPinnedMessageIndex,
            eventSink = ::handleEvent
        )
    }
}

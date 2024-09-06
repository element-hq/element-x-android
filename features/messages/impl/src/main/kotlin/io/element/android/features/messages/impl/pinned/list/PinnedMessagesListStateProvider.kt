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

package io.element.android.features.messages.impl.pinned.list

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.messages.impl.UserEventPermissions
import io.element.android.features.messages.impl.actionlist.ActionListState
import io.element.android.features.messages.impl.actionlist.anActionListState
import io.element.android.features.messages.impl.timeline.TimelineRoomInfo
import io.element.android.features.messages.impl.timeline.aTimelineItemDaySeparator
import io.element.android.features.messages.impl.timeline.aTimelineItemEvent
import io.element.android.features.messages.impl.timeline.aTimelineItemReactions
import io.element.android.features.messages.impl.timeline.aTimelineRoomInfo
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.TimelineItemGroupPosition
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemAudioContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemFileContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemPollContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemTextContent
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

open class PinnedMessagesListStateProvider : PreviewParameterProvider<PinnedMessagesListState> {
    override val values: Sequence<PinnedMessagesListState>
        get() = sequenceOf(
            aFailedPinnedMessagesListState(),
            aLoadingPinnedMessagesListState(),
            anEmptyPinnedMessagesListState(),
            aLoadedPinnedMessagesListState(
                timelineItems = persistentListOf(
                    aTimelineItemEvent(
                        isMine = false,
                        content = aTimelineItemTextContent("A pinned message"),
                        groupPosition = TimelineItemGroupPosition.Last,
                        timelineItemReactions = aTimelineItemReactions(0)
                    ),
                    aTimelineItemEvent(
                        isMine = false,
                        content = aTimelineItemAudioContent("A pinned file"),
                        groupPosition = TimelineItemGroupPosition.Middle,
                        timelineItemReactions = aTimelineItemReactions(0)
                    ),
                    aTimelineItemEvent(
                        isMine = false,
                        content = aTimelineItemPollContent("A pinned poll?"),
                        groupPosition = TimelineItemGroupPosition.First,
                        timelineItemReactions = aTimelineItemReactions(0)
                    ),
                    aTimelineItemDaySeparator(),
                    aTimelineItemEvent(
                        isMine = true,
                        content = aTimelineItemTextContent("A pinned message"),
                        groupPosition = TimelineItemGroupPosition.Last,
                        timelineItemReactions = aTimelineItemReactions(0)
                    ),
                    aTimelineItemEvent(
                        isMine = true,
                        content = aTimelineItemFileContent("A pinned file?"),
                        groupPosition = TimelineItemGroupPosition.Middle,
                        timelineItemReactions = aTimelineItemReactions(0)
                    ),
                    aTimelineItemEvent(
                        isMine = true,
                        content = aTimelineItemPollContent("A pinned poll?"),
                        groupPosition = TimelineItemGroupPosition.First,
                        timelineItemReactions = aTimelineItemReactions(0)
                    ),
                )
            )
        )
}

fun aFailedPinnedMessagesListState() = PinnedMessagesListState.Failed

fun aLoadingPinnedMessagesListState() = PinnedMessagesListState.Loading

fun anEmptyPinnedMessagesListState() = PinnedMessagesListState.Empty

fun aLoadedPinnedMessagesListState(
    timelineRoomInfo: TimelineRoomInfo = aTimelineRoomInfo(),
    timelineItems: List<TimelineItem> = emptyList(),
    actionListState: ActionListState = anActionListState(),
    aUserEventPermissions: UserEventPermissions = UserEventPermissions.DEFAULT,
    eventSink: (PinnedMessagesListEvents) -> Unit = {}
) = PinnedMessagesListState.Filled(
    timelineRoomInfo = timelineRoomInfo,
    timelineItems = timelineItems.toImmutableList(),
    actionListState = actionListState,
    userEventPermissions = aUserEventPermissions,
    eventSink = eventSink,
)

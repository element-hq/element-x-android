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

import io.element.android.features.messages.impl.timeline.TimelineRoomInfo
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import kotlinx.collections.immutable.ImmutableList

data class PinnedMessagesListState(
    val timelineRoomInfo: TimelineRoomInfo,
    val timelineItems: ImmutableList<TimelineItem>,
    val eventSink: (PinnedMessagesListEvents) -> Unit,

    val pinnedMessagesCount: String =
        timelineItems
            .count { timelineItem -> timelineItem is TimelineItem.Event }
            .takeIf { it > 0 }
            ?.toString()
            ?: ""
)

/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.messages.impl.timeline.model.event

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.poll.api.pollcontent.PollAnswerItem
import io.element.android.features.poll.api.pollcontent.aPollAnswerItemList
import io.element.android.features.poll.api.pollcontent.aPollQuestion
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.poll.PollKind

open class TimelineItemPollContentProvider : PreviewParameterProvider<TimelineItemPollContent> {
    override val values: Sequence<TimelineItemPollContent>
        get() = sequenceOf(
            aTimelineItemPollContent(),
            aTimelineItemPollContent().copy(pollKind = PollKind.Undisclosed),
            aTimelineItemPollContent().copy(isMine = true),
            aTimelineItemPollContent().copy(isMine = true, isEditable = true),
        )
}

fun aTimelineItemPollContent(
    question: String = aPollQuestion(),
    answerItems: List<PollAnswerItem> = aPollAnswerItemList(),
    isMine: Boolean = false,
    isEditable: Boolean = false,
    isEnded: Boolean = false,
    isEdited: Boolean = false,
): TimelineItemPollContent {
    return TimelineItemPollContent(
        eventId = EventId("\$anEventId"),
        pollKind = PollKind.Disclosed,
        question = question,
        answerItems = answerItems,
        isMine = isMine,
        isEditable = isEditable,
        isEnded = isEnded,
        isEdited = isEdited,
    )
}

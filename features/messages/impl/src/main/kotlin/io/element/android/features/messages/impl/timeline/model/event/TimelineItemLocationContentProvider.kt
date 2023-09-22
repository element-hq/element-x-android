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

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.location.api.Location
import io.element.android.features.poll.api.PollAnswerItem
import io.element.android.features.poll.api.content.PollContentPresenter
import io.element.android.features.poll.api.content.PollContentState
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.poll.PollAnswer
import io.element.android.libraries.matrix.api.poll.PollKind
import io.element.android.libraries.matrix.api.timeline.item.event.PollContent

open class TimelineItemLocationContentProvider : PreviewParameterProvider<TimelineItemLocationContent> {
    override val values: Sequence<TimelineItemLocationContent>
        get() = sequenceOf(
            aTimelineItemLocationContent(),
            aTimelineItemLocationContent("This is a description!"),
        )
}

fun aTimelineItemLocationContent(description: String? = null) = TimelineItemLocationContent(
    body = "User location geo:52.2445,0.7186;u=5000",
    location = Location(
        lat = 52.2445,
        lon = 0.7186,
        accuracy = 5000f,
    ),
    description = description,
)

fun aTimelineItemPollContent(
    isEnded: Boolean = false,
) = TimelineItemPollContent(
    eventId = EventId("\$anEventId"),
    question = "Some question?",
    answerItems = listOf(
        PollAnswerItem(
            answer = PollAnswer("id_1", "Answer1"),
            isSelected = false,
            isEnabled = false,
            isWinner = false,
            isDisclosed = false,
            votesCount = 0,
            percentage = 0.0f,
        ),
        PollAnswerItem(
            answer = PollAnswer("id_2", "Answer2"),
            isSelected = false,
            isEnabled = false,
            isWinner = false,
            isDisclosed = false,
            votesCount = 0,
            percentage = 0.0f,
        ),
    ),
    pollKind = PollKind.Disclosed,
    isEnded = isEnded,
    presenter = aPollContentPresenter(),
)

fun aPollContentPresenter() = object: PollContentPresenter {
    @Composable
    override fun present(): PollContentState {
        return PollContentState(
            content = PollContent(
                question = "ea",
                kind = PollKind.Disclosed,
                maxSelections = 1u,
                answers = listOf(),
                votes = mapOf(),
                endTime = null
            ),
            someRandomString = "saperet",
            someState = false,
            eventSink = {}
        )
    }
}

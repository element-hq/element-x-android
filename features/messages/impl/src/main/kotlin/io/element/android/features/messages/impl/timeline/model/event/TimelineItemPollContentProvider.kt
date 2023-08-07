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
import io.element.android.libraries.matrix.api.poll.PollAnswer
import io.element.android.libraries.matrix.api.poll.PollKind
import io.element.android.libraries.matrix.ui.components.aMatrixUserList

open class TimelineItemPollContentProvider : PreviewParameterProvider<TimelineItemPollContent> {
    override val values: Sequence<TimelineItemPollContent>
        get() = sequenceOf(
            aTimelineItemPollContent(),
            aTimelineItemPollContent().copy(kind = PollKind.Undisclosed)
        )
}

fun aTimelineItemPollContent(): TimelineItemPollContent {
    val aUserList = aMatrixUserList().map { it.userId }
    return TimelineItemPollContent(
        kind = PollKind.Disclosed,
        question = "What type of food should we have at the party?",
        answers = listOf(
            PollAnswer("option_1", "Italian \uD83C\uDDEE\uD83C\uDDF9"),
            PollAnswer("option_2", "Chinese \uD83C\uDDE8\uD83C\uDDF3"),
            PollAnswer("option_3", "Brazilian \uD83C\uDDE7\uD83C\uDDF7"),
            PollAnswer("option_4", "French \uD83C\uDDEB\uD83C\uDDF7 But make it a very very very long option then this should just keep expanding"),
        ),
        votes = mapOf(
            "option_1" to aUserList.take(2),
            "option_2" to aUserList.take(5),
            "option_3" to aUserList.take(3),
            "option_4" to emptyList(),
        ),
    )
}

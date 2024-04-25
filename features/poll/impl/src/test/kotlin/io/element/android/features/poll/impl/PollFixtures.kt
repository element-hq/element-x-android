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

package io.element.android.features.poll.impl

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.poll.PollAnswer
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.api.timeline.item.event.PollContent
import io.element.android.libraries.matrix.test.timeline.aPollContent
import io.element.android.libraries.matrix.test.timeline.anEventTimelineItem
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

fun aPollTimelineItems(
    polls: Map<EventId, PollContent> = emptyMap(),
): Flow<List<MatrixTimelineItem>> {
    return flowOf(
        polls.map { entry ->
            MatrixTimelineItem.Event(
                entry.key.value,
                anEventTimelineItem(
                    eventId = entry.key,
                    content = entry.value,
                )
            )
        }
    )
}

fun anOngoingPollContent() = aPollContent(
    question = "Do you like polls?",
    answers = persistentListOf(
        PollAnswer("1", "Yes"),
        PollAnswer("2", "No"),
        PollAnswer("2", "Maybe"),
    ),
)

fun anEndedPollContent() = anOngoingPollContent().copy(
    endTime = 1702400215U
)

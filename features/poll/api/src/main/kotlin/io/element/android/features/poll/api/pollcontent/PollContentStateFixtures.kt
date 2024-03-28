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

package io.element.android.features.poll.api.pollcontent

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.poll.PollAnswer
import io.element.android.libraries.matrix.api.poll.PollKind
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

fun aPollQuestion() = "What type of food should we have at the party?"

fun aPollAnswerItemList(
    hasVotes: Boolean = true,
    isEnded: Boolean = false,
    showVotes: Boolean = true,
) = persistentListOf(
    aPollAnswerItem(
        answer = PollAnswer("option_1", "Italian \uD83C\uDDEE\uD83C\uDDF9"),
        showVotes = showVotes,
        isEnabled = !isEnded,
        isWinner = isEnded,
        votesCount = if (hasVotes) 5 else 0,
        percentage = if (hasVotes) 0.5f else 0f
    ),
    aPollAnswerItem(
        answer = PollAnswer("option_2", "Chinese \uD83C\uDDE8\uD83C\uDDF3"),
        showVotes = showVotes,
        isEnabled = !isEnded,
        isWinner = false,
        votesCount = 0,
        percentage = 0f
    ),
    aPollAnswerItem(
        answer = PollAnswer("option_3", "Brazilian \uD83C\uDDE7\uD83C\uDDF7"),
        showVotes = showVotes,
        isEnabled = !isEnded,
        isWinner = false,
        isSelected = true,
        votesCount = if (hasVotes) 1 else 0,
        percentage = if (hasVotes) 0.1f else 0f
    ),
    aPollAnswerItem(
        showVotes = showVotes,
        isEnabled = !isEnded,
        votesCount = if (hasVotes) 4 else 0,
        percentage = if (hasVotes) 0.4f else 0f,
    ),
)

fun aPollAnswerItem(
    answer: PollAnswer = PollAnswer(
        "option_4",
        "French \uD83C\uDDEB\uD83C\uDDF7 But make it a very very very long option then this should just keep expanding"
    ),
    isSelected: Boolean = false,
    isEnabled: Boolean = true,
    isWinner: Boolean = false,
    showVotes: Boolean = true,
    votesCount: Int = 4,
    percentage: Float = 0.4f,
) = PollAnswerItem(
    answer = answer,
    isSelected = isSelected,
    isEnabled = isEnabled,
    isWinner = isWinner,
    showVotes = showVotes,
    votesCount = votesCount,
    percentage = percentage
)

fun aPollContentState(
    eventId: EventId? = null,
    isMine: Boolean = false,
    isEnded: Boolean = false,
    showVotes: Boolean = true,
    isPollEditable: Boolean = true,
    hasVotes: Boolean = true,
    question: String = aPollQuestion(),
    pollKind: PollKind = PollKind.Disclosed,
    answerItems: ImmutableList<PollAnswerItem> = aPollAnswerItemList(
        isEnded = isEnded,
        showVotes = showVotes,
        hasVotes = hasVotes
    ),
) = PollContentState(
    eventId = eventId,
    question = question,
    answerItems = answerItems,
    pollKind = pollKind,
    isPollEditable = isMine && !isEnded && isPollEditable,
    isPollEnded = isEnded,
    isMine = isMine,
)

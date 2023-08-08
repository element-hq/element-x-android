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

package io.element.android.features.messages.impl.timeline.factories.event

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEventContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemPollContent
import io.element.android.features.poll.api.PollAnswerItem
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.poll.PollKind
import io.element.android.libraries.matrix.api.timeline.item.event.PollContent
import io.element.android.libraries.matrix.api.user.getCurrentUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class TimelineItemContentPollFactory @Inject constructor(
    private val matrixClient: MatrixClient,
    private val dispatchers: CoroutineDispatchers,
    private val appScope: CoroutineScope,
) {

    fun create(content: PollContent): TimelineItemEventContent {
        return TimelineItemPollContent(
            question = content.question,
            answers = content.getPollAnswerItems(),
            votes = content.votes,
            isDisclosed = content.isPollDisclosed(),
        )
    }

    private fun PollContent.getPollAnswerItems(): List<PollAnswerItem> {
        val pollVotesCount = votes.flatMap { it.value }.size
        return answers.map { answer ->
            val answerVotes = votes[answer.id]?.size ?: 0
            val progress = if (pollVotesCount > 0) answerVotes / pollVotesCount.toFloat() else 0f
            PollAnswerItem(
                answer = answer,
                votesCount = answerVotes,
                progress = progress,
            )
        }
    }

    // Todo Move this computation to the matrix rust sdk
    private fun PollContent.isPollDisclosed(): MutableState<Async<Boolean>> {
        val mutableState = mutableStateOf<Async<Boolean>>(Async.Loading())
        appScope.launch(dispatchers.io) {
            mutableState.value = runCatching {
                val myUserId = matrixClient.getCurrentUser().userId
                val hasVoted = myUserId in votes.flatMap { it.value }
                kind == PollKind.Disclosed && hasVoted
            }.fold(
                { Async.Success(it) },
                { Async.Failure(it) },
            )
        }
        return mutableState
    }
}

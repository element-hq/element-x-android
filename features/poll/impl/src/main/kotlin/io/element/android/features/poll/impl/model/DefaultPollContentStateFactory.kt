/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.poll.impl.model

import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.poll.api.pollcontent.PollAnswerItem
import io.element.android.features.poll.api.pollcontent.PollContentState
import io.element.android.features.poll.api.pollcontent.PollContentStateFactory
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.poll.isDisclosed
import io.element.android.libraries.matrix.api.timeline.item.event.PollContent
import kotlinx.collections.immutable.toImmutableList

@ContributesBinding(RoomScope::class)
class DefaultPollContentStateFactory(
    private val matrixClient: MatrixClient,
) : PollContentStateFactory {
    override suspend fun create(
        eventId: EventId?,
        isEditable: Boolean,
        isOwn: Boolean,
        content: PollContent,
    ): PollContentState {
        val totalVoteCount = content.votes.flatMap { it.value }.size
        val myVotes = content.votes.filter { matrixClient.sessionId in it.value }.keys
        val isPollEnded = content.endTime != null
        val winnerIds = if (!isPollEnded) {
            emptyList()
        } else {
            content.answers
                .map { answer -> answer.id }
                .groupBy { answerId -> content.votes[answerId]?.size ?: 0 } // Group by votes count
                .maxByOrNull { (votes, _) -> votes } // Keep max voted answers
                ?.takeIf { (votes, _) -> votes > 0 } // Ignore if no option has been voted
                ?.value
                .orEmpty()
        }
        val answerItems = content.answers.map { answer ->
            val answerVoteCount = content.votes[answer.id]?.size ?: 0
            val isSelected = answer.id in myVotes
            val isWinner = answer.id in winnerIds
            val percentage = if (totalVoteCount > 0) answerVoteCount.toFloat() / totalVoteCount.toFloat() else 0f
            PollAnswerItem(
                answer = answer,
                isSelected = isSelected,
                isEnabled = !isPollEnded,
                isWinner = isWinner,
                showVotes = content.kind.isDisclosed || isPollEnded,
                votesCount = answerVoteCount,
                percentage = percentage,
            )
        }

        return PollContentState(
            eventId = eventId,
            question = content.question,
            answerItems = answerItems.toImmutableList(),
            pollKind = content.kind,
            isPollEditable = isEditable,
            isPollEnded = isPollEnded,
            isMine = isOwn,
        )
    }
}

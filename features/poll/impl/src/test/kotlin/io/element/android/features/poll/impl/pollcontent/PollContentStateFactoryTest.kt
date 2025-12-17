/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.poll.impl.pollcontent

import com.google.common.truth.Truth.assertThat
import io.element.android.features.poll.api.pollcontent.PollAnswerItem
import io.element.android.features.poll.api.pollcontent.PollContentState
import io.element.android.features.poll.impl.model.DefaultPollContentStateFactory
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.poll.PollAnswer
import io.element.android.libraries.matrix.api.poll.PollKind
import io.element.android.libraries.matrix.api.timeline.item.event.PollContent
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.A_USER_ID_10
import io.element.android.libraries.matrix.test.A_USER_ID_2
import io.element.android.libraries.matrix.test.A_USER_ID_3
import io.element.android.libraries.matrix.test.A_USER_ID_4
import io.element.android.libraries.matrix.test.A_USER_ID_5
import io.element.android.libraries.matrix.test.A_USER_ID_6
import io.element.android.libraries.matrix.test.A_USER_ID_7
import io.element.android.libraries.matrix.test.A_USER_ID_8
import io.element.android.libraries.matrix.test.A_USER_ID_9
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.timeline.anEventTimelineItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.test.runTest
import org.junit.Test

class PollContentStateFactoryTest {
    private val factory = DefaultPollContentStateFactory(FakeMatrixClient())
    private val eventTimelineItem = anEventTimelineItem()

    @Test
    fun `Disclosed poll - not ended, no votes`() = runTest {
        val state = factory.create(eventTimelineItem, aPollContent())
        val expectedState = aPollContentState()
        assertThat(state).isEqualTo(expectedState)
    }

    @Test
    fun `Disclosed poll - not ended, some votes, including one from current user`() = runTest {
        val votes = MY_USER_WINNING_VOTES.mapKeys { it.key.id }.toImmutableMap()
        val state = factory.create(
            eventTimelineItem,
            aPollContent(votes = votes)
        )
        val expectedState = aPollContentState(
            answerItems = listOf(
                aPollAnswerItem(answer = A_POLL_ANSWER_1, votesCount = 3, percentage = 0.3f),
                aPollAnswerItem(answer = A_POLL_ANSWER_2, isSelected = true, votesCount = 6, percentage = 0.6f),
                aPollAnswerItem(answer = A_POLL_ANSWER_3),
                aPollAnswerItem(answer = A_POLL_ANSWER_4, votesCount = 1, percentage = 0.1f),
            )
        )
        assertThat(state).isEqualTo(expectedState)
    }

    @Test
    fun `Disclosed poll - ended, no votes, no winner`() = runTest {
        val state = factory.create(eventTimelineItem, aPollContent(endTime = 1UL))
        val expectedState = aPollContentState().let {
            it.copy(
                answerItems = it.answerItems.map { answerItem -> answerItem.copy(isEnabled = false) }.toImmutableList(),
                isPollEnded = true,
            )
        }
        assertThat(state).isEqualTo(expectedState)
    }

    @Test
    fun `Disclosed poll - ended, some votes, including one from current user (winner)`() = runTest {
        val votes = MY_USER_WINNING_VOTES.mapKeys { it.key.id }.toImmutableMap()
        val state = factory.create(
            eventTimelineItem,
            aPollContent(votes = votes, endTime = 1UL)
        )
        val expectedState = aPollContentState(
            answerItems = listOf(
                aPollAnswerItem(answer = A_POLL_ANSWER_1, isEnabled = false, votesCount = 3, percentage = 0.3f),
                aPollAnswerItem(answer = A_POLL_ANSWER_2, isSelected = true, isEnabled = false, isWinner = true, votesCount = 6, percentage = 0.6f),
                aPollAnswerItem(answer = A_POLL_ANSWER_3, isEnabled = false),
                aPollAnswerItem(answer = A_POLL_ANSWER_4, isEnabled = false, votesCount = 1, percentage = 0.1f),
            ),
            isEnded = true,
        )
        assertThat(state).isEqualTo(expectedState)
    }

    @Test
    fun `Disclosed poll - ended, some votes, including one from current user (not winner) and two winning votes`() = runTest {
        val votes = OTHER_WINNING_VOTES.mapKeys { it.key.id }.toImmutableMap()
        val state = factory.create(
            eventTimelineItem,
            aPollContent(votes = votes, endTime = 1UL)
        )
        val expectedState = aPollContentState(
            answerItems = listOf(
                aPollAnswerItem(answer = A_POLL_ANSWER_1, isEnabled = false, isWinner = true, votesCount = 4, percentage = 0.4f),
                aPollAnswerItem(answer = A_POLL_ANSWER_2, isSelected = true, isEnabled = false, votesCount = 2, percentage = 0.2f),
                aPollAnswerItem(answer = A_POLL_ANSWER_3, isEnabled = false),
                aPollAnswerItem(answer = A_POLL_ANSWER_4, isEnabled = false, isWinner = true, votesCount = 4, percentage = 0.4f),
            ),
            isEnded = true,
        )
        assertThat(state).isEqualTo(expectedState)
    }

    @Test
    fun `Undisclosed poll - not ended, no votes`() = runTest {
        val state = factory.create(eventTimelineItem, aPollContent(PollKind.Undisclosed))
        val expectedState = aPollContentState(pollKind = PollKind.Undisclosed).let {
            it.copy(
                answerItems = it.answerItems.map { answerItem -> answerItem.copy(showVotes = false) }.toImmutableList()
            )
        }
        assertThat(state).isEqualTo(expectedState)
    }

    @Test
    fun `Undisclosed poll - not ended, some votes, including one from current user`() = runTest {
        val votes = MY_USER_WINNING_VOTES.mapKeys { it.key.id }.toImmutableMap()
        val state = factory.create(
            eventTimelineItem,
            aPollContent(PollKind.Undisclosed, votes = votes)
        )
        val expectedState = aPollContentState(
            pollKind = PollKind.Undisclosed,
            answerItems = listOf(
                aPollAnswerItem(answer = A_POLL_ANSWER_1, showVotes = false, votesCount = 3, percentage = 0.3f),
                aPollAnswerItem(answer = A_POLL_ANSWER_2, showVotes = false, isSelected = true, votesCount = 6, percentage = 0.6f),
                aPollAnswerItem(answer = A_POLL_ANSWER_3, showVotes = false),
                aPollAnswerItem(answer = A_POLL_ANSWER_4, showVotes = false, votesCount = 1, percentage = 0.1f),
            ),
        )
        assertThat(state).isEqualTo(expectedState)
    }

    @Test
    fun `Undisclosed poll - ended, no votes, no winner`() = runTest {
        val state = factory.create(eventTimelineItem, aPollContent(PollKind.Undisclosed, endTime = 1UL))
        val expectedState = aPollContentState(
            isEnded = true,
            pollKind = PollKind.Undisclosed
        ).let {
            it.copy(
                answerItems = it.answerItems.map { answerItem -> answerItem.copy(showVotes = true, isEnabled = false) }.toImmutableList(),
            )
        }
        assertThat(state).isEqualTo(expectedState)
    }

    @Test
    fun `Undisclosed poll - ended, some votes, including one from current user (winner)`() = runTest {
        val votes = MY_USER_WINNING_VOTES.mapKeys { it.key.id }.toImmutableMap()
        val state = factory.create(
            eventTimelineItem,
            aPollContent(PollKind.Undisclosed, votes = votes, endTime = 1UL)
        )
        val expectedState = aPollContentState(
            pollKind = PollKind.Undisclosed,
            answerItems = listOf(
                aPollAnswerItem(answer = A_POLL_ANSWER_1, isEnabled = false, votesCount = 3, percentage = 0.3f),
                aPollAnswerItem(answer = A_POLL_ANSWER_2, isSelected = true, isEnabled = false, isWinner = true, votesCount = 6, percentage = 0.6f),
                aPollAnswerItem(answer = A_POLL_ANSWER_3, isEnabled = false),
                aPollAnswerItem(answer = A_POLL_ANSWER_4, isEnabled = false, votesCount = 1, percentage = 0.1f),
            ),
            isEnded = true,
        )
        assertThat(state).isEqualTo(expectedState)
    }

    @Test
    fun `Undisclosed poll - ended, some votes, including one from current user (not winner) and two winning votes`() = runTest {
        val votes = OTHER_WINNING_VOTES.mapKeys { it.key.id }.toImmutableMap()
        val state = factory.create(
            eventTimelineItem,
            aPollContent(PollKind.Undisclosed, votes = votes, endTime = 1UL)
        )
        val expectedState = aPollContentState(
            pollKind = PollKind.Undisclosed,
            answerItems = listOf(
                aPollAnswerItem(answer = A_POLL_ANSWER_1, isEnabled = false, isWinner = true, votesCount = 4, percentage = 0.4f),
                aPollAnswerItem(answer = A_POLL_ANSWER_2, isSelected = true, isEnabled = false, votesCount = 2, percentage = 0.2f),
                aPollAnswerItem(answer = A_POLL_ANSWER_3, isEnabled = false),
                aPollAnswerItem(answer = A_POLL_ANSWER_4, isEnabled = false, isWinner = true, votesCount = 4, percentage = 0.4f),
            ),
            isEnded = true,
        )
        assertThat(state).isEqualTo(expectedState)
    }

    @Test
    fun `eventId is populated`() = runTest {
        val state = factory.create(eventTimelineItem, aPollContent())
        assertThat(state.eventId).isEqualTo(eventTimelineItem.eventId)
    }

    private fun aPollContent(
        pollKind: PollKind = PollKind.Disclosed,
        votes: ImmutableMap<String, ImmutableList<UserId>> = persistentMapOf(),
        endTime: ULong? = null,
    ): PollContent = PollContent(
        question = A_POLL_QUESTION,
        kind = pollKind,
        maxSelections = 1UL,
        answers = persistentListOf(A_POLL_ANSWER_1, A_POLL_ANSWER_2, A_POLL_ANSWER_3, A_POLL_ANSWER_4),
        votes = votes,
        endTime = endTime,
        isEdited = false,
    )

    private fun aPollContentState(
        eventId: EventId? = AN_EVENT_ID,
        pollKind: PollKind = PollKind.Disclosed,
        answerItems: List<PollAnswerItem> = listOf(
            aPollAnswerItem(A_POLL_ANSWER_1),
            aPollAnswerItem(A_POLL_ANSWER_2),
            aPollAnswerItem(A_POLL_ANSWER_3),
            aPollAnswerItem(A_POLL_ANSWER_4),
        ),
        isEnded: Boolean = false,
        isMine: Boolean = false,
        isEditable: Boolean = false,
        question: String = A_POLL_QUESTION,
    ) = PollContentState(
        eventId = eventId,
        question = question,
        answerItems = answerItems.toImmutableList(),
        pollKind = pollKind,
        isPollEditable = isEditable,
        isPollEnded = isEnded,
        isMine = isMine,
    )

    private fun aPollAnswerItem(
        answer: PollAnswer,
        isSelected: Boolean = false,
        isEnabled: Boolean = true,
        isWinner: Boolean = false,
        showVotes: Boolean = true,
        votesCount: Int = 0,
        percentage: Float = 0f,
    ) = PollAnswerItem(
        answer = answer,
        isSelected = isSelected,
        isEnabled = isEnabled,
        isWinner = isWinner,
        showVotes = showVotes,
        votesCount = votesCount,
        percentage = percentage,
    )

    private companion object TestData {
        private const val A_POLL_QUESTION = "What is your favorite food?"
        private val A_POLL_ANSWER_1 = PollAnswer("id_1", "Pizza")
        private val A_POLL_ANSWER_2 = PollAnswer("id_2", "Pasta")
        private val A_POLL_ANSWER_3 = PollAnswer("id_3", "French Fries")
        private val A_POLL_ANSWER_4 = PollAnswer("id_4", "Hamburger")

        private val MY_USER_WINNING_VOTES = persistentMapOf(
            A_POLL_ANSWER_1 to persistentListOf(A_USER_ID_2, A_USER_ID_3, A_USER_ID_4),
            // First item (A_USER_ID) is for my vote
            // winner
            A_POLL_ANSWER_2 to persistentListOf(A_USER_ID, A_USER_ID_5, A_USER_ID_6, A_USER_ID_7, A_USER_ID_8, A_USER_ID_9),
            A_POLL_ANSWER_3 to persistentListOf(),
            A_POLL_ANSWER_4 to persistentListOf(A_USER_ID_10),
        )
        private val OTHER_WINNING_VOTES = persistentMapOf(
            // A winner
            A_POLL_ANSWER_1 to persistentListOf(A_USER_ID_2, A_USER_ID_3, A_USER_ID_4, A_USER_ID_5),
            // First item (A_USER_ID) is for my vote
            A_POLL_ANSWER_2 to persistentListOf(A_USER_ID, A_USER_ID_6),
            A_POLL_ANSWER_3 to persistentListOf(),
            // Other winner
            A_POLL_ANSWER_4 to persistentListOf(A_USER_ID_7, A_USER_ID_8, A_USER_ID_9, A_USER_ID_10),
        )
    }
}

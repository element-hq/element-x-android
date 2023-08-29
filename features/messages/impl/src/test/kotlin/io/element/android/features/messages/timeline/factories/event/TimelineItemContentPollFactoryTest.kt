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

package io.element.android.features.messages.timeline.factories.event

import com.google.common.truth.Truth
import io.element.android.features.messages.impl.timeline.factories.event.TimelineItemContentPollFactory
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemPollContent
import io.element.android.features.poll.api.PollAnswerItem
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.featureflag.test.FakeFeatureFlagService
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.poll.PollAnswer
import io.element.android.libraries.matrix.api.poll.PollKind
import io.element.android.libraries.matrix.api.timeline.item.event.PollContent
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
import kotlinx.coroutines.test.runTest
import org.junit.Test

internal class TimelineItemContentPollFactoryTest {

    private val factory = TimelineItemContentPollFactory(
        matrixClient = FakeMatrixClient(),
        featureFlagService = FakeFeatureFlagService(mapOf(FeatureFlags.Polls.key to true)),
    )

    @Test
    fun `Disclosed poll - not ended, no votes`() = runTest {
        Truth.assertThat(factory.create(aPollContent())).isEqualTo(aTimelineItemPollContent())
    }

    @Test
    fun `Disclosed poll - not ended, some votes, including one from current user`() = runTest {
        val votes = MY_USER_WINNING_VOTES.mapKeys { it.key.id }
        Truth.assertThat(
            factory.create(aPollContent(votes = votes))
        )
            .isEqualTo(
                aTimelineItemPollContent(
                    answerItems = listOf(
                        aPollAnswerItem(answer = A_POLL_ANSWER_1, votesCount = 3, percentage = 0.3f),
                        aPollAnswerItem(answer = A_POLL_ANSWER_2, isSelected = true, votesCount = 6, percentage = 0.6f),
                        aPollAnswerItem(answer = A_POLL_ANSWER_3),
                        aPollAnswerItem(answer = A_POLL_ANSWER_4, votesCount = 1, percentage = 0.1f),
                    ),
                )
            )
    }

    @Test
    fun `Disclosed poll - ended, no votes, no winner`() = runTest {
        Truth.assertThat(
            factory.create(aPollContent(endTime = 1UL))
        ).isEqualTo(
            aTimelineItemPollContent().let {
                it.copy(
                    answerItems = it.answerItems.map { answerItem -> answerItem.copy(isEnabled = false) },
                    isEnded = true,
                )
            }
        )
    }

    @Test
    fun `Disclosed poll - ended, some votes, including one from current user (winner)`() = runTest {
        val votes = MY_USER_WINNING_VOTES.mapKeys { it.key.id }
        Truth.assertThat(
            factory.create(aPollContent(votes = votes, endTime = 1UL))
        )
            .isEqualTo(
                aTimelineItemPollContent(
                    answerItems = listOf(
                        aPollAnswerItem(answer = A_POLL_ANSWER_1, isEnabled = false, votesCount = 3, percentage = 0.3f),
                        aPollAnswerItem(answer = A_POLL_ANSWER_2, isSelected = true, isEnabled = false, isWinner = true, votesCount = 6, percentage = 0.6f),
                        aPollAnswerItem(answer = A_POLL_ANSWER_3, isEnabled = false),
                        aPollAnswerItem(answer = A_POLL_ANSWER_4, isEnabled = false, votesCount = 1, percentage = 0.1f),
                    ),
                    isEnded = true,
                )
            )
    }

    @Test
    fun `Disclosed poll - ended, some votes, including one from current user (not winner) and two winning votes`() = runTest {
        val votes = OTHER_WINNING_VOTES.mapKeys { it.key.id }
        Truth.assertThat(
            factory.create(aPollContent(votes = votes, endTime = 1UL))
        )
            .isEqualTo(
                aTimelineItemPollContent(
                    answerItems = listOf(
                        aPollAnswerItem(answer = A_POLL_ANSWER_1, isEnabled = false, isWinner = true, votesCount = 4, percentage = 0.4f),
                        aPollAnswerItem(answer = A_POLL_ANSWER_2, isSelected = true, isEnabled = false, votesCount = 2, percentage = 0.2f),
                        aPollAnswerItem(answer = A_POLL_ANSWER_3, isEnabled = false),
                        aPollAnswerItem(answer = A_POLL_ANSWER_4, isEnabled = false, isWinner = true, votesCount = 4, percentage = 0.4f),
                    ),
                    isEnded = true,
                )
            )
    }

    @Test
    fun `Undisclosed poll - not ended, no votes`() = runTest {
        Truth.assertThat(
            factory.create(aPollContent(PollKind.Undisclosed).copy())
        ).isEqualTo(
            aTimelineItemPollContent(PollKind.Undisclosed).let {
                it.copy(answerItems = it.answerItems.map { answerItem -> answerItem.copy(isDisclosed = false) })
            }
        )
    }

    @Test
    fun `Undisclosed poll - not ended, some votes, including one from current user`() = runTest {
        val votes = MY_USER_WINNING_VOTES.mapKeys { it.key.id }
        Truth.assertThat(
            factory.create(aPollContent(pollKind = PollKind.Undisclosed, votes = votes))
        )
            .isEqualTo(
                aTimelineItemPollContent(
                    pollKind = PollKind.Undisclosed,
                    answerItems = listOf(
                        aPollAnswerItem(answer = A_POLL_ANSWER_1, isDisclosed = false, votesCount = 3, percentage = 0.3f),
                        aPollAnswerItem(answer = A_POLL_ANSWER_2, isDisclosed = false, isSelected = true, votesCount = 6, percentage = 0.6f),
                        aPollAnswerItem(answer = A_POLL_ANSWER_3, isDisclosed = false),
                        aPollAnswerItem(answer = A_POLL_ANSWER_4, isDisclosed = false, votesCount = 1, percentage = 0.1f),
                    ),
                )
            )
    }

    @Test
    fun `Undisclosed poll - ended, no votes, no winner`() = runTest {
        Truth.assertThat(
            factory.create(aPollContent(pollKind = PollKind.Undisclosed, endTime = 1UL))
        ).isEqualTo(
            aTimelineItemPollContent().let {
                it.copy(
                    pollKind = PollKind.Undisclosed,
                    answerItems = it.answerItems.map { answerItem ->
                        answerItem.copy(isDisclosed = true, isEnabled = false, isWinner = false)
                    },
                    isEnded = true,
                )
            }
        )
    }

    @Test
    fun `Undisclosed poll - ended, some votes, including one from current user (winner)`() = runTest {
        val votes = MY_USER_WINNING_VOTES.mapKeys { it.key.id }
        Truth.assertThat(
            factory.create(aPollContent(pollKind = PollKind.Undisclosed, votes = votes, endTime = 1UL))
        )
            .isEqualTo(
                aTimelineItemPollContent(
                    pollKind = PollKind.Undisclosed,
                    answerItems = listOf(
                        aPollAnswerItem(answer = A_POLL_ANSWER_1, isEnabled = false, votesCount = 3, percentage = 0.3f),
                        aPollAnswerItem(answer = A_POLL_ANSWER_2, isSelected = true, isEnabled = false, isWinner = true, votesCount = 6, percentage = 0.6f),
                        aPollAnswerItem(answer = A_POLL_ANSWER_3, isEnabled = false),
                        aPollAnswerItem(answer = A_POLL_ANSWER_4, isEnabled = false, votesCount = 1, percentage = 0.1f),
                    ),
                    isEnded = true,
                )
            )
    }

    @Test
    fun `Undisclosed poll - ended, some votes, including one from current user (not winner) and two winning votes`() = runTest {
        val votes = OTHER_WINNING_VOTES.mapKeys { it.key.id }
        Truth.assertThat(
            factory.create(aPollContent(PollKind.Undisclosed).copy(votes = votes, endTime = 1UL))
        )
            .isEqualTo(
                aTimelineItemPollContent(
                    pollKind = PollKind.Undisclosed,
                    answerItems = listOf(
                        aPollAnswerItem(A_POLL_ANSWER_1, isEnabled = false, isWinner = true, votesCount = 4, percentage = 0.4f),
                        aPollAnswerItem(A_POLL_ANSWER_2, isSelected = true, isEnabled = false, votesCount = 2, percentage = 0.2f),
                        aPollAnswerItem(A_POLL_ANSWER_3, isEnabled = false),
                        aPollAnswerItem(A_POLL_ANSWER_4, isEnabled = false, isWinner = true, votesCount = 4, percentage = 0.4f),
                    ),
                    isEnded = true,
                )
            )
    }

    private fun aPollContent(
        pollKind: PollKind = PollKind.Disclosed,
        votes: Map<String, List<UserId>> = emptyMap(),
        endTime: ULong? = null,
    ): PollContent = PollContent(
        question = A_POLL_QUESTION,
        kind = pollKind,
        maxSelections = 1UL,
        answers = listOf(A_POLL_ANSWER_1, A_POLL_ANSWER_2, A_POLL_ANSWER_3, A_POLL_ANSWER_4),
        votes = votes,
        endTime = endTime,
    )

    private fun aTimelineItemPollContent(
        pollKind: PollKind = PollKind.Disclosed,
        answerItems: List<PollAnswerItem> = listOf(
            aPollAnswerItem(A_POLL_ANSWER_1),
            aPollAnswerItem(A_POLL_ANSWER_2),
            aPollAnswerItem(A_POLL_ANSWER_3),
            aPollAnswerItem(A_POLL_ANSWER_4),
        ),
        isEnded: Boolean = false,
    ) = TimelineItemPollContent(
        question = A_POLL_QUESTION,
        answerItems = answerItems,
        pollKind = pollKind,
        isEnded = isEnded,
    )

    private fun aPollAnswerItem(
        answer: PollAnswer,
        isSelected: Boolean = false,
        isEnabled: Boolean = true,
        isWinner: Boolean = false,
        isDisclosed: Boolean = true,
        votesCount: Int = 0,
        percentage: Float = 0f,
    ) = PollAnswerItem(
        answer = answer,
        isSelected = isSelected,
        isEnabled = isEnabled,
        isWinner = isWinner,
        isDisclosed = isDisclosed,
        votesCount = votesCount,
        percentage = percentage,
    )

    private companion object TestData {
        private const val A_POLL_QUESTION = "What is your favorite food?"
        private val A_POLL_ANSWER_1 = PollAnswer("id_1", "Pizza")
        private val A_POLL_ANSWER_2 = PollAnswer("id_2", "Pasta")
        private val A_POLL_ANSWER_3 = PollAnswer("id_3", "French Fries")
        private val A_POLL_ANSWER_4 = PollAnswer("id_4", "Hamburger")

        private val MY_USER_WINNING_VOTES = mapOf(
            A_POLL_ANSWER_1 to listOf(A_USER_ID_2, A_USER_ID_3, A_USER_ID_4),
            A_POLL_ANSWER_2 to listOf(A_USER_ID /* my vote */, A_USER_ID_5, A_USER_ID_6, A_USER_ID_7, A_USER_ID_8, A_USER_ID_9), // winner
            A_POLL_ANSWER_3 to emptyList(),
            A_POLL_ANSWER_4 to listOf(A_USER_ID_10),
        )
        private val OTHER_WINNING_VOTES = mapOf(
            A_POLL_ANSWER_1 to listOf(A_USER_ID_2, A_USER_ID_3, A_USER_ID_4, A_USER_ID_5), // winner
            A_POLL_ANSWER_2 to listOf(A_USER_ID /* my vote */, A_USER_ID_6),
            A_POLL_ANSWER_3 to emptyList(),
            A_POLL_ANSWER_4 to listOf(A_USER_ID_7, A_USER_ID_8, A_USER_ID_9, A_USER_ID_10), // winner
        )
    }
}

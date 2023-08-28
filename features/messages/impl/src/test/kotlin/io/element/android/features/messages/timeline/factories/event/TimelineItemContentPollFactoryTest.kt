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

private const val A_POLL_QUESTION = "What is your favorite food?"
private val A_POLL_ANSWER_1 = PollAnswer("id_1", "Pizza")
private val A_POLL_ANSWER_2 = PollAnswer("id_2", "Pasta")
private val A_POLL_ANSWER_3 = PollAnswer("id_3", "French Fries")
private val A_POLL_ANSWER_4 = PollAnswer("id_4", "Hamburger")

internal class TimelineItemContentPollFactoryTest {

    private val factory = TimelineItemContentPollFactory(
        matrixClient = FakeMatrixClient(),
        featureFlagService = FakeFeatureFlagService(mapOf(FeatureFlags.Polls.key to true)),
    )

    @Test
    fun `Disclosed poll - not ended states`() = runTest {
        // No votes
        Truth.assertThat(
            factory.create(aPollContent(PollKind.Disclosed))
        ).isEqualTo(aTimelineItemPollContent(PollKind.Disclosed))

        // Some votes, according one from current user
        val votes = mapOf(
            A_POLL_ANSWER_1.id to listOf(A_USER_ID_2, A_USER_ID_3, A_USER_ID_4),
            A_POLL_ANSWER_2.id to listOf(A_USER_ID, A_USER_ID_5, A_USER_ID_6, A_USER_ID_7, A_USER_ID_8, A_USER_ID_9),
            A_POLL_ANSWER_3.id to emptyList(),
            A_POLL_ANSWER_4.id to listOf(A_USER_ID_10),
        )
        Truth.assertThat(
            factory.create(aPollContent(PollKind.Disclosed).copy(votes = votes))
        )
            .isEqualTo(
                aTimelineItemPollContent(PollKind.Disclosed).copy(
                    answerItems = listOf(
                        aPollAnswerItem(A_POLL_ANSWER_1).copy(votesCount = 3, percentage = 0.3f),
                        aPollAnswerItem(A_POLL_ANSWER_2).copy(isSelected = true, votesCount = 6, percentage = 0.6f),
                        aPollAnswerItem(A_POLL_ANSWER_3).copy(votesCount = 0, percentage = 0f),
                        aPollAnswerItem(A_POLL_ANSWER_4).copy(votesCount = 1, percentage = 0.1f),
                    ),
                    votes = votes,
                )
            )
    }

    @Test
    fun `Disclosed poll - ended states`() = runTest {
        // No votes, no winner
        Truth.assertThat(
            factory.create(aPollContent(PollKind.Disclosed).copy(endTime = 1UL))
        ).isEqualTo(
            aTimelineItemPollContent(PollKind.Disclosed).let {
                it.copy(
                    answerItems = it.answerItems.map { answerItem ->
                        answerItem.copy(isEnabled = false, isWinner = false)
                    },
                    isEnded = true,
                )
            }
        )

        // Some votes, according one from current user (winner)
        var votes = mapOf(
            A_POLL_ANSWER_1.id to listOf(A_USER_ID_2, A_USER_ID_3, A_USER_ID_4),
            A_POLL_ANSWER_2.id to listOf(A_USER_ID, A_USER_ID_5, A_USER_ID_6, A_USER_ID_7, A_USER_ID_8, A_USER_ID_9),
            A_POLL_ANSWER_3.id to emptyList(),
            A_POLL_ANSWER_4.id to listOf(A_USER_ID_10),
        )
        Truth.assertThat(
            factory.create(aPollContent(PollKind.Disclosed).copy(votes = votes, endTime = 1UL))
        )
            .isEqualTo(
                aTimelineItemPollContent(PollKind.Disclosed).copy(
                    answerItems = listOf(
                        aPollAnswerItem(A_POLL_ANSWER_1).copy(isEnabled = false, votesCount = 3, percentage = 0.3f),
                        aPollAnswerItem(A_POLL_ANSWER_2).copy(isSelected = true, isEnabled = false, isWinner = true, votesCount = 6, percentage = 0.6f),
                        aPollAnswerItem(A_POLL_ANSWER_3).copy(isEnabled = false, votesCount = 0, percentage = 0f),
                        aPollAnswerItem(A_POLL_ANSWER_4).copy(isEnabled = false, votesCount = 1, percentage = 0.1f),
                    ),
                    votes = votes,
                    isEnded = true,
                )
            )

        // Some votes, according one from current user (not winner) and two winning votes
        votes = mapOf(
            A_POLL_ANSWER_1.id to listOf(A_USER_ID_2, A_USER_ID_3, A_USER_ID_4, A_USER_ID_5),
            A_POLL_ANSWER_2.id to listOf(A_USER_ID, A_USER_ID_6),
            A_POLL_ANSWER_3.id to emptyList(),
            A_POLL_ANSWER_4.id to listOf(A_USER_ID_7, A_USER_ID_8, A_USER_ID_9, A_USER_ID_10),
        )
        Truth.assertThat(
            factory.create(aPollContent(PollKind.Disclosed).copy(votes = votes, endTime = 1UL))
        )
            .isEqualTo(
                aTimelineItemPollContent(PollKind.Disclosed).copy(
                    answerItems = listOf(
                        aPollAnswerItem(A_POLL_ANSWER_1).copy(isEnabled = false, isWinner = true, votesCount = 4, percentage = 0.4f),
                        aPollAnswerItem(A_POLL_ANSWER_2).copy(isSelected = true, isEnabled = false, votesCount = 2, percentage = 0.2f),
                        aPollAnswerItem(A_POLL_ANSWER_3).copy(isEnabled = false, votesCount = 0, percentage = 0f),
                        aPollAnswerItem(A_POLL_ANSWER_4).copy(isEnabled = false, isWinner = true, votesCount = 4, percentage = 0.4f),
                    ),
                    votes = votes,
                    isEnded = true,
                )
            )
    }

    @Test
    fun `Undisclosed poll - not ended states`() = runTest {
        // No votes
        Truth.assertThat(
            factory.create(aPollContent(PollKind.Undisclosed).copy())
        ).isEqualTo(
            aTimelineItemPollContent(PollKind.Undisclosed).let {
                it.copy(answerItems = it.answerItems.map { answerItem -> answerItem.copy(isDisclosed = false) })
            }
        )

        // Some votes, according one from current user
        val votes = mapOf(
            A_POLL_ANSWER_1.id to listOf(A_USER_ID_2, A_USER_ID_3, A_USER_ID_4),
            A_POLL_ANSWER_2.id to listOf(A_USER_ID, A_USER_ID_5, A_USER_ID_6, A_USER_ID_7, A_USER_ID_8, A_USER_ID_9),
            A_POLL_ANSWER_3.id to emptyList(),
            A_POLL_ANSWER_4.id to listOf(A_USER_ID_10),
        )
        Truth.assertThat(
            factory.create(aPollContent(PollKind.Undisclosed).copy(votes = votes))
        )
            .isEqualTo(
                aTimelineItemPollContent(PollKind.Undisclosed).copy(
                    answerItems = listOf(
                        aPollAnswerItem(A_POLL_ANSWER_1).copy(isDisclosed = false, votesCount = 3, percentage = 0.3f),
                        aPollAnswerItem(A_POLL_ANSWER_2).copy(isDisclosed = false, isSelected = true, votesCount = 6, percentage = 0.6f),
                        aPollAnswerItem(A_POLL_ANSWER_3).copy(isDisclosed = false, votesCount = 0, percentage = 0f),
                        aPollAnswerItem(A_POLL_ANSWER_4).copy(isDisclosed = false, votesCount = 1, percentage = 0.1f),
                    ),
                    votes = votes,
                )
            )
    }

    @Test
    fun `Undisclosed poll - ended states`() = runTest {
        // No votes, no winner
        Truth.assertThat(
            factory.create(aPollContent(PollKind.Undisclosed).copy(endTime = 1UL))
        ).isEqualTo(
            aTimelineItemPollContent(PollKind.Undisclosed).let {
                it.copy(
                    answerItems = it.answerItems.map { answerItem ->
                        answerItem.copy(isDisclosed = true, isEnabled = false, isWinner = false)
                    },
                    isEnded = true,
                )
            }
        )

        // Some votes, according one from current user (winner)
        var votes = mapOf(
            A_POLL_ANSWER_1.id to listOf(A_USER_ID_2, A_USER_ID_3, A_USER_ID_4),
            A_POLL_ANSWER_2.id to listOf(A_USER_ID, A_USER_ID_5, A_USER_ID_6, A_USER_ID_7, A_USER_ID_8, A_USER_ID_9),
            A_POLL_ANSWER_3.id to emptyList(),
            A_POLL_ANSWER_4.id to listOf(A_USER_ID_10),
        )
        Truth.assertThat(
            factory.create(aPollContent(PollKind.Undisclosed).copy(votes = votes, endTime = 1UL))
        )
            .isEqualTo(
                aTimelineItemPollContent(PollKind.Undisclosed).copy(
                    answerItems = listOf(
                        aPollAnswerItem(A_POLL_ANSWER_1).copy(isDisclosed = true, isEnabled = false, votesCount = 3, percentage = 0.3f),
                        aPollAnswerItem(A_POLL_ANSWER_2).copy(
                            isDisclosed = true,
                            isSelected = true,
                            isEnabled = false,
                            isWinner = true,
                            votesCount = 6,
                            percentage = 0.6f
                        ),
                        aPollAnswerItem(A_POLL_ANSWER_3).copy(isDisclosed = true, isEnabled = false, votesCount = 0, percentage = 0f),
                        aPollAnswerItem(A_POLL_ANSWER_4).copy(isDisclosed = true, isEnabled = false, votesCount = 1, percentage = 0.1f),
                    ),
                    votes = votes,
                    isEnded = true,
                )
            )

        // Some votes, according one from current user (not winner) and two winning votes
        votes = mapOf(
            A_POLL_ANSWER_1.id to listOf(A_USER_ID_2, A_USER_ID_3, A_USER_ID_4, A_USER_ID_5),
            A_POLL_ANSWER_2.id to listOf(A_USER_ID, A_USER_ID_6),
            A_POLL_ANSWER_3.id to emptyList(),
            A_POLL_ANSWER_4.id to listOf(A_USER_ID_7, A_USER_ID_8, A_USER_ID_9, A_USER_ID_10),
        )
        Truth.assertThat(
            factory.create(aPollContent(PollKind.Undisclosed).copy(votes = votes, endTime = 1UL))
        )
            .isEqualTo(
                aTimelineItemPollContent(PollKind.Undisclosed).copy(
                    answerItems = listOf(
                        aPollAnswerItem(A_POLL_ANSWER_1).copy(isDisclosed = true, isEnabled = false, isWinner = true, votesCount = 4, percentage = 0.4f),
                        aPollAnswerItem(A_POLL_ANSWER_2).copy(isDisclosed = true, isSelected = true, isEnabled = false, votesCount = 2, percentage = 0.2f),
                        aPollAnswerItem(A_POLL_ANSWER_3).copy(isDisclosed = true, isEnabled = false, votesCount = 0, percentage = 0f),
                        aPollAnswerItem(A_POLL_ANSWER_4).copy(isDisclosed = true, isEnabled = false, isWinner = true, votesCount = 4, percentage = 0.4f),
                    ),
                    votes = votes,
                    isEnded = true,
                )
            )
    }

    private fun aPollContent(pollKind: PollKind): PollContent = PollContent(
        question = A_POLL_QUESTION,
        kind = pollKind,
        maxSelections = 1UL,
        answers = listOf(
            A_POLL_ANSWER_1,
            A_POLL_ANSWER_2,
            A_POLL_ANSWER_3,
            A_POLL_ANSWER_4,
        ),
        votes = emptyMap(),
        endTime = null,
    )

    private fun aTimelineItemPollContent(pollKind: PollKind) = TimelineItemPollContent(
        question = A_POLL_QUESTION,
        answerItems = listOf(
            aPollAnswerItem(A_POLL_ANSWER_1),
            aPollAnswerItem(A_POLL_ANSWER_2),
            aPollAnswerItem(A_POLL_ANSWER_3),
            aPollAnswerItem(A_POLL_ANSWER_4),
        ),
        votes = emptyMap(),
        pollKind = pollKind,
        isEnded = false,
    )

    private fun aPollAnswerItem(answer: PollAnswer) = PollAnswerItem(
        answer = answer,
        isSelected = false,
        isEnabled = true,
        isWinner = false,
        isDisclosed = true,
        votesCount = 0,
        percentage = 0f,
    )
}

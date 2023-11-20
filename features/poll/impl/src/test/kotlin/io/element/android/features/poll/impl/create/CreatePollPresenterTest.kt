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

package io.element.android.features.poll.impl.create

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth
import im.vector.app.features.analytics.plan.Composer
import im.vector.app.features.analytics.plan.PollCreation
import io.element.android.features.messages.test.FakeMessageComposerContext
import io.element.android.libraries.matrix.api.poll.PollKind
import io.element.android.libraries.matrix.test.room.CreatePollInvocation
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.services.analytics.test.FakeAnalyticsService
import io.element.android.tests.testutils.WarmUpRule
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class CreatePollPresenterTest {

    @get:Rule
    val warmUpRule = WarmUpRule()

    private var navUpInvocationsCount = 0
    private val fakeMatrixRoom = FakeMatrixRoom()
    private val fakeAnalyticsService = FakeAnalyticsService()
    private val fakeMessageComposerContext = FakeMessageComposerContext()

    private val presenter = CreatePollPresenter(
        room = fakeMatrixRoom,
        analyticsService = fakeAnalyticsService,
        messageComposerContext = fakeMessageComposerContext,
        navigateUp = { navUpInvocationsCount++ },
    )

    @Test
    fun `default state has proper default values`() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem().let {
                Truth.assertThat(it.canCreate).isEqualTo(false)
                Truth.assertThat(it.canAddAnswer).isEqualTo(true)
                Truth.assertThat(it.question).isEqualTo("")
                Truth.assertThat(it.answers).isEqualTo(listOf(Answer("", false), Answer("", false)))
                Truth.assertThat(it.pollKind).isEqualTo(PollKind.Disclosed)
                Truth.assertThat(it.showConfirmation).isEqualTo(false)
            }
        }
    }

    @Test
    fun `non blank question and 2 answers are required to create a poll`() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initial = awaitItem()
            Truth.assertThat(initial.canCreate).isEqualTo(false)

            initial.eventSink(CreatePollEvents.SetQuestion("A question?"))
            val questionSet = awaitItem()
            Truth.assertThat(questionSet.canCreate).isEqualTo(false)

            questionSet.eventSink(CreatePollEvents.SetAnswer(0, "Answer 1"))
            val answer1Set = awaitItem()
            Truth.assertThat(answer1Set.canCreate).isEqualTo(false)

            answer1Set.eventSink(CreatePollEvents.SetAnswer(1, "Answer 2"))
            val answer2Set = awaitItem()
            Truth.assertThat(answer2Set.canCreate).isEqualTo(true)
        }
    }

    @Test
    fun `create polls sends a poll start event`() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initial = awaitItem()
            initial.eventSink(CreatePollEvents.SetQuestion("A question?"))
            initial.eventSink(CreatePollEvents.SetAnswer(0, "Answer 1"))
            initial.eventSink(CreatePollEvents.SetAnswer(1, "Answer 2"))
            skipItems(3)
            initial.eventSink(CreatePollEvents.Create)
            delay(1) // Wait for the coroutine to finish
            Truth.assertThat(fakeMatrixRoom.createPollInvocations.size).isEqualTo(1)
            Truth.assertThat(fakeMatrixRoom.createPollInvocations.last()).isEqualTo(
                CreatePollInvocation(
                    question = "A question?",
                    answers = listOf("Answer 1", "Answer 2"),
                    maxSelections = 1,
                    pollKind = PollKind.Disclosed
                )
            )
            Truth.assertThat(fakeAnalyticsService.capturedEvents.size).isEqualTo(2)
            Truth.assertThat(fakeAnalyticsService.capturedEvents[0]).isEqualTo(
                Composer(
                    inThread = false,
                    isEditing = false,
                    isReply = false,
                    messageType = Composer.MessageType.Poll,
                )
            )
            Truth.assertThat(fakeAnalyticsService.capturedEvents[1]).isEqualTo(
                PollCreation(
                    action = PollCreation.Action.Create,
                    isUndisclosed = false,
                    numberOfAnswers = 2,
                )
            )
        }
    }

    @Test
    fun `add answer button adds an empty answer and removing it removes it`() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initial = awaitItem()
            Truth.assertThat(initial.answers.size).isEqualTo(2)

            initial.eventSink(CreatePollEvents.AddAnswer)
            val answerAdded = awaitItem()
            Truth.assertThat(answerAdded.answers.size).isEqualTo(3)
            Truth.assertThat(answerAdded.answers[2].text).isEqualTo("")

            initial.eventSink(CreatePollEvents.RemoveAnswer(2))
            val answerRemoved = awaitItem()
            Truth.assertThat(answerRemoved.answers.size).isEqualTo(2)
        }
    }

    @Test
    fun `set question sets the question`() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initial = awaitItem()
            initial.eventSink(CreatePollEvents.SetQuestion("A question?"))
            val questionSet = awaitItem()
            Truth.assertThat(questionSet.question).isEqualTo("A question?")
        }
    }

    @Test
    fun `set poll answer sets the given poll answer`() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initial = awaitItem()
            initial.eventSink(CreatePollEvents.SetAnswer(0, "This is answer 1"))
            val answerSet = awaitItem()
            Truth.assertThat(answerSet.answers.first().text).isEqualTo("This is answer 1")
        }
    }

    @Test
    fun `set poll kind sets the poll kind`() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initial = awaitItem()
            initial.eventSink(CreatePollEvents.SetPollKind(PollKind.Undisclosed))
            val kindSet = awaitItem()
            Truth.assertThat(kindSet.pollKind).isEqualTo(PollKind.Undisclosed)
        }
    }

    @Test
    fun `can add options when between 2 and 20 and then no more`() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initial = awaitItem()
            Truth.assertThat(initial.canAddAnswer).isEqualTo(true)
            repeat(17) {
                initial.eventSink(CreatePollEvents.AddAnswer)
                Truth.assertThat(awaitItem().canAddAnswer).isEqualTo(true)
            }
            initial.eventSink(CreatePollEvents.AddAnswer)
            Truth.assertThat(awaitItem().canAddAnswer).isEqualTo(false)
        }
    }

    @Test
    fun `can delete option if there are more than 2`() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initial = awaitItem()
            Truth.assertThat(initial.answers.all { it.canDelete }).isEqualTo(false)
            initial.eventSink(CreatePollEvents.AddAnswer)
            Truth.assertThat(awaitItem().answers.all { it.canDelete }).isEqualTo(true)
        }
    }

    @Test
    fun `option with more than 240 char is truncated`() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initial = awaitItem()
            initial.eventSink(CreatePollEvents.SetAnswer(0, "A".repeat(241)))
            Truth.assertThat(awaitItem().answers.first().text.length).isEqualTo(240)
        }
    }

    @Test
    fun `navBack event calls navBack lambda`() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initial = awaitItem()
            Truth.assertThat(navUpInvocationsCount).isEqualTo(0)
            initial.eventSink(CreatePollEvents.NavBack)
            Truth.assertThat(navUpInvocationsCount).isEqualTo(1)
        }
    }

    @Test
    fun `confirm nav back with blank fields calls nav back lambda`() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initial = awaitItem()
            Truth.assertThat(navUpInvocationsCount).isEqualTo(0)
            Truth.assertThat(initial.showConfirmation).isEqualTo(false)
            initial.eventSink(CreatePollEvents.ConfirmNavBack)
            Truth.assertThat(navUpInvocationsCount).isEqualTo(1)
        }
    }

    @Test
    fun `confirm nav back with non blank fields shows confirmation dialog and sending hide hids it`() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initial = awaitItem()
            initial.eventSink(CreatePollEvents.SetQuestion("Non blank"))
            Truth.assertThat(navUpInvocationsCount).isEqualTo(0)
            Truth.assertThat(awaitItem().showConfirmation).isEqualTo(false)
            initial.eventSink(CreatePollEvents.ConfirmNavBack)
            Truth.assertThat(navUpInvocationsCount).isEqualTo(0)
            Truth.assertThat(awaitItem().showConfirmation).isEqualTo(true)
            initial.eventSink(CreatePollEvents.HideConfirmation)
            Truth.assertThat(awaitItem().showConfirmation).isEqualTo(false)
        }
    }
}

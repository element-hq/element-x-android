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
import app.cash.turbine.TurbineTestContext
import app.cash.turbine.test
import com.google.common.truth.Truth
import im.vector.app.features.analytics.plan.Composer
import im.vector.app.features.analytics.plan.PollCreation
import io.element.android.features.messages.test.FakeMessageComposerContext
import io.element.android.features.poll.api.create.CreatePollMode
import io.element.android.libraries.matrix.api.poll.PollAnswer
import io.element.android.libraries.matrix.api.poll.PollKind
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.api.timeline.item.event.PollContent
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.room.CreatePollInvocation
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.libraries.matrix.test.room.aPollContent
import io.element.android.libraries.matrix.test.room.anEventTimelineItem
import io.element.android.libraries.matrix.test.timeline.FakeMatrixTimeline
import io.element.android.services.analytics.test.FakeAnalyticsService
import io.element.android.tests.testutils.WarmUpRule
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class CreatePollPresenterTest {

    @get:Rule
    val warmUpRule = WarmUpRule()

    private val pollEventId = AN_EVENT_ID
    private var navUpInvocationsCount = 0
    private val existingPoll = anExistingPoll()
    private val fakeMatrixRoom = createFakeMatrixRoom(existingPoll)
    private val fakeAnalyticsService = FakeAnalyticsService()
    private val fakeMessageComposerContext = FakeMessageComposerContext()

    @Test
    fun `default state has proper default values`() = runTest {
        val presenter = createCreatePollPresenter(mode = CreatePollMode.NewPoll)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitDefaultItem()
        }
    }

    @Test
    fun `in edit mode, poll values are loaded`() = runTest {
        val presenter = createCreatePollPresenter(mode = CreatePollMode.EditPoll(AN_EVENT_ID))
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitDefaultItem()
            awaitPollLoaded()
        }
    }

    @Test
    fun `in edit mode, if poll doesn't exist, error is tracked and screen is closed`() = runTest {
        val room = createFakeMatrixRoom(existingPoll = null)
        val presenter = createCreatePollPresenter(mode = CreatePollMode.EditPoll(AN_EVENT_ID), room = room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitDefaultItem()
            Truth.assertThat(fakeAnalyticsService.trackedErrors.filterIsInstance<CreatePollException.GetPollFailed>()).isNotEmpty()
            Truth.assertThat(navUpInvocationsCount).isEqualTo(1)
        }
    }

    @Test
    fun `non blank question and 2 answers are required to create a poll`() = runTest {
        val presenter = createCreatePollPresenter(mode = CreatePollMode.NewPoll)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initial = awaitItem()
            Truth.assertThat(initial.canSave).isEqualTo(false)

            initial.eventSink(CreatePollEvents.SetQuestion("A question?"))
            val questionSet = awaitItem()
            Truth.assertThat(questionSet.canSave).isEqualTo(false)

            questionSet.eventSink(CreatePollEvents.SetAnswer(0, "Answer 1"))
            val answer1Set = awaitItem()
            Truth.assertThat(answer1Set.canSave).isEqualTo(false)

            answer1Set.eventSink(CreatePollEvents.SetAnswer(1, "Answer 2"))
            val answer2Set = awaitItem()
            Truth.assertThat(answer2Set.canSave).isEqualTo(true)
        }
    }

    @Test
    fun `create polls sends a poll start event`() = runTest {
        val presenter = createCreatePollPresenter(mode = CreatePollMode.NewPoll)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initial = awaitItem()
            initial.eventSink(CreatePollEvents.SetQuestion("A question?"))
            initial.eventSink(CreatePollEvents.SetAnswer(0, "Answer 1"))
            initial.eventSink(CreatePollEvents.SetAnswer(1, "Answer 2"))
            skipItems(3)
            initial.eventSink(CreatePollEvents.Save)
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
        val presenter = createCreatePollPresenter(mode = CreatePollMode.NewPoll)
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
        val presenter = createCreatePollPresenter(mode = CreatePollMode.NewPoll)
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
        val presenter = createCreatePollPresenter(mode = CreatePollMode.NewPoll)
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
        val presenter = createCreatePollPresenter(mode = CreatePollMode.NewPoll)
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
        val presenter = createCreatePollPresenter(mode = CreatePollMode.NewPoll)
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
        val presenter = createCreatePollPresenter(mode = CreatePollMode.NewPoll)
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
        val presenter = createCreatePollPresenter(mode = CreatePollMode.NewPoll)
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
        val presenter = createCreatePollPresenter(mode = CreatePollMode.NewPoll)
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
        val presenter = createCreatePollPresenter(mode = CreatePollMode.NewPoll)
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
    fun `confirm nav back with non blank fields shows confirmation dialog and sending hides it`() = runTest {
        val presenter = createCreatePollPresenter(mode = CreatePollMode.NewPoll)
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

    private suspend fun TurbineTestContext<CreatePollState>.awaitDefaultItem() =
        awaitItem().let {
            Truth.assertThat(it.canSave).isEqualTo(false)
            Truth.assertThat(it.canAddAnswer).isEqualTo(true)
            Truth.assertThat(it.question).isEqualTo("")
            Truth.assertThat(it.answers).isEqualTo(listOf(Answer("", false), Answer("", false)))
            Truth.assertThat(it.pollKind).isEqualTo(PollKind.Disclosed)
            Truth.assertThat(it.showConfirmation).isEqualTo(false)
        }

    private suspend fun TurbineTestContext<CreatePollState>.awaitPollLoaded() =
        awaitItem().let {
            Truth.assertThat(it.canSave).isEqualTo(true)
            Truth.assertThat(it.canAddAnswer).isEqualTo(true)
            Truth.assertThat(it.question).isEqualTo(existingPoll.question)
            Truth.assertThat(it.answers).isEqualTo(existingPoll.expectedAnswersState())
            Truth.assertThat(it.pollKind).isEqualTo(existingPoll.kind)
        }

    private fun createCreatePollPresenter(
        mode: CreatePollMode = CreatePollMode.NewPoll,
        room: MatrixRoom = fakeMatrixRoom,
    ): CreatePollPresenter = CreatePollPresenter(
        room = room,
        analyticsService = fakeAnalyticsService,
        messageComposerContext = fakeMessageComposerContext,
        navigateUp = { navUpInvocationsCount++ },
        mode = mode,
    )

    private fun createFakeMatrixRoom(
        existingPoll: PollContent? = anExistingPoll(),
    ) = FakeMatrixRoom(
        matrixTimeline = FakeMatrixTimeline(
            initialTimelineItems = existingPoll?.let {
                listOf(
                    MatrixTimelineItem.Event(
                        0,
                        anEventTimelineItem(
                            eventId = pollEventId,
                            content = it,
                        )
                    )
                )
            }.orEmpty()
        )
    )
}

private fun anExistingPoll() = aPollContent(
    question = "Do you like polls?",
    answers = listOf(
        PollAnswer("1", "Yes"),
        PollAnswer("2", "No"),
        PollAnswer("2", "Maybe"),
    ),
)

private fun PollContent.expectedAnswersState() = answers.map { answer ->
    Answer(
        text = answer.text,
        canDelete = answers.size > 2,
    )
}

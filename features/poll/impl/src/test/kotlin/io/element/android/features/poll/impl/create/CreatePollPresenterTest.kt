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
import com.google.common.truth.Truth.assertThat
import im.vector.app.features.analytics.plan.Composer
import im.vector.app.features.analytics.plan.PollCreation
import io.element.android.features.messages.test.FakeMessageComposerContext
import io.element.android.features.poll.api.create.CreatePollMode
import io.element.android.features.poll.impl.aPollTimeline
import io.element.android.features.poll.impl.anOngoingPollContent
import io.element.android.features.poll.impl.data.PollRepository
import io.element.android.libraries.matrix.api.poll.PollKind
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.timeline.item.event.PollContent
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.libraries.matrix.test.room.SavePollInvocation
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
    private val existingPoll = anOngoingPollContent()
    private val fakeMatrixRoom = FakeMatrixRoom(
        matrixTimeline = aPollTimeline(
            mapOf(pollEventId to existingPoll)
        )
    )
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
        val room = FakeMatrixRoom(
            matrixTimeline = aPollTimeline()
        )
        val presenter = createCreatePollPresenter(mode = CreatePollMode.EditPoll(AN_EVENT_ID), room = room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitDefaultItem()
            assertThat(fakeAnalyticsService.trackedErrors.filterIsInstance<CreatePollException.GetPollFailed>()).isNotEmpty()
            assertThat(navUpInvocationsCount).isEqualTo(1)
        }
    }

    @Test
    fun `non blank question and 2 answers are required to create a poll`() = runTest {
        val presenter = createCreatePollPresenter(mode = CreatePollMode.NewPoll)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initial = awaitItem()
            assertThat(initial.canSave).isFalse()

            initial.eventSink(CreatePollEvents.SetQuestion("A question?"))
            val questionSet = awaitItem()
            assertThat(questionSet.canSave).isFalse()

            questionSet.eventSink(CreatePollEvents.SetAnswer(0, "Answer 1"))
            val answer1Set = awaitItem()
            assertThat(answer1Set.canSave).isFalse()

            answer1Set.eventSink(CreatePollEvents.SetAnswer(1, "Answer 2"))
            val answer2Set = awaitItem()
            assertThat(answer2Set.canSave).isTrue()
        }
    }

    @Test
    fun `create poll sends a poll start event`() = runTest {
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
            assertThat(fakeMatrixRoom.createPollInvocations.size).isEqualTo(1)
            assertThat(fakeMatrixRoom.createPollInvocations.last()).isEqualTo(
                SavePollInvocation(
                    question = "A question?",
                    answers = listOf("Answer 1", "Answer 2"),
                    maxSelections = 1,
                    pollKind = PollKind.Disclosed
                )
            )
            assertThat(fakeAnalyticsService.capturedEvents.size).isEqualTo(2)
            assertThat(fakeAnalyticsService.capturedEvents[0]).isEqualTo(
                Composer(
                    inThread = false,
                    isEditing = false,
                    isReply = false,
                    messageType = Composer.MessageType.Poll,
                )
            )
            assertThat(fakeAnalyticsService.capturedEvents[1]).isEqualTo(
                PollCreation(
                    action = PollCreation.Action.Create,
                    isUndisclosed = false,
                    numberOfAnswers = 2,
                )
            )
        }
    }

    @Test
    fun `when poll creation fails, error is tracked`() = runTest {
        val error = Exception("cause")
        fakeMatrixRoom.givenCreatePollResult(Result.failure(error))
        val presenter = createCreatePollPresenter(mode = CreatePollMode.NewPoll)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitDefaultItem().eventSink(CreatePollEvents.SetQuestion("A question?"))
            awaitItem().eventSink(CreatePollEvents.SetAnswer(0, "Answer 1"))
            awaitItem().eventSink(CreatePollEvents.SetAnswer(1, "Answer 2"))
            awaitItem().eventSink(CreatePollEvents.Save)
            delay(1) // Wait for the coroutine to finish
            assertThat(fakeMatrixRoom.createPollInvocations).hasSize(1)
            assertThat(fakeAnalyticsService.capturedEvents).isEmpty()
            assertThat(fakeAnalyticsService.trackedErrors).hasSize(1)
            assertThat(fakeAnalyticsService.trackedErrors).containsExactly(
                CreatePollException.SavePollFailed("Failed to create poll", error)
            )
        }
    }

    @Test
    fun `edit poll sends a poll edit event`() = runTest {
        val presenter = createCreatePollPresenter(mode = CreatePollMode.EditPoll(pollEventId))
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitDefaultItem()
            awaitPollLoaded().apply {
                eventSink(CreatePollEvents.SetQuestion("Changed question"))
            }
            awaitItem().apply {
                eventSink(CreatePollEvents.SetAnswer(0, "Changed answer 1"))
            }
            awaitItem().apply {
                eventSink(CreatePollEvents.SetAnswer(1, "Changed answer 2"))
            }
            awaitPollLoaded(
                newQuestion = "Changed question",
                newAnswer1 = "Changed answer 1",
                newAnswer2 = "Changed answer 2",
            ).apply {
                eventSink(CreatePollEvents.Save)
            }
            delay(1) // Wait for the coroutine to finish
            assertThat(fakeMatrixRoom.editPollInvocations.size).isEqualTo(1)
            assertThat(fakeMatrixRoom.editPollInvocations.last()).isEqualTo(
                SavePollInvocation(
                    question = "Changed question",
                    answers = listOf("Changed answer 1", "Changed answer 2", "Maybe"),
                    maxSelections = 1,
                    pollKind = PollKind.Disclosed
                )
            )
            assertThat(fakeAnalyticsService.capturedEvents.size).isEqualTo(2)
            assertThat(fakeAnalyticsService.capturedEvents[0]).isEqualTo(
                Composer(
                    inThread = false,
                    isEditing = true,
                    isReply = false,
                    messageType = Composer.MessageType.Poll,
                )
            )
            assertThat(fakeAnalyticsService.capturedEvents[1]).isEqualTo(
                PollCreation(
                    action = PollCreation.Action.Edit,
                    isUndisclosed = false,
                    numberOfAnswers = 3,
                )
            )
        }
    }

    @Test
    fun `when edit poll fails, error is tracked`() = runTest {
        val error = Exception("cause")
        fakeMatrixRoom.givenEditPollResult(Result.failure(error))
        val presenter = createCreatePollPresenter(mode = CreatePollMode.EditPoll(pollEventId))
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitDefaultItem()
            awaitPollLoaded().eventSink(CreatePollEvents.SetAnswer(0, "A"))
            awaitPollLoaded(newAnswer1 = "A").eventSink(CreatePollEvents.Save)
            delay(1) // Wait for the coroutine to finish
            assertThat(fakeMatrixRoom.editPollInvocations).hasSize(1)
            assertThat(fakeAnalyticsService.capturedEvents).isEmpty()
            assertThat(fakeAnalyticsService.trackedErrors).hasSize(1)
            assertThat(fakeAnalyticsService.trackedErrors).containsExactly(
                CreatePollException.SavePollFailed("Failed to edit poll", error)
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
            assertThat(initial.answers.size).isEqualTo(2)

            initial.eventSink(CreatePollEvents.AddAnswer)
            val answerAdded = awaitItem()
            assertThat(answerAdded.answers.size).isEqualTo(3)
            assertThat(answerAdded.answers[2].text).isEmpty()

            initial.eventSink(CreatePollEvents.RemoveAnswer(2))
            val answerRemoved = awaitItem()
            assertThat(answerRemoved.answers.size).isEqualTo(2)
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
            assertThat(questionSet.question).isEqualTo("A question?")
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
            assertThat(answerSet.answers.first().text).isEqualTo("This is answer 1")
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
            assertThat(kindSet.pollKind).isEqualTo(PollKind.Undisclosed)
        }
    }

    @Test
    fun `can add options when between 2 and 20 and then no more`() = runTest {
        val presenter = createCreatePollPresenter(mode = CreatePollMode.NewPoll)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initial = awaitItem()
            assertThat(initial.canAddAnswer).isTrue()
            repeat(17) {
                initial.eventSink(CreatePollEvents.AddAnswer)
                assertThat(awaitItem().canAddAnswer).isTrue()
            }
            initial.eventSink(CreatePollEvents.AddAnswer)
            assertThat(awaitItem().canAddAnswer).isFalse()
        }
    }

    @Test
    fun `can delete option if there are more than 2`() = runTest {
        val presenter = createCreatePollPresenter(mode = CreatePollMode.NewPoll)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initial = awaitItem()
            assertThat(initial.answers.all { it.canDelete }).isFalse()
            initial.eventSink(CreatePollEvents.AddAnswer)
            assertThat(awaitItem().answers.all { it.canDelete }).isTrue()
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
            assertThat(awaitItem().answers.first().text.length).isEqualTo(240)
        }
    }

    @Test
    fun `navBack event calls navBack lambda`() = runTest {
        val presenter = createCreatePollPresenter(mode = CreatePollMode.NewPoll)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initial = awaitItem()
            assertThat(navUpInvocationsCount).isEqualTo(0)
            initial.eventSink(CreatePollEvents.NavBack)
            assertThat(navUpInvocationsCount).isEqualTo(1)
        }
    }

    @Test
    fun `confirm nav back from new poll with blank fields calls nav back lambda`() = runTest {
        val presenter = createCreatePollPresenter(mode = CreatePollMode.NewPoll)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initial = awaitItem()
            assertThat(navUpInvocationsCount).isEqualTo(0)
            assertThat(initial.showBackConfirmation).isFalse()
            initial.eventSink(CreatePollEvents.ConfirmNavBack)
            assertThat(navUpInvocationsCount).isEqualTo(1)
        }
    }

    @Test
    fun `confirm nav back from new poll with non blank fields shows confirmation dialog and cancelling hides it`() = runTest {
        val presenter = createCreatePollPresenter(mode = CreatePollMode.NewPoll)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initial = awaitItem()
            initial.eventSink(CreatePollEvents.SetQuestion("Non blank"))
            assertThat(awaitItem().showBackConfirmation).isFalse()
            initial.eventSink(CreatePollEvents.ConfirmNavBack)
            assertThat(awaitItem().showBackConfirmation).isTrue()
            initial.eventSink(CreatePollEvents.HideConfirmation)
            assertThat(awaitItem().showBackConfirmation).isFalse()
            assertThat(navUpInvocationsCount).isEqualTo(0)
        }
    }

    @Test
    fun `confirm nav back from existing poll with unchanged fields calls nav back lambda`() = runTest {
        val presenter = createCreatePollPresenter(mode = CreatePollMode.EditPoll(pollEventId))
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitDefaultItem()
            val loaded = awaitPollLoaded()
            assertThat(navUpInvocationsCount).isEqualTo(0)
            assertThat(loaded.showBackConfirmation).isFalse()
            loaded.eventSink(CreatePollEvents.ConfirmNavBack)
            assertThat(navUpInvocationsCount).isEqualTo(1)
        }
    }

    @Test
    fun `confirm nav back from existing poll with changed fields shows confirmation dialog and cancelling hides it`() = runTest {
        val presenter = createCreatePollPresenter(mode = CreatePollMode.EditPoll(pollEventId))
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitDefaultItem()
            val loaded = awaitPollLoaded()
            loaded.eventSink(CreatePollEvents.SetQuestion("CHANGED"))
            assertThat(awaitItem().showBackConfirmation).isFalse()
            loaded.eventSink(CreatePollEvents.ConfirmNavBack)
            assertThat(awaitItem().showBackConfirmation).isTrue()
            loaded.eventSink(CreatePollEvents.HideConfirmation)
            assertThat(awaitItem().showBackConfirmation).isFalse()
            assertThat(navUpInvocationsCount).isEqualTo(0)
        }
    }

    @Test
    fun `delete confirms`() = runTest {
        val presenter = createCreatePollPresenter(mode = CreatePollMode.EditPoll(pollEventId))
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitDefaultItem()
            awaitPollLoaded().eventSink(CreatePollEvents.Delete(confirmed = false))
            awaitDeleteConfirmation()
            assertThat(fakeMatrixRoom.redactEventEventIdParam).isNull()
        }
    }

    @Test
    fun `delete can be cancelled`() = runTest {
        val presenter = createCreatePollPresenter(mode = CreatePollMode.EditPoll(pollEventId))
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitDefaultItem()
            awaitPollLoaded().eventSink(CreatePollEvents.Delete(confirmed = false))
            assertThat(fakeMatrixRoom.redactEventEventIdParam).isNull()
            awaitDeleteConfirmation().eventSink(CreatePollEvents.HideConfirmation)
            awaitPollLoaded().apply {
                assertThat(showDeleteConfirmation).isFalse()
            }
            assertThat(fakeMatrixRoom.redactEventEventIdParam).isNull()
        }
    }

    @Test
    fun `delete can be confirmed`() = runTest {
        val presenter = createCreatePollPresenter(mode = CreatePollMode.EditPoll(pollEventId))
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitDefaultItem()
            awaitPollLoaded().eventSink(CreatePollEvents.Delete(confirmed = false))
            assertThat(fakeMatrixRoom.redactEventEventIdParam).isNull()
            awaitDeleteConfirmation().eventSink(CreatePollEvents.Delete(confirmed = true))
            awaitPollLoaded().apply {
                assertThat(showDeleteConfirmation).isFalse()
            }
            assertThat(fakeMatrixRoom.redactEventEventIdParam).isEqualTo(pollEventId)
        }
    }

    private suspend fun TurbineTestContext<CreatePollState>.awaitDefaultItem() =
        awaitItem().apply {
            assertThat(canSave).isFalse()
            assertThat(canAddAnswer).isTrue()
            assertThat(question).isEmpty()
            assertThat(answers).isEqualTo(listOf(Answer("", false), Answer("", false)))
            assertThat(pollKind).isEqualTo(PollKind.Disclosed)
            assertThat(showBackConfirmation).isFalse()
            assertThat(showDeleteConfirmation).isFalse()
        }

    private suspend fun TurbineTestContext<CreatePollState>.awaitDeleteConfirmation() =
        awaitItem().apply {
            assertThat(showDeleteConfirmation).isTrue()
        }

    private suspend fun TurbineTestContext<CreatePollState>.awaitPollLoaded(
        newQuestion: String? = null,
        newAnswer1: String? = null,
        newAnswer2: String? = null,
    ) =
        awaitItem().apply {
            assertThat(canSave).isTrue()
            assertThat(canAddAnswer).isTrue()
            assertThat(question).isEqualTo(newQuestion ?: existingPoll.question)
            assertThat(answers).isEqualTo(existingPoll.expectedAnswersState().toMutableList().apply {
                newAnswer1?.let { this[0] = Answer(it, true) }
                newAnswer2?.let { this[1] = Answer(it, true) }
            })
            assertThat(pollKind).isEqualTo(existingPoll.kind)
        }

    private fun createCreatePollPresenter(
        mode: CreatePollMode = CreatePollMode.NewPoll,
        room: MatrixRoom = fakeMatrixRoom,
    ): CreatePollPresenter = CreatePollPresenter(
        repository = PollRepository(room),
        analyticsService = fakeAnalyticsService,
        messageComposerContext = fakeMessageComposerContext,
        navigateUp = { navUpInvocationsCount++ },
        mode = mode,
    )
}

private fun PollContent.expectedAnswersState() = answers.map { answer ->
    Answer(
        text = answer.text,
        canDelete = answers.size > 2,
    )
}

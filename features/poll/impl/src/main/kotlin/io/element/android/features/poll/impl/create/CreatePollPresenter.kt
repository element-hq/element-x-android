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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.poll.PollKind
import io.element.android.libraries.matrix.api.room.MatrixRoom
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import timber.log.Timber

private const val MIN_ANSWERS = 2
private const val MAX_ANSWERS = 20
private const val MAX_ANSWER_LENGTH = 240
private const val MAX_SELECTIONS = 1

class CreatePollPresenter @AssistedInject constructor(
    private val room: MatrixRoom,
    // private val analyticsService: AnalyticsService, // TODO Polls: add analytics
    @Assisted private val navigateUp: () -> Unit,
    // private val messageComposerContext: MessageComposerContext, // TODO Polls: add analytics
) : Presenter<CreatePollState> {

    @AssistedFactory
    interface Factory {
        fun create(backNavigator: () -> Unit): CreatePollPresenter
    }

    @Composable
    override fun present(): CreatePollState {

        var question: String by rememberSaveable { mutableStateOf("") }
        var answers: List<String> by rememberSaveable() { mutableStateOf(listOf("", "")) }
        var pollKind: PollKind by rememberSaveable(saver = pollKindSaver) { mutableStateOf(PollKind.Disclosed) }
        var showConfirmation: Boolean by rememberSaveable { mutableStateOf(false) }

        val canCreate: Boolean by remember { derivedStateOf { canCreate(question, answers) } }
        val canAddAnswer: Boolean by remember { derivedStateOf { canAddAnswer(answers) } }
        val immutableAnswers: ImmutableList<Answer> by remember { derivedStateOf { answers.toAnswers() } }

        val scope = rememberCoroutineScope()

        fun handleEvents(event: CreatePollEvents) {
            when (event) {
                is CreatePollEvents.Create -> scope.launch {
                    if (canCreate) {
                        room.createPoll(
                            question = question,
                            answers = answers,
                            maxSelections = MAX_SELECTIONS,
                            pollKind = pollKind,
                        )
                        // analyticsService.capture(PollCreate()) // TODO Polls: add analytics
                        navigateUp()
                    } else {
                        Timber.d("Cannot create poll")
                    }
                }
                is CreatePollEvents.AddAnswer -> {
                    answers = answers + ""
                }
                is CreatePollEvents.RemoveAnswer -> {
                    answers = answers.filterIndexed { index, _ -> index != event.index }
                }
                is CreatePollEvents.SetAnswer -> {
                    val text = if (event.text.length > MAX_ANSWER_LENGTH) {
                        event.text.substring(0, MAX_ANSWER_LENGTH)
                    } else {
                        event.text
                    }
                    answers = answers.toMutableList().apply {
                        this[event.index] = text
                    }
                }
                is CreatePollEvents.SetPollKind -> {
                    pollKind = event.pollKind
                }
                is CreatePollEvents.SetQuestion -> {
                    question = event.question
                }
                is CreatePollEvents.NavBack -> {
                    navigateUp()
                }
                CreatePollEvents.ConfirmNavBack -> {
                    val shouldConfirm = question.isNotBlank() || answers.any { it.isNotBlank() }
                    if (shouldConfirm) {
                        showConfirmation = true
                    } else {
                        navigateUp()
                    }
                }
                is CreatePollEvents.HideConfirmation -> showConfirmation = false
            }
        }

        return CreatePollState(
            canCreate = canCreate,
            canAddAnswer = canAddAnswer,
            question = question,
            answers = immutableAnswers,
            pollKind = pollKind,
            showConfirmation = showConfirmation,
            eventSink = ::handleEvents,
        )
    }
}

private fun canCreate(
    question: String,
    answers: List<String>
) = question.isNotBlank() && answers.size >= MIN_ANSWERS && answers.all { it.isNotBlank() }

private fun canAddAnswer(answers: List<String>) = answers.size < MAX_ANSWERS

private fun List<String>.toAnswers(): ImmutableList<Answer> {
    return map { answer ->
        Answer(
            text = answer,
            canDelete = this.size > MIN_ANSWERS,
        )
    }.toImmutableList()
}

private val pollKindSaver: Saver<MutableState<PollKind>, Boolean> = Saver(
    save = {
        when (it.value) {
            PollKind.Disclosed -> false
            PollKind.Undisclosed -> true
        }
    },
    restore = {
        mutableStateOf(
            when(it) {
                true -> PollKind.Undisclosed
                else -> PollKind.Disclosed
            }
        )
    }
)

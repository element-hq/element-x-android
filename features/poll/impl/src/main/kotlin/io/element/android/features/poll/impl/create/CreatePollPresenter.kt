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
import androidx.compose.runtime.LaunchedEffect
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
import im.vector.app.features.analytics.plan.Composer
import im.vector.app.features.analytics.plan.PollCreation
import io.element.android.features.messages.api.MessageComposerContext
import io.element.android.features.poll.api.create.CreatePollMode
import io.element.android.features.poll.impl.data.PollRepository
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.poll.PollAnswer
import io.element.android.libraries.matrix.api.poll.PollKind
import io.element.android.services.analytics.api.AnalyticsService
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import timber.log.Timber

private const val MIN_ANSWERS = 2
private const val MAX_ANSWERS = 20
private const val MAX_ANSWER_LENGTH = 240
private const val MAX_SELECTIONS = 1

class CreatePollPresenter @AssistedInject constructor(
    private val repository: PollRepository,
    private val analyticsService: AnalyticsService,
    private val messageComposerContext: MessageComposerContext,
    @Assisted private val navigateUp: () -> Unit,
    @Assisted private val mode: CreatePollMode,
) : Presenter<CreatePollState> {

    @AssistedFactory
    interface Factory {
        fun create(backNavigator: () -> Unit, mode: CreatePollMode): CreatePollPresenter
    }

    @Composable
    override fun present(): CreatePollState {
        var question: String by rememberSaveable { mutableStateOf("") }
        var answers: List<String> by rememberSaveable { mutableStateOf(listOf("", "")) }
        var pollKind: PollKind by rememberSaveable(saver = pollKindSaver) { mutableStateOf(PollKind.Disclosed) }
        var showConfirmation: Boolean by rememberSaveable { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            if (mode is CreatePollMode.EditPoll) {
                repository.getPoll(mode.eventId).onSuccess {
                    question = it.question
                    answers = it.answers.map(PollAnswer::text)
                    pollKind = it.kind
                }.onFailure {
                    analyticsService.trackGetPollFailed(it)
                    navigateUp()
                }
            }
        }

        val canSave: Boolean by remember { derivedStateOf { canSave(question, answers) } }
        val canAddAnswer: Boolean by remember { derivedStateOf { canAddAnswer(answers) } }
        val immutableAnswers: ImmutableList<Answer> by remember { derivedStateOf { answers.toAnswers() } }

        val scope = rememberCoroutineScope()

        fun handleEvents(event: CreatePollEvents) {
            when (event) {
                is CreatePollEvents.Save -> scope.launch {
                    if (canSave) {
                        repository.savePoll(
                            existingPollId = when (mode) {
                                is CreatePollMode.EditPoll -> mode.eventId
                                is CreatePollMode.NewPoll -> null
                            },
                            question = question,
                            answers = answers,
                            pollKind = pollKind,
                            maxSelections = MAX_SELECTIONS,
                        ).onSuccess {
                            analyticsService.capturePollSaved(
                                isUndisclosed = pollKind == PollKind.Undisclosed,
                                numberOfAnswers = answers.size,
                            )
                        }.onFailure {
                            analyticsService.trackSavePollFailed(it, mode)
                        }
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
                    answers = answers.toMutableList().apply {
                        this[event.index] = event.text.take(MAX_ANSWER_LENGTH)
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
            mode = when (mode) {
                is CreatePollMode.NewPoll -> CreatePollState.Mode.New
                is CreatePollMode.EditPoll -> CreatePollState.Mode.Edit
            },
            canSave = canSave,
            canAddAnswer = canAddAnswer,
            question = question,
            answers = immutableAnswers,
            pollKind = pollKind,
            showConfirmation = showConfirmation,
            eventSink = ::handleEvents,
        )
    }

    private fun AnalyticsService.capturePollSaved(
        isUndisclosed: Boolean,
        numberOfAnswers: Int,
    ) {
        capture(
            Composer(
                inThread = messageComposerContext.composerMode.inThread,
                isEditing = mode is CreatePollMode.EditPoll,
                isReply = messageComposerContext.composerMode.isReply,
                messageType = Composer.MessageType.Poll,
            )
        )
        capture(
            PollCreation(
                action = when (mode) {
                    is CreatePollMode.EditPoll -> PollCreation.Action.Edit
                    is CreatePollMode.NewPoll -> PollCreation.Action.Create
                },
                isUndisclosed = isUndisclosed,
                numberOfAnswers = numberOfAnswers,
            )
        )
    }
}

private fun AnalyticsService.trackGetPollFailed(cause: Throwable) {
    val exception = CreatePollException.GetPollFailed(
        message = "Tried to edit poll but couldn't get poll",
        cause = cause,
    )
    Timber.e(exception)
    trackError(exception)
}

private fun AnalyticsService.trackSavePollFailed(cause: Throwable, mode: CreatePollMode) {
    val exception = CreatePollException.SavePollFailed(
        message = when (mode) {
            CreatePollMode.NewPoll -> "Failed to create poll"
            is CreatePollMode.EditPoll -> "Failed to edit poll"
        },
        cause = cause,
    )
    Timber.e(exception)
    trackError(exception)
}

private fun canSave(
    question: String,
    answers: List<String>
) = question.isNotBlank() && answers.size >= MIN_ANSWERS && answers.all { it.isNotBlank() }

private fun canAddAnswer(answers: List<String>) = answers.size < MAX_ANSWERS

fun List<String>.toAnswers(): ImmutableList<Answer> {
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
            when (it) {
                true -> PollKind.Undisclosed
                else -> PollKind.Disclosed
            }
        )
    }
)

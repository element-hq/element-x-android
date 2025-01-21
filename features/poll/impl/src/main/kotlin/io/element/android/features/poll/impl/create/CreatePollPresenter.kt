/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.poll.impl.create

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import im.vector.app.features.analytics.plan.Composer
import im.vector.app.features.analytics.plan.PollCreation
import io.element.android.features.messages.api.MessageComposerContext
import io.element.android.features.poll.api.create.CreatePollMode
import io.element.android.features.poll.impl.PollConstants.MAX_SELECTIONS
import io.element.android.features.poll.impl.data.PollRepository
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.poll.PollAnswer
import io.element.android.libraries.matrix.api.poll.PollKind
import io.element.android.libraries.matrix.api.poll.isDisclosed
import io.element.android.services.analytics.api.AnalyticsService
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.launch
import timber.log.Timber

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
        // The initial state of the form. In edit mode this will be populated with the poll being edited.
        var initialPoll: PollFormState by rememberSaveable(stateSaver = pollFormStateSaver) {
            mutableStateOf(PollFormState.Empty)
        }
        // The current state of the form.
        var poll: PollFormState by rememberSaveable(stateSaver = pollFormStateSaver) {
            mutableStateOf(initialPoll)
        }

        // Whether the form has been changed from the initial state
        val isDirty: Boolean by remember { derivedStateOf { poll != initialPoll } }

        var showBackConfirmation: Boolean by rememberSaveable { mutableStateOf(false) }
        var showDeleteConfirmation: Boolean by rememberSaveable { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            if (mode is CreatePollMode.EditPoll) {
                repository.getPoll(mode.eventId).onSuccess {
                    val loadedPoll = PollFormState(
                        question = it.question,
                        answers = it.answers.map(PollAnswer::text).toPersistentList(),
                        isDisclosed = it.kind.isDisclosed,
                    )
                    initialPoll = loadedPoll
                    poll = loadedPoll
                }.onFailure {
                    analyticsService.trackGetPollFailed(it)
                    navigateUp()
                }
            }
        }

        val canSave: Boolean by remember { derivedStateOf { poll.isValid } }
        val canAddAnswer: Boolean by remember { derivedStateOf { poll.canAddAnswer } }
        val immutableAnswers: ImmutableList<Answer> by remember { derivedStateOf { poll.toUiAnswers() } }

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
                            question = poll.question,
                            answers = poll.answers,
                            pollKind = poll.pollKind,
                            maxSelections = MAX_SELECTIONS,
                        ).onSuccess {
                            analyticsService.capturePollSaved(
                                isUndisclosed = poll.pollKind == PollKind.Undisclosed,
                                numberOfAnswers = poll.answers.size,
                            )
                        }.onFailure {
                            analyticsService.trackSavePollFailed(it, mode)
                        }
                        navigateUp()
                    } else {
                        Timber.d("Cannot create poll")
                    }
                }
                is CreatePollEvents.Delete -> {
                    if (mode !is CreatePollMode.EditPoll) {
                        return
                    }

                    if (!event.confirmed) {
                        showDeleteConfirmation = true
                        return
                    }

                    scope.launch {
                        showDeleteConfirmation = false
                        repository.deletePoll(mode.eventId)
                        navigateUp()
                    }
                }
                is CreatePollEvents.AddAnswer -> {
                    poll = poll.withNewAnswer()
                }
                is CreatePollEvents.RemoveAnswer -> {
                    poll = poll.withAnswerRemoved(event.index)
                }
                is CreatePollEvents.SetAnswer -> {
                    poll = poll.withAnswerChanged(event.index, event.text)
                }
                is CreatePollEvents.SetPollKind -> {
                    poll = poll.copy(isDisclosed = event.pollKind.isDisclosed)
                }
                is CreatePollEvents.SetQuestion -> {
                    poll = poll.copy(question = event.question)
                }
                is CreatePollEvents.NavBack -> {
                    navigateUp()
                }
                CreatePollEvents.ConfirmNavBack -> {
                    val shouldConfirm = isDirty
                    if (shouldConfirm) {
                        showBackConfirmation = true
                    } else {
                        navigateUp()
                    }
                }
                is CreatePollEvents.HideConfirmation -> {
                    showBackConfirmation = false
                    showDeleteConfirmation = false
                }
            }
        }

        return CreatePollState(
            mode = when (mode) {
                is CreatePollMode.NewPoll -> CreatePollState.Mode.New
                is CreatePollMode.EditPoll -> CreatePollState.Mode.Edit
            },
            canSave = canSave,
            canAddAnswer = canAddAnswer,
            question = poll.question,
            answers = immutableAnswers,
            pollKind = poll.pollKind,
            showBackConfirmation = showBackConfirmation,
            showDeleteConfirmation = showDeleteConfirmation,
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

fun PollFormState.toUiAnswers(): ImmutableList<Answer> {
    return answers.map { answer ->
        Answer(
            text = answer,
            canDelete = canDeleteAnswer,
        )
    }.toImmutableList()
}

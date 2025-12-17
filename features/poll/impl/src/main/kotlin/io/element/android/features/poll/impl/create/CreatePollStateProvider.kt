/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.poll.impl.create

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.matrix.api.poll.PollKind
import kotlinx.collections.immutable.toImmutableList

class CreatePollStateProvider : PreviewParameterProvider<CreatePollState> {
    override val values: Sequence<CreatePollState>
        get() = sequenceOf(
            aCreatePollState(
                mode = CreatePollState.Mode.New,
                canCreate = false,
                canAddAnswer = true,
                question = "",
                answers = listOf(
                    Answer("", false),
                    Answer("", false)
                ),
                pollKind = PollKind.Disclosed,
                showBackConfirmation = false,
                showDeleteConfirmation = false,
            ),
            aCreatePollState(
                mode = CreatePollState.Mode.New,
                canCreate = true,
                canAddAnswer = true,
                question = "What type of food should we have?",
                answers = listOf(
                    Answer("Italian \uD83C\uDDEE\uD83C\uDDF9", false),
                    Answer("Chinese \uD83C\uDDE8\uD83C\uDDF3", false),
                ),
                showBackConfirmation = false,
                showDeleteConfirmation = false,
                pollKind = PollKind.Undisclosed,
            ),
            aCreatePollState(
                mode = CreatePollState.Mode.New,
                canCreate = true,
                canAddAnswer = true,
                question = "What type of food should we have?",
                answers = listOf(
                    Answer("Italian \uD83C\uDDEE\uD83C\uDDF9", false),
                    Answer("Chinese \uD83C\uDDE8\uD83C\uDDF3", false),
                ),
                showBackConfirmation = true,
                showDeleteConfirmation = false,
                pollKind = PollKind.Undisclosed,
            ),
            aCreatePollState(
                mode = CreatePollState.Mode.New,
                canCreate = true,
                canAddAnswer = true,
                question = "What type of food should we have?",
                answers = listOf(
                    Answer("Italian \uD83C\uDDEE\uD83C\uDDF9", true),
                    Answer("Chinese \uD83C\uDDE8\uD83C\uDDF3", true),
                    Answer("Brazilian \uD83C\uDDE7\uD83C\uDDF7", true),
                    Answer("French \uD83C\uDDEB\uD83C\uDDF7", true),
                ),
                showBackConfirmation = false,
                showDeleteConfirmation = false,
                pollKind = PollKind.Undisclosed,
            ),
            aCreatePollState(
                mode = CreatePollState.Mode.New,
                canCreate = true,
                canAddAnswer = false,
                question = "Should there be more than 20 answers?",
                answers = listOf(
                    Answer("1", true),
                    Answer("2", true),
                    Answer("3", true),
                    Answer("4", true),
                    Answer("5", true),
                    Answer("6", true),
                    Answer("7", true),
                    Answer("8", true),
                    Answer("9", true),
                    Answer("10", true),
                    Answer("11", true),
                    Answer("12", true),
                    Answer("13", true),
                    Answer("14", true),
                    Answer("15", true),
                    Answer("16", true),
                    Answer("17", true),
                    Answer("18", true),
                    Answer("19", true),
                    Answer("20", true),
                ),
                showBackConfirmation = false,
                showDeleteConfirmation = false,
                pollKind = PollKind.Undisclosed,
            ),
            aCreatePollState(
                mode = CreatePollState.Mode.New,
                canCreate = true,
                canAddAnswer = true,
                question = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua." +
                    " Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor" +
                    " in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt" +
                    " in culpa qui officia deserunt mollit anim id est laborum.",
                answers = listOf(
                    Answer(
                        "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua." +
                            " Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis a.",
                        false
                    ),
                    Answer(
                        "Laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore" +
                            " eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mol.",
                        false
                    ),
                ),
                showBackConfirmation = false,
                showDeleteConfirmation = false,
                pollKind = PollKind.Undisclosed,
            ),
            aCreatePollState(
                mode = CreatePollState.Mode.Edit,
                canCreate = false,
                canAddAnswer = true,
                question = "",
                answers = listOf(
                    Answer("", false),
                    Answer("", false)
                ),
                pollKind = PollKind.Disclosed,
                showDeleteConfirmation = false,
                showBackConfirmation = false,
            ),
            aCreatePollState(
                mode = CreatePollState.Mode.Edit,
                canCreate = false,
                canAddAnswer = true,
                question = "",
                answers = listOf(
                    Answer("", false),
                    Answer("", false)
                ),
                pollKind = PollKind.Disclosed,
                showDeleteConfirmation = true,
                showBackConfirmation = false,
            ),
        )
}

private fun aCreatePollState(
    mode: CreatePollState.Mode,
    canCreate: Boolean,
    canAddAnswer: Boolean,
    question: String,
    answers: List<Answer>,
    showBackConfirmation: Boolean,
    showDeleteConfirmation: Boolean,
    pollKind: PollKind
): CreatePollState {
    return CreatePollState(
        mode = mode,
        canSave = canCreate,
        canAddAnswer = canAddAnswer,
        question = question,
        answers = answers.toImmutableList(),
        showBackConfirmation = showBackConfirmation,
        showDeleteConfirmation = showDeleteConfirmation,
        pollKind = pollKind,
        eventSink = {}
    )
}

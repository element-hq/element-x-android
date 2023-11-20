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

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.matrix.api.poll.PollKind
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

class CreatePollStateProvider : PreviewParameterProvider<CreatePollState> {
    override val values: Sequence<CreatePollState>
        get() = sequenceOf(
            aCreatePollState(
                canCreate = false,
                canAddAnswer = true,
                question = "",
                answers = persistentListOf(
                    Answer("", false),
                    Answer("", false)
                ),
                pollKind = PollKind.Disclosed,
                showConfirmation = false,
            ),
            aCreatePollState(
                canCreate = true,
                canAddAnswer = true,
                question = "What type of food should we have?",
                answers = persistentListOf(
                    Answer("Italian \uD83C\uDDEE\uD83C\uDDF9", false),
                    Answer("Chinese \uD83C\uDDE8\uD83C\uDDF3", false),
                ),
                showConfirmation = false,
                pollKind = PollKind.Undisclosed,
            ),
            aCreatePollState(
                canCreate = true,
                canAddAnswer = true,
                question = "What type of food should we have?",
                answers = persistentListOf(
                    Answer("Italian \uD83C\uDDEE\uD83C\uDDF9", false),
                    Answer("Chinese \uD83C\uDDE8\uD83C\uDDF3", false),
                ),
                showConfirmation = true,
                pollKind = PollKind.Undisclosed,
            ),
            aCreatePollState(
                canCreate = true,
                canAddAnswer = true,
                question = "What type of food should we have?",
                answers = persistentListOf(
                    Answer("Italian \uD83C\uDDEE\uD83C\uDDF9", true),
                    Answer("Chinese \uD83C\uDDE8\uD83C\uDDF3", true),
                    Answer("Brazilian \uD83C\uDDE7\uD83C\uDDF7", true),
                    Answer("French \uD83C\uDDEB\uD83C\uDDF7", true),
                ),
                showConfirmation = false,
                pollKind = PollKind.Undisclosed,
            ),
            aCreatePollState(
                canCreate = true,
                canAddAnswer = false,
                question = "Should there be more than 20 answers?",
                answers = persistentListOf(
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
                showConfirmation = false,
                pollKind = PollKind.Undisclosed,
            ),
            aCreatePollState(
                canCreate = true,
                canAddAnswer = true,
                question = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua." +
                    " Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor" +
                    " in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt" +
                    " in culpa qui officia deserunt mollit anim id est laborum.",
                answers = persistentListOf(
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
                showConfirmation = false,
                pollKind = PollKind.Undisclosed,
            )
        )
}

private fun aCreatePollState(
    canCreate: Boolean,
    canAddAnswer: Boolean,
    question: String,
    answers: PersistentList<Answer>,
    showConfirmation: Boolean,
    pollKind: PollKind
): CreatePollState {
    return CreatePollState(
        canCreate = canCreate,
        canAddAnswer = canAddAnswer,
        question = question,
        answers = answers,
        showConfirmation = showConfirmation,
        pollKind = pollKind,
        eventSink = {}
    )
}

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

import io.element.android.libraries.matrix.api.poll.PollKind
import kotlinx.collections.immutable.ImmutableList

data class CreatePollState(
    val mode: Mode,
    val canSave: Boolean,
    val canAddAnswer: Boolean,
    val question: String,
    val answers: ImmutableList<Answer>,
    val pollKind: PollKind,
    val showBackConfirmation: Boolean,
    val showDeleteConfirmation: Boolean,
    val eventSink: (CreatePollEvents) -> Unit,
) {
    enum class Mode {
        New,
        Edit,
    }

    val canDelete: Boolean = mode == Mode.Edit
}

data class Answer(
    val text: String,
    val canDelete: Boolean,
)

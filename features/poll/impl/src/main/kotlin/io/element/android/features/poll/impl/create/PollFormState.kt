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

import androidx.compose.runtime.saveable.mapSaver
import io.element.android.libraries.matrix.api.poll.PollKind
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList

data class PollFormState(
    val question: String,
    val answers: ImmutableList<String>,
    val isDisclosed: Boolean,
) {
    val pollKind
        get() = when(isDisclosed) {
            true -> PollKind.Disclosed
            false -> PollKind.Undisclosed
        }
}

val pollFormStateSaver = mapSaver(
    save = {
        mutableMapOf(
            "question" to it.question,
            "answers" to it.answers.toTypedArray(),
            "isDisclosed" to it.isDisclosed,
        )
    },
    restore = { saved ->
        PollFormState(
            question = saved["question"] as String,
            answers = (saved["answers"] as Array<*>).map { it as String }.toPersistentList(),
            isDisclosed = saved["isDisclosed"] as Boolean,
        )
    }
)

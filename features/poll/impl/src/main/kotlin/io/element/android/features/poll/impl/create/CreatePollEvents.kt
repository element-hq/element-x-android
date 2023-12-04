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

sealed interface CreatePollEvents {
    data object Save : CreatePollEvents
    data class Delete(val confirmed: Boolean) : CreatePollEvents
    data class SetQuestion(val question: String) : CreatePollEvents
    data class SetAnswer(val index: Int, val text: String) : CreatePollEvents
    data object AddAnswer : CreatePollEvents
    data class RemoveAnswer(val index: Int) : CreatePollEvents
    data class SetPollKind(val pollKind: PollKind) : CreatePollEvents
    data object NavBack : CreatePollEvents
    data object ConfirmNavBack : CreatePollEvents
    data object HideConfirmation : CreatePollEvents
}

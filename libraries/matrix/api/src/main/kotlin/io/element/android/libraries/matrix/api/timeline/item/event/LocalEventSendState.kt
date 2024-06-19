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

package io.element.android.libraries.matrix.api.timeline.item.event

import androidx.compose.runtime.Immutable
import io.element.android.libraries.matrix.api.core.EventId

@Immutable
sealed interface LocalEventSendState {
    data object NotSentYet : LocalEventSendState
    sealed class SendingFailed(open val error: String) : LocalEventSendState {
        data class Recoverable(override val error: String) : SendingFailed(error)
        data class Unrecoverable(override val error: String) : SendingFailed(error)
    }
    data class Sent(
        val eventId: EventId
    ) : LocalEventSendState
}

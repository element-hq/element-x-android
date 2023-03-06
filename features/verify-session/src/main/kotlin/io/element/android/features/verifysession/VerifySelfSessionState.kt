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

package io.element.android.features.verifysession

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import io.element.android.libraries.architecture.Async

@Immutable
data class VerifySelfSessionState(
    val verificationState: VerificationState,
    val eventSink: (VerifySelfSessionEvents) -> Unit,
)

@Stable
sealed interface VerificationState {
    object Initial : VerificationState
    object Canceled : VerificationState
    object AwaitingOtherDeviceResponse : VerificationState
    data class Verifying(val emojiList: List<EmojiEntry>, val state: Async<Boolean>) : VerificationState
    object Completed : VerificationState
}

data class EmojiEntry(
    val code: String,
    val name: String,
)

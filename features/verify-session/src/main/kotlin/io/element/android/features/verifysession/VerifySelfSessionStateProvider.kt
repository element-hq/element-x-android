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

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.Async

open class VerifySelfSessionStateProvider : PreviewParameterProvider<VerifySelfSessionState> {
    override val values: Sequence<VerifySelfSessionState>
        get() = sequenceOf(
            aTemplateState(),
            aTemplateState().copy(verificationState = VerificationState.AwaitingOtherDeviceResponse),
            aTemplateState().copy(verificationState = VerificationState.Verifying(aEmojiEntryList(),  Async.Uninitialized)),
            aTemplateState().copy(verificationState = VerificationState.Verifying(aEmojiEntryList(),  Async.Loading())),
            aTemplateState().copy(verificationState = VerificationState.Canceled),
            // Add other state here
        )
}

fun aTemplateState() = VerifySelfSessionState(
    verificationState = VerificationState.Initial,
    eventSink = {},
)

fun aEmojiEntryList() = listOf(
    VerificationEmoji("🍕", "Pizza"),
    VerificationEmoji("🚀", "Rocket"),
    VerificationEmoji("🚀", "Rocket"),
    VerificationEmoji("🗺️", "Map"),
    VerificationEmoji("🎳", "Bowling"),
    VerificationEmoji("🎳", "Bowling"),
    VerificationEmoji("📌", "Pin"),
)

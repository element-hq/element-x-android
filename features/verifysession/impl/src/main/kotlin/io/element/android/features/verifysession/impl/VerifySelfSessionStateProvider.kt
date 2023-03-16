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

package io.element.android.features.verifysession.impl

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.matrix.api.verification.VerificationEmoji

open class VerifySelfSessionStateProvider : PreviewParameterProvider<VerifySelfSessionState> {
    override val values: Sequence<VerifySelfSessionState>
        get() = sequenceOf(
            aTemplateState(),
            aTemplateState().copy(verificationFlowStep = VerifySelfSessionState.VerificationStep.AwaitingOtherDeviceResponse),
            aTemplateState().copy(verificationFlowStep = VerifySelfSessionState.VerificationStep.Verifying(aVerificationEmojiList(), Async.Uninitialized)),
            aTemplateState().copy(verificationFlowStep = VerifySelfSessionState.VerificationStep.Verifying(aVerificationEmojiList(), Async.Loading())),
            aTemplateState().copy(verificationFlowStep = VerifySelfSessionState.VerificationStep.Canceled),
            // Add other state here
        )
}

fun aTemplateState() = VerifySelfSessionState(
    verificationFlowStep = VerifySelfSessionState.VerificationStep.Initial,
    eventSink = {},
)

fun aVerificationEmojiList() = listOf(
    VerificationEmoji("🍕", "Pizza"),
    VerificationEmoji("🚀", "Rocket"),
    VerificationEmoji("🚀", "Rocket"),
    VerificationEmoji("🗺️", "Map"),
    VerificationEmoji("🎳", "Bowling"),
    VerificationEmoji("🎳", "Bowling"),
    VerificationEmoji("📌", "Pin"),
)

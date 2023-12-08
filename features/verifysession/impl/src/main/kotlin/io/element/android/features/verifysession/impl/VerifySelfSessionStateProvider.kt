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
import io.element.android.libraries.matrix.api.verification.SessionVerificationData
import io.element.android.libraries.matrix.api.verification.VerificationEmoji

open class VerifySelfSessionStateProvider : PreviewParameterProvider<VerifySelfSessionState> {
    override val values: Sequence<VerifySelfSessionState>
        get() = sequenceOf(
            aVerifySelfSessionState(),
            aVerifySelfSessionState().copy(
                verificationFlowStep = VerifySelfSessionState.VerificationStep.AwaitingOtherDeviceResponse
            ),
            aVerifySelfSessionState().copy(
                verificationFlowStep = VerifySelfSessionState.VerificationStep.Verifying(aEmojisSessionVerificationData(), Async.Uninitialized)
            ),
            aVerifySelfSessionState().copy(
                verificationFlowStep = VerifySelfSessionState.VerificationStep.Verifying(aEmojisSessionVerificationData(), Async.Loading())
            ),
            aVerifySelfSessionState().copy(
                verificationFlowStep = VerifySelfSessionState.VerificationStep.Canceled
            ),
            aVerifySelfSessionState().copy(
                verificationFlowStep = VerifySelfSessionState.VerificationStep.Ready
            ),
            aVerifySelfSessionState().copy(
                verificationFlowStep = VerifySelfSessionState.VerificationStep.Verifying(aDecimalsSessionVerificationData(), Async.Uninitialized)
            ),
            // Add other state here
        )
}

private fun aEmojisSessionVerificationData(
    emojiList: List<VerificationEmoji> = aVerificationEmojiList(),
): SessionVerificationData {
    return SessionVerificationData.Emojis(emojiList)
}

private fun aDecimalsSessionVerificationData(
    decimals: List<Int> = listOf(123, 456, 789),
): SessionVerificationData {
    return SessionVerificationData.Decimals(decimals)
}

private fun aVerifySelfSessionState() = VerifySelfSessionState(
    verificationFlowStep = VerifySelfSessionState.VerificationStep.Initial,
    eventSink = {},
)

private fun aVerificationEmojiList() = listOf(
    VerificationEmoji(number = 27, emoji = "🍕", description = "Pizza"),
    VerificationEmoji(number = 54, emoji = "🚀", description = "Rocket"),
    VerificationEmoji(number = 54, emoji = "🚀", description = "Rocket"),
    VerificationEmoji(number = 42, emoji = "📕", description = "Book"),
    VerificationEmoji(number = 48, emoji = "🔨", description = "Hammer"),
    VerificationEmoji(number = 48, emoji = "🔨", description = "Hammer"),
    VerificationEmoji(number = 63, emoji = "📌", description = "Pin"),
)

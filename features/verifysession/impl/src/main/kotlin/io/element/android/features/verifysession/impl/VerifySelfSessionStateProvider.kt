/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.verifysession.impl

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.verifysession.impl.VerifySelfSessionState.VerificationStep
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.verification.SessionVerificationData
import io.element.android.libraries.matrix.api.verification.VerificationEmoji

open class VerifySelfSessionStateProvider : PreviewParameterProvider<VerifySelfSessionState> {
    override val values: Sequence<VerifySelfSessionState>
        get() = sequenceOf(
            aVerifySelfSessionState(displaySkipButton = true),
            aVerifySelfSessionState(
                verificationFlowStep = VerificationStep.AwaitingOtherDeviceResponse
            ),
            aVerifySelfSessionState(
                verificationFlowStep = VerificationStep.Verifying(aEmojisSessionVerificationData(), AsyncData.Uninitialized)
            ),
            aVerifySelfSessionState(
                verificationFlowStep = VerificationStep.Verifying(aEmojisSessionVerificationData(), AsyncData.Loading())
            ),
            aVerifySelfSessionState(
                verificationFlowStep = VerificationStep.Canceled
            ),
            aVerifySelfSessionState(
                verificationFlowStep = VerificationStep.Ready
            ),
            aVerifySelfSessionState(
                verificationFlowStep = VerificationStep.Verifying(aDecimalsSessionVerificationData(), AsyncData.Uninitialized)
            ),
            aVerifySelfSessionState(
                verificationFlowStep = VerificationStep.Initial(canEnterRecoveryKey = true)
            ),
            aVerifySelfSessionState(
                verificationFlowStep = VerificationStep.Initial(canEnterRecoveryKey = true, isLastDevice = true)
            ),
            aVerifySelfSessionState(
                verificationFlowStep = VerificationStep.Completed,
                displaySkipButton = true,
            ),
            aVerifySelfSessionState(
                signOutAction = AsyncAction.Loading,
                displaySkipButton = true,
            ),
            aVerifySelfSessionState(
                verificationFlowStep = VerificationStep.Loading
            ),
            aVerifySelfSessionState(
                verificationFlowStep = VerificationStep.Skipped
            ),
            // Add other state here
        )
}

internal fun aEmojisSessionVerificationData(
    emojiList: List<VerificationEmoji> = aVerificationEmojiList(),
): SessionVerificationData {
    return SessionVerificationData.Emojis(emojiList)
}

private fun aDecimalsSessionVerificationData(
    decimals: List<Int> = listOf(123, 456, 789),
): SessionVerificationData {
    return SessionVerificationData.Decimals(decimals)
}

internal fun aVerifySelfSessionState(
    verificationFlowStep: VerificationStep = VerificationStep.Initial(canEnterRecoveryKey = false),
    signOutAction: AsyncAction<String?> = AsyncAction.Uninitialized,
    displaySkipButton: Boolean = false,
    eventSink: (VerifySelfSessionViewEvents) -> Unit = {},
) = VerifySelfSessionState(
    verificationFlowStep = verificationFlowStep,
    displaySkipButton = displaySkipButton,
    eventSink = eventSink,
    signOutAction = signOutAction,
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

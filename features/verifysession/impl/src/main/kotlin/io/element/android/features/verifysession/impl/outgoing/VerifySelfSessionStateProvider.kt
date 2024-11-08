/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.verifysession.impl.outgoing

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.verifysession.impl.outgoing.VerifySelfSessionState.Step
import io.element.android.features.verifysession.impl.ui.aDecimalsSessionVerificationData
import io.element.android.features.verifysession.impl.ui.aEmojisSessionVerificationData
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData

open class VerifySelfSessionStateProvider : PreviewParameterProvider<VerifySelfSessionState> {
    override val values: Sequence<VerifySelfSessionState>
        get() = sequenceOf(
            aVerifySelfSessionState(displaySkipButton = true),
            aVerifySelfSessionState(
                step = Step.AwaitingOtherDeviceResponse
            ),
            aVerifySelfSessionState(
                step = Step.Verifying(aEmojisSessionVerificationData(), AsyncData.Uninitialized)
            ),
            aVerifySelfSessionState(
                step = Step.Verifying(aEmojisSessionVerificationData(), AsyncData.Loading())
            ),
            aVerifySelfSessionState(
                step = Step.Canceled
            ),
            aVerifySelfSessionState(
                step = Step.Ready
            ),
            aVerifySelfSessionState(
                step = Step.Verifying(aDecimalsSessionVerificationData(), AsyncData.Uninitialized)
            ),
            aVerifySelfSessionState(
                step = Step.Initial(canEnterRecoveryKey = true)
            ),
            aVerifySelfSessionState(
                step = Step.Initial(canEnterRecoveryKey = true, isLastDevice = true)
            ),
            aVerifySelfSessionState(
                step = Step.Completed,
                displaySkipButton = true,
            ),
            aVerifySelfSessionState(
                signOutAction = AsyncAction.Loading,
                displaySkipButton = true,
            ),
            aVerifySelfSessionState(
                step = Step.Loading
            ),
            aVerifySelfSessionState(
                step = Step.Skipped
            ),
            aVerifySelfSessionState(
                step = Step.UseAnotherDevice
            ),
            // Add other state here
        )
}

internal fun aVerifySelfSessionState(
    step: Step = Step.Initial(canEnterRecoveryKey = false),
    signOutAction: AsyncAction<String?> = AsyncAction.Uninitialized,
    displaySkipButton: Boolean = false,
    eventSink: (VerifySelfSessionViewEvents) -> Unit = {},
) = VerifySelfSessionState(
    step = step,
    displaySkipButton = displaySkipButton,
    eventSink = eventSink,
    signOutAction = signOutAction,
)

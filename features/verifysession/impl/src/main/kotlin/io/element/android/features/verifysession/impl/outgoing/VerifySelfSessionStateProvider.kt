/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.verifysession.impl.outgoing

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.verifysession.impl.outgoing.VerifySelfSessionState.Step
import io.element.android.features.verifysession.impl.ui.aDecimalsSessionVerificationData
import io.element.android.features.verifysession.impl.ui.aEmojisSessionVerificationData
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.verification.VerificationRequest

open class VerifySelfSessionStateProvider : PreviewParameterProvider<VerifySelfSessionState> {
    override val values: Sequence<VerifySelfSessionState>
        get() = sequenceOf(
            aVerifySelfSessionState(displaySkipButton = true),
            aVerifySelfSessionState(
                step = Step.AwaitingOtherDeviceResponse,
                request = anOutgoingSessionVerificationRequest(),
            ),
            aVerifySelfSessionState(
                step = Step.AwaitingOtherDeviceResponse,
                request = anOutgoingUserVerificationRequest(),
            ),
            aVerifySelfSessionState(
                step = Step.Verifying(aEmojisSessionVerificationData(), AsyncData.Uninitialized),
                request = anOutgoingSessionVerificationRequest(),
            ),
            aVerifySelfSessionState(
                step = Step.Verifying(aEmojisSessionVerificationData(), AsyncData.Uninitialized),
                request = anOutgoingUserVerificationRequest(),
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
                request = anOutgoingSessionVerificationRequest(),
            ),
            aVerifySelfSessionState(
                step = Step.Completed,
                displaySkipButton = true,
                request = anOutgoingUserVerificationRequest(),
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
                step = Step.UseAnotherDevice,
                request = anOutgoingSessionVerificationRequest(),
            ),
            aVerifySelfSessionState(
                step = Step.UseAnotherDevice,
                request = anOutgoingUserVerificationRequest(),
            ),
            // Add other state here
        )
}

internal fun anOutgoingUserVerificationRequest() = VerificationRequest.Outgoing.User(userId = UserId("@alice:example.com"))
internal fun anOutgoingSessionVerificationRequest() = VerificationRequest.Outgoing.CurrentSession

internal fun aVerifySelfSessionState(
    step: Step = Step.Initial(canEnterRecoveryKey = false),
    request: VerificationRequest.Outgoing = anOutgoingSessionVerificationRequest(),
    signOutAction: AsyncAction<String?> = AsyncAction.Uninitialized,
    displaySkipButton: Boolean = false,
    eventSink: (VerifySelfSessionViewEvents) -> Unit = {},
) = VerifySelfSessionState(
    step = step,
    request = request,
    displaySkipButton = displaySkipButton,
    eventSink = eventSink,
    signOutAction = signOutAction,
)

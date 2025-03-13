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
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.verification.VerificationRequest

open class VerifySelfSessionStateProvider : PreviewParameterProvider<VerifySelfSessionState> {
    override val values: Sequence<VerifySelfSessionState>
        get() = sequenceOf(
            aVerifySelfSessionState(
                step = Step.Initial,
                request = anOutgoingSessionVerificationRequest(),
            ),
            aVerifySelfSessionState(
                step = Step.Initial,
                request = anOutgoingUserVerificationRequest(),
            ),
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
                step = Step.Completed,
                request = anOutgoingSessionVerificationRequest(),
            ),
            aVerifySelfSessionState(
                step = Step.Completed,
                request = anOutgoingUserVerificationRequest(),
            ),
            aVerifySelfSessionState(
                step = Step.Loading
            ),
            aVerifySelfSessionState(
                step = Step.Exit
            ),
            // Add other state here
        )
}

internal fun anOutgoingUserVerificationRequest() = VerificationRequest.Outgoing.User(userId = UserId("@alice:example.com"))
internal fun anOutgoingSessionVerificationRequest() = VerificationRequest.Outgoing.CurrentSession

internal fun aVerifySelfSessionState(
    step: Step = Step.Initial,
    request: VerificationRequest.Outgoing = anOutgoingSessionVerificationRequest(),
    eventSink: (VerifySelfSessionViewEvents) -> Unit = {},
) = VerifySelfSessionState(
    step = step,
    request = request,
    eventSink = eventSink,
)

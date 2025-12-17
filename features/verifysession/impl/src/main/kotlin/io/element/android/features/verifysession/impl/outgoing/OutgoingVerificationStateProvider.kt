/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.verifysession.impl.outgoing

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.verifysession.impl.outgoing.OutgoingVerificationState.Step
import io.element.android.features.verifysession.impl.ui.aDecimalsSessionVerificationData
import io.element.android.features.verifysession.impl.ui.aEmojisSessionVerificationData
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.verification.VerificationRequest

open class OutgoingVerificationStateProvider : PreviewParameterProvider<OutgoingVerificationState> {
    override val values: Sequence<OutgoingVerificationState>
        get() = sequenceOf(
            anOutgoingVerificationState(
                step = Step.Initial,
                request = anOutgoingSessionVerificationRequest(),
            ),
            anOutgoingVerificationState(
                step = Step.Initial,
                request = anOutgoingUserVerificationRequest(),
            ),
            anOutgoingVerificationState(
                step = Step.AwaitingOtherDeviceResponse,
                request = anOutgoingSessionVerificationRequest(),
            ),
            anOutgoingVerificationState(
                step = Step.AwaitingOtherDeviceResponse,
                request = anOutgoingUserVerificationRequest(),
            ),
            anOutgoingVerificationState(
                step = Step.Verifying(aEmojisSessionVerificationData(), AsyncData.Uninitialized),
                request = anOutgoingSessionVerificationRequest(),
            ),
            anOutgoingVerificationState(
                step = Step.Verifying(aEmojisSessionVerificationData(), AsyncData.Uninitialized),
                request = anOutgoingUserVerificationRequest(),
            ),
            anOutgoingVerificationState(
                step = Step.Verifying(aEmojisSessionVerificationData(), AsyncData.Loading())
            ),
            anOutgoingVerificationState(
                step = Step.Canceled
            ),
            anOutgoingVerificationState(
                step = Step.Ready
            ),
            anOutgoingVerificationState(
                step = Step.Verifying(aDecimalsSessionVerificationData(), AsyncData.Uninitialized)
            ),
            anOutgoingVerificationState(
                step = Step.Completed,
                request = anOutgoingSessionVerificationRequest(),
            ),
            anOutgoingVerificationState(
                step = Step.Completed,
                request = anOutgoingUserVerificationRequest(),
            ),
            anOutgoingVerificationState(
                step = Step.Loading
            ),
            anOutgoingVerificationState(
                step = Step.Exit
            ),
            // Add other state here
        )
}

internal fun anOutgoingUserVerificationRequest() = VerificationRequest.Outgoing.User(userId = UserId("@alice:example.com"))
internal fun anOutgoingSessionVerificationRequest() = VerificationRequest.Outgoing.CurrentSession

internal fun anOutgoingVerificationState(
    step: Step = Step.Initial,
    request: VerificationRequest.Outgoing = anOutgoingSessionVerificationRequest(),
    eventSink: (OutgoingVerificationViewEvents) -> Unit = {},
) = OutgoingVerificationState(
    step = step,
    request = request,
    eventSink = eventSink,
)

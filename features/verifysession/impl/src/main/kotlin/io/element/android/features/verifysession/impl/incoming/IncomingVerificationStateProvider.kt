/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.verifysession.impl.incoming

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.verifysession.impl.incoming.IncomingVerificationState.Step
import io.element.android.features.verifysession.impl.ui.aDecimalsSessionVerificationData
import io.element.android.features.verifysession.impl.ui.aEmojisSessionVerificationData
import io.element.android.libraries.matrix.api.core.DeviceId

open class IncomingVerificationStateProvider : PreviewParameterProvider<IncomingVerificationState> {
    override val values: Sequence<IncomingVerificationState>
        get() = sequenceOf(
            anIncomingVerificationState(),
            anIncomingVerificationState(step = aStepInitial(isWaiting = true)),
            anIncomingVerificationState(step = Step.Verifying(data = aEmojisSessionVerificationData(), isWaiting = false)),
            anIncomingVerificationState(step = Step.Verifying(data = aEmojisSessionVerificationData(), isWaiting = true)),
            anIncomingVerificationState(step = Step.Verifying(data = aDecimalsSessionVerificationData(), isWaiting = false)),
            anIncomingVerificationState(step = Step.Completed),
            anIncomingVerificationState(step = Step.Failure),
            anIncomingVerificationState(step = Step.Canceled),
            // Add other state here
        )
}

internal fun aStepInitial(
    isWaiting: Boolean = false,
) = Step.Initial(
    deviceDisplayName = "Element X Android",
    deviceId = DeviceId("ILAKNDNASDLK"),
    formattedSignInTime = "12:34",
    isWaiting = isWaiting,
)

internal fun anIncomingVerificationState(
    step: Step = aStepInitial(),
    eventSink: (IncomingVerificationViewEvents) -> Unit = {},
) = IncomingVerificationState(
    step = step,
    eventSink = eventSink,
)

/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ftue.impl.sessionverification.choosemode

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.logout.api.direct.aDirectLogoutState

class ChooseSelfVerificationModeStateProvider :
    PreviewParameterProvider<ChooseSelfVerificationModeState> {
    override val values = sequenceOf(
        aChooseSelfVerificationModeState(isLastDevice = true, canEnterRecoveryKey = true),
        aChooseSelfVerificationModeState(isLastDevice = true, canEnterRecoveryKey = false),
        aChooseSelfVerificationModeState(isLastDevice = false, canEnterRecoveryKey = true),
        aChooseSelfVerificationModeState(isLastDevice = false, canEnterRecoveryKey = false),
    )
}

fun aChooseSelfVerificationModeState(
    isLastDevice: Boolean = false,
    canEnterRecoveryKey: Boolean = true,
) = ChooseSelfVerificationModeState(
    isLastDevice = isLastDevice,
    canEnterRecoveryKey = canEnterRecoveryKey,
    directLogoutState = aDirectLogoutState(),
    eventSink = {},
)

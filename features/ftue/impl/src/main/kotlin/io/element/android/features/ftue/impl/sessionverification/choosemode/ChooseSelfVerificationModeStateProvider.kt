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
        aChooseSelfVerificationModeState(canUseAnotherDevice = false, canEnterRecoveryKey = true),
        aChooseSelfVerificationModeState(canUseAnotherDevice = false, canEnterRecoveryKey = false),
        aChooseSelfVerificationModeState(canUseAnotherDevice = true, canEnterRecoveryKey = true),
        aChooseSelfVerificationModeState(canUseAnotherDevice = true, canEnterRecoveryKey = false),
    )
}

fun aChooseSelfVerificationModeState(
    canUseAnotherDevice: Boolean = true,
    canEnterRecoveryKey: Boolean = true,
) = ChooseSelfVerificationModeState(
    canUseAnotherDevice = canUseAnotherDevice,
    canEnterRecoveryKey = canEnterRecoveryKey,
    directLogoutState = aDirectLogoutState(),
    eventSink = {},
)

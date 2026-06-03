/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ftue.impl.sessionverification.choosemode

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.logout.api.direct.aDirectLogoutState
import io.element.android.libraries.architecture.AsyncData

class ChooseSelfVerificationModeStateProvider :
    PreviewParameterProvider<ChooseSelfVerificationModeState> {
    override val values = sequenceOf(
        aChooseSelfVerificationModeState(
            buttonsState = AsyncData.Success(
                aButtonsState(canUseAnotherDevice = false, canUseRecoveryKey = true),
            ),
        ),
        aChooseSelfVerificationModeState(
            buttonsState = AsyncData.Success(
                aButtonsState(canUseAnotherDevice = false, canUseRecoveryKey = false),
            ),
        ),
        aChooseSelfVerificationModeState(
            buttonsState = AsyncData.Success(
                aButtonsState(canUseAnotherDevice = true, canUseRecoveryKey = true),
            ),
        ),
        aChooseSelfVerificationModeState(
            buttonsState = AsyncData.Success(
                aButtonsState(canUseAnotherDevice = true, canUseRecoveryKey = false),
            ),
        ),
        aChooseSelfVerificationModeState(
            buttonsState = AsyncData.Loading(),
        ),
    )
}

fun aChooseSelfVerificationModeState(
    buttonsState: AsyncData<ChooseSelfVerificationModeState.ButtonsState> = AsyncData.Success(aButtonsState()),
) = ChooseSelfVerificationModeState(
    buttonsState = buttonsState,
    directLogoutState = aDirectLogoutState(),
    eventSink = {},
)

fun aButtonsState(
    canUseAnotherDevice: Boolean = true,
    canUseRecoveryKey: Boolean = true,
) = ChooseSelfVerificationModeState.ButtonsState(
    canUseAnotherDevice = canUseAnotherDevice,
    canUseRecoveryKey = canUseRecoveryKey,
)

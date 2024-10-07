/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.lockscreen.impl.settings

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

open class LockScreenSettingsStateProvider : PreviewParameterProvider<LockScreenSettingsState> {
    override val values: Sequence<LockScreenSettingsState>
        get() = sequenceOf(
            aLockScreenSettingsState(),
            aLockScreenSettingsState(isLockMandatory = true),
            aLockScreenSettingsState(showRemovePinConfirmation = true),
        )
}

fun aLockScreenSettingsState(
    isLockMandatory: Boolean = false,
    isBiometricEnabled: Boolean = false,
    showRemovePinConfirmation: Boolean = false,
    showToggleBiometric: Boolean = true,
) = LockScreenSettingsState(
    showRemovePinOption = isLockMandatory,
    isBiometricEnabled = isBiometricEnabled,
    showRemovePinConfirmation = showRemovePinConfirmation,
    showToggleBiometric = showToggleBiometric,
    eventSink = {}
)

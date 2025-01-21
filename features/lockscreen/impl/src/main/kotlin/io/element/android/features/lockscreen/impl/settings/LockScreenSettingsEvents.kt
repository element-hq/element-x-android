/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.lockscreen.impl.settings

sealed interface LockScreenSettingsEvents {
    data object OnRemovePin : LockScreenSettingsEvents
    data object ConfirmRemovePin : LockScreenSettingsEvents
    data object CancelRemovePin : LockScreenSettingsEvents
    data object ToggleBiometricAllowed : LockScreenSettingsEvents
}

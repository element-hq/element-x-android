/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.securebackup.impl.setup

sealed interface SecureBackupSetupEvents {
    data object CreateRecoveryKey : SecureBackupSetupEvents
    data object RecoveryKeyHasBeenSaved : SecureBackupSetupEvents
    data object Done : SecureBackupSetupEvents
    data object DismissDialog : SecureBackupSetupEvents
}

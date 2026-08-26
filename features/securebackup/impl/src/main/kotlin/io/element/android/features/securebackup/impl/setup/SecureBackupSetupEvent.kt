/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securebackup.impl.setup

sealed interface SecureBackupSetupEvent {
    data object CreateRecoveryKey : SecureBackupSetupEvent
    data object RecoveryKeyHasBeenSaved : SecureBackupSetupEvent
    data object Done : SecureBackupSetupEvent
    data object DismissDialog : SecureBackupSetupEvent
}

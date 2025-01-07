/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securebackup.impl.enter

sealed interface SecureBackupEnterRecoveryKeyEvents {
    data class OnRecoveryKeyChange(val recoveryKey: String) : SecureBackupEnterRecoveryKeyEvents
    data object Submit : SecureBackupEnterRecoveryKeyEvents
    data object ClearDialog : SecureBackupEnterRecoveryKeyEvents
}

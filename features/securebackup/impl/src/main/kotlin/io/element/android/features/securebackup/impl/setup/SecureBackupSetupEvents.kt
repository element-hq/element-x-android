/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securebackup.impl.setup

sealed interface SecureBackupSetupEvents {
    data object CreateRecoveryKey : SecureBackupSetupEvents
    data object RecoveryKeyHasBeenSaved : SecureBackupSetupEvents
    data object Done : SecureBackupSetupEvents
    data object DismissDialog : SecureBackupSetupEvents

    /** Update the user-typed custom recovery passphrase. */
    data class UpdateCustomPassphrase(val value: String) : SecureBackupSetupEvents

    /** Update the user-typed confirmation field. */
    data class UpdateCustomPassphraseConfirm(val value: String) : SecureBackupSetupEvents

    /** Advance from the Entry step to the Confirm step. No-op unless the entry passphrase meets requirements. */
    data object ContinueCustomPassphrase : SecureBackupSetupEvents

    /** Step back from Confirm to Entry. Both typed values are preserved. */
    data object BackToCustomEntry : SecureBackupSetupEvents

    /** Submit the custom passphrase to the SDK (only valid when [SecureBackupSetupState.canSubmitCustomPassphrase]). */
    data object SubmitCustomPassphrase : SecureBackupSetupEvents

    /** Abort an in-flight custom submit (back press while creating) and return to the Confirm step. */
    data object CancelCustomPassphraseSubmit : SecureBackupSetupEvents
}

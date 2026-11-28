/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securebackup.impl.generate

sealed interface SecureBackupGenerateKeyEvent {
    data object CopyRecoveryKey : SecureBackupGenerateKeyEvent

    /** The user wants to carry on, which asks them to confirm that they have saved the key. */
    data object Continue : SecureBackupGenerateKeyEvent

    /** The user has confirmed that they have saved the key, so it can be activated. */
    data object ConfirmSaved : SecureBackupGenerateKeyEvent

    data object DismissConfirmation : SecureBackupGenerateKeyEvent

    data object DismissError : SecureBackupGenerateKeyEvent
}

/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securebackup.impl.generate

import io.element.android.libraries.architecture.AsyncAction

data class SecureBackupGenerateKeyState(
    val isReplacingExistingKey: Boolean,
    val recoveryKey: String,
    val showSaveConfirmationDialog: Boolean,
    val activateAction: AsyncAction<Unit>,
    val eventSink: (SecureBackupGenerateKeyEvent) -> Unit,
) {
    /** Whether the new key is saved and active, so the screen is only showing that. */
    val isCompleted: Boolean get() = activateAction.isSuccess()
}

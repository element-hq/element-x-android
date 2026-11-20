/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securebackup.impl.generate

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.AsyncAction

private const val A_RECOVERY_KEY = "EsTM njec uHYA yHmh dQdW Nj4o bNRU 9jMN XGMc KUNM UFr5 R8GY"

open class SecureBackupGenerateKeyStatePreviewParam : PreviewParameterProvider<SecureBackupGenerateKeyState> {
    override val values: Sequence<SecureBackupGenerateKeyState>
        get() = sequenceOf(
            aSecureBackupGenerateKeyState(),
            aSecureBackupGenerateKeyState(showSaveConfirmationDialog = true),
            aSecureBackupGenerateKeyState(activateAction = AsyncAction.Loading),
            aSecureBackupGenerateKeyState(activateAction = AsyncAction.Failure(Exception("Failed to activate"))),
            // The new key replaces an existing one
            aSecureBackupGenerateKeyState(isReplacingExistingKey = true, activateAction = AsyncAction.Success(Unit)),
            // The key is the first one
            aSecureBackupGenerateKeyState(isReplacingExistingKey = false, activateAction = AsyncAction.Success(Unit)),
        )
}

fun aSecureBackupGenerateKeyState(
    isReplacingExistingKey: Boolean = true,
    recoveryKey: String = A_RECOVERY_KEY,
    showSaveConfirmationDialog: Boolean = false,
    activateAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    eventSink: (SecureBackupGenerateKeyEvent) -> Unit = {},
) = SecureBackupGenerateKeyState(
    isReplacingExistingKey = isReplacingExistingKey,
    recoveryKey = recoveryKey,
    showSaveConfirmationDialog = showSaveConfirmationDialog,
    activateAction = activateAction,
    eventSink = eventSink,
)

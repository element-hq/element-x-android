/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securebackup.impl.setup

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.securebackup.impl.setup.views.RecoveryKeyUserStory
import io.element.android.features.securebackup.impl.setup.views.RecoveryKeyViewState
import io.element.android.features.securebackup.impl.setup.views.aFormattedRecoveryKey

open class SecureBackupSetupStateProvider : PreviewParameterProvider<SecureBackupSetupState> {
    override val values: Sequence<SecureBackupSetupState>
        get() = sequenceOf(
            aSecureBackupSetupState(setupState = SetupState.Init),
            aSecureBackupSetupState(setupState = SetupState.Creating),
            aSecureBackupSetupState(setupState = SetupState.Created(aFormattedRecoveryKey())),
            aSecureBackupSetupState(setupState = SetupState.CreatedAndSaved(aFormattedRecoveryKey())),
            aSecureBackupSetupState(
                setupState = SetupState.CreatedAndSaved(aFormattedRecoveryKey()),
                showSaveConfirmationDialog = true,
            ),
            aSecureBackupSetupState(setupState = SetupState.Error(Exception("Test error"))),
            // Add other states here
        )
}

fun aSecureBackupSetupState(
    setupState: SetupState = SetupState.Init,
    showSaveConfirmationDialog: Boolean = false,
) = SecureBackupSetupState(
    isChangeRecoveryKeyUserStory = false,
    setupState = setupState,
    showSaveConfirmationDialog = showSaveConfirmationDialog,
    recoveryKeyViewState = setupState.toRecoveryKeyViewState(),
    eventSink = {}
)

private fun SetupState.toRecoveryKeyViewState(): RecoveryKeyViewState {
    return RecoveryKeyViewState(
        recoveryKeyUserStory = RecoveryKeyUserStory.Setup,
        formattedRecoveryKey = recoveryKey(),
        displayTextFieldContents = true,
        inProgress = this is SetupState.Creating,
    )
}

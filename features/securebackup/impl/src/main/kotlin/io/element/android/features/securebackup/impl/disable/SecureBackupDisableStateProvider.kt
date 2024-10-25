/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.securebackup.impl.disable

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.encryption.BackupState

open class SecureBackupDisableStateProvider : PreviewParameterProvider<SecureBackupDisableState> {
    override val values: Sequence<SecureBackupDisableState>
        get() = sequenceOf(
            aSecureBackupDisableState(),
            aSecureBackupDisableState(disableAction = AsyncAction.ConfirmingNoParams),
            aSecureBackupDisableState(disableAction = AsyncAction.Loading),
            aSecureBackupDisableState(disableAction = AsyncAction.Failure(Exception("Failed to disable"))),
            // Add other states here
        )
}

fun aSecureBackupDisableState(
    backupState: BackupState = BackupState.UNKNOWN,
    disableAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
) = SecureBackupDisableState(
    backupState = backupState,
    disableAction = disableAction,
    appName = "Element",
    eventSink = {}
)

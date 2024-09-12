/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.securebackup.impl.disable

import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.encryption.BackupState

data class SecureBackupDisableState(
    val backupState: BackupState,
    val disableAction: AsyncAction<Unit>,
    val appName: String,
    val eventSink: (SecureBackupDisableEvents) -> Unit
)

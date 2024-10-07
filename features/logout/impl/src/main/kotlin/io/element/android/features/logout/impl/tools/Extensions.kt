/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.logout.impl.tools

import io.element.android.libraries.matrix.api.encryption.BackupUploadState
import io.element.android.libraries.matrix.api.encryption.SteadyStateException

internal fun BackupUploadState.isBackingUp(): Boolean {
    return when (this) {
        BackupUploadState.Waiting,
        is BackupUploadState.Uploading -> true
        // The backup is in progress, but there have been a network issue, so we have to warn the user.
        is BackupUploadState.SteadyException -> exception is SteadyStateException.Connection
        BackupUploadState.Unknown,
        BackupUploadState.Done,
        BackupUploadState.Error -> false
    }
}

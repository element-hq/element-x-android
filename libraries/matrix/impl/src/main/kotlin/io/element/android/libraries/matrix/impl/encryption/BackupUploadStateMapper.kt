/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.encryption

import io.element.android.libraries.matrix.api.encryption.BackupUploadState
import org.matrix.rustcomponents.sdk.BackupUploadState as RustBackupUploadState

class BackupUploadStateMapper {
    fun map(rustEnableProgress: RustBackupUploadState): BackupUploadState {
        return when (rustEnableProgress) {
            RustBackupUploadState.Done ->
                BackupUploadState.Done
            is RustBackupUploadState.Uploading -> {
                val backedUpCount = rustEnableProgress.backedUpCount.toInt()
                val totalCount = rustEnableProgress.totalCount.toInt()
                if (backedUpCount == totalCount) {
                    // Consider that the state is Done in this case,
                    // the SDK will not send a Done state
                    BackupUploadState.Done
                } else {
                    BackupUploadState.Uploading(
                        backedUpCount = backedUpCount,
                        totalCount = totalCount,
                    )
                }
            }
            RustBackupUploadState.Waiting ->
                BackupUploadState.Waiting
            RustBackupUploadState.Error ->
                BackupUploadState.Error
        }
    }
}

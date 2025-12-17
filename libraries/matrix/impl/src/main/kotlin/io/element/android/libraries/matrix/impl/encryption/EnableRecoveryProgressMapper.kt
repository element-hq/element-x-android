/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.encryption

import io.element.android.libraries.matrix.api.encryption.EnableRecoveryProgress
import org.matrix.rustcomponents.sdk.EnableRecoveryProgress as RustEnableRecoveryProgress

class EnableRecoveryProgressMapper {
    fun map(rustEnableProgress: RustEnableRecoveryProgress): EnableRecoveryProgress {
        return when (rustEnableProgress) {
            is RustEnableRecoveryProgress.Starting -> EnableRecoveryProgress.Starting
            is RustEnableRecoveryProgress.CreatingBackup -> EnableRecoveryProgress.CreatingBackup
            is RustEnableRecoveryProgress.CreatingRecoveryKey -> EnableRecoveryProgress.CreatingRecoveryKey
            is RustEnableRecoveryProgress.BackingUp -> EnableRecoveryProgress.BackingUp(
                backedUpCount = rustEnableProgress.backedUpCount.toInt(),
                totalCount = rustEnableProgress.totalCount.toInt(),
            )
            is RustEnableRecoveryProgress.RoomKeyUploadError -> EnableRecoveryProgress.RoomKeyUploadError
            is RustEnableRecoveryProgress.Done -> EnableRecoveryProgress.Done(
                recoveryKey = rustEnableProgress.recoveryKey
            )
        }
    }
}

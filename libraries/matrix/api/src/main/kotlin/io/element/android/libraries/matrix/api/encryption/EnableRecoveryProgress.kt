/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.encryption

sealed interface EnableRecoveryProgress {
    data object Starting : EnableRecoveryProgress
    data object CreatingBackup : EnableRecoveryProgress
    data object CreatingRecoveryKey : EnableRecoveryProgress
    data class BackingUp(val backedUpCount: Int, val totalCount: Int) : EnableRecoveryProgress
    data object RoomKeyUploadError : EnableRecoveryProgress
    data class Done(val recoveryKey: String) : EnableRecoveryProgress
}

/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.sync

import io.element.android.libraries.matrix.api.sync.SyncState
import org.matrix.rustcomponents.sdk.SyncServiceState

internal fun SyncServiceState.toSyncState(): SyncState {
    return when (this) {
        SyncServiceState.IDLE -> SyncState.Idle
        SyncServiceState.RUNNING -> SyncState.Running
        SyncServiceState.TERMINATED -> SyncState.Terminated
        SyncServiceState.ERROR -> SyncState.Error
        SyncServiceState.OFFLINE -> SyncState.Offline
    }
}

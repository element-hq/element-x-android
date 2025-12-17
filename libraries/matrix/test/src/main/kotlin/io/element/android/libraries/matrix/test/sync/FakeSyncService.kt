/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.test.sync

import io.element.android.libraries.core.coroutine.mapState
import io.element.android.libraries.matrix.api.sync.SyncService
import io.element.android.libraries.matrix.api.sync.SyncState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeSyncService(
    initialSyncState: SyncState = SyncState.Idle,
) : SyncService {
    private val syncStateFlow: MutableStateFlow<SyncState> = MutableStateFlow(initialSyncState)

    var startSyncLambda: () -> Result<Unit> = { Result.success(Unit) }
    override suspend fun startSync(): Result<Unit> {
        return startSyncLambda()
    }

    var stopSyncLambda: () -> Result<Unit> = { Result.success(Unit) }
    override suspend fun stopSync(): Result<Unit> {
        return stopSyncLambda()
    }

    override val syncState: StateFlow<SyncState> = syncStateFlow

    override val isOnline: StateFlow<Boolean> = syncState.mapState { it != SyncState.Offline }

    suspend fun emitSyncState(syncState: SyncState) {
        syncStateFlow.emit(syncState)
    }
}

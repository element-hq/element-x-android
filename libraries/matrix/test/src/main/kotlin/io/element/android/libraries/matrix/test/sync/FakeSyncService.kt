/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.test.sync

import io.element.android.libraries.matrix.api.sync.SyncService
import io.element.android.libraries.matrix.api.sync.SyncState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeSyncService(
    syncStateFlow: MutableStateFlow<SyncState> = MutableStateFlow(SyncState.Idle)
) : SyncService {
    var startSyncLambda: () -> Result<Unit> = { Result.success(Unit) }
    override suspend fun startSync(): Result<Unit> {
        return startSyncLambda()
    }

    var stopSyncLambda: () -> Result<Unit> = { Result.success(Unit) }
    override suspend fun stopSync(): Result<Unit> {
        return stopSyncLambda()
    }

    override val syncState: StateFlow<SyncState> = syncStateFlow
}

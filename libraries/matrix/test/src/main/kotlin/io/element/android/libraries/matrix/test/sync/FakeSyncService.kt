/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

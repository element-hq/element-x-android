/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.features.roomlist.impl.migration

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.roomlist.RoomListService
import javax.inject.Inject

class MigrationScreenPresenter @Inject constructor(
    private val matrixClient: MatrixClient,
    private val migrationScreenStore: MigrationScreenStore,
) : Presenter<MigrationScreenState> {
    @Composable
    override fun present(): MigrationScreenState {
        val roomListState by matrixClient.roomListService.state.collectAsState()
        var needsMigration by remember { mutableStateOf(migrationScreenStore.isMigrationScreenNeeded(matrixClient.sessionId)) }
        if (roomListState == RoomListService.State.Running) {
            LaunchedEffect(Unit) {
                needsMigration = false
                migrationScreenStore.setMigrationScreenShown(matrixClient.sessionId)
            }
        }
        return MigrationScreenState(
            isMigrating = needsMigration && roomListState != RoomListService.State.Running
        )
    }
}

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

package io.element.android.features.migration.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.element.android.features.api.MigrationState
import io.element.android.features.rageshake.api.logs.LogFilesRemover
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import javax.inject.Inject

class MigrationPresenter @Inject constructor(
    private val migrationStore: MigrationStore,
    private val logFilesRemover: LogFilesRemover,
) : Presenter<MigrationState> {
    @Composable
    override fun present(): MigrationState {
        val migrationStoreVersion = migrationStore.applicationMigrationVersion().collectAsState(initial = null)
        var migrationAction: AsyncData<Unit> by remember { mutableStateOf(AsyncData.Uninitialized) }

        // Uncomment this block to run the migration everytime
        /*
        LaunchedEffect(Unit) {
            migrationStore.setApplicationMigrationVersion(0)
        }
         */

        LaunchedEffect(migrationStoreVersion.value) {
            val migrationValue = migrationStoreVersion.value ?: return@LaunchedEffect
            if (migrationValue == MIGRATION_VERSION) {
                migrationAction = AsyncData.Success(Unit)
                return@LaunchedEffect
            }
            migrationAction = AsyncData.Loading(Unit)
            if (migrationValue < 1) {
                logFilesRemover.perform()
            }
            // Add new step here

            migrationStore.setApplicationMigrationVersion(MIGRATION_VERSION)
        }

        return MigrationState(
            migrationAction = migrationAction,
        )
    }

    companion object {
        // Increment this value when you need to run the migration again, and
        // add step in the LaunchedEffect above
        const val MIGRATION_VERSION = 1
    }
}

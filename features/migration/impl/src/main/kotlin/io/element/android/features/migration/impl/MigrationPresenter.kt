/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
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
import io.element.android.features.migration.impl.migrations.AppMigration
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.SingleIn
import timber.log.Timber
import javax.inject.Inject

@SingleIn(AppScope::class)
class MigrationPresenter @Inject constructor(
    private val migrationStore: MigrationStore,
    migrations: Set<@JvmSuppressWildcards AppMigration>,
) : Presenter<MigrationState> {
    private val orderedMigrations = migrations.sortedBy { it.order }
    private val lastMigration: Int = orderedMigrations.lastOrNull()?.order ?: 0

    @Composable
    override fun present(): MigrationState {
        val migrationStoreVersion by remember {
            migrationStore.applicationMigrationVersion()
        }.collectAsState(initial = null)
        var migrationAction: AsyncData<Unit> by remember { mutableStateOf(AsyncData.Uninitialized) }

        // Uncomment this block to run the migration everytime
//        LaunchedEffect(Unit) {
//            Timber.d("Resetting migration version to 0")
//            migrationStore.setApplicationMigrationVersion(0)
//        }

        LaunchedEffect(migrationStoreVersion) {
            val migrationValue = migrationStoreVersion ?: return@LaunchedEffect
            if (migrationValue == -1) {
                Timber.d("Fresh install, or previous installed application did not have the migration mechanism.")
            }
            if (migrationValue == lastMigration) {
                Timber.d("Current app migration version: $migrationValue. No migration needed.")
                migrationAction = AsyncData.Success(Unit)
                return@LaunchedEffect
            }
            migrationAction = AsyncData.Loading(Unit)
            val nextMigration = orderedMigrations.firstOrNull { it.order > migrationValue }
            if (nextMigration != null) {
                Timber.d("Current app migration version: $migrationValue. Applying migration: ${nextMigration.order}")
                nextMigration.migrate()
                migrationStore.setApplicationMigrationVersion(nextMigration.order)
            }
        }

        return MigrationState(
            migrationAction = migrationAction,
        )
    }
}

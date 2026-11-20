/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.migration.impl

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.element.android.features.migration.api.AppMigrationRunner
import io.element.android.features.migration.impl.migrations.AppMigration
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import timber.log.Timber

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DefaultAppMigrationRunner(
    private val migrationStore: MigrationStore,
    migrations: Set<@JvmSuppressWildcards AppMigration>,
    private val coroutineDispatchers: CoroutineDispatchers,
) : AppMigrationRunner {
    private val orderedMigrations = migrations.sortedBy { it.order }
    private val lastMigration: Int = orderedMigrations.lastOrNull()?.order ?: 0
    private val mutex = Mutex()

    @Volatile
    private var isDone = false

    override suspend fun hasPendingMigrations(): Boolean {
        if (isDone) return false
        return migrationStore.applicationMigrationVersion().first() != lastMigration
    }

    override suspend fun runPendingMigrations() {
        if (isDone) return
        mutex.withLock {
            if (isDone) return
            val currentVersion = migrationStore.applicationMigrationVersion().first()
            val isFreshInstall = currentVersion == -1
            if (isFreshInstall) {
                Timber.d("Fresh install, or previous installed application did not have the migration mechanism.")
            }
            if (currentVersion == lastMigration) {
                Timber.d("Current app migration version: $currentVersion. No migration needed.")
            } else {
                orderedMigrations
                    .filter { it.order > currentVersion }
                    .forEach { migration ->
                        Timber.d("Current app migration version: $currentVersion. Applying migration: ${migration.order}")
                        // Migrations can perform IO (database, files), do not run them on the main thread.
                        withContext(coroutineDispatchers.io) {
                            migration.migrate(isFreshInstall)
                        }
                        migrationStore.setApplicationMigrationVersion(migration.order)
                    }
            }
            isDone = true
        }
    }
}

/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

// Modified by Feral: repairSkippedMigrations() — see below.

package io.element.android.libraries.sessionstorage.impl.di

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.QueryResult
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.sessionstorage.impl.SessionDatabase
import io.element.encrypteddb.SqlCipherDriverFactory
import io.element.encrypteddb.passphrase.RandomDatabaseSecretProvider
import io.element.encrypteddb.utils.ReplaceDatabaseKey
import timber.log.Timber

@BindingContainer
@ContributesTo(AppScope::class)
object SessionStorageModule {
    @Provides
    @SingleIn(AppScope::class)
    fun provideMatrixDatabase(
        @ApplicationContext context: Context,
    ): SessionDatabase {
        val name = "session_database"
        val secretFile = context.getDatabasePath("$name.key")

        // Make sure the parent directory of the key file exists, otherwise it will crash in older Android versions
        val parentDir = secretFile.parentFile
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs()
        }

        val rekeyMigrationVersion = 11L
        val passphraseProvider = RandomDatabaseSecretProvider(context, secretFile)
        val driver = SqlCipherDriverFactory(passphraseProvider)
            .create(
                schema = SessionDatabase.Schema,
                name = "$name.db",
                context = context,
            ) { db, oldVersion, newVersion ->
                Timber.d("Migrating $name database from version $oldVersion to $newVersion")
                if (rekeyMigrationVersion in oldVersion..newVersion) {
                    ReplaceDatabaseKey(passphraseProvider).replaceKey(name, db)
                }
            }

        repairSkippedMigrations(driver)
        return SessionDatabase(driver)
    }

    /**
     * Modified by Feral: self-heal a session database whose SQLDelight migrations were skipped.
     *
     * Upstream's SqlCipherDriverFactory did not run `schema.migrate()` on upgrade (fixed in the
     * Feral fork), but Android still stamped the new `user_version` — so a database upgraded by
     * an affected build (Feral 26.08.2 / 26.08.3 over 25.05.4) claims to be current while still
     * having the old columns, and every query on `position` crashes at startup. Detect the real
     * schema level from the columns and apply the missing migrations (9 → adds position…,
     * 10 → drops slidingSyncProxy, 11 → no-op) before anything reads the table.
     */
    private fun repairSkippedMigrations(driver: SqlDriver) {
        val columns = driver.executeQuery(
            identifier = null,
            sql = "PRAGMA table_info(SessionData)",
            mapper = { cursor ->
                val names = mutableListOf<String>()
                while (cursor.next().value) {
                    cursor.getString(1)?.let(names::add)
                }
                QueryResult.Value(names.toList())
            },
            parameters = 0,
        ).value
        if (columns.isEmpty()) return // no table yet: fresh database, nothing to repair
        val effectiveVersion = when {
            "position" !in columns -> 9L
            "slidingSyncProxy" in columns -> 10L
            else -> return
        }
        val target = SessionDatabase.Schema.version
        Timber.w("SessionData schema is at level $effectiveVersion but the database claims $target: applying skipped migrations")
        SessionDatabase.Schema.migrate(driver, effectiveVersion, target).value
        driver.execute(identifier = null, sql = "PRAGMA user_version = $target", parameters = 0).value
    }
}

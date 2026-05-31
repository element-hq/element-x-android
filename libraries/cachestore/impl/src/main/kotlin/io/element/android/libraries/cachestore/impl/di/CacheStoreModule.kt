/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.cachestore.impl.di

import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.element.android.libraries.cachestore.impl.CacheDatabase
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.encrypteddb.SqlCipherDriverFactory
import io.element.encrypteddb.passphrase.RandomDatabaseSecretProvider
import io.element.encrypteddb.utils.ReplaceDatabaseKey
import timber.log.Timber

@BindingContainer
@ContributesTo(AppScope::class)
object CacheStoreModule {
    @Provides
    @SingleIn(AppScope::class)
    fun provideCacheDatabase(
        @ApplicationContext context: Context,
    ): CacheDatabase {
        val name = "cache_database"
        val secretFile = context.getDatabasePath("$name.key")

        // Make sure the parent directory of the key file exists, otherwise it will crash in older Android versions
        val parentDir = secretFile.parentFile
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs()
        }

        val rekeyMigrationVersion = 2L
        val passphraseProvider = RandomDatabaseSecretProvider(context, secretFile)
        val driver = SqlCipherDriverFactory(passphraseProvider)
            .create(
                schema = CacheDatabase.Schema,
                name = "$name.db",
                context = context
            ) { db, oldVersion, newVersion ->
                Timber.d("Migrating $name database from version $oldVersion to $newVersion")
                if (rekeyMigrationVersion in oldVersion..newVersion) {
                    ReplaceDatabaseKey(passphraseProvider).replaceKey(name, db)
                }
            }

        return CacheDatabase(driver)
    }
}

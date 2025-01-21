/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.migration.impl.migrations

import com.squareup.anvil.annotations.ContributesMultibinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.CacheDirectory
import io.element.android.libraries.sessionstorage.api.SessionStore
import java.io.File
import javax.inject.Inject

/**
 * Create the cache directory for the existing sessions.
 */
@ContributesMultibinding(AppScope::class)
class AppMigration06 @Inject constructor(
    private val sessionStore: SessionStore,
    @CacheDirectory private val cacheDirectory: File,
) : AppMigration {
    override val order: Int = 6

    override suspend fun migrate() {
        val allSessions = sessionStore.getAllSessions()
        for (session in allSessions) {
            if (session.cachePath.isEmpty()) {
                val sessionFile = File(session.sessionPath)
                val sessionFolder = sessionFile.name
                val cachePath = File(cacheDirectory, sessionFolder)
                sessionStore.updateData(session.copy(cachePath = cachePath.absolutePath))
                // Move existing cache files
                listOf(
                    "matrix-sdk-event-cache.sqlite3",
                    "matrix-sdk-event-cache.sqlite3-shm",
                    "matrix-sdk-event-cache.sqlite3-wal",
                ).map { fileName ->
                    File(sessionFile, fileName)
                }.takeIf { files ->
                    files.all { it.exists() }
                }?.forEach { cacheFile ->
                    val targetFile = File(cachePath, cacheFile.name)
                    cacheFile.copyTo(targetFile)
                    cacheFile.delete()
                }
            }
        }
    }
}

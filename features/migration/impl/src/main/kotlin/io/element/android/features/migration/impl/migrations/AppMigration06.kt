/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.migration.impl.migrations

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import io.element.android.libraries.di.CacheDirectory
import io.element.android.libraries.sessionstorage.api.SessionStore
import java.io.File

/**
 * Create the cache directory for the existing sessions.
 */
@ContributesIntoSet(AppScope::class)
@Inject
class AppMigration06(
    private val sessionStore: SessionStore,
    @CacheDirectory private val cacheDirectory: File,
) : AppMigration {
    override val order: Int = 6

    override suspend fun migrate(isFreshInstall: Boolean) {
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

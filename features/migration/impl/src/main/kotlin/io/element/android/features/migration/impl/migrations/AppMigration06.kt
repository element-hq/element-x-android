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

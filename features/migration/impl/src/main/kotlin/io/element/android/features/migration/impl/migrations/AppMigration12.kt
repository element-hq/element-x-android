/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.migration.impl.migrations

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import io.element.android.libraries.core.extensions.isUuid
import io.element.android.libraries.di.CacheDirectory
import io.element.android.libraries.di.SessionsCacheDirectory
import io.element.android.libraries.sessionstorage.api.SessionStore
import timber.log.Timber
import java.io.File

/**
 * Move the cache directories of the sessions from the root of the cache directory to the sessions cache directory,
 * so that it mirrors the organization of the session file directories.
 */
@ContributesIntoSet(AppScope::class)
class AppMigration12(
    private val sessionStore: SessionStore,
    @CacheDirectory private val cacheDirectory: File,
    @SessionsCacheDirectory private val sessionsCacheDirectory: File,
) : AppMigration {
    override val order: Int = 12

    override suspend fun migrate(isFreshInstall: Boolean) {
        if (isFreshInstall) return
        sessionsCacheDirectory.mkdirs()
        // Move the session cache directories, which are named with a UUID.
        cacheDirectory.listFiles()
            .orEmpty()
            .filter { it.isDirectory && it.name.isUuid() }
            .forEach { directory ->
                val target = File(sessionsCacheDirectory, directory.name)
                if (!directory.renameTo(target)) {
                    // It only contains cache data, which will be recreated by the SDK
                    Timber.w("Failed to move the cache directory ${directory.name}, deleting it")
                    directory.deleteRecursively()
                }
            }
        // Update the cache path of the sessions
        val cacheDirectoryPath = cacheDirectory.canonicalPath
        sessionStore.getAllSessions()
            .filter { it.cachePath.isNotEmpty() && File(it.cachePath).parentFile?.canonicalPath == cacheDirectoryPath }
            .forEach { session ->
                val newCachePath = File(sessionsCacheDirectory, File(session.cachePath).name)
                sessionStore.updateData(session.copy(cachePath = newCachePath.absolutePath))
            }
    }
}

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
import io.element.android.libraries.sessionstorage.api.SessionData
import io.element.android.libraries.sessionstorage.api.SessionStore
import timber.log.Timber
import java.io.File
import java.util.UUID

/**
 * Rename the directories (files and cache) of the sessions created by old versions of the application, which are named
 * after the user id (see [AppMigration05] and [AppMigration06]), to use a UUID, like the directories of the new sessions.
 */
@ContributesIntoSet(AppScope::class)
class AppMigration11(
    private val sessionStore: SessionStore,
) : AppMigration {
    override val order: Int = 11

    override suspend fun migrate(isFreshInstall: Boolean) {
        if (isFreshInstall) return
        sessionStore.getAllSessions()
            .filter { it.sessionPath.isNotEmpty() && !File(it.sessionPath).name.isUuid() }
            .forEach { migrateSession(it) }
    }

    private suspend fun migrateSession(session: SessionData) {
        // Use a deterministic UUID, so that if the migration is interrupted after moving the directories but before
        // updating the session, the next run will use the same target.
        val newName = UUID.nameUUIDFromBytes(session.userId.toByteArray()).toString()
        val filesDirectory = File(session.sessionPath)
        val newFilesDirectory = File(filesDirectory.parentFile, newName)
        if (!filesDirectory.moveIfExists(newFilesDirectory)) {
            // It contains the crypto store, never delete it, keep using the current name.
            Timber.e("Failed to rename the files directory of a session")
            return
        }
        val cacheDirectory = File(session.cachePath)
        val newCachePath = if (session.cachePath.isNotEmpty() && !cacheDirectory.name.isUuid()) {
            val newCacheDirectory = File(cacheDirectory.parentFile, newName)
            if (!cacheDirectory.moveIfExists(newCacheDirectory)) {
                // It only contains cache data, which will be recreated by the SDK
                Timber.w("Failed to rename the cache directory of a session, deleting it")
                cacheDirectory.deleteRecursively()
            }
            newCacheDirectory.absolutePath
        } else {
            session.cachePath
        }
        sessionStore.updateData(
            session.copy(
                sessionPath = newFilesDirectory.absolutePath,
                cachePath = newCachePath,
            )
        )
    }

    /**
     * Return true if this directory has been moved to [target], or if it does not exist (for instance if it has
     * already been moved by a previous run of the migration).
     */
    private fun File.moveIfExists(target: File): Boolean {
        return !exists() || renameTo(target)
    }
}

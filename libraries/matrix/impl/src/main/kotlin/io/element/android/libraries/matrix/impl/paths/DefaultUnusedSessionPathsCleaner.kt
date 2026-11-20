/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.paths

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.di.SessionsCacheDirectory
import io.element.android.libraries.di.SessionsFilesDirectory
import io.element.android.libraries.matrix.api.paths.UnusedSessionPathsCleaner
import io.element.android.libraries.sessionstorage.api.SessionStore
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

@ContributesBinding(AppScope::class)
class DefaultUnusedSessionPathsCleaner(
    private val sessionStore: SessionStore,
    @SessionsFilesDirectory private val sessionsFilesDirectory: File,
    @SessionsCacheDirectory private val sessionsCacheDirectory: File,
    private val sessionPathsFactory: SessionPathsFactory,
    private val coroutineDispatchers: CoroutineDispatchers,
) : UnusedSessionPathsCleaner {
    override suspend fun clean() {
        withContext(coroutineDispatchers.io) {
            cleanInternal()
        }
    }

    private suspend fun cleanInternal() {
        runCatchingExceptions {
            val usedPaths = buildSet {
                sessionStore.getAllSessions().forEach { sessionData ->
                    add(sessionData.sessionPath)
                    add(sessionData.cachePath)
                }
                sessionPathsFactory.pendingPaths.forEach { sessionPaths ->
                    add(sessionPaths.fileDirectory.path)
                    add(sessionPaths.cacheDirectory.path)
                }
            }
                .filter { it.isNotEmpty() }
                .map { File(it).canonicalPath }
                .toSet()
            sessionsFilesDirectory.deleteUnusedDirectories(usedPaths)
            sessionsCacheDirectory.deleteUnusedDirectories(usedPaths)
        }.onFailure {
            Timber.e(it, "Failed to clean the unused session directories")
        }
    }

    private fun File.deleteUnusedDirectories(usedPaths: Set<String>) {
        listFiles()
            .orEmpty()
            .filter { it.isDirectory && it.canonicalPath !in usedPaths }
            .forEach {
                Timber.w("Deleting unused session directory ${it.name} from $name")
                it.deleteRecursively()
            }
    }
}

/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.paths

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.element.android.libraries.di.SessionsCacheDirectory
import io.element.android.libraries.di.SessionsFilesDirectory
import io.element.android.libraries.matrix.api.paths.SessionPaths
import java.io.File
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@SingleIn(AppScope::class)
@Inject
class SessionPathsFactory(
    @SessionsFilesDirectory private val sessionsFilesDirectory: File,
    @SessionsCacheDirectory private val sessionsCacheDirectory: File,
) {
    private val _pendingPaths = ConcurrentHashMap.newKeySet<SessionPaths>()

    /**
     * The session paths which have been created and which are used by a client, but which are not (yet) referenced
     * by a session in the session store. Their directories must not be deleted by [DefaultUnusedSessionPathsCleaner].
     */
    val pendingPaths: Set<SessionPaths> get() = _pendingPaths.toSet()

    fun create(): SessionPaths {
        val subPath = UUID.randomUUID().toString()
        return SessionPaths(
            fileDirectory = File(sessionsFilesDirectory, subPath),
            cacheDirectory = File(sessionsCacheDirectory, subPath),
        ).also { _pendingPaths.add(it) }
    }

    /**
     * To be called when the session paths are not pending anymore, i.e. when they are now referenced by
     * a session in the session store, or when their directories have been deleted.
     */
    fun release(sessionPaths: SessionPaths) {
        _pendingPaths.remove(sessionPaths)
    }

    /**
     * Delete the directories of the session paths, and release them.
     */
    fun delete(sessionPaths: SessionPaths) {
        sessionPaths.deleteRecursively()
        release(sessionPaths)
    }
}

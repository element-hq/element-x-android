/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.migration.impl.migrations

import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Named
import dev.zacsweers.metro.AppScope
import io.element.android.libraries.di.BaseDirectory
import io.element.android.libraries.sessionstorage.api.SessionStore
import java.io.File

@ContributesIntoSet(AppScope::class)
@Inject
class AppMigration05(
    private val sessionStore: SessionStore,
    @Named("baseDirectory") private val baseDirectory: File,
) : AppMigration {
    override val order: Int = 5

    override suspend fun migrate() {
        val allSessions = sessionStore.getAllSessions()
        for (session in allSessions) {
            if (session.sessionPath.isEmpty()) {
                val sessionPath = File(baseDirectory, session.userId.replace(':', '_')).absolutePath
                sessionStore.updateData(session.copy(sessionPath = sessionPath))
            }
        }
    }
}

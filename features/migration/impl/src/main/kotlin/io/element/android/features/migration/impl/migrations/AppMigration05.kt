/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.migration.impl.migrations

import com.squareup.anvil.annotations.ContributesMultibinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.sessionstorage.api.SessionStore
import java.io.File
import javax.inject.Inject

@ContributesMultibinding(AppScope::class)
class AppMigration05 @Inject constructor(
    private val sessionStore: SessionStore,
    private val baseDirectory: File,
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

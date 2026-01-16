/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.migration.impl.migrations

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import io.element.android.libraries.matrix.api.MatrixClientProvider
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.sessionstorage.api.SessionStore

/**
 * Ensure we clear the well-known cached config, since it could be invalid due to an SDK issue.
 */
@ContributesIntoSet(AppScope::class)
@Inject
class AppMigration09(
    private val sessionStore: SessionStore,
    private val matrixClientProvider: MatrixClientProvider,
) : AppMigration {
    override val order: Int = 9

    override suspend fun migrate(isFreshInstall: Boolean) {
        if (isFreshInstall) return

        val sessions = sessionStore.getAllSessions()

        for (session in sessions) {
            val client = matrixClientProvider.getOrRestore(SessionId(session.userId)).getOrNull() ?: continue
            client.resetWellKnownConfig()
        }
    }
}

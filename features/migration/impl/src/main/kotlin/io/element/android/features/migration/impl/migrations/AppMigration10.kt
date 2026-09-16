/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.migration.impl.migrations

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.sessionstorage.api.SessionStore
import io.element.android.libraries.workmanager.api.WorkManagerRequestType
import io.element.android.libraries.workmanager.api.WorkManagerScheduler

/**
 * Remove existing fetch notifications work manager requests since their format has changed.
 */
@ContributesIntoSet(AppScope::class)
class AppMigration10(
    private val sessionStore: SessionStore,
    private val workManagerScheduler: WorkManagerScheduler,
) : AppMigration {
    override val order: Int = 10

    override suspend fun migrate(isFreshInstall: Boolean) {
        if (isFreshInstall) return

        val sessions = sessionStore.getAllSessions()

        for (session in sessions) {
            workManagerScheduler.cancel(
                sessionId = SessionId(session.userId),
                requestType = WorkManagerRequestType.NOTIFICATION_SYNC
            )
        }
    }
}

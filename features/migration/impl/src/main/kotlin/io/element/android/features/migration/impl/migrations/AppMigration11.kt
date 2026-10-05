/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.migration.impl.migrations

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import io.element.android.features.announcement.api.Announcement
import io.element.android.features.announcement.api.AnnouncementService
import io.element.android.libraries.sessionstorage.api.SessionStore

/**
 * Ensure the multi-account announcement is displayed, but only on application upgrade,
 * and only if the user does not already have several accounts.
 */
@ContributesIntoSet(AppScope::class)
class AppMigration11(
    private val sessionStore: SessionStore,
    private val announcementService: AnnouncementService,
) : AppMigration {
    override val order: Int = 11

    override suspend fun migrate(isFreshInstall: Boolean) {
        if (isFreshInstall) return

        if (sessionStore.getAllSessions().size > 1) {
            // The user already uses several accounts, no need to announce the feature
            announcementService.onAnnouncementDismissed(Announcement.MultiAccount)
        } else {
            announcementService.showAnnouncement(Announcement.MultiAccount)
        }
    }
}

/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.migration.impl.migrations

import io.element.android.features.announcement.api.Announcement
import io.element.android.features.rageshake.test.logs.FakeAnnouncementService
import io.element.android.libraries.sessionstorage.test.InMemorySessionStore
import io.element.android.libraries.sessionstorage.test.aSessionData
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import kotlinx.coroutines.test.runTest
import org.junit.Test

class AppMigration11Test {
    @Test
    fun `migration on fresh install should not invoke the AnnouncementService`() = runTest {
        val service = FakeAnnouncementService()
        val migration = AppMigration11(
            sessionStore = InMemorySessionStore(initialList = listOf(aSessionData())),
            announcementService = service,
        )
        // FakeAnnouncementService lambdas throw by default
        migration.migrate(isFreshInstall = true)
    }

    @Test
    fun `migration on upgrade with a single account should show the announcement`() = runTest {
        val showAnnouncementResult = lambdaRecorder<Announcement, Unit> { }
        val service = FakeAnnouncementService(
            showAnnouncementResult = showAnnouncementResult,
        )
        val migration = AppMigration11(
            sessionStore = InMemorySessionStore(initialList = listOf(aSessionData())),
            announcementService = service,
        )
        migration.migrate(isFreshInstall = false)
        showAnnouncementResult.assertions().isCalledOnce()
            .with(value(Announcement.MultiAccount))
    }

    @Test
    fun `migration on upgrade with several accounts should mark the announcement as dismissed`() = runTest {
        val onAnnouncementDismissedResult = lambdaRecorder<Announcement, Unit> { }
        val service = FakeAnnouncementService(
            onAnnouncementDismissedResult = onAnnouncementDismissedResult,
        )
        val migration = AppMigration11(
            sessionStore = InMemorySessionStore(
                initialList = listOf(
                    aSessionData(sessionId = "@alice:server.org"),
                    aSessionData(sessionId = "@bob:server.org"),
                )
            ),
            announcementService = service,
        )
        migration.migrate(isFreshInstall = false)
        onAnnouncementDismissedResult.assertions().isCalledOnce()
            .with(value(Announcement.MultiAccount))
    }
}

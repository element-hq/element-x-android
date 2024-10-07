/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.migration.impl.migrations

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.sessionstorage.impl.memory.InMemorySessionStore
import io.element.android.libraries.sessionstorage.test.aSessionData
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.File

class AppMigration06Test {
    @Test
    fun `empty cache path should be set to an expected path`() = runTest {
        val sessionStore = InMemorySessionStore().apply {
            updateData(
                aSessionData(
                    sessionId = A_SESSION_ID.value,
                    sessionPath = "/a/path/to/a/session/AN_ID",
                    cachePath = "",
                )
            )
        }
        val migration = AppMigration06(sessionStore = sessionStore, cacheDirectory = File("/a/path/cache"))
        migration.migrate()
        val storedData = sessionStore.getSession(A_SESSION_ID.value)!!
        assertThat(storedData.cachePath).isEqualTo("/a/path/cache/AN_ID")
    }

    @Test
    fun `non empty cache path should not be impacted by the migration`() = runTest {
        val sessionStore = InMemorySessionStore().apply {
            updateData(
                aSessionData(
                    sessionId = A_SESSION_ID.value,
                    cachePath = "/a/path/existing",
                )
            )
        }
        val migration = AppMigration05(sessionStore = sessionStore, baseDirectory = File("/a/path/cache"))
        migration.migrate()
        val storedData = sessionStore.getSession(A_SESSION_ID.value)!!
        assertThat(storedData.cachePath).isEqualTo("/a/path/existing")
    }
}

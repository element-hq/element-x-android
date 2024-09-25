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

class AppMigration05Test {
    @Test
    fun `empty session path should be set to an expected path`() = runTest {
        val sessionStore = InMemorySessionStore().apply {
            updateData(
                aSessionData(
                    sessionId = A_SESSION_ID.value,
                    sessionPath = "",
                )
            )
        }
        val migration = AppMigration05(sessionStore = sessionStore, baseDirectory = File("/a/path"))
        migration.migrate()
        val storedData = sessionStore.getSession(A_SESSION_ID.value)!!
        assertThat(storedData.sessionPath).isEqualTo("/a/path/${A_SESSION_ID.value.replace(':', '_')}")
    }

    @Test
    fun `non empty session path should not be impacted by the migration`() = runTest {
        val sessionStore = InMemorySessionStore().apply {
            updateData(
                aSessionData(
                    sessionId = A_SESSION_ID.value,
                    sessionPath = "/a/path/existing",
                )
            )
        }
        val migration = AppMigration05(sessionStore = sessionStore, baseDirectory = File("/a/path"))
        migration.migrate()
        val storedData = sessionStore.getSession(A_SESSION_ID.value)!!
        assertThat(storedData.sessionPath).isEqualTo("/a/path/existing")
    }
}

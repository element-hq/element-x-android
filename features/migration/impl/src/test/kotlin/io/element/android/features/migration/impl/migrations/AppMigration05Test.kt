/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
                    sessionId = A_SESSION_ID,
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
                    sessionId = A_SESSION_ID,
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

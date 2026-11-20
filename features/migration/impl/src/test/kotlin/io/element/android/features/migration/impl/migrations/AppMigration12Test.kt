/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.migration.impl.migrations

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID_2
import io.element.android.libraries.sessionstorage.api.SessionData
import io.element.android.libraries.sessionstorage.test.InMemorySessionStore
import io.element.android.libraries.sessionstorage.test.aSessionData
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.util.UUID

class AppMigration12Test {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private lateinit var cacheDirectory: File
    private lateinit var sessionsCacheDirectory: File

    @Before
    fun setup() {
        cacheDirectory = temporaryFolder.newFolder("cache")
        sessionsCacheDirectory = File(cacheDirectory, "sessions")
    }

    @Test
    fun `migration on fresh install does nothing`() = runTest {
        val sessionCacheDirectory = aSessionCacheDirectory(A_UUID)
        val session = aSessionData(cachePath = sessionCacheDirectory.absolutePath)
        val sessionStore = InMemorySessionStore(initialList = listOf(session))

        createMigration(sessionStore).migrate(isFreshInstall = true)

        assertThat(sessionCacheDirectory.exists()).isTrue()
        assertThat(sessionsCacheDirectory.exists()).isFalse()
        assertThat(sessionStore.getAllSessions()).containsExactly(session)
    }

    @Test
    fun `session cache directory is moved and the session is updated`() = runTest {
        val sessionCacheDirectory = aSessionCacheDirectory(A_UUID)
        val session = aSessionData(
            sessionId = A_SESSION_ID.value,
            sessionPath = "/files/sessions/$A_UUID",
            cachePath = sessionCacheDirectory.absolutePath,
        )
        val sessionStore = InMemorySessionStore(initialList = listOf(session))

        createMigration(sessionStore).migrate(isFreshInstall = false)

        val expectedDirectory = File(sessionsCacheDirectory, A_UUID)
        assertThat(sessionCacheDirectory.exists()).isFalse()
        assertThat(File(expectedDirectory, A_FILE_NAME).readText()).isEqualTo(A_FILE_CONTENT)
        val storedSession = sessionStore.getSession(A_SESSION_ID.value)!!
        assertThat(storedSession.cachePath).isEqualTo(expectedDirectory.absolutePath)
        assertThat(storedSession.sessionPath).isEqualTo("/files/sessions/$A_UUID")
    }

    @Test
    fun `other files and directories of the cache directory are not moved`() = runTest {
        val logs = File(cacheDirectory, "logs").apply { mkdirs() }
        val temp = File(cacheDirectory, "temp").apply { mkdirs() }
        val aFile = File(cacheDirectory, A_UUID_2).apply { writeText(A_FILE_CONTENT) }

        createMigration(InMemorySessionStore()).migrate(isFreshInstall = false)

        assertThat(logs.exists()).isTrue()
        assertThat(temp.exists()).isTrue()
        assertThat(aFile.exists()).isTrue()
        assertThat(sessionsCacheDirectory.list().orEmpty()).isEmpty()
    }

    @Test
    fun `cache directory not referenced by a session is moved too`() = runTest {
        aSessionCacheDirectory(A_UUID)

        createMigration(InMemorySessionStore()).migrate(isFreshInstall = false)

        assertThat(File(sessionsCacheDirectory, A_UUID).exists()).isTrue()
    }

    @Test
    fun `session is updated even if its cache directory does not exist`() = runTest {
        val session = aSessionData(cachePath = File(cacheDirectory, A_UUID).absolutePath)
        val sessionStore = InMemorySessionStore(initialList = listOf(session))

        createMigration(sessionStore).migrate(isFreshInstall = false)

        assertThat(sessionStore.getAllSessions().single().cachePath).isEqualTo(File(sessionsCacheDirectory, A_UUID).absolutePath)
    }

    @Test
    fun `migration is idempotent`() = runTest {
        aSessionCacheDirectory(A_UUID)
        val alreadyMigratedDirectory = File(sessionsCacheDirectory, A_UUID_2).apply { mkdirs() }
        val sessionStore = InMemorySessionStore(
            initialList = listOf(
                aSessionData(sessionId = A_SESSION_ID.value, cachePath = File(cacheDirectory, A_UUID).absolutePath),
                aSessionData(sessionId = A_SESSION_ID_2.value, cachePath = alreadyMigratedDirectory.absolutePath),
            )
        )
        val migration = createMigration(sessionStore)

        migration.migrate(isFreshInstall = false)
        val sessionsAfterFirstRun = sessionStore.getAllSessions()
        migration.migrate(isFreshInstall = false)

        assertThat(sessionStore.getAllSessions()).isEqualTo(sessionsAfterFirstRun)
        assertThat(sessionsAfterFirstRun.map(SessionData::cachePath)).containsExactly(
            File(sessionsCacheDirectory, A_UUID).absolutePath,
            alreadyMigratedDirectory.absolutePath,
        )
        assertThat(sessionsCacheDirectory.list().orEmpty().toList()).containsExactly(A_UUID, A_UUID_2)
    }

    @Test
    fun `legacy session ends up in the sessions directories after AppMigration11`() = runTest {
        // Name used by old versions of the application, see AppMigration05 and AppMigration06
        val legacyUserId = "@alice:server.org"
        val legacyName = "@alice_server.org"
        val sessionsFilesDirectory = temporaryFolder.newFolder("files", "sessions")
        val sessionStore = InMemorySessionStore(
            initialList = listOf(
                aSessionData(
                    sessionId = legacyUserId,
                    sessionPath = File(sessionsFilesDirectory, legacyName).apply { mkdirs() }.absolutePath,
                    cachePath = aSessionCacheDirectory(legacyName).absolutePath,
                )
            )
        )

        AppMigration11(sessionStore).migrate(isFreshInstall = false)
        createMigration(sessionStore).migrate(isFreshInstall = false)

        val expectedName = UUID.nameUUIDFromBytes(legacyUserId.toByteArray()).toString()
        val storedSession = sessionStore.getAllSessions().single()
        assertThat(storedSession.sessionPath).isEqualTo(File(sessionsFilesDirectory, expectedName).absolutePath)
        assertThat(storedSession.cachePath).isEqualTo(File(sessionsCacheDirectory, expectedName).absolutePath)
        assertThat(File(storedSession.cachePath, A_FILE_NAME).readText()).isEqualTo(A_FILE_CONTENT)
    }

    private fun aSessionCacheDirectory(name: String) = File(cacheDirectory, name).apply {
        mkdirs()
        File(this, A_FILE_NAME).writeText(A_FILE_CONTENT)
    }

    private fun createMigration(sessionStore: InMemorySessionStore) = AppMigration12(
        sessionStore = sessionStore,
        cacheDirectory = cacheDirectory,
        sessionsCacheDirectory = sessionsCacheDirectory,
    )

    private companion object {
        const val A_UUID = "123e4567-e89b-12d3-a456-426614174000"
        const val A_UUID_2 = "123e4567-e89b-12d3-a456-426614174001"
        const val A_FILE_NAME = "matrix-sdk-event-cache.sqlite3"
        const val A_FILE_CONTENT = "content"
    }
}

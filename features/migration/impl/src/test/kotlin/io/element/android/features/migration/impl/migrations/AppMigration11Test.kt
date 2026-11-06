/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.migration.impl.migrations

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.core.extensions.isUuid
import io.element.android.libraries.sessionstorage.test.InMemorySessionStore
import io.element.android.libraries.sessionstorage.test.aSessionData
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.util.UUID

class AppMigration11Test {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private lateinit var cacheDirectory: File
    private lateinit var sessionsFilesDirectory: File

    @Before
    fun setup() {
        cacheDirectory = temporaryFolder.newFolder("cache")
        sessionsFilesDirectory = temporaryFolder.newFolder("files", "sessions")
    }

    @Test
    fun `migration on fresh install does nothing`() = runTest {
        val session = aLegacySession()
        val sessionStore = InMemorySessionStore(initialList = listOf(session))

        AppMigration11(sessionStore).migrate(isFreshInstall = true)

        assertThat(File(session.sessionPath).exists()).isTrue()
        assertThat(File(session.cachePath).exists()).isTrue()
        assertThat(sessionStore.getAllSessions()).containsExactly(session)
    }

    @Test
    fun `legacy session directories are renamed to a UUID`() = runTest {
        val session = aLegacySession()
        val sessionStore = InMemorySessionStore(initialList = listOf(session))

        AppMigration11(sessionStore).migrate(isFreshInstall = false)

        val expectedFilesDirectory = File(sessionsFilesDirectory, A_DERIVED_UUID)
        val expectedCacheDirectory = File(cacheDirectory, A_DERIVED_UUID)
        assertThat(A_DERIVED_UUID.isUuid()).isTrue()
        assertThat(File(session.sessionPath).exists()).isFalse()
        assertThat(File(session.cachePath).exists()).isFalse()
        assertThat(File(expectedFilesDirectory, A_FILE_NAME).readText()).isEqualTo(A_FILE_CONTENT)
        assertThat(File(expectedCacheDirectory, A_FILE_NAME).readText()).isEqualTo(A_FILE_CONTENT)
        val storedSession = sessionStore.getAllSessions().single()
        assertThat(storedSession.sessionPath).isEqualTo(expectedFilesDirectory.absolutePath)
        assertThat(storedSession.cachePath).isEqualTo(expectedCacheDirectory.absolutePath)
    }

    @Test
    fun `session already using UUID directories is not changed`() = runTest {
        val session = aSessionData(
            sessionPath = aDirectory(sessionsFilesDirectory, A_UUID).absolutePath,
            cachePath = aDirectory(cacheDirectory, A_UUID).absolutePath,
        )
        val sessionStore = InMemorySessionStore(initialList = listOf(session))

        AppMigration11(sessionStore).migrate(isFreshInstall = false)

        assertThat(File(session.sessionPath).exists()).isTrue()
        assertThat(File(session.cachePath).exists()).isTrue()
        assertThat(sessionStore.getAllSessions()).containsExactly(session)
    }

    @Test
    fun `interrupted migration is completed by the next run`() = runTest {
        val session = aLegacySession()
        // Simulate a previous run which has moved the directories, but has not updated the session
        File(session.sessionPath).renameTo(File(sessionsFilesDirectory, A_DERIVED_UUID))
        File(session.cachePath).renameTo(File(cacheDirectory, A_DERIVED_UUID))
        val sessionStore = InMemorySessionStore(initialList = listOf(session))

        AppMigration11(sessionStore).migrate(isFreshInstall = false)

        val storedSession = sessionStore.getAllSessions().single()
        assertThat(File(storedSession.sessionPath, A_FILE_NAME).readText()).isEqualTo(A_FILE_CONTENT)
        assertThat(File(storedSession.cachePath, A_FILE_NAME).readText()).isEqualTo(A_FILE_CONTENT)
        assertThat(File(storedSession.sessionPath).name).isEqualTo(A_DERIVED_UUID)
        assertThat(File(storedSession.cachePath).name).isEqualTo(A_DERIVED_UUID)
    }

    @Test
    fun `legacy session without cache directory on disk is migrated`() = runTest {
        val session = aLegacySession()
        File(session.cachePath).deleteRecursively()
        val sessionStore = InMemorySessionStore(initialList = listOf(session))

        AppMigration11(sessionStore).migrate(isFreshInstall = false)

        val storedSession = sessionStore.getAllSessions().single()
        assertThat(storedSession.sessionPath).isEqualTo(File(sessionsFilesDirectory, A_DERIVED_UUID).absolutePath)
        assertThat(storedSession.cachePath).isEqualTo(File(cacheDirectory, A_DERIVED_UUID).absolutePath)
    }

    @Test
    fun `session with an empty session path is not changed`() = runTest {
        val session = aSessionData(sessionPath = "", cachePath = "")
        val sessionStore = InMemorySessionStore(initialList = listOf(session))

        AppMigration11(sessionStore).migrate(isFreshInstall = false)

        assertThat(sessionStore.getAllSessions()).containsExactly(session)
    }

    private fun aLegacySession() = aSessionData(
        sessionId = A_LEGACY_USER_ID,
        sessionPath = aDirectory(sessionsFilesDirectory, A_LEGACY_NAME).absolutePath,
        cachePath = aDirectory(cacheDirectory, A_LEGACY_NAME).absolutePath,
    )

    private fun aDirectory(parent: File, name: String) = File(parent, name).apply {
        mkdirs()
        File(this, A_FILE_NAME).writeText(A_FILE_CONTENT)
    }

    private companion object {
        const val A_LEGACY_USER_ID = "@alice:server.org"
        const val A_LEGACY_NAME = "@alice_server.org"
        val A_DERIVED_UUID = UUID.nameUUIDFromBytes(A_LEGACY_USER_ID.toByteArray()).toString()
        const val A_UUID = "123e4567-e89b-12d3-a456-426614174000"
        const val A_FILE_NAME = "matrix-sdk-state.sqlite3"
        const val A_FILE_CONTENT = "content"
    }
}

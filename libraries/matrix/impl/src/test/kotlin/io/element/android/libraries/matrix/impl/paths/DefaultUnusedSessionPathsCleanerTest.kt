/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.paths

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.sessionstorage.api.SessionData
import io.element.android.libraries.sessionstorage.test.InMemorySessionStore
import io.element.android.libraries.sessionstorage.test.aSessionData
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.util.UUID

class DefaultUnusedSessionPathsCleanerTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private lateinit var sessionsFilesDirectory: File
    private lateinit var sessionsCacheDirectory: File

    @Before
    fun setup() {
        sessionsFilesDirectory = temporaryFolder.newFolder("files", "sessions")
        sessionsCacheDirectory = temporaryFolder.newFolder("cache", "sessions")
    }

    @Test
    fun `directories used by a session are kept, unused ones are deleted`() = runTest {
        val usedName = aUuid()
        val unusedName = aUuid()
        val usedFiles = File(sessionsFilesDirectory, usedName).apply { mkdirs() }
        val usedCache = File(sessionsCacheDirectory, usedName).apply { mkdirs() }
        val unusedFiles = File(sessionsFilesDirectory, unusedName).apply { mkdirs() }
        val unusedCache = File(sessionsCacheDirectory, unusedName).apply { mkdirs() }
        // A ghost directory containing only a search index
        val ghostFiles = File(sessionsFilesDirectory, "${aUuid()}/search-index/!room:domain").apply { mkdirs() }.parentFile!!.parentFile!!
        val sut = createCleaner(
            sessions = listOf(aSessionData(sessionPath = usedFiles.absolutePath, cachePath = usedCache.absolutePath)),
        )

        sut.clean()

        assertThat(usedFiles.exists()).isTrue()
        assertThat(usedCache.exists()).isTrue()
        assertThat(unusedFiles.exists()).isFalse()
        assertThat(unusedCache.exists()).isFalse()
        assertThat(ghostFiles.exists()).isFalse()
    }

    @Test
    fun `files are not deleted`() = runTest {
        val aFile = File(sessionsFilesDirectory, "aFile").apply { writeText("content") }
        val aCacheFile = File(sessionsCacheDirectory, "aFile").apply { writeText("content") }
        val sut = createCleaner()

        sut.clean()

        assertThat(aFile.exists()).isTrue()
        assertThat(aCacheFile.exists()).isTrue()
    }

    @Test
    fun `directories of pending session paths are kept`() = runTest {
        val sessionPathsFactory = SessionPathsFactory(sessionsFilesDirectory, sessionsCacheDirectory)
        val pendingPaths = sessionPathsFactory.create()
        pendingPaths.fileDirectory.mkdirs()
        pendingPaths.cacheDirectory.mkdirs()
        val sut = createCleaner(sessionPathsFactory = sessionPathsFactory)

        sut.clean()

        assertThat(pendingPaths.fileDirectory.exists()).isTrue()
        assertThat(pendingPaths.cacheDirectory.exists()).isTrue()

        // Once released, the directories are not protected anymore
        sessionPathsFactory.release(pendingPaths)
        sut.clean()

        assertThat(pendingPaths.fileDirectory.exists()).isFalse()
        assertThat(pendingPaths.cacheDirectory.exists()).isFalse()
    }

    @Test
    fun `when there is no session, all the session directories are deleted`() = runTest {
        val files = List(3) { File(sessionsFilesDirectory, aUuid()).apply { mkdirs() } }
        val sut = createCleaner()

        sut.clean()

        files.forEach { assertThat(it.exists()).isFalse() }
        assertThat(sessionsFilesDirectory.exists()).isTrue()
    }

    @Test
    fun `missing base directory does not crash`() = runTest {
        sessionsFilesDirectory.deleteRecursively()
        val sut = createCleaner()

        sut.clean()

        assertThat(sessionsFilesDirectory.exists()).isFalse()
    }

    private fun aUuid() = UUID.randomUUID().toString()

    private fun TestScope.createCleaner(
        sessions: List<SessionData> = emptyList(),
        sessionPathsFactory: SessionPathsFactory = SessionPathsFactory(sessionsFilesDirectory, sessionsCacheDirectory),
    ) = DefaultUnusedSessionPathsCleaner(
        sessionStore = InMemorySessionStore(initialList = sessions),
        sessionsFilesDirectory = sessionsFilesDirectory,
        sessionsCacheDirectory = sessionsCacheDirectory,
        sessionPathsFactory = sessionPathsFactory,
        coroutineDispatchers = testCoroutineDispatchers(),
    )
}

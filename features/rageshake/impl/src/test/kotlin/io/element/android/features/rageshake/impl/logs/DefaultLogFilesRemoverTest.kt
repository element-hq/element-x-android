/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rageshake.impl.logs

import com.google.common.truth.Truth.assertThat
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class DefaultLogFilesRemoverTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun `perform deletes the log files matching the predicate`() = runTest {
        val cacheDirectory = temporaryFolder.newFolder("cache")
        val logDirectory = File(cacheDirectory, "logs").apply { mkdirs() }
        val matchingLog = File(logDirectory, "logs.2026-11-04-10.log").apply { writeText("log") }
        val otherLog = File(logDirectory, "other.txt").apply { writeText("log") }
        val matchingLogInSubfolder = File(logDirectory, "subfolder/logs.2026-11-04-11.log").apply {
            parentFile!!.mkdirs()
            writeText("log")
        }
        // A file outside the log directory must not be deleted
        val matchingFileOutsideLogs = File(cacheDirectory, "logs.2026-11-04-12.log").apply { writeText("log") }
        val sut = DefaultLogFilesRemover(
            cacheDirectory = cacheDirectory,
            coroutineDispatchers = testCoroutineDispatchers(),
        )

        sut.perform { it.name.startsWith("logs.") }

        assertThat(matchingLog.exists()).isFalse()
        assertThat(matchingLogInSubfolder.exists()).isFalse()
        assertThat(otherLog.exists()).isTrue()
        assertThat(matchingFileOutsideLogs.exists()).isTrue()
    }

    @Test
    fun `perform does nothing if there is no log directory`() = runTest {
        val cacheDirectory = temporaryFolder.newFolder("cache")
        val sut = DefaultLogFilesRemover(
            cacheDirectory = cacheDirectory,
            coroutineDispatchers = testCoroutineDispatchers(),
        )

        sut.perform { true }

        assertThat(File(cacheDirectory, "logs").exists()).isFalse()
    }
}

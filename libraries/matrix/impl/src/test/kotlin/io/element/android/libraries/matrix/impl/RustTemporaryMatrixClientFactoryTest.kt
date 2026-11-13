/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.impl.paths.SessionPathsFactory
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class RustTemporaryMatrixClientFactoryTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun `create returns a TemporaryMatrixClient`() = runTest {
        val sut = createRustTemporaryMatrixClientFactory()
        val result = sut.create("https://matrix.org")
        assertThat(result.isSuccess).isTrue()
    }

    @Test
    fun `create can fail gracefully`() = runTest {
        val sut = createRustTemporaryMatrixClientFactory(
            rustMatrixClientFactory = createRustMatrixClientFactory(
                clientBuilderProvider = FakeClientBuilderProvider(
                    provideResult = { throw IllegalStateException("Failed to create client builder") }
                )
            )
        )
        val result = sut.create("https://matrix.org")
        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun `closing the TemporaryMatrixClient deletes and releases its session paths`() = runTest {
        val sessionPathsFactory = SessionPathsFactory(temporaryFolder.newFolder("base"), temporaryFolder.newFolder("cache"))
        val sut = createRustTemporaryMatrixClientFactory(sessionPathsFactory = sessionPathsFactory)
        val client = sut.create("https://matrix.org").getOrThrow()
        val sessionPaths = sessionPathsFactory.pendingPaths.single()
        // Simulate the SDK creating the directories
        sessionPaths.fileDirectory.mkdirs()
        sessionPaths.cacheDirectory.mkdirs()

        client.close()

        assertThat(sessionPaths.fileDirectory.exists()).isFalse()
        assertThat(sessionPaths.cacheDirectory.exists()).isFalse()
        assertThat(sessionPathsFactory.pendingPaths).isEmpty()
    }

    @Test
    fun `failing to create the client releases its session paths`() = runTest {
        val sessionPathsFactory = SessionPathsFactory(temporaryFolder.newFolder("base"), temporaryFolder.newFolder("cache"))
        val sut = createRustTemporaryMatrixClientFactory(
            sessionPathsFactory = sessionPathsFactory,
            rustMatrixClientFactory = createRustMatrixClientFactory(
                clientBuilderProvider = FakeClientBuilderProvider(
                    provideResult = { throw IllegalStateException("Failed to create client builder") }
                )
            ),
        )

        assertThat(sut.create("https://matrix.org").isFailure).isTrue()
        assertThat(sessionPathsFactory.pendingPaths).isEmpty()
    }

    private fun TestScope.createRustTemporaryMatrixClientFactory(
        sessionPathsFactory: SessionPathsFactory = SessionPathsFactory(File("/base"), File("/cache")),
        rustMatrixClientFactory: RustMatrixClientFactory = createRustMatrixClientFactory(),
    ): RustTemporaryMatrixClientFactory {
        return RustTemporaryMatrixClientFactory(
            sessionPathsFactory = sessionPathsFactory,
            rustMatrixClientFactory = rustMatrixClientFactory,
            coroutineDispatchers = testCoroutineDispatchers(),
        )
    }
}

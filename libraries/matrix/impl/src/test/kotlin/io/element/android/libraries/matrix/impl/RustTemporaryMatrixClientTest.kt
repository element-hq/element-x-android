/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.exception.ClientException
import io.element.android.libraries.matrix.api.paths.SessionPaths
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiClient
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.matrix.rustcomponents.sdk.Client
import java.io.File
import kotlin.io.path.createTempDirectory
import org.matrix.rustcomponents.sdk.ClientException as RustClientException

class RustTemporaryMatrixClientTest {
    @Test
    fun `getUrl method uses client getUrl`() = runTest {
        val paths = SessionPaths(
            fileDirectory = File("files"),
            cacheDirectory = File("cache"),
        )

        val getUrlRecorder = lambdaRecorder<String, ByteArray> { ByteArray(0) }
        val client = createRustTemporaryMatrixClient(
            client = FakeFfiClient(
                getUrlResult = getUrlRecorder,
            ),
            paths = paths,
        )
        val url = "https://example.com"

        client.getUrl(url)

        getUrlRecorder.assertions().isCalledOnce().with(value(url))
    }

    @Test
    fun `getUrl method can fail gracefully and map errors to public ones`() = runTest {
        val paths = SessionPaths(
            fileDirectory = File("files"),
            cacheDirectory = File("cache"),
        )

        val getUrlRecorder = lambdaRecorder<String, ByteArray> { throw RustClientException.Generic("Error", null) }
        val client = createRustTemporaryMatrixClient(
            client = FakeFfiClient(
                getUrlResult = getUrlRecorder,
            ),
            paths = paths,
        )
        val url = "https://example.com"

        val result = client.getUrl(url)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(ClientException.Generic::class.java)
    }

    @Test
    fun `close deletes the directories if they exist`() = runTest {
        val paths = SessionPaths(
            fileDirectory = createTempDirectory().toFile(),
            cacheDirectory = createTempDirectory().toFile(),
        )

        val client = createRustTemporaryMatrixClient(paths = paths)

        assertThat(paths.fileDirectory.exists()).isTrue()
        assertThat(paths.cacheDirectory.exists()).isTrue()

        client.close()

        assertThat(paths.fileDirectory.exists()).isFalse()
        assertThat(paths.cacheDirectory.exists()).isFalse()
    }

    private fun createRustTemporaryMatrixClient(
        client: Client = FakeFfiClient(),
        paths: SessionPaths,
    ): RustTemporaryMatrixClient {
        return RustTemporaryMatrixClient(client, paths)
    }
}

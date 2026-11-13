/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.exception.ClientException
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiClient
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.matrix.rustcomponents.sdk.Client
import org.matrix.rustcomponents.sdk.ClientException as RustClientException

class RustTemporaryMatrixClientTest {
    @Test
    fun `getUrl method uses client getUrl`() = runTest {
        val getUrlRecorder = lambdaRecorder<String, ByteArray> { ByteArray(0) }
        val client = createRustTemporaryMatrixClient(
            client = FakeFfiClient(
                getUrlResult = getUrlRecorder,
            ),
        )
        val url = "https://example.com"

        client.getUrl(url)

        getUrlRecorder.assertions().isCalledOnce().with(value(url))
    }

    @Test
    fun `getUrl method can fail gracefully and map errors to public ones`() = runTest {
        val getUrlRecorder = lambdaRecorder<String, ByteArray> { throw RustClientException.Generic("Error", null) }
        val client = createRustTemporaryMatrixClient(
            client = FakeFfiClient(
                getUrlResult = getUrlRecorder,
            ),
        )
        val url = "https://example.com"

        val result = client.getUrl(url)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(ClientException.Generic::class.java)
    }

    @Test
    fun `close closes the client and invokes onClose`() = runTest {
        val closeResult = lambdaRecorder<Unit> {}
        val onClose = lambdaRecorder<Unit> {}
        val client = createRustTemporaryMatrixClient(
            client = FakeFfiClient(closeResult = closeResult),
            onClose = onClose,
        )

        client.close()

        closeResult.assertions().isCalledOnce()
        onClose.assertions().isCalledOnce()
    }

    private fun TestScope.createRustTemporaryMatrixClient(
        client: Client = FakeFfiClient(),
        onClose: () -> Unit = {},
    ): RustTemporaryMatrixClient {
        return RustTemporaryMatrixClient(client, StandardTestDispatcher(testScheduler), onClose)
    }
}

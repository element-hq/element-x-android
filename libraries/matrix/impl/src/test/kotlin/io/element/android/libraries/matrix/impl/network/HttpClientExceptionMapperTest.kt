/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.network

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.network.HttpClientException
import org.junit.Test
import org.matrix.rustcomponents.sdk.HttpClientException as RustHttpClientException

class HttpClientExceptionMapperTest {
    @Test
    fun `GetUrl errors keep the status code`() {
        val error = RustHttpClientException.GetUrl(msg = "Not found", statusCode = 404u, details = null)
        val result = error.mapHttpClientException()
        assertThat(result.statusCode).isEqualTo(404)
        assertThat(result.message).isEqualTo("Not found")
        assertThat(result.cause).isEqualTo(error)
    }

    @Test
    fun `GetUrl errors without a status code are mapped`() {
        val error = RustHttpClientException.GetUrl(msg = "Connection refused", statusCode = null, details = null)
        val result = error.mapHttpClientException()
        assertThat(result.statusCode).isNull()
        assertThat(result.cause).isEqualTo(error)
    }

    @Test
    fun `other SDK errors have no status code`() {
        val error = RustHttpClientException.FailedToFormatUrl(url = "not a url", msg = "Invalid URL")
        val result = error.mapHttpClientException()
        assertThat(result.statusCode).isNull()
        assertThat(result.cause).isEqualTo(error)
    }

    @Test
    fun `an HttpClientException is returned as is`() {
        val error = HttpClientException("Already mapped", statusCode = 500)
        assertThat(error.mapHttpClientException()).isSameInstanceAs(error)
    }

    @Test
    fun `other errors are wrapped`() {
        val error = IllegalStateException("Boom")
        val result = error.mapHttpClientException()
        assertThat(result.message).isEqualTo("Boom")
        assertThat(result.statusCode).isNull()
        assertThat(result.cause).isEqualTo(error)
    }
}

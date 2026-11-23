/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.di

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.network.HttpClient
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.network.FakeHttpClient
import io.element.android.libraries.matrix.test.network.FakeHttpClientFactory
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import org.junit.Test

class SessionMatrixBindingContainerTest {
    @Test
    fun `providesHttpClient creates a client for the homeserver and server of the session`() {
        val httpClient = FakeHttpClient()
        val createLambda = lambdaRecorder<String, String?, HttpClient> { _, _ -> httpClient }
        val result = SessionMatrixBindingContainer.providesHttpClient(
            matrixClient = FakeMatrixClient(server = "server.org", homeserverUrl = "https://matrix.server.org"),
            httpClientFactory = FakeHttpClientFactory(createResult = createLambda),
        )
        assertThat(result).isSameInstanceAs(httpClient)
        createLambda.assertions().isCalledOnce().with(value("https://matrix.server.org"), value("server.org"))
    }
}

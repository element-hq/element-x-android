/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.libraries.matrix.impl.call

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.matrix.rustcomponents.sdk.ElementCallWellKnown
import org.matrix.rustcomponents.sdk.ElementWellKnown

class DefaultElementCallBaseUrlProviderTest {
    @Test
    fun `provides returns null when getUrl returns an error`() = runTest {
        val userIdServerNameLambda = lambdaRecorder<String> { "example.com" }
        val getUrlLambda = lambdaRecorder<String, Result<String>> { _ ->
            Result.failure(AN_EXCEPTION)
        }
        val sut = DefaultElementCallBaseUrlProvider(
            FakeElementWellKnownParser(
                Result.success(createElementWellKnown(""))
            )
        )
        val matrixClient = FakeMatrixClient(
            userIdServerNameLambda = userIdServerNameLambda,
            getUrlLambda = getUrlLambda,
        )
        val result = sut.provides(matrixClient)
        assertThat(result).isNull()
        userIdServerNameLambda.assertions().isCalledOnce()
        getUrlLambda.assertions().isCalledOnce()
            .with(value("https://example.com/.well-known/element/element.json"))
    }

    @Test
    fun `provides returns null when content parsing fails`() = runTest {
        val userIdServerNameLambda = lambdaRecorder<String> { "example.com" }
        val getUrlLambda = lambdaRecorder<String, Result<String>> { _ ->
            Result.success("""{"call":{"widget_url":"https://example.com/call"}}""")
        }
        val sut = DefaultElementCallBaseUrlProvider(
            createFakeElementWellKnownParser(
                Result.failure(AN_EXCEPTION)
            )
        )
        val matrixClient = FakeMatrixClient(
            userIdServerNameLambda = userIdServerNameLambda,
            getUrlLambda = getUrlLambda,
        )
        val result = sut.provides(matrixClient)
        assertThat(result).isNull()
        userIdServerNameLambda.assertions().isCalledOnce()
        getUrlLambda.assertions().isCalledOnce()
            .with(value("https://example.com/.well-known/element/element.json"))
    }

    @Test
    fun `provides returns value when getUrl returns correct content`() = runTest {
        val userIdServerNameLambda = lambdaRecorder<String> { "example.com" }
        val getUrlLambda = lambdaRecorder<String, Result<String>> { _ ->
            Result.success("""{"call":{"widget_url":"https://example.com/call"}}""")
        }
        val sut = DefaultElementCallBaseUrlProvider(
            createFakeElementWellKnownParser(
                Result.success(createElementWellKnown("aUrl"))
            )
        )
        val matrixClient = FakeMatrixClient(
            userIdServerNameLambda = userIdServerNameLambda,
            getUrlLambda = getUrlLambda,
        )
        val result = sut.provides(matrixClient)
        assertThat(result).isEqualTo("aUrl")
        userIdServerNameLambda.assertions().isCalledOnce()
        getUrlLambda.assertions().isCalledOnce()
            .with(value("https://example.com/.well-known/element/element.json"))
    }

    private fun createFakeElementWellKnownParser(result: Result<ElementWellKnown>): FakeElementWellKnownParser {
        return FakeElementWellKnownParser(result)
    }

    private fun createElementWellKnown(widgetUrl: String): ElementWellKnown {
        return ElementWellKnown(
            call = ElementCallWellKnown(
                widgetUrl = widgetUrl
            )
        )
    }
}

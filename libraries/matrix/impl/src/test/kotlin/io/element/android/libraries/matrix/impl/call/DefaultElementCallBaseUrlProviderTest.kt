/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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

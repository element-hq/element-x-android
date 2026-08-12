/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.resolver

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.test.auth.FakeHomeServerLoginCompatibilityChecker
import io.element.android.libraries.permissions.test.FakeLocalNetworkPermissionAdvisor
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test

class HomeserverResolverTest {
    @Test
    fun `resolve - a server name is used as is`() = runTest {
        val checkResult = lambdaRecorder<String, Result<Boolean>> { Result.success(it == "https://example.org") }
        val sut = createHomeserverResolver(checkResult)
        sut.resolve("example.org").test {
            assertThat(awaitItem()).isEqualTo(listOf(HomeserverData(homeserverUrl = "https://example.org")))
            awaitComplete()
        }
        checkResult.assertions().isCalledExactly(1)
            .withSequence(
                listOf(value("https://example.org")),
            )
    }

    @Test
    fun `resolve - an url is used as is`() = runTest {
        val checkResult = lambdaRecorder<String, Result<Boolean>> { Result.success(it == "https://example.org") }
        val sut = createHomeserverResolver(checkResult)
        sut.resolve("https://example.org").test {
            assertThat(awaitItem()).isEqualTo(listOf(HomeserverData(homeserverUrl = "https://example.org")))
            awaitComplete()
        }
        checkResult.assertions().isCalledExactly(1)
            .withSequence(
                listOf(value("https://example.org")),
            )
    }

    @Test
    fun `resolve - a user id resolves the server name of the user id`() = runTest {
        val checkResult = lambdaRecorder<String, Result<Boolean>> { Result.success(it == "https://example.org") }
        val sut = createHomeserverResolver(checkResult)
        sut.resolve("@alice:example.org").test {
            assertThat(awaitItem()).isEqualTo(listOf(HomeserverData(homeserverUrl = "https://example.org")))
            awaitComplete()
        }
        checkResult.assertions().isCalledExactly(1)
            .withSequence(
                listOf(value("https://example.org")),
            )
    }

    @Test
    fun `resolve - a user id with a port resolves the server name of the user id`() = runTest {
        val checkResult = lambdaRecorder<String, Result<Boolean>> { Result.success(it == "https://example.org:8448") }
        val sut = createHomeserverResolver(checkResult)
        sut.resolve("@alice:example.org:8448").test {
            assertThat(awaitItem()).isEqualTo(listOf(HomeserverData(homeserverUrl = "https://example.org:8448")))
            awaitComplete()
        }
        checkResult.assertions().isCalledExactly(1)
            .withSequence(
                listOf(value("https://example.org:8448")),
            )
    }

    @Test
    fun `resolve - an incomplete user id does not resolve anything`() = runTest {
        val checkResult = lambdaRecorder<String, Result<Boolean>> { Result.success(true) }
        val sut = createHomeserverResolver(checkResult)
        sut.resolve("@alice").test {
            awaitComplete()
        }
        sut.resolve("@alice:").test {
            awaitComplete()
        }
        checkResult.assertions().isNeverCalled()
    }

    @Test
    fun `resolve - a user id with a too short server name does not resolve anything`() = runTest {
        val checkResult = lambdaRecorder<String, Result<Boolean>> { Result.success(true) }
        val sut = createHomeserverResolver(checkResult)
        sut.resolve("@alice:a.b").test {
            awaitComplete()
        }
        checkResult.assertions().isNeverCalled()
    }

    private fun TestScope.createHomeserverResolver(
        checkResult: (String) -> Result<Boolean>,
    ) = HomeserverResolver(
        dispatchers = testCoroutineDispatchers(),
        homeServerLoginCompatibilityChecker = FakeHomeServerLoginCompatibilityChecker(checkResult = checkResult),
        localNetworkPermissionAdvisor = FakeLocalNetworkPermissionAdvisor(),
    )
}

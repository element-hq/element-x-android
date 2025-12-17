/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.searchaccountprovider

import com.google.common.truth.Truth.assertThat
import io.element.android.features.login.impl.changeserver.aChangeServerState
import io.element.android.features.login.impl.resolver.HomeserverResolver
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.test.auth.FakeHomeServerLoginCompatibilityChecker
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import io.element.android.tests.testutils.test
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class SearchAccountProviderPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val fakeLoginCompatibilityChecker = FakeHomeServerLoginCompatibilityChecker(checkResult = { Result.success(true) })
        val presenter = SearchAccountProviderPresenter(
            homeserverResolver = HomeserverResolver(testCoroutineDispatchers(), fakeLoginCompatibilityChecker),
            changeServerPresenter = { aChangeServerState() }
        )
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.userInput).isEmpty()
            assertThat(initialState.userInputResult).isEqualTo(AsyncData.Uninitialized)
        }
    }

    @Test
    fun `present - error while checking login compatibility`() = runTest {
        val fakeLoginCompatibilityChecker = FakeHomeServerLoginCompatibilityChecker(checkResult = { Result.failure(IllegalStateException("Oops")) })
        val presenter = SearchAccountProviderPresenter(
            homeserverResolver = HomeserverResolver(testCoroutineDispatchers(), fakeLoginCompatibilityChecker),
            changeServerPresenter = { aChangeServerState() }
        )
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(SearchAccountProviderEvents.UserInput("https://test.org"))
            val withInputState = awaitItem()
            assertThat(withInputState.userInput).isEqualTo("https://test.org")
            assertThat(initialState.userInputResult).isEqualTo(AsyncData.Uninitialized)
            assertThat(awaitItem().userInputResult).isInstanceOf(AsyncData.Loading::class.java)
            assertThat(awaitItem().userInputResult).isEqualTo(
                AsyncData.Success(
                    listOf(
                        aHomeserverData(homeserverUrl = "https://test.org")
                    )
                )
            )
        }
    }

    @Test
    fun `present - enter text no result`() = runTest {
        val fakeWellknownRetriever = FakeHomeServerLoginCompatibilityChecker(checkResult = { Result.success(false) })
        val presenter = SearchAccountProviderPresenter(
            homeserverResolver = HomeserverResolver(testCoroutineDispatchers(), fakeWellknownRetriever),
            changeServerPresenter = { aChangeServerState() }
        )
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(SearchAccountProviderEvents.UserInput("test"))
            val withInputState = awaitItem()
            assertThat(withInputState.userInput).isEqualTo("test")
            assertThat(initialState.userInputResult).isEqualTo(AsyncData.Uninitialized)
            assertThat(awaitItem().userInputResult).isInstanceOf(AsyncData.Loading::class.java)
            assertThat(awaitItem().userInputResult).isEqualTo(
                AsyncData.Success(
                    listOf(
                        aHomeserverData(homeserverUrl = "https://test"),
                    )
                )
            )
        }
    }

    @Test
    fun `present - enter text one result with wellknown`() = runTest {
        val checkResult = lambdaRecorder<String, Result<Boolean>> {
            when (it) {
                "https://test.org" -> Result.success(false)
                "https://test.com" -> Result.success(false)
                "https://test.io" -> Result.success(true)
                "https://test" -> Result.success(false)
                else -> error("should not happen")
            }
        }
        val fakeLoginCompatibilityChecker = FakeHomeServerLoginCompatibilityChecker(checkResult = checkResult)
        val presenter = SearchAccountProviderPresenter(
            homeserverResolver = HomeserverResolver(testCoroutineDispatchers(), fakeLoginCompatibilityChecker),
            changeServerPresenter = { aChangeServerState() }
        )
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(SearchAccountProviderEvents.UserInput("test"))
            val withInputState = awaitItem()
            assertThat(withInputState.userInput).isEqualTo("test")
            assertThat(initialState.userInputResult).isEqualTo(AsyncData.Uninitialized)
            assertThat(awaitItem().userInputResult).isInstanceOf(AsyncData.Loading::class.java)
            assertThat(awaitItem().userInputResult).isEqualTo(
                AsyncData.Success(
                    listOf(
                        aHomeserverData(homeserverUrl = "https://test.io")
                    )
                )
            )
            checkResult.assertions().isCalledExactly(4)
                .withSequence(
                    listOf(value("https://test.org")),
                    listOf(value("https://test.com")),
                    listOf(value("https://test.io")),
                    listOf(value("https://test")),
                )
        }
    }

    @Test
    fun `present - enter text two results with wellknown`() = runTest {
        val checkResult = lambdaRecorder<String, Result<Boolean>> {
            when (it) {
                "https://test.org" -> Result.success(true)
                "https://test.com" -> Result.success(false)
                "https://test.io" -> Result.success(true)
                "https://test" -> Result.success(false)
                else -> error("should not happen")
            }
        }
        val fakeLoginCompatibilityChecker = FakeHomeServerLoginCompatibilityChecker(checkResult = checkResult)
        val presenter = SearchAccountProviderPresenter(
            homeserverResolver = HomeserverResolver(testCoroutineDispatchers(), fakeLoginCompatibilityChecker),
            changeServerPresenter = { aChangeServerState() }
        )
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(SearchAccountProviderEvents.UserInput("test"))
            val withInputState = awaitItem()
            assertThat(withInputState.userInput).isEqualTo("test")
            assertThat(initialState.userInputResult).isEqualTo(AsyncData.Uninitialized)
            assertThat(awaitItem().userInputResult).isInstanceOf(AsyncData.Loading::class.java)
            assertThat(awaitItem().userInputResult).isEqualTo(
                AsyncData.Success(
                    listOf(
                        aHomeserverData(homeserverUrl = "https://test.org"),
                    )
                )
            )
            assertThat(awaitItem().userInputResult).isEqualTo(
                AsyncData.Success(
                    listOf(
                        aHomeserverData(homeserverUrl = "https://test.org"),
                        aHomeserverData(homeserverUrl = "https://test.io"),
                    )
                )
            )
            checkResult.assertions().isCalledExactly(4)
                .withSequence(
                    listOf(value("https://test.org")),
                    listOf(value("https://test.com")),
                    listOf(value("https://test.io")),
                    listOf(value("https://test")),
                )
        }
    }
}

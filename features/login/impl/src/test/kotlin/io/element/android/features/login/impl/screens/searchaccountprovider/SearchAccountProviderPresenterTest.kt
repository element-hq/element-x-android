/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.searchaccountprovider

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.login.impl.changeserver.aChangeServerState
import io.element.android.features.login.impl.resolver.HomeserverResolver
import io.element.android.features.wellknown.test.FakeWellknownRetriever
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.test.A_HOMESERVER_URL
import io.element.android.libraries.wellknown.api.WellKnown
import io.element.android.libraries.wellknown.api.WellKnownBaseConfig
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class SearchAccountProviderPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val fakeWellknownRetriever = FakeWellknownRetriever()
        val presenter = SearchAccountProviderPresenter(
            homeserverResolver = HomeserverResolver(testCoroutineDispatchers(), fakeWellknownRetriever),
            changeServerPresenter = { aChangeServerState() }
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.userInput).isEmpty()
            assertThat(initialState.userInputResult).isEqualTo(AsyncData.Uninitialized)
        }
    }

    @Test
    fun `present - enter text no result`() = runTest {
        val fakeWellknownRetriever = FakeWellknownRetriever()
        val presenter = SearchAccountProviderPresenter(
            homeserverResolver = HomeserverResolver(testCoroutineDispatchers(), fakeWellknownRetriever),
            changeServerPresenter = { aChangeServerState() }
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(SearchAccountProviderEvents.UserInput("test"))
            val withInputState = awaitItem()
            assertThat(withInputState.userInput).isEqualTo("test")
            assertThat(initialState.userInputResult).isEqualTo(AsyncData.Uninitialized)
            assertThat(awaitItem().userInputResult).isInstanceOf(AsyncData.Loading::class.java)
            assertThat(awaitItem().userInputResult).isEqualTo(AsyncData.Uninitialized)
        }
    }

    @Test
    fun `present - enter valid url no wellknown`() = runTest {
        val fakeWellknownRetriever = FakeWellknownRetriever()
        val presenter = SearchAccountProviderPresenter(
            homeserverResolver = HomeserverResolver(testCoroutineDispatchers(), fakeWellknownRetriever),
            changeServerPresenter = { aChangeServerState() }
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(SearchAccountProviderEvents.UserInput("https://test.org"))
            val withInputState = awaitItem()
            assertThat(withInputState.userInput).isEqualTo("https://test.org")
            assertThat(initialState.userInputResult).isEqualTo(AsyncData.Uninitialized)
            assertThat(awaitItem().userInputResult).isInstanceOf(AsyncData.Loading::class.java)
            assertThat(awaitItem().userInputResult).isEqualTo(
                AsyncData.Success(
                    listOf(
                        aHomeserverData(homeserverUrl = "https://test.org", isWellknownValid = false)
                    )
                )
            )
        }
    }

    @Test
    fun `present - enter text one result with wellknown`() = runTest {
        val getWellKnownResult = lambdaRecorder<String, WellKnown> {
            when (it) {
                "https://test.org" -> error("not found")
                "https://test.com" -> error("not found")
                "https://test.io" -> aWellKnown()
                "https://test" -> error("not found")
                else -> error("should not happen")
            }
        }
        val fakeWellknownRetriever = FakeWellknownRetriever(
            getWellKnownResult = getWellKnownResult,
        )
        val presenter = SearchAccountProviderPresenter(
            homeserverResolver = HomeserverResolver(testCoroutineDispatchers(), fakeWellknownRetriever),
            changeServerPresenter = { aChangeServerState() }
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
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
            getWellKnownResult.assertions().isCalledExactly(4)
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
        val getWellKnownResult = lambdaRecorder<String, WellKnown> {
            when (it) {
                "https://test.org" -> aWellKnown()
                "https://test.com" -> error("not found")
                "https://test.io" -> aWellKnown()
                "https://test" -> error("not found")
                else -> error("should not happen")
            }
        }
        val fakeWellknownRetriever = FakeWellknownRetriever(
            getWellKnownResult = getWellKnownResult,
        )
        val presenter = SearchAccountProviderPresenter(
            homeserverResolver = HomeserverResolver(testCoroutineDispatchers(), fakeWellknownRetriever),
            changeServerPresenter = { aChangeServerState() }
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
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
            getWellKnownResult.assertions().isCalledExactly(4)
                .withSequence(
                    listOf(value("https://test.org")),
                    listOf(value("https://test.com")),
                    listOf(value("https://test.io")),
                    listOf(value("https://test")),
                )
        }
    }

    private fun aWellKnown(): WellKnown {
        return WellKnown(
            homeServer = WellKnownBaseConfig(
                baseURL = A_HOMESERVER_URL
            ),
            identityServer = WellKnownBaseConfig(
                baseURL = A_HOMESERVER_URL
            ),
        )
    }
}

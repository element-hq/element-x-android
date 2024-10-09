/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.searchaccountprovider

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.login.impl.changeserver.aChangeServerState
import io.element.android.features.login.impl.resolver.HomeserverResolver
import io.element.android.features.login.impl.resolver.network.FakeWellknownRequest
import io.element.android.features.login.impl.resolver.network.WellKnown
import io.element.android.features.login.impl.resolver.network.WellKnownBaseConfig
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.test.A_HOMESERVER_URL
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class SearchAccountProviderPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val fakeWellknownRequest = FakeWellknownRequest()
        val presenter = SearchAccountProviderPresenter(
            homeserverResolver = HomeserverResolver(testCoroutineDispatchers(), fakeWellknownRequest),
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
        val fakeWellknownRequest = FakeWellknownRequest()
        val presenter = SearchAccountProviderPresenter(
            homeserverResolver = HomeserverResolver(testCoroutineDispatchers(), fakeWellknownRequest),
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
        val fakeWellknownRequest = FakeWellknownRequest()
        val presenter = SearchAccountProviderPresenter(
            homeserverResolver = HomeserverResolver(testCoroutineDispatchers(), fakeWellknownRequest),
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
        val fakeWellknownRequest = FakeWellknownRequest()
        fakeWellknownRequest.givenResultMap(
            mapOf(
                "https://test.io" to aWellKnown(),
            )
        )
        val presenter = SearchAccountProviderPresenter(
            homeserverResolver = HomeserverResolver(testCoroutineDispatchers(), fakeWellknownRequest),
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

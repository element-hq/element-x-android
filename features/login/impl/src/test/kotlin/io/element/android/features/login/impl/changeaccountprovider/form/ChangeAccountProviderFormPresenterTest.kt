/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.login.impl.changeaccountprovider.form

import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.login.impl.changeaccountprovider.common.ChangeServerPresenter
import io.element.android.features.login.impl.changeaccountprovider.form.network.WellKnown
import io.element.android.features.login.impl.changeaccountprovider.form.network.WellKnownBaseConfig
import io.element.android.features.login.impl.changeaccountprovider.form.network.WellKnownSlidingSyncConfig
import io.element.android.features.login.impl.datasource.AccountProviderDataSource
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.matrix.test.A_HOMESERVER_URL
import io.element.android.libraries.matrix.test.auth.FakeAuthenticationService
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ChangeAccountProviderFormPresenterTest {
    @Test
    fun `present - initial state`() = runTest {
        val fakeWellknownRequest = FakeWellknownRequest()
        val changeServerPresenter = ChangeServerPresenter(
            FakeAuthenticationService(),
            AccountProviderDataSource()
        )
        val presenter = ChangeAccountProviderFormPresenter(
            DefaultHomeserverResolver(testCoroutineDispatchers(), fakeWellknownRequest),
            changeServerPresenter
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.userInput).isEmpty()
            assertThat(initialState.userInputResult).isEqualTo(Async.Uninitialized)
        }
    }

    @Test
    fun `present - enter text no result`() = runTest {
        val fakeWellknownRequest = FakeWellknownRequest()
        val changeServerPresenter = ChangeServerPresenter(
            FakeAuthenticationService(),
            AccountProviderDataSource()
        )
        val presenter = ChangeAccountProviderFormPresenter(
            DefaultHomeserverResolver(testCoroutineDispatchers(), fakeWellknownRequest),
            changeServerPresenter
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(ChangeAccountProviderFormEvents.UserInput("test"))
            val withInputState = awaitItem()
            assertThat(withInputState.userInput).isEqualTo("test")
            assertThat(initialState.userInputResult).isEqualTo(Async.Uninitialized)
            assertThat(awaitItem().userInputResult).isInstanceOf(Async.Loading::class.java)
            assertThat(awaitItem().userInputResult).isEqualTo(Async.Uninitialized)
        }
    }

    @Test
    fun `present - enter valid url no wellknown`() = runTest {
        val fakeWellknownRequest = FakeWellknownRequest()
        val changeServerPresenter = ChangeServerPresenter(
            FakeAuthenticationService(),
            AccountProviderDataSource()
        )
        val presenter = ChangeAccountProviderFormPresenter(
            DefaultHomeserverResolver(testCoroutineDispatchers(), fakeWellknownRequest),
            changeServerPresenter
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(ChangeAccountProviderFormEvents.UserInput("https://test.org"))
            val withInputState = awaitItem()
            assertThat(withInputState.userInput).isEqualTo("https://test.org")
            assertThat(initialState.userInputResult).isEqualTo(Async.Uninitialized)
            assertThat(awaitItem().userInputResult).isInstanceOf(Async.Loading::class.java)
            assertThat(awaitItem().userInputResult).isEqualTo(
                Async.Success(
                    listOf(
                        aHomeserverData(homeserverUrl = "https://test.org", isWellknownValid = false, supportSlidingSync = false)
                    )
                )
            )
        }
    }

    @Test
    fun `present - enter text one result no sliding sync`() = runTest {
        val fakeWellknownRequest = FakeWellknownRequest()
        fakeWellknownRequest.givenResultMap(
            mapOf(
                "https://test.org" to aWellKnown().copy(slidingSyncProxy = null),
            )
        )
        val changeServerPresenter = ChangeServerPresenter(
            FakeAuthenticationService(),
            AccountProviderDataSource()
        )
        val presenter = ChangeAccountProviderFormPresenter(
            DefaultHomeserverResolver(testCoroutineDispatchers(), fakeWellknownRequest),
            changeServerPresenter
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(ChangeAccountProviderFormEvents.UserInput("test"))
            val withInputState = awaitItem()
            assertThat(withInputState.userInput).isEqualTo("test")
            assertThat(initialState.userInputResult).isEqualTo(Async.Uninitialized)
            assertThat(awaitItem().userInputResult).isInstanceOf(Async.Loading::class.java)
            assertThat(awaitItem().userInputResult).isEqualTo(
                Async.Success(
                    listOf(
                        aHomeserverData(homeserverUrl = "https://test.org", isWellknownValid = true, supportSlidingSync = false)
                    )
                )
            )
        }
    }

    @Test
    fun `present - enter text one result with sliding sync`() = runTest {
        val fakeWellknownRequest = FakeWellknownRequest()
        fakeWellknownRequest.givenResultMap(
            mapOf(
                "https://test.io" to aWellKnown(),
            )
        )
        val changeServerPresenter = ChangeServerPresenter(
            FakeAuthenticationService(),
            AccountProviderDataSource()
        )
        val presenter = ChangeAccountProviderFormPresenter(
            DefaultHomeserverResolver(testCoroutineDispatchers(), fakeWellknownRequest),
            changeServerPresenter
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(ChangeAccountProviderFormEvents.UserInput("test"))
            val withInputState = awaitItem()
            assertThat(withInputState.userInput).isEqualTo("test")
            assertThat(initialState.userInputResult).isEqualTo(Async.Uninitialized)
            assertThat(awaitItem().userInputResult).isInstanceOf(Async.Loading::class.java)
            assertThat(awaitItem().userInputResult).isEqualTo(
                Async.Success(
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
            slidingSyncProxy = WellKnownSlidingSyncConfig(
                url = A_HOMESERVER_URL
            )
        )
    }
}

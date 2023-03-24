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

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.features.login.impl.changeserver

import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.login.impl.changeserver.ChangeServerEvents
import io.element.android.features.login.impl.changeserver.ChangeServerPresenter
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.matrix.test.A_HOMESERVER
import io.element.android.libraries.matrix.test.A_HOMESERVER_URL
import io.element.android.libraries.matrix.test.A_HOMESERVER_URL_2
import io.element.android.libraries.matrix.test.A_THROWABLE
import io.element.android.libraries.matrix.test.auth.FakeAuthenticationService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ChangeServerPresenterTest {
    @Test
    fun `present - should start with default homeserver`() = runTest {
        val presenter = ChangeServerPresenter(
            FakeAuthenticationService(),
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.homeserver).isEqualTo(A_HOMESERVER_URL)
            assertThat(initialState.submitEnabled).isTrue()
        }
    }

    @Test
    fun `present - authentication service can provide a homeserver`() = runTest {
        val presenter = ChangeServerPresenter(
            FakeAuthenticationService().apply {
                givenHomeserver(A_HOMESERVER.copy(url = A_HOMESERVER_URL_2))
            },
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.homeserver).isEqualTo(A_HOMESERVER_URL_2)
            assertThat(initialState.submitEnabled).isTrue()
        }
    }

    @Test
    fun `present - disable if empty or not correct`() = runTest {
        val presenter = ChangeServerPresenter(
            FakeAuthenticationService(),
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(ChangeServerEvents.SetServer(""))
            val emptyState = awaitItem()
            assertThat(emptyState.homeserver).isEqualTo("")
            assertThat(emptyState.submitEnabled).isFalse()
        }
    }

    @Test
    fun `present - submit`() = runTest {
        val presenter = ChangeServerPresenter(
            FakeAuthenticationService(),
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(ChangeServerEvents.Submit)
            val loadingState = awaitItem()
            assertThat(loadingState.submitEnabled).isFalse()
            assertThat(loadingState.changeServerAction).isInstanceOf(Async.Loading::class.java)
            val successState = awaitItem()
            assertThat(successState.submitEnabled).isFalse()
            assertThat(successState.changeServerAction).isInstanceOf(Async.Success::class.java)
        }
    }

    @Test
    fun `present - submit parses URL`() = runTest {
        val presenter = ChangeServerPresenter(
            FakeAuthenticationService(),
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val longUrl = "https://matrix.org/.well-known/"
            val initialState = awaitItem()
            initialState.eventSink.invoke(ChangeServerEvents.SetServer(longUrl))
            awaitItem()
            initialState.eventSink.invoke(ChangeServerEvents.Submit)
            val loadingState = awaitItem()
            assertThat(loadingState.submitEnabled).isFalse()
            assertThat(loadingState.changeServerAction).isInstanceOf(Async.Loading::class.java)
            awaitItem() // Skip changing the url to the parsed domain
            val successState = awaitItem()
            assertThat(successState.submitEnabled).isFalse()
            assertThat(successState.changeServerAction).isInstanceOf(Async.Success::class.java)
            assertThat(successState.homeserver).isEqualTo("matrix.org")
        }
    }

    @Test
    fun `present - submit fails`() = runTest {
        val authServer = FakeAuthenticationService()
        val presenter = ChangeServerPresenter(authServer)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            authServer.givenChangeServerError(Throwable())
            initialState.eventSink.invoke(ChangeServerEvents.Submit)
            skipItems(1) // Loading
            val failureState = awaitItem()
            assertThat(failureState.submitEnabled).isFalse()
            assertThat(failureState.changeServerAction).isInstanceOf(Async.Failure::class.java)
        }
    }

    @Test
    fun `present - clear error`() = runTest {
        val authenticationService = FakeAuthenticationService()
        val presenter = ChangeServerPresenter(
            authenticationService,
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()

            // Submit will return an error
            authenticationService.givenChangeServerError(A_THROWABLE)
            initialState.eventSink(ChangeServerEvents.Submit)

            skipItems(1) // Loading

            // Check an error was returned
            val submittedState = awaitItem()
            assertThat(submittedState.changeServerAction).isInstanceOf(Async.Failure::class.java)

            // Assert the error is then cleared
            submittedState.eventSink(ChangeServerEvents.ClearError)
            val clearedState = awaitItem()
            assertThat(clearedState.changeServerAction).isEqualTo(Async.Uninitialized)
        }
    }
}

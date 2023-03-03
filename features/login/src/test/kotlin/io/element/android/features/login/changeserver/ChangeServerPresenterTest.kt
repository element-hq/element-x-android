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

package io.element.android.features.login.changeserver

import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.matrix.test.A_HOMESERVER
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
            assertThat(initialState.homeserver).isEqualTo(A_HOMESERVER)
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
            assertThat(successState.submitEnabled).isTrue()
            assertThat(successState.changeServerAction).isInstanceOf(Async.Success::class.java)
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

            // Check an error was returned
            val submittedState = awaitItem()
            assertThat(submittedState.changeServerAction).isEqualTo(Async.Failure<Unit>(A_THROWABLE))

            // Assert the error is then cleared
            submittedState.eventSink(ChangeServerEvents.ClearError)
            val clearedState = awaitItem()
            assertThat(clearedState.changeServerAction).isEqualTo(Async.Uninitialized)
        }
    }
}

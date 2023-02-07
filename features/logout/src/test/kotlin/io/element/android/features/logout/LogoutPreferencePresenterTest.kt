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

package io.element.android.features.logout

import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.matrix.core.SessionId
import io.element.android.libraries.matrixtest.FakeMatrixClient
import io.element.android.libraries.matrixtest.auth.A_FAILURE
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test

class LogoutPreferencePresenterTest {
    @Test
    fun `present - initial state`() = runTest {
        val presenter = LogoutPreferencePresenter(
            FakeMatrixClient(SessionId("sessionId")),
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.logoutAction).isEqualTo(Async.Uninitialized)
        }
    }

    @Test
    fun `present - logout`() = runTest {
        val presenter = LogoutPreferencePresenter(
            FakeMatrixClient(SessionId("sessionId")),
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(LogoutPreferenceEvents.Logout)
            val loadingState = awaitItem()
            assertThat(loadingState.logoutAction).isInstanceOf(Async.Loading::class.java)
            val successState = awaitItem()
            assertThat(successState.logoutAction).isInstanceOf(Async.Success::class.java)
        }
    }

    @Test
    fun `present - logout with error`() = runTest {
        val matrixClient = FakeMatrixClient(SessionId("sessionId"))
        val presenter = LogoutPreferencePresenter(
            matrixClient,
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            matrixClient.givenLogoutError(A_FAILURE)
            initialState.eventSink.invoke(LogoutPreferenceEvents.Logout)
            val loadingState = awaitItem()
            assertThat(loadingState.logoutAction).isInstanceOf(Async.Loading::class.java)
            val successState = awaitItem()
            assertThat(successState.logoutAction).isEqualTo(Async.Failure<LogoutPreferenceState>(A_FAILURE))
        }
    }
}


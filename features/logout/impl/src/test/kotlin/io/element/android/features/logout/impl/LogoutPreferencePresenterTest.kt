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

package io.element.android.features.logout.impl

import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.logout.api.LogoutPreferenceEvents
import io.element.android.features.logout.api.LogoutPreferenceState
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.matrix.test.A_THROWABLE
import io.element.android.libraries.matrix.test.aFakeMatrixClient
import kotlinx.coroutines.test.runTest
import org.junit.Test

class LogoutPreferencePresenterTest {
    @Test
    fun `present - initial state`() = runTest {
        val presenter = DefaultLogoutPreferencePresenter(
            aFakeMatrixClient(),
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
        val presenter = DefaultLogoutPreferencePresenter(
            aFakeMatrixClient(),
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
        val matrixClient = aFakeMatrixClient()
        val presenter = DefaultLogoutPreferencePresenter(
            matrixClient,
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            matrixClient.givenLogoutError(A_THROWABLE)
            initialState.eventSink.invoke(LogoutPreferenceEvents.Logout)
            val loadingState = awaitItem()
            assertThat(loadingState.logoutAction).isInstanceOf(Async.Loading::class.java)
            val successState = awaitItem()
            assertThat(successState.logoutAction).isEqualTo(Async.Failure<LogoutPreferenceState>(A_THROWABLE))
        }
    }
}


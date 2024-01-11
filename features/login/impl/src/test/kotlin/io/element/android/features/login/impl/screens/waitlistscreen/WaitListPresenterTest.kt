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

package io.element.android.features.login.impl.screens.waitlistscreen

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.login.impl.DefaultLoginUserStory
import io.element.android.features.login.impl.screens.loginpassword.LoginFormState
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.test.A_HOMESERVER
import io.element.android.libraries.matrix.test.A_HOMESERVER_URL
import io.element.android.libraries.matrix.test.A_THROWABLE
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.auth.FakeAuthenticationService
import io.element.android.libraries.matrix.test.core.aBuildMeta
import io.element.android.tests.testutils.WarmUpRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class WaitListPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val authenticationService = FakeAuthenticationService().apply {
            givenHomeserver(A_HOMESERVER)
        }
        val loginUserStory = DefaultLoginUserStory()
        val presenter = WaitListPresenter(
            LoginFormState.Default,
            aBuildMeta(applicationName = "Application Name"),
            authenticationService,
            loginUserStory,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.appName).isEqualTo("Application Name")
            assertThat(initialState.serverName).isEqualTo(A_HOMESERVER_URL)
            assertThat(initialState.loginAction).isEqualTo(AsyncData.Uninitialized)
        }
    }

    @Test
    fun `present - attempt login with error`() = runTest {
        val authenticationService = FakeAuthenticationService().apply {
            givenLoginError(A_THROWABLE)
        }
        val loginUserStory = DefaultLoginUserStory()
        val presenter = WaitListPresenter(
            LoginFormState.Default,
            aBuildMeta(),
            authenticationService,
            loginUserStory,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            // First usage of AttemptLogin, nothing should happen
            initialState.eventSink.invoke(WaitListEvents.AttemptLogin)
            expectNoEvents()
            initialState.eventSink.invoke(WaitListEvents.AttemptLogin)
            val submitState = awaitItem()
            assertThat(submitState.loginAction).isInstanceOf(AsyncData.Loading::class.java)
            val errorState = awaitItem()
            assertThat(errorState.loginAction).isEqualTo(AsyncData.Failure<SessionId>(A_THROWABLE))
            // Assert the error can be cleared
            errorState.eventSink(WaitListEvents.ClearError)
            val clearedState = awaitItem()
            assertThat(clearedState.loginAction).isEqualTo(AsyncData.Uninitialized)
        }
    }

    @Test
    fun `present - attempt login with success`() = runTest {
        val authenticationService = FakeAuthenticationService()
        val loginUserStory = DefaultLoginUserStory().apply { setLoginFlowIsDone(false) }
        val presenter = WaitListPresenter(
            LoginFormState.Default,
            aBuildMeta(),
            authenticationService,
            loginUserStory,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            assertThat(loginUserStory.loginFlowIsDone.value).isFalse()
            val initialState = awaitItem()
            // First usage of AttemptLogin, nothing should happen
            initialState.eventSink.invoke(WaitListEvents.AttemptLogin)
            expectNoEvents()
            initialState.eventSink.invoke(WaitListEvents.AttemptLogin)
            val submitState = awaitItem()
            assertThat(submitState.loginAction).isInstanceOf(AsyncData.Loading::class.java)
            val successState = awaitItem()
            assertThat(successState.loginAction).isEqualTo(AsyncData.Success(A_USER_ID))
            assertThat(loginUserStory.loginFlowIsDone.value).isFalse()
            successState.eventSink.invoke(WaitListEvents.Continue)
            assertThat(loginUserStory.loginFlowIsDone.value).isTrue()
        }
    }
}

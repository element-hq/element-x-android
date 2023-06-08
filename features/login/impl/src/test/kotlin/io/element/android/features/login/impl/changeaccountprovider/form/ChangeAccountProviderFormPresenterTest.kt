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
import io.element.android.features.login.impl.datasource.AccountProviderDataSource
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.matrix.test.auth.FakeAuthenticationService
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ChangeAccountProviderFormPresenterTest {
    @Test
    fun `present - initial state`() = runTest {
        val homeServerResolver = FakeHomeServerResolver()
        val changeServerPresenter = ChangeServerPresenter(
            FakeAuthenticationService(),
            AccountProviderDataSource()
        )
        val presenter = ChangeAccountProviderFormPresenter(
            homeServerResolver,
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
        val homeServerResolver = FakeHomeServerResolver()
        val changeServerPresenter = ChangeServerPresenter(
            FakeAuthenticationService(),
            AccountProviderDataSource()
        )
        val presenter = ChangeAccountProviderFormPresenter(
            homeServerResolver,
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
    fun `present - enter text one then two results`() = runTest {
        val homeServerResolver = FakeHomeServerResolver()
        homeServerResolver.givenResult(
            listOf(
                listOf(aHomeserverData()),
                listOf(aHomeserverData(), aHomeserverData()),
            )
        )
        val changeServerPresenter = ChangeServerPresenter(
            FakeAuthenticationService(),
            AccountProviderDataSource()
        )
        val presenter = ChangeAccountProviderFormPresenter(
            homeServerResolver,
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
            assertThat(awaitItem().userInputResult).isEqualTo(Async.Success(listOf(aHomeserverData())))
            assertThat(awaitItem().userInputResult).isEqualTo(Async.Success(listOf(aHomeserverData(), aHomeserverData())))
        }
    }
}

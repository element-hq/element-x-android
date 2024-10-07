/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.login.impl.changeserver

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.login.impl.accountprovider.AccountProvider
import io.element.android.features.login.impl.accountprovider.AccountProviderDataSource
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.test.A_HOMESERVER
import io.element.android.libraries.matrix.test.A_HOMESERVER_URL
import io.element.android.libraries.matrix.test.auth.FakeMatrixAuthenticationService
import io.element.android.tests.testutils.WarmUpRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ChangeServerPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val presenter = ChangeServerPresenter(
            FakeMatrixAuthenticationService(),
            AccountProviderDataSource()
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.changeServerAction).isEqualTo(AsyncData.Uninitialized)
        }
    }

    @Test
    fun `present - change server ok`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService()
        val presenter = ChangeServerPresenter(
            authenticationService,
            AccountProviderDataSource()
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.changeServerAction).isEqualTo(AsyncData.Uninitialized)
            authenticationService.givenHomeserver(A_HOMESERVER)
            initialState.eventSink.invoke(ChangeServerEvents.ChangeServer(AccountProvider(url = A_HOMESERVER_URL)))
            val loadingState = awaitItem()
            assertThat(loadingState.changeServerAction).isInstanceOf(AsyncData.Loading::class.java)
            val successState = awaitItem()
            assertThat(successState.changeServerAction).isEqualTo(AsyncData.Success(Unit))
        }
    }

    @Test
    fun `present - change server error`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService()
        val presenter = ChangeServerPresenter(
            authenticationService,
            AccountProviderDataSource()
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.changeServerAction).isEqualTo(AsyncData.Uninitialized)
            initialState.eventSink.invoke(ChangeServerEvents.ChangeServer(AccountProvider(url = A_HOMESERVER_URL)))
            val loadingState = awaitItem()
            assertThat(loadingState.changeServerAction).isInstanceOf(AsyncData.Loading::class.java)
            val failureState = awaitItem()
            assertThat(failureState.changeServerAction).isInstanceOf(AsyncData.Failure::class.java)
            // Clear error
            failureState.eventSink.invoke(ChangeServerEvents.ClearError)
            val finalState = awaitItem()
            assertThat(finalState.changeServerAction).isEqualTo(AsyncData.Uninitialized)
        }
    }
}

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

package io.element.android.features.logout.impl.direct

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.logout.api.direct.DirectLogoutEvents
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.featureflag.test.FakeFeatureFlagService
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.encryption.BackupUploadState
import io.element.android.libraries.matrix.api.encryption.EncryptionService
import io.element.android.libraries.matrix.test.A_THROWABLE
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.encryption.FakeEncryptionService
import io.element.android.tests.testutils.WarmUpRule
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class DefaultDirectLogoutPresenterTest {

    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val presenter = createDefaultDirectLogoutPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            assertThat(initialState.canDoDirectSignOut).isTrue()
            assertThat(initialState.logoutAction).isEqualTo(AsyncAction.Uninitialized)
        }
    }

    @Test
    fun `present - initial state - last session`() = runTest {
        val presenter = createDefaultDirectLogoutPresenter(
            encryptionService = FakeEncryptionService().apply {
                givenIsLastDevice(true)
            }
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(2)
            val initialState = awaitItem()
            assertThat(initialState.canDoDirectSignOut).isFalse()
            assertThat(initialState.logoutAction).isEqualTo(AsyncAction.Uninitialized)
        }
    }

    @Test
    fun `present - initial state - backing up`() = runTest {
        val encryptionService = FakeEncryptionService()
        encryptionService.givenWaitForBackupUploadSteadyStateFlow(
            flow {
                emit(BackupUploadState.Waiting)
            }
        )
        val presenter = createDefaultDirectLogoutPresenter(
            encryptionService = encryptionService
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(2)
            val initialState = awaitItem()
            assertThat(initialState.canDoDirectSignOut).isFalse()
            assertThat(initialState.logoutAction).isEqualTo(AsyncAction.Uninitialized)
        }
    }

    @Test
    fun `present - logout then cancel`() = runTest {
        val presenter = createDefaultDirectLogoutPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            initialState.eventSink.invoke(DirectLogoutEvents.Logout(ignoreSdkError = false))
            val confirmationState = awaitItem()
            assertThat(confirmationState.logoutAction).isEqualTo(AsyncAction.Confirming)
            initialState.eventSink.invoke(DirectLogoutEvents.CloseDialogs)
            val finalState = awaitItem()
            assertThat(finalState.logoutAction).isEqualTo(AsyncAction.Uninitialized)
        }
    }

    @Test
    fun `present - logout then confirm`() = runTest {
        val presenter = createDefaultDirectLogoutPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            initialState.eventSink.invoke(DirectLogoutEvents.Logout(ignoreSdkError = false))
            val confirmationState = awaitItem()
            assertThat(confirmationState.logoutAction).isEqualTo(AsyncAction.Confirming)
            confirmationState.eventSink.invoke(DirectLogoutEvents.Logout(ignoreSdkError = false))
            val loadingState = awaitItem()
            assertThat(loadingState.logoutAction).isInstanceOf(AsyncAction.Loading::class.java)
            val successState = awaitItem()
            assertThat(successState.logoutAction).isInstanceOf(AsyncAction.Success::class.java)
        }
    }

    @Test
    fun `present - logout with error then cancel`() = runTest {
        val matrixClient = FakeMatrixClient().apply {
            givenLogoutError(A_THROWABLE)
        }
        val presenter = createDefaultDirectLogoutPresenter(
            matrixClient,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            initialState.eventSink.invoke(DirectLogoutEvents.Logout(ignoreSdkError = false))
            val confirmationState = awaitItem()
            assertThat(confirmationState.logoutAction).isEqualTo(AsyncAction.Confirming)
            confirmationState.eventSink.invoke(DirectLogoutEvents.Logout(ignoreSdkError = false))
            val loadingState = awaitItem()
            assertThat(loadingState.logoutAction).isInstanceOf(AsyncAction.Loading::class.java)
            val errorState = awaitItem()
            assertThat(errorState.logoutAction).isEqualTo(AsyncAction.Failure(A_THROWABLE))
            errorState.eventSink.invoke(DirectLogoutEvents.CloseDialogs)
            val finalState = awaitItem()
            assertThat(finalState.logoutAction).isEqualTo(AsyncAction.Uninitialized)
        }
    }

    @Test
    fun `present - logout with error then force`() = runTest {
        val matrixClient = FakeMatrixClient().apply {
            givenLogoutError(A_THROWABLE)
        }
        val presenter = createDefaultDirectLogoutPresenter(
            matrixClient,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            initialState.eventSink.invoke(DirectLogoutEvents.Logout(ignoreSdkError = false))
            val confirmationState = awaitItem()
            assertThat(confirmationState.logoutAction).isEqualTo(AsyncAction.Confirming)
            confirmationState.eventSink.invoke(DirectLogoutEvents.Logout(ignoreSdkError = false))
            val loadingState = awaitItem()
            assertThat(loadingState.logoutAction).isInstanceOf(AsyncAction.Loading::class.java)
            val errorState = awaitItem()
            assertThat(errorState.logoutAction).isEqualTo(AsyncAction.Failure(A_THROWABLE))
            errorState.eventSink.invoke(DirectLogoutEvents.Logout(ignoreSdkError = true))
            val loadingState2 = awaitItem()
            assertThat(loadingState2.logoutAction).isInstanceOf(AsyncAction.Loading::class.java)
            val successState = awaitItem()
            assertThat(successState.logoutAction).isInstanceOf(AsyncAction.Success::class.java)
        }
    }

    private suspend fun <T> ReceiveTurbine<T>.awaitFirstItem(): T {
        skipItems(1)
        return awaitItem()
    }

    private fun createDefaultDirectLogoutPresenter(
        matrixClient: MatrixClient = FakeMatrixClient(),
        encryptionService: EncryptionService = FakeEncryptionService(),
    ): DefaultDirectLogoutPresenter = DefaultDirectLogoutPresenter(
        matrixClient = matrixClient,
        encryptionService = encryptionService,
        featureFlagService = FakeFeatureFlagService(mapOf(FeatureFlags.SecureStorage.key to true)),
    )
}


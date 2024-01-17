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

package io.element.android.features.securebackup.impl.enable

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.encryption.EncryptionService
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.matrix.test.encryption.FakeEncryptionService
import io.element.android.tests.testutils.WarmUpRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class SecureBackupEnablePresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val presenter = createPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.enableAction).isEqualTo(AsyncData.Uninitialized)
        }
    }

    @Test
    fun `present - user enable backup`() = runTest {
        val presenter = createPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(SecureBackupEnableEvents.EnableBackup)
            val loadingState = awaitItem()
            assertThat(loadingState.enableAction).isInstanceOf(AsyncData.Loading::class.java)
            val finalState = awaitItem()
            assertThat(finalState.enableAction).isEqualTo(AsyncData.Success(Unit))
        }
    }

    @Test
    fun `present - user enable backup with error`() = runTest {
        val encryptionService = FakeEncryptionService()
        encryptionService.givenEnableBackupsFailure(AN_EXCEPTION)
        val presenter = createPresenter(encryptionService = encryptionService)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(SecureBackupEnableEvents.EnableBackup)
            val loadingState = awaitItem()
            assertThat(loadingState.enableAction).isInstanceOf(AsyncData.Loading::class.java)
            val errorState = awaitItem()
            assertThat(errorState.enableAction).isEqualTo(AsyncData.Failure<Unit>(AN_EXCEPTION))
            errorState.eventSink(SecureBackupEnableEvents.DismissDialog)
            val finalState = awaitItem()
            assertThat(finalState.enableAction).isEqualTo(AsyncData.Uninitialized)
        }
    }

    private fun createPresenter(
        encryptionService: EncryptionService = FakeEncryptionService(),
    ) = SecureBackupEnablePresenter(
        encryptionService = encryptionService,
    )
}

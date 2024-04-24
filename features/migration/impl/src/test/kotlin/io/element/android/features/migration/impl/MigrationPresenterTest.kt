/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.features.migration.impl

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.rageshake.api.logs.LogFilesRemover
import io.element.android.features.rageshake.test.logs.FakeLogFilesRemover
import io.element.android.libraries.architecture.AsyncData
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.lambda.lambdaRecorder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class MigrationPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - no migration should occurs if ApplicationMigrationVersion is the last one`() = runTest {
        val store = InMemoryMigrationStore(MigrationPresenter.MIGRATION_VERSION)
        val presenter = createPresenter(
            migrationStore = store,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.migrationAction).isEqualTo(AsyncData.Uninitialized)
            awaitItem().also { state ->
                assertThat(state.migrationAction).isEqualTo(AsyncData.Success(Unit))
            }
        }
    }

    @Test
    fun `present - testing all migrations`() = runTest {
        val store = InMemoryMigrationStore(0)
        val logFilesRemoverLambda = lambdaRecorder { -> }
        val presenter = createPresenter(
            migrationStore = store,
            logFilesRemover = FakeLogFilesRemover(logFilesRemoverLambda),
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.migrationAction).isEqualTo(AsyncData.Uninitialized)
            awaitItem().also { state ->
                assertThat(state.migrationAction).isEqualTo(AsyncData.Loading(Unit))
            }
            awaitItem().also { state ->
                assertThat(state.migrationAction).isEqualTo(AsyncData.Success(Unit))
            }
            logFilesRemoverLambda.assertions().isCalledExactly(1)
            assertThat(store.applicationMigrationVersion().first()).isEqualTo(MigrationPresenter.MIGRATION_VERSION)
        }
    }

    private fun createPresenter(
        migrationStore: MigrationStore = InMemoryMigrationStore(0),
        logFilesRemover: LogFilesRemover = FakeLogFilesRemover(lambdaRecorder(ensureNeverCalled = true) { -> }),
    ): MigrationPresenter {
        return MigrationPresenter(
            migrationStore = migrationStore,
            logFilesRemover = logFilesRemover,
        )
    }
}

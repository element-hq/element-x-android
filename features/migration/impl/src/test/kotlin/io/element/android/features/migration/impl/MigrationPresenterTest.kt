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
import io.element.android.features.migration.impl.migrations.AppMigration
import io.element.android.libraries.architecture.AsyncData
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.consumeItemsUntilPredicate
import io.element.android.tests.testutils.lambda.LambdaNoParamRecorder
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
        val migrations = (1..10).map { FakeMigration(it) }
        val store = InMemoryMigrationStore(migrations.maxOf { it.order })
        val presenter = createPresenter(
            migrationStore = store,
            migrations = migrations.toSet(),
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
        val migrations = (1..10).map { FakeMigration(it) }
        val presenter = createPresenter(
            migrationStore = store,
            migrations = migrations.toSet(),
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.migrationAction).isEqualTo(AsyncData.Uninitialized)
            awaitItem().also { state ->
                assertThat(state.migrationAction).isEqualTo(AsyncData.Loading(Unit))
            }
            consumeItemsUntilPredicate { it.migrationAction is AsyncData.Success }
            assertThat(store.applicationMigrationVersion().first()).isEqualTo(migrations.maxOf { it.order })
            for (migration in migrations) {
                migration.migrateLambda.assertions().isCalledOnce()
            }
        }
    }
}

private fun createPresenter(
    migrationStore: MigrationStore = InMemoryMigrationStore(0),
    migrations: Set<AppMigration> = setOf(FakeMigration(1)),
) = MigrationPresenter(
    migrationStore = migrationStore,
    migrations = migrations,
)

private class FakeMigration(
    override val order: Int,
    var migrateLambda: LambdaNoParamRecorder<Unit> = lambdaRecorder { -> },
) : AppMigration {
    override suspend fun migrate() {
        migrateLambda()
    }
}

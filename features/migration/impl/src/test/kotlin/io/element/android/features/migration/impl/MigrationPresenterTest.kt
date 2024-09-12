/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
    fun `present - no migration should occurs on fresh installation, and last version should be stored`() = runTest {
        val migrations = (1..10).map { order ->
            FakeAppMigration(
                order = order,
                migrateLambda = LambdaNoParamRecorder(ensureNeverCalled = true) { },
            )
        }
        val store = InMemoryMigrationStore(initialApplicationMigrationVersion = -1)
        val presenter = createPresenter(
            migrationStore = store,
            migrations = migrations.toSet(),
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.migrationAction).isEqualTo(AsyncData.Uninitialized)
            skipItems(1)
            awaitItem().also { state ->
                assertThat(state.migrationAction).isEqualTo(AsyncData.Success(Unit))
            }
            assertThat(store.applicationMigrationVersion().first()).isEqualTo(migrations.maxOf { it.order })
        }
    }

    @Test
    fun `present - no migration should occurs if ApplicationMigrationVersion is the last one`() = runTest {
        val migrations = (1..10).map { FakeAppMigration(it) }
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
        val migrations = (1..10).map { FakeAppMigration(it) }
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
    migrations: Set<AppMigration> = setOf(FakeAppMigration(1)),
) = MigrationPresenter(
    migrationStore = migrationStore,
    migrations = migrations,
)

private class FakeAppMigration(
    override val order: Int,
    val migrateLambda: LambdaNoParamRecorder<Unit> = lambdaRecorder { -> },
) : AppMigration {
    override suspend fun migrate() {
        migrateLambda()
    }
}

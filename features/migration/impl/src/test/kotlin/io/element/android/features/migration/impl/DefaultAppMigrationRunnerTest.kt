/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.migration.impl

import com.google.common.truth.Truth.assertThat
import io.element.android.features.migration.impl.migrations.AppMigration
import io.element.android.tests.testutils.lambda.LambdaOneParamRecorder
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultAppMigrationRunnerTest {
    @Test
    fun `run all migrations on fresh installation, and last version should be stored`() = runTest {
        val migrations = (1..10).map { FakeAppMigration(order = it) }
        val store = InMemoryMigrationStore(initialApplicationMigrationVersion = -1)
        val sut = createRunner(store, migrations.toSet())

        assertThat(sut.hasPendingMigrations()).isTrue()
        sut.runPendingMigrations()

        assertThat(sut.hasPendingMigrations()).isFalse()
        assertThat(store.applicationMigrationVersion().first()).isEqualTo(10)
        migrations.forEach { it.migrateLambda.assertions().isCalledOnce().with(value(true)) }
    }

    @Test
    fun `no migration should occur if the version is the last one`() = runTest {
        val migrations = (1..10).map {
            FakeAppMigration(order = it, migrateLambda = lambdaRecorder<Boolean, Unit> { lambdaError() })
        }
        val sut = createRunner(InMemoryMigrationStore(10), migrations.toSet())

        assertThat(sut.hasPendingMigrations()).isFalse()
        sut.runPendingMigrations()
    }

    @Test
    fun `only the pending migrations are applied, in order`() = runTest {
        val appliedMigrations = mutableListOf<Int>()
        val migrations = (1..10).map { order ->
            FakeAppMigration(order = order, migrateLambda = lambdaRecorder<Boolean, Unit> { appliedMigrations.add(order) })
        }
        val store = InMemoryMigrationStore(7)
        val sut = createRunner(store, migrations.shuffled().toSet())

        sut.runPendingMigrations()

        assertThat(appliedMigrations).containsExactly(8, 9, 10).inOrder()
        migrations.filter { it.order > 7 }.forEach { it.migrateLambda.assertions().isCalledOnce().with(value(false)) }
        assertThat(store.applicationMigrationVersion().first()).isEqualTo(10)
    }

    @Test
    fun `migrations are applied only once, even with several or concurrent calls`() = runTest {
        val migrations = (1..10).map { FakeAppMigration(order = it) }
        val sut = createRunner(InMemoryMigrationStore(0), migrations.toSet())

        List(3) { async { sut.runPendingMigrations() } }.awaitAll()
        sut.runPendingMigrations()

        migrations.forEach { it.migrateLambda.assertions().isCalledOnce() }
    }

    private fun TestScope.createRunner(
        migrationStore: MigrationStore,
        migrations: Set<AppMigration>,
    ) = DefaultAppMigrationRunner(
        migrationStore = migrationStore,
        migrations = migrations,
        coroutineDispatchers = testCoroutineDispatchers(),
    )
}

private class FakeAppMigration(
    override val order: Int,
    val migrateLambda: LambdaOneParamRecorder<Boolean, Unit> = lambdaRecorder<Boolean, Unit> { },
) : AppMigration {
    override suspend fun migrate(isFreshInstall: Boolean) {
        migrateLambda(isFreshInstall)
    }
}

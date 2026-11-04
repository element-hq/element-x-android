/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.migration.impl

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.migration.api.AppMigrationRunner
import io.element.android.features.migration.test.FakeAppMigrationRunner
import io.element.android.libraries.architecture.AsyncData
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.lambda.lambdaRecorder
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class MigrationPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - no pending migration`() = runTest {
        val runLambda = lambdaRecorder<Unit> { }
        val presenter = createPresenter(
            FakeAppMigrationRunner(
                hasPendingMigrationsResult = { false },
                runPendingMigrationsResult = { runLambda() },
            )
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            assertThat(awaitItem().migrationAction).isEqualTo(AsyncData.Uninitialized)
            assertThat(awaitItem().migrationAction).isEqualTo(AsyncData.Success(Unit))
        }
        runLambda.assertions().isCalledOnce()
    }

    @Test
    fun `present - pending migrations`() = runTest {
        val migrationsDone = CompletableDeferred<Unit>()
        val presenter = createPresenter(
            FakeAppMigrationRunner(
                hasPendingMigrationsResult = { true },
                runPendingMigrationsResult = { migrationsDone.await() },
            )
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            assertThat(awaitItem().migrationAction).isEqualTo(AsyncData.Uninitialized)
            assertThat(awaitItem().migrationAction).isEqualTo(AsyncData.Loading(Unit))
            migrationsDone.complete(Unit)
            assertThat(awaitItem().migrationAction).isEqualTo(AsyncData.Success(Unit))
        }
    }

    private fun createPresenter(appMigrationRunner: AppMigrationRunner) = MigrationPresenter(
        appMigrationRunner = appMigrationRunner,
    )
}

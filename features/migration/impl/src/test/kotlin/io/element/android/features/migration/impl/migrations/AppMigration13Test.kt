/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.migration.impl.migrations

import io.element.android.libraries.matrix.test.paths.FakeUnusedSessionPathsCleaner
import io.element.android.tests.testutils.lambda.lambdaRecorder
import kotlinx.coroutines.test.runTest
import org.junit.Test

class AppMigration13Test {
    @Test
    fun `migration on fresh install does nothing`() = runTest {
        val cleanLambda = lambdaRecorder<Unit> { }
        val migration = AppMigration13(FakeUnusedSessionPathsCleaner(cleanLambda))
        migration.migrate(isFreshInstall = true)
        cleanLambda.assertions().isNeverCalled()
    }

    @Test
    fun `migration on upgrade cleans the unused session directories`() = runTest {
        val cleanLambda = lambdaRecorder<Unit> { }
        val migration = AppMigration13(FakeUnusedSessionPathsCleaner(cleanLambda))
        migration.migrate(isFreshInstall = false)
        cleanLambda.assertions().isCalledOnce()
    }
}

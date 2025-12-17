/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.migration.impl.migrations

import com.google.common.truth.Truth.assertThat
import io.element.android.features.rageshake.test.logs.FakeLogFilesRemover
import io.element.android.tests.testutils.lambda.lambdaRecorder
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.File

class AppMigration07Test {
    @Test
    fun `test migration`() = runTest {
        val performLambda = lambdaRecorder<(File) -> Boolean, Unit> { predicate ->
            // Test the predicate
            assertThat(predicate(File("logs-0433.log.gz"))).isTrue()
            assertThat(predicate(File("logs.2024-08-01-20.log.gz"))).isFalse()
        }
        val logsFileRemover = FakeLogFilesRemover(performLambda = performLambda)
        val migration = AppMigration07(logsFileRemover)
        migration.migrate(true)
        performLambda.assertions().isCalledOnce()
    }
}

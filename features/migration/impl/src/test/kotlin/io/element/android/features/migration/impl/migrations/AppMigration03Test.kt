/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.migration.impl.migrations

import io.element.android.features.rageshake.test.logs.FakeLogFilesRemover
import kotlinx.coroutines.test.runTest
import org.junit.Test

class AppMigration03Test {
    @Test
    fun `test migration`() = runTest {
        val logsFileRemover = FakeLogFilesRemover()
        val migration = AppMigration03(migration01 = AppMigration01(logsFileRemover))

        migration.migrate()

        logsFileRemover.performLambda.assertions().isCalledOnce()
    }
}

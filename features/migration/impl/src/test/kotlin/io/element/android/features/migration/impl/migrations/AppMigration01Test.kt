/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.migration.impl.migrations

import io.element.android.features.rageshake.test.logs.FakeLogFilesRemover
import kotlinx.coroutines.test.runTest
import org.junit.Test

class AppMigration01Test {
    @Test
    fun `test migration`() = runTest {
        val logsFileRemover = FakeLogFilesRemover()
        val migration = AppMigration01(logsFileRemover)

        migration.migrate(true)

        logsFileRemover.performLambda.assertions().isCalledOnce()
    }
}

/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.migration.impl.migrations

import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AppMigration04Test {
    @Test
    fun `test migration`() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context

        // Create fake temporary file at the path to be deleted
        val file = context.getDatabasePath(AppMigration04.NOTIFICATION_FILE_NAME)
        file.parentFile?.mkdirs()
        file.createNewFile()
        assertThat(file.exists()).isTrue()

        val migration = AppMigration04(context)

        migration.migrate(true)

        // Check that the file has been deleted
        assertThat(file.exists()).isFalse()
    }
}

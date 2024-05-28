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

        val migration = AppMigration04(context)

        migration.migrate()

        // Check that the file has been deleted
        assertThat(file.exists()).isFalse()
    }
}

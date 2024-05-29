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

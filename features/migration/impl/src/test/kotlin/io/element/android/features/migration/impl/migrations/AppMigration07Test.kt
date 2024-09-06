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
        migration.migrate()
        logsFileRemover.performLambda.assertions().isCalledOnce()
    }
}

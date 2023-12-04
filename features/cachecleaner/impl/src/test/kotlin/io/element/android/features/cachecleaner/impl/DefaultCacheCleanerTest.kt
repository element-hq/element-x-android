/*
 * Copyright (c) 2023 New Vector Ltd
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

package io.element.android.features.cachecleaner.impl

import com.google.common.truth.Truth.assertThat
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class DefaultCacheCleanerTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun `calling clearCache actually removes file in the SUBDIRS_TO_CLEANUP list`() = runTest {
        // Create temp subdirs and fill with 2 files each
        DefaultCacheCleaner.SUBDIRS_TO_CLEANUP.forEach {
            File(temporaryFolder.root, it).apply {
                mkdirs()
                File(this, "temp1").createNewFile()
                File(this, "temp2").createNewFile()
            }
        }

        // Clear cache
        aCacheCleaner().clearCache()

        // Check the files are gone but the sub dirs are not.
        DefaultCacheCleaner.SUBDIRS_TO_CLEANUP.forEach {
            File(temporaryFolder.root, it).apply {
                assertThat(exists()).isTrue()
                assertThat(isDirectory).isTrue()
                assertThat(listFiles()).isEmpty()
            }
        }
    }

    @Test
    fun `clear cache fails silently`() = runTest {
        // Set cache dir as unreadable, unwritable and unexecutable so that the deletion fails.
        check(temporaryFolder.root.setReadable(false))
        check(temporaryFolder.root.setWritable(false))
        check(temporaryFolder.root.setExecutable(false))

        aCacheCleaner().clearCache()
    }

    private fun TestScope.aCacheCleaner() = DefaultCacheCleaner(
        scope = this,
        dispatchers = this.testCoroutineDispatchers(true),
        cacheDir = temporaryFolder.root,
    )
}

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

package io.element.android.features.preferences.impl.root

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.test.core.aBuildMeta
import io.element.android.services.toolbox.test.strings.FakeStringProvider
import kotlinx.coroutines.test.runTest
import org.junit.Test

class VersionFormatterTest {
    @Test
    fun `version formatter should return simplified version for main branch`() = runTest {
        val sut = DefaultVersionFormatter(
            stringProvider = FakeStringProvider(defaultResult = VERSION),
            buildMeta = aBuildMeta(
                gitBranchName = "main",
                versionName = "versionName",
                versionCode = 123
            )
        )
        assertThat(sut.get()).isEqualTo("${VERSION}versionName, 123")
    }

    @Test
    fun `version formatter should return simplified version for other branch`() = runTest {
        val sut = DefaultVersionFormatter(
            stringProvider = FakeStringProvider(defaultResult = VERSION),
            buildMeta = aBuildMeta(
                versionName = "versionName",
                versionCode = 123,
                gitBranchName = "branch",
                gitRevision = "1234567890",
            )
        )
        assertThat(sut.get()).isEqualTo("${VERSION}versionName, 123\nbranch (1234567890)")
    }

    companion object {
        const val VERSION = "version"
    }
}

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

package io.element.android.features.rageshake.impl.reporter

import com.google.common.truth.Truth.assertThat
import io.element.android.appconfig.RageshakeConfig
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Test

class DefaultBugReporterUrlProviderTest {
    @Test
    fun `test DefaultBugReporterUrlProvider`() {
        val sut = DefaultBugReporterUrlProvider()
        val result = sut.provide()
        assertThat(result).isEqualTo(RageshakeConfig.BUG_REPORT_URL.toHttpUrl())
    }
}

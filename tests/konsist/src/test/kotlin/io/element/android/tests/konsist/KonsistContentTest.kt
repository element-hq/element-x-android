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

package io.element.android.tests.konsist

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withImportNamed
import com.lemonappdev.konsist.api.verify.assertFalse
import org.junit.Test

class KonsistContentTest {
    @Test
    fun `assert that BuildConfig dot VersionCode is not used`() {
        Konsist
            .scopeFromProduction()
            .files
            .withImportNamed("io.element.android.x.BuildConfig")
            .assertFalse(additionalMessage = "Please do not use BuildConfig.VERSION_CODE, but use the versionCode from BuildMeta") {
                it.text.contains("BuildConfig.VERSION_CODE")
            }
    }
}

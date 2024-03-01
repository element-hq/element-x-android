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

package ui

import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Screenshot test for the Locale other then English.
 */
@RunWith(TestParameterInjector::class)
class T : ScreenshotTest() {
    /**
     * *Note*: keep the method name as short as possible to get shorter filename for generated screenshot.
     * Long name was preview_test.
     */
    @SuppressWarnings("MemberNameEqualsClassName")
    @Test
    fun t(
        @TestParameter(valuesProvider = PreviewProvider::class) componentTestPreview: TestPreview,
        @TestParameter baseDeviceConfig: BaseDeviceConfig,
        @TestParameter(value = ["1.0"]) fontScale: Float,
        @TestParameter(value = ["de"]) localeStr: String,
    ) {
        // Only test ComponentTestPreview, and only with the light theme
        if (componentTestPreview.isNightMode() || componentTestPreview !is ComponentTestPreview) {
            return
        }
        doTest(
            componentTestPreview = componentTestPreview,
            baseDeviceConfig = baseDeviceConfig,
            fontScale = fontScale,
            localeStr = localeStr,
        )
    }
}

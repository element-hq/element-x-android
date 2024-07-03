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

package translations

import android.content.res.Configuration
import base.ComposablePreviewProvider
import base.PaparazziPreviewRule
import base.ScreenshotTest
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import sergio.sastre.composable.preview.scanner.android.AndroidPreviewInfo
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview

/**
 * Test that takes a preview and a locale and runs a screenshot test on it.
 */
@RunWith(TestParameterInjector::class)
class TranslationsScreenshotTest(
    @TestParameter(valuesProvider = ComposablePreviewProvider::class)
    val indexedPreview: IndexedValue<ComposablePreview<AndroidPreviewInfo>>,
    @TestParameter(value = ["de"])
    val localeStr: String,
) {
    @get:Rule
    val paparazziRule = PaparazziPreviewRule.createFor(indexedPreview.value, locale = localeStr)

    @Test
    fun snapshot() {
        val (_, preview) = indexedPreview
        // Skip for dark mode screenshots
        if (preview.previewInfo.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
            return
        }
        // Skip for design system screenshots
        if (preview.previewInfo.name.startsWith("io.element.android.libraries.designsystem")) {
            return
        }
        ScreenshotTest.runTest(paparazzi = paparazziRule, preview = preview, localeStr = localeStr)
    }
}

/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
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

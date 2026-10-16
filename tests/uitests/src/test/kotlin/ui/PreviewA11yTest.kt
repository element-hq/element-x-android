/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package ui

import app.cash.paparazzi.accessibility.AccessibilityRenderExtension
import base.ComposableA11yPreviewProvider
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
 * Test that takes a preview and runs a screenshot test on it.
 * It uses [ComposableA11yPreviewProvider] to test only previews that ends with "A11yPreview".
 */
@RunWith(TestParameterInjector::class)
class PreviewA11yTest(
    @TestParameter(valuesProvider = ComposableA11yPreviewProvider::class)
    val preview: ComposablePreview<AndroidPreviewInfo>,
) {
    @get:Rule
    val paparazziRule = PaparazziPreviewRule.createFor(
        preview = preview,
        locale = "en",
        renderExtensions = setOf(AccessibilityRenderExtension()),
    )

    @Test
    fun snapshot() {
        ScreenshotTest.runTest(paparazzi = paparazziRule, preview = preview, localeStr = "en")
    }
}

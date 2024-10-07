/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package ui

import base.PaparazziPreviewRule
import base.ScreenshotTest
import base.Shard4ComposablePreviewProvider
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import sergio.sastre.composable.preview.scanner.android.AndroidPreviewInfo
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview

/**
 * Test that takes a preview and runs a screenshot test on it.
 * It uses a sharded preview provider so multiple 'shards' can run in parallel, optimizing CPU and time usage.
 */
@RunWith(TestParameterInjector::class)
class PreviewShard4Test(
    @TestParameter(valuesProvider = Shard4ComposablePreviewProvider::class)
    val preview: ComposablePreview<AndroidPreviewInfo>,
) {
    @get:Rule
    val paparazziRule = PaparazziPreviewRule.createFor(preview, locale = "en")

    @Test
    fun snapshot() {
        ScreenshotTest.runTest(paparazzi = paparazziRule, preview = preview, localeStr = "en")
    }
}

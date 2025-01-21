/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

@file:Suppress("DEPRECATION")

package base

import com.google.testing.junit.testparameterinjector.TestParameter
import sergio.sastre.composable.preview.scanner.android.AndroidComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.android.AndroidPreviewInfo
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview

// Make sure we don't import Compound previews by mistake
private val PACKAGE_TREES = arrayOf(
    "io.element.android.features",
    "io.element.android.libraries",
    "io.element.android.services",
    "io.element.android.appicon",
    "io.element.android.appnav",
    "io.element.android.x",
)

object ComposablePreviewProvider : TestParameter.TestParameterValuesProvider {
    private val values: List<IndexedValue<ComposablePreview<AndroidPreviewInfo>>> by lazy {
        AndroidComposablePreviewScanner()
            .scanPackageTrees(*PACKAGE_TREES)
            .getPreviews()
            .withIndex()
            .toList()
    }

    override fun provideValues(): List<IndexedValue<ComposablePreview<AndroidPreviewInfo>>> = values
}

object Shard1ComposablePreviewProvider : TestParameter.TestParameterValuesProvider {
    override fun provideValues(): List<ComposablePreview<AndroidPreviewInfo>> =
        ComposablePreviewProvider.provideValues().filter { it.index % 4 == 0 }.map { it.value }
}

object Shard2ComposablePreviewProvider : TestParameter.TestParameterValuesProvider {
    override fun provideValues(): List<ComposablePreview<AndroidPreviewInfo>> =
        ComposablePreviewProvider.provideValues().filter { it.index % 4 == 1 }.map { it.value }
}

object Shard3ComposablePreviewProvider : TestParameter.TestParameterValuesProvider {
    override fun provideValues(): List<ComposablePreview<AndroidPreviewInfo>> =
        ComposablePreviewProvider.provideValues().filter { it.index % 4 == 2 }.map { it.value }
}

object Shard4ComposablePreviewProvider : TestParameter.TestParameterValuesProvider {
    override fun provideValues(): List<ComposablePreview<AndroidPreviewInfo>> =
        ComposablePreviewProvider.provideValues().filter { it.index % 4 == 3 }.map { it.value }
}

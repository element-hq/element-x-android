/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:Suppress("DEPRECATION")

package base

import com.google.testing.junit.testparameterinjector.TestParameterValuesProvider
import io.element.android.tests.screenshotutils.PreviewScanner
import sergio.sastre.composable.preview.scanner.android.AndroidPreviewInfo
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview

// Make sure we don't import Compound previews by mistake
private val previewScanner = PreviewScanner(
    "io.element.android.features",
    "io.element.android.libraries",
    "io.element.android.services",
    "io.element.android.appicon",
    "io.element.android.appnav",
    "io.element.android.x",
)

private const val SHARD_COUNT = 4

object ComposablePreviewProvider : TestParameterValuesProvider() {
    override fun provideValues(context: Context): List<IndexedValue<ComposablePreview<AndroidPreviewInfo>>> = previewScanner.previews
}

object ComposableA11yPreviewProvider : TestParameterValuesProvider() {
    override fun provideValues(context: Context): List<ComposablePreview<AndroidPreviewInfo>> = previewScanner.a11yPreviews
}

object Shard1ComposablePreviewProvider : TestParameterValuesProvider() {
    override fun provideValues(context: Context): List<ComposablePreview<AndroidPreviewInfo>> = previewScanner.shard(0, SHARD_COUNT)
}

object Shard2ComposablePreviewProvider : TestParameterValuesProvider() {
    override fun provideValues(context: Context): List<ComposablePreview<AndroidPreviewInfo>> = previewScanner.shard(1, SHARD_COUNT)
}

object Shard3ComposablePreviewProvider : TestParameterValuesProvider() {
    override fun provideValues(context: Context): List<ComposablePreview<AndroidPreviewInfo>> = previewScanner.shard(2, SHARD_COUNT)
}

object Shard4ComposablePreviewProvider : TestParameterValuesProvider() {
    override fun provideValues(context: Context): List<ComposablePreview<AndroidPreviewInfo>> = previewScanner.shard(3, SHARD_COUNT)
}

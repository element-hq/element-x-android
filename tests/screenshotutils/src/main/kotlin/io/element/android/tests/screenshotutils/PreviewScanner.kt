/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:Suppress("DEPRECATION")

package io.element.android.tests.screenshotutils

import sergio.sastre.composable.preview.scanner.android.AndroidComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.android.AndroidPreviewInfo
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview

/**
 * Scans the Composable previews declared in the given package trees, and splits them for the screenshot tests.
 * The packages are scanned only once, when the previews are first accessed.
 */
class PreviewScanner(
    private vararg val packageTrees: String,
) {
    private val allPreviews: List<ComposablePreview<AndroidPreviewInfo>> by lazy {
        AndroidComposablePreviewScanner()
            .scanPackageTrees(*packageTrees)
            .getPreviews()
    }

    /**
     * The previews, except the accessibility ones, with a stable index which can be used to shard them.
     */
    val previews: List<IndexedValue<ComposablePreview<AndroidPreviewInfo>>> by lazy {
        allPreviews
            .filter { composablePreview -> composablePreview.methodName.endsWith(A11Y_PREVIEW_SUFFIX).not() }
            .withIndex()
            .toList()
    }

    /**
     * The accessibility previews, i.e. the previews whose function name ends with "A11yPreview".
     */
    val a11yPreviews: List<ComposablePreview<AndroidPreviewInfo>> by lazy {
        allPreviews
            .filter { composablePreview -> composablePreview.methodName.endsWith(A11Y_PREVIEW_SUFFIX) }
    }

    /**
     * The previews of the shard [index] (starting from 0), when the [previews] are split in [count] shards.
     */
    fun shard(index: Int, count: Int): List<ComposablePreview<AndroidPreviewInfo>> {
        require(index in 0 until count) { "Invalid shard index $index for $count shards" }
        return previews.filter { it.index % count == index }.map { it.value }
    }

    private companion object {
        const val A11Y_PREVIEW_SUFFIX = "A11yPreview"
    }
}

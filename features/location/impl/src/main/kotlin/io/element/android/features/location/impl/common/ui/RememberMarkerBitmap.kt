/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.common.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionContext
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCompositionContext
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalView
import androidx.core.graphics.createBitmap

/**
 * Renders a composable [content] to an Android [Bitmap].
 * Useful for MapLibre SymbolLayer rendering.
 *
 * Uses a temporary ComposeView to render off-screen without
 * adding to the visible composition tree.
 *
 * Note: This function provides a software-only ImageLoader to avoid
 * "Software rendering doesn't support hardware bitmaps" errors when
 * rendering Coil images to a Canvas.
 *
 * @param keys to trigger recomposition.
 * @return The rendered Android [Bitmap].
 */
@Composable
fun rememberMarkerBitmap(
    vararg keys: Any,
    content: @Composable () -> Unit,
): Bitmap {
    val parent = LocalView.current as ViewGroup
    val compositionContext = rememberCompositionContext()
    return remember(parent, compositionContext, *keys) {
        renderComposableToBitmap(parent, compositionContext, content)
    }
}

private fun renderComposableToBitmap(
    parent: ViewGroup,
    compositionContext: CompositionContext,
    content: @Composable () -> Unit,
): Bitmap {
    val composeView = ComposeView(parent.context).apply {
        setParentCompositionContext(compositionContext)
        setContent(content)
    }
    // Temporarily add to parent for measurement
    parent.addView(
        composeView,
        ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    )
    // Measure
    composeView.measure(
        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    )

    val width = composeView.measuredWidth
    val height = composeView.measuredHeight

    // Layout
    composeView.layout(0, 0, width, height)

    // Draw to bitmap
    val bitmap = createBitmap(width, height)
    val canvas = Canvas(bitmap)
    composeView.draw(canvas)

    // Cleanup
    parent.removeView(composeView)

    return bitmap
}

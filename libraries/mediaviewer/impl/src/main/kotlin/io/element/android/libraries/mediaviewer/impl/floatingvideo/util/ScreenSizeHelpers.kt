/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.floatingvideo.util

import android.content.Context
import android.graphics.Point
import android.os.Build
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager

/** Edge inset applied when the floating window is minimized (bmarty review). */
const val MINIMIZED_EDGE_INSET_DP = 16

fun WindowManager?.getScreenWidth(): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        this?.currentWindowMetrics?.bounds?.width() ?: 0
    } else {
        val displayMetrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        this?.defaultDisplay?.getMetrics(displayMetrics)
        displayMetrics.widthPixels
    }
}

fun WindowManager?.getScreenHeight(): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        this?.currentWindowMetrics?.bounds?.height() ?: 0
    } else {
        val displayMetrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        this?.defaultDisplay?.getMetrics(displayMetrics)
        displayMetrics.heightPixels
    }
}

fun Context.dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()

fun updateWindowSize(
    aspectRatio: Float,
    isMinimized: Boolean,
    windowManager: WindowManager?,
    windowLayoutParams: WindowManager.LayoutParams,
    floatingView: View?,
) {
    val wm = windowManager ?: return
    if (floatingView?.parent == null) return
    val newSize = calculateDimensions(aspectRatio, isMinimized, wm.getScreenWidth())
    windowLayoutParams.width = newSize.x
    windowLayoutParams.height = newSize.y
    wm.updateViewLayout(floatingView, windowLayoutParams)
}

fun movePosition(
    x: Int,
    y: Int,
    windowLayoutParams: WindowManager.LayoutParams,
    floatingView: View?,
    windowManager: WindowManager?,
) {
    windowLayoutParams.x += x
    windowLayoutParams.y += y
    windowManager?.updateViewLayout(floatingView, windowLayoutParams)
}

fun minimizeWindowHelper(
    aspectRatio: Float,
    windowManager: WindowManager?,
    windowLayoutParams: WindowManager.LayoutParams,
    floatingView: View?,
    edgeInsetPx: Int,
) {
    val wm = windowManager ?: return
    if (floatingView?.parent == null) return
    val screenHeight = wm.getScreenHeight()
    val targetSize = calculateDimensions(aspectRatio, true, wm.getScreenWidth())
    windowLayoutParams.x = edgeInsetPx
    windowLayoutParams.y = (screenHeight - targetSize.y - edgeInsetPx).coerceAtLeast(edgeInsetPx)
    updateWindowSize(aspectRatio, true, wm, windowLayoutParams, floatingView)
}

fun maximizeWindowHelper(
    aspectRatio: Float,
    windowManager: WindowManager?,
    windowLayoutParams: WindowManager.LayoutParams,
    floatingView: View?,
) {
    val wm = windowManager ?: return
    if (floatingView?.parent == null) return
    val screenWidth = wm.getScreenWidth()
    val screenHeight = wm.getScreenHeight()
    val targetSize = calculateDimensions(aspectRatio, false, screenWidth)
    windowLayoutParams.x = (screenWidth - targetSize.x) / 2
    windowLayoutParams.y = (screenHeight - targetSize.y) / 2
    updateWindowSize(aspectRatio, false, wm, windowLayoutParams, floatingView)
}

private fun calculateDimensions(aspectRatio: Float, isMinimized: Boolean, screenWidth: Int): Point {
    val width = if (isMinimized) {
        val widthFraction = if (aspectRatio > 1f) 0.6f else 0.35f
        (screenWidth * widthFraction).toInt()
    } else {
        (screenWidth * 0.9f).toInt()
    }
    return Point(width, (width / aspectRatio).toInt())
}

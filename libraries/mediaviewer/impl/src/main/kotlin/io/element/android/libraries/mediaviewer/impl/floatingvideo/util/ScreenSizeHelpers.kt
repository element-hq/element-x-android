/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.floatingvideo.util

import android.content.Context
import android.graphics.Point
import android.os.Build
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager

fun WindowManager?.getScreenWidth(): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val windowMetrics = this?.currentWindowMetrics
        windowMetrics?.bounds?.width() ?: 0
    } else {
        val displayMetrics = DisplayMetrics()
        @Suppress("DEPRECATION") this?.defaultDisplay?.getMetrics(displayMetrics)
        displayMetrics.widthPixels
    }
}

fun WindowManager?.getScreenHeight(): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val windowMetrics = this?.currentWindowMetrics
        windowMetrics?.bounds?.height() ?: 0
    } else {
        val displayMetrics = DisplayMetrics()
        @Suppress("DEPRECATION") this?.defaultDisplay?.getMetrics(displayMetrics)
        displayMetrics.heightPixels
    }
}

fun updateWindowSize(
    aspectRatio: Float,
    isMinimized: Boolean,
    windowManager: WindowManager?,
    windowLayoutParams: WindowManager.LayoutParams,
    floatingView: View?
) {
    val wm = windowManager ?: return
    if (floatingView?.parent == null) return

    // Just call our new helper
    val newSize = calculateDimensions(
        aspectRatio = aspectRatio,
        isMinimized = isMinimized,
        screenWidth = wm.getScreenWidth()
    )

    windowLayoutParams.width = newSize.x
    windowLayoutParams.height = newSize.y
    wm.updateViewLayout(floatingView, windowLayoutParams)
}

fun movePosition(x: Int, y: Int, windowLayoutParams: WindowManager.LayoutParams, floatingView: View?, windowManager: WindowManager?) {
    val newX = windowLayoutParams.x + x
    val newY = windowLayoutParams.y + y
    windowLayoutParams.x = newX
    windowLayoutParams.y = newY
    windowManager?.updateViewLayout(floatingView, windowLayoutParams)
}

fun minimizeWindowHelper(
    aspectRatio: Float,
    windowManager: WindowManager?,
    windowLayoutParams: WindowManager.LayoutParams,
    floatingView: View?
) {
    val wm = windowManager ?: return
    if (floatingView?.parent == null) return

    val screenWidth = wm.getScreenWidth()
    val screenHeight = wm.getScreenHeight()

    // Get the upcoming size from our helper
    val targetSize = calculateDimensions(
        aspectRatio = aspectRatio,
        isMinimized = true,
        screenWidth = screenWidth
    )

    // Use the calculated size to set position
    windowLayoutParams.x = 0
    windowLayoutParams.y = screenHeight - targetSize.y // Use targetSize.y (height)

    // Apply the final layout
    updateWindowSize(
        aspectRatio = aspectRatio,
        isMinimized = true,
        windowManager = wm,
        windowLayoutParams = windowLayoutParams,
        floatingView = floatingView
    )
}

fun maximizeWindowHelper(
    aspectRatio: Float,
    windowManager: WindowManager?,
    windowLayoutParams: WindowManager.LayoutParams,
    floatingView: View?
) {
    val wm = windowManager ?: return
    if (floatingView?.parent == null) return

    val screenWidth = wm.getScreenWidth()
    val screenHeight = wm.getScreenHeight()

    // Get the upcoming size from our helper
    val targetSize = calculateDimensions(
        aspectRatio = aspectRatio,
        isMinimized = false,
        screenWidth = screenWidth
    )
    // Use the calculated size to set position
    windowLayoutParams.x = (screenWidth - targetSize.x) / 2  // Use targetSize.x (width)
    windowLayoutParams.y = (screenHeight - targetSize.y) / 2 // Use targetSize.y (height)

    // Apply the final layout
    updateWindowSize(
        aspectRatio = aspectRatio,
        isMinimized = false,
        windowManager = wm,
        windowLayoutParams = windowLayoutParams,
        floatingView = floatingView
    )
}
private fun calculateDimensions(
    aspectRatio: Float,
    isMinimized: Boolean,
    screenWidth: Int
): Point {
    val width = if (isMinimized) {
        val widthFraction = if (aspectRatio > 1f) 0.6f else 0.35f
        (screenWidth * widthFraction).toInt()
    } else {
        // Maximized
        (screenWidth * 0.9f).toInt()
    }
    val height = (width / aspectRatio).toInt()
    return Point(width, height)
}

fun Context.dpToPx(dp: Int): Int {
    return (dp * resources.displayMetrics.density).toInt()
}

/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.floatingvideo.util

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

fun updateWindowSize(aspectRatio: Float, isMaximized: Boolean, windowManager: WindowManager?, windowLayoutParams: WindowManager.LayoutParams, floatingView: View?) {
    val widthFrac = if (aspectRatio > 1f) 0.6f else 0.3f
    val width = if (isMaximized) {
        (windowManager.getScreenWidth() * widthFrac).toInt()
    } else {
        (windowManager.getScreenWidth() * 0.9f).toInt()
    }
    val height = (width / aspectRatio).toInt()


    windowLayoutParams.width = width
    windowLayoutParams.height = height
    windowLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
    windowManager?.updateViewLayout(floatingView, windowLayoutParams)
}

fun movePosition(x: Int, y: Int, windowLayoutParams: WindowManager.LayoutParams, floatingView: View?, windowManager: WindowManager?) {
    val newX = windowLayoutParams.x + x
    val newY = windowLayoutParams.y + y
    windowLayoutParams.x = newX
    windowLayoutParams.y = newY
    windowManager?.updateViewLayout(floatingView, windowLayoutParams)
}

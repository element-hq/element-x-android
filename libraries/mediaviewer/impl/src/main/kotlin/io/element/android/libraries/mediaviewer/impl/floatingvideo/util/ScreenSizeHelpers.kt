/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.floatingvideo.util

import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager

fun WindowManager?.getScreenWidth() : Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val windowMetrics = this?.currentWindowMetrics
        windowMetrics?.bounds?.width() ?: 0
    } else {
        val displayMetrics = DisplayMetrics()
        @Suppress("DEPRECATION") this?.defaultDisplay?.getMetrics(displayMetrics)
        displayMetrics.widthPixels
    }
}
fun WindowManager?.getScreenHeight() : Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val windowMetrics = this?.currentWindowMetrics
        windowMetrics?.bounds?.height() ?: 0
    } else {
        val displayMetrics = DisplayMetrics()
        @Suppress("DEPRECATION") this?.defaultDisplay?.getMetrics(displayMetrics)
        displayMetrics.heightPixels
    }
}

/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.androidutils.bitmap

import android.graphics.Bitmap
import androidx.core.graphics.scale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Approximate Gaussian blur by downsampling, running a stack blur, then scaling back up.
 */
fun Bitmap.blur(radius: Float): Bitmap {
    val safeRadius = radius.coerceAtLeast(1f)
    val scale = (1f / (safeRadius / 8f).coerceIn(2f, 6f))
    val smallWidth = max(8, (width * scale).roundToInt())
    val smallHeight = max(8, (height * scale).roundToInt())
    val small = scale(smallWidth, smallHeight)
    val blurred = small.stackBlur((safeRadius / 2f).roundToInt().coerceIn(8, 25))
    return if (smallWidth == width && smallHeight == height) {
        blurred
    } else {
        blurred.scale(width, height)
    }
}

/**
 * Mario Klingemann stack blur. [radius] is in pixels in `1..25`.
 */
fun Bitmap.stackBlur(radius: Int): Bitmap {
    val blurRadius = radius.coerceIn(1, 25)
    val bitmap = copy(Bitmap.Config.ARGB_8888, true)
    val w = bitmap.width
    val h = bitmap.height
    val pix = IntArray(w * h)
    bitmap.getPixels(pix, 0, w, 0, 0, w, h)

    val wm = w - 1
    val hm = h - 1
    val div = blurRadius + blurRadius + 1
    val r = IntArray(w * h)
    val g = IntArray(w * h)
    val b = IntArray(w * h)
    val vmin = IntArray(max(w, h))
    var divsum = (div + 1) shr 1
    divsum *= divsum
    val dv = IntArray(256 * divsum)
    for (i in dv.indices) {
        dv[i] = i / divsum
    }

    var yi = 0
    var yw = 0
    val stack = Array(div) { IntArray(3) }
    val r1 = blurRadius + 1
    for (y in 0 until h) {
        var bsum = 0
        var gsum = 0
        var rsum = 0
        var boutsum = 0
        var goutsum = 0
        var routsum = 0
        var binsum = 0
        var ginsum = 0
        var rinsum = 0
        for (i in -blurRadius..blurRadius) {
            val p = pix[yi + min(wm, max(i, 0))]
            val sir = stack[i + blurRadius]
            sir[0] = p and 0xff0000 shr 16
            sir[1] = p and 0x00ff00 shr 8
            sir[2] = p and 0x0000ff
            val rbs = r1 - abs(i)
            rsum += sir[0] * rbs
            gsum += sir[1] * rbs
            bsum += sir[2] * rbs
            if (i > 0) {
                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]
            } else {
                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]
            }
        }
        var stackpointer = blurRadius
        for (x in 0 until w) {
            r[yi] = dv[rsum]
            g[yi] = dv[gsum]
            b[yi] = dv[bsum]
            rsum -= routsum
            gsum -= goutsum
            bsum -= boutsum
            val stackstart = stackpointer - blurRadius + div
            val sir = stack[stackstart % div]
            routsum -= sir[0]
            goutsum -= sir[1]
            boutsum -= sir[2]
            if (y == 0) {
                vmin[x] = min(x + blurRadius + 1, wm)
            }
            val p = pix[yw + vmin[x]]
            sir[0] = p and 0xff0000 shr 16
            sir[1] = p and 0x00ff00 shr 8
            sir[2] = p and 0x0000ff
            rinsum += sir[0]
            ginsum += sir[1]
            binsum += sir[2]
            rsum += rinsum
            gsum += ginsum
            bsum += binsum
            stackpointer = (stackpointer + 1) % div
            val sir2 = stack[stackpointer % div]
            routsum += sir2[0]
            goutsum += sir2[1]
            boutsum += sir2[2]
            rinsum -= sir2[0]
            ginsum -= sir2[1]
            binsum -= sir2[2]
            yi++
        }
        yw += w
    }

    for (x in 0 until w) {
        var bsum = 0
        var gsum = 0
        var rsum = 0
        var boutsum = 0
        var goutsum = 0
        var routsum = 0
        var binsum = 0
        var ginsum = 0
        var rinsum = 0
        var yiStart = -blurRadius * w
        for (i in -blurRadius..blurRadius) {
            yi = max(0, yiStart) + x
            val sir = stack[i + blurRadius]
            sir[0] = r[yi]
            sir[1] = g[yi]
            sir[2] = b[yi]
            val rbs = r1 - abs(i)
            rsum += r[yi] * rbs
            gsum += g[yi] * rbs
            bsum += b[yi] * rbs
            if (i > 0) {
                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]
            } else {
                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]
            }
            if (i < hm) {
                yiStart += w
            }
        }
        yi = x
        var stackpointer = blurRadius
        for (y in 0 until h) {
            pix[yi] = (pix[yi] and BLACK_ALPHA) or (dv[rsum] shl 16) or (dv[gsum] shl 8) or dv[bsum]
            rsum -= routsum
            gsum -= goutsum
            bsum -= boutsum
            val stackstart = stackpointer - blurRadius + div
            val sir = stack[stackstart % div]
            routsum -= sir[0]
            goutsum -= sir[1]
            boutsum -= sir[2]
            if (x == 0) {
                vmin[y] = min(y + r1, hm) * w
            }
            val p = x + vmin[y]
            sir[0] = r[p]
            sir[1] = g[p]
            sir[2] = b[p]
            rinsum += sir[0]
            ginsum += sir[1]
            binsum += sir[2]
            rsum += rinsum
            gsum += ginsum
            bsum += binsum
            stackpointer = (stackpointer + 1) % div
            val sir2 = stack[stackpointer]
            routsum += sir2[0]
            goutsum += sir2[1]
            boutsum += sir2[2]
            rinsum -= sir2[0]
            ginsum -= sir2[1]
            binsum -= sir2[2]
            yi += w
        }
    }
    bitmap.setPixels(pix, 0, w, 0, 0, w, h)
    return bitmap
}

private const val BLACK_ALPHA = -16777216

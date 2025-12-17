/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rageshake.api.screenshot

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import android.view.View

fun View.screenshot(bitmapCallback: (ImageResult) -> Unit) {
    try {
        val handler = Handler(Looper.getMainLooper())
        val bitmap = Bitmap.createBitmap(
            width,
            height,
            Bitmap.Config.ARGB_8888,
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PixelCopy.request(
                (this.context as Activity).window,
                clipBounds,
                bitmap,
                {
                    when (it) {
                        PixelCopy.SUCCESS -> {
                            bitmapCallback.invoke(ImageResult.Success(bitmap))
                        }
                        else -> {
                            bitmapCallback.invoke(ImageResult.Error(Exception(it.toString())))
                        }
                    }
                },
                handler
            )
        } else {
            handler.post {
                val canvas = Canvas(bitmap)
                    .apply {
                        translate(-clipBounds.left.toFloat(), -clipBounds.top.toFloat())
                    }
                this.draw(canvas)
                canvas.setBitmap(null)
                bitmapCallback.invoke(ImageResult.Success(bitmap))
            }
        }
    } catch (e: Exception) {
        bitmapCallback.invoke(ImageResult.Error(e))
    }
}

sealed interface ImageResult {
    data class Error(val exception: Exception) : ImageResult
    data class Success(val data: Bitmap) : ImageResult
}

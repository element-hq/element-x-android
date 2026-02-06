/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.blurhash

import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntSize

fun Modifier.blurHashBackground(blurHash: String?, alpha: Float = 1f) = this.composed {
    val blurHashBitmap = rememberBlurHashImage(blurHash)
    val hasAlpha = remember(blurHash) {
        val pixels = intArrayOf(0)
        blurHashBitmap?.readPixels(buffer = pixels, width = 1, height = 1)
        Color(pixels[0]).alpha < 1f
    }
    if (blurHashBitmap != null) {
        if (hasAlpha) {
            this
        } else {
            Modifier.drawBehind {
                drawImage(blurHashBitmap, dstSize = IntSize(size.width.toInt(), size.height.toInt()), alpha = alpha)
            }
        }
    } else {
        this
    }
}

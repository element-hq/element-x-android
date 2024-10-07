/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.blurhash

import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.unit.IntSize

fun Modifier.blurHashBackground(blurHash: String?, alpha: Float = 1f) = this.composed {
    val blurHashBitmap = rememberBlurHashImage(blurHash)
    if (blurHashBitmap != null) {
        Modifier.drawBehind {
            drawImage(blurHashBitmap, dstSize = IntSize(size.width.toInt(), size.height.toInt()), alpha = alpha)
        }
    } else {
        this
    }
}

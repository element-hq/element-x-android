/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaupload.impl

import android.graphics.Matrix
import androidx.annotation.OptIn
import androidx.media3.common.Effect
import androidx.media3.common.util.Size
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.LanczosResample
import androidx.media3.effect.MatrixTransformation
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Center-crops a frame to a square at the input resolution (no scale).
 */
@OptIn(UnstableApi::class)
internal class CenterSquareCrop : MatrixTransformation {
    private val matrix = Matrix()

    override fun configure(inputWidth: Int, inputHeight: Int): Size {
        require(inputWidth > 0 && inputHeight > 0)
        val (left, right, bottom, top) = ndcCrop(inputWidth, inputHeight)
        val scaleX = (right - left) / NDC_RANGE
        val scaleY = (top - bottom) / NDC_RANGE
        matrix.reset()
        matrix.postTranslate((left + right) / 2f, (bottom + top) / 2f)
        matrix.postScale(scaleX, scaleY)
        matrix.invert(matrix)
        return Size(
            (inputWidth * scaleX).roundToInt().coerceAtLeast(1),
            (inputHeight * scaleY).roundToInt().coerceAtLeast(1),
        )
    }

    override fun getMatrix(presentationTimeUs: Long): Matrix = matrix

    override fun isNoOp(inputWidth: Int, inputHeight: Int): Boolean = inputWidth == inputHeight
}

@OptIn(UnstableApi::class)
internal fun squareCropEffects(maxSide: Int, croppedSide: Int): List<Effect> {
    return buildList {
        add(CenterSquareCrop())
        if (croppedSide > maxSide) {
            add(LanczosResample.scaleToFit(maxSide, maxSide))
        }
    }
}

internal fun displaySize(width: Int, height: Int, rotation: Int): Pair<Int, Int> {
    return if (rotation == 90 || rotation == 270) {
        height to width
    } else {
        width to height
    }
}

internal fun croppedSquareSide(width: Int, height: Int, rotation: Int): Int {
    val (displayWidth, displayHeight) = displaySize(width, height, rotation)
    return min(displayWidth, displayHeight)
}

private data class NdcRect(val left: Float, val right: Float, val bottom: Float, val top: Float)

private fun ndcCrop(inputWidth: Int, inputHeight: Int): NdcRect {
    return when {
        inputWidth > inputHeight -> {
            val x = inputHeight.toFloat() / inputWidth
            NdcRect(-x, x, -1f, 1f)
        }
        inputHeight > inputWidth -> {
            val y = inputWidth.toFloat() / inputHeight
            NdcRect(-1f, 1f, -y, y)
        }
        else -> NdcRect(-1f, 1f, -1f, 1f)
    }
}

private const val NDC_RANGE = 2f

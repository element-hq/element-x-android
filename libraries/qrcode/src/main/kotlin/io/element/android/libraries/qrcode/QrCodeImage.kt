/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.qrcode

import android.graphics.Bitmap
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import com.google.zxing.BarcodeFormat
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import io.element.android.libraries.designsystem.modifiers.squareSize
import io.element.android.libraries.designsystem.utils.ForceMaxBrightness

private fun String.toBitMatrix(size: Int): BitMatrix {
    return QRCodeWriter().encode(
        this,
        BarcodeFormat.QR_CODE,
        size,
        size,
    )
}

private fun BitMatrix.toBitmap(
    @ColorInt backgroundColor: Int = Color.WHITE,
    @ColorInt foregroundColor: Int = Color.BLACK,
): Bitmap {
    val colorBuffer = IntArray(width * height)
    var rowOffset = 0
    for (y in 0 until height) {
        for (x in 0 until width) {
            val arrayIndex = x + rowOffset
            colorBuffer[arrayIndex] = if (get(x, y)) foregroundColor else backgroundColor
        }
        rowOffset += width
    }
    return Bitmap.createBitmap(colorBuffer, width, height, Bitmap.Config.ARGB_8888)
}

@Composable
fun QrCodeImage(
    data: String,
    modifier: Modifier = Modifier,
    forceMaxBrightness: Boolean = true,
) {
    if (forceMaxBrightness) {
        ForceMaxBrightness()
    }
    var size by remember { mutableStateOf(IntSize.Zero) }
    Box(
        modifier = modifier
            .squareSize()
            .onSizeChanged {
                size = it
            },
    ) {
        val image = remember(data, size) {
            val sideSide = maxOf(size.width, size.height).coerceAtLeast(128)
            data.toBitMatrix(sideSide).toBitmap().asImageBitmap()
        }
        Image(
            contentDescription = null,
            bitmap = image,
        )
    }
}

@Composable
@Preview
internal fun QrCodeViewPreview() {
    QrCodeImage(
        modifier = Modifier.fillMaxHeight(),
        data = "RANDOM_QRCODE_DATA",
    )
}

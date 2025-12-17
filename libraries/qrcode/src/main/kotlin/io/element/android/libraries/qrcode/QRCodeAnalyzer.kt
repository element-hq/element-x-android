/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.qrcode

import android.graphics.ImageFormat
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import timber.log.Timber
import zxingcpp.BarcodeReader

internal class QRCodeAnalyzer(
    private val onScanQrCode: (data: ByteArray) -> Unit
) : ImageAnalysis.Analyzer {
    private val reader by lazy { BarcodeReader() }

    override fun analyze(image: ImageProxy) {
        image.use {
            if (image.format in SUPPORTED_IMAGE_FORMATS) {
                try {
                    val bytes = reader.read(image).firstNotNullOfOrNull { it.bytes }
                    if (bytes != null) {
                        Timber.d("QR code scanned!")
                        onScanQrCode(bytes)
                    }
                } catch (e: Exception) {
                    Timber.w(e, "Error decoding QR code")
                }
            } else {
                Timber.w("Unsupported image format: ${image.format}")
            }
        }
    }

    companion object {
        private val SUPPORTED_IMAGE_FORMATS = listOf(
            ImageFormat.YUV_420_888,
            ImageFormat.YUV_422_888,
            ImageFormat.YUV_444_888,
        )
    }
}

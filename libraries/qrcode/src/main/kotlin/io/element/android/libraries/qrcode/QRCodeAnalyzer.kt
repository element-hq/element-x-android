/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.libraries.qrcode

import android.graphics.ImageFormat
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import timber.log.Timber
import zxingcpp.BarcodeReader

internal class QRCodeAnalyzer(
    private val onQrCodeScanned: (result: ByteArray?) -> Unit
) : ImageAnalysis.Analyzer {
    private val reader by lazy { BarcodeReader() }

    override fun analyze(image: ImageProxy) {
        if (image.format in SUPPORTED_IMAGE_FORMATS) {

            try {
                val bytes = reader.read(image).firstNotNullOfOrNull { it.bytes }
                if (bytes != null ) {
                    onQrCodeScanned(bytes)
                }
            } catch (e: Exception) {
                Timber.w(e, "Error decoding QR code")
            } finally {
                image.close()
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

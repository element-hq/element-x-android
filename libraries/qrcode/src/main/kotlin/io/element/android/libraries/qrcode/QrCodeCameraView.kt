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

import android.graphics.Bitmap
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.theme.components.Text
import timber.log.Timber

@Composable
fun QrCodeCameraView(
    onQrCodeScanned: (ByteArray) -> Unit,
    modifier: Modifier = Modifier,
    renderPreview: Boolean = true,
) {
    if (LocalInspectionMode.current) {
        Box(
            modifier = modifier
                .background(color = ElementTheme.colors.bgSubtlePrimary),
            contentAlignment = Alignment.Center,
        ) {
            Text("CameraView")
        }
    } else {
        val localContext = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current
        val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(localContext) }
        val previewUseCase = remember { Preview.Builder().build() }
        var lastFrame by remember { mutableStateOf<Bitmap?>(null) }
        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
        Box(modifier.clipToBounds()) {
            AndroidView(
                factory = { context ->
                    val previewView = PreviewView(context)
                    previewView
                },
                update = { previewView ->
                    if (renderPreview) {
                        lastFrame = null
                        val selector = CameraSelector.Builder()
                            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                            .build()
                        previewUseCase.setSurfaceProvider(previewView.surfaceProvider)
                        imageAnalysis.setAnalyzer(
                            ContextCompat.getMainExecutor(previewView.context),
                            QRCodeAnalyzer { result ->
                                result?.let {
                                    Timber.d("QR code scanned!")
                                    onQrCodeScanned(it)
                                }
                            }
                        )
                        try {
                            cameraProviderFuture.get().bindToLifecycle(
                                lifecycleOwner,
                                selector,
                                previewUseCase,
                                imageAnalysis
                            )
                            lastFrame = null
                        } catch (e: Exception) {
                            Timber.e(e, "Use case binding failed")
                        }
                    } else {
                        // Stop analyzer
                        imageAnalysis.clearAnalyzer()

                        // Save last frame to display it as the 'frozen' preview
                        if (lastFrame == null) {
                            lastFrame = previewView.bitmap
                            Timber.d("Saving last frame. Is null? ${lastFrame == null}")
                        }
                        cameraProviderFuture.get().let { cameraProvider ->
                            if (cameraProvider.isBound(previewUseCase)) {
                                cameraProvider.unbind(previewUseCase)
                            }
                        }
                    }
                },
                onRelease = {
                    cameraProviderFuture.get().unbindAll()
                },
            )
            lastFrame?.let {
                Image(bitmap = it.asImageBitmap(), contentDescription = null)
            }
        }
    }
}

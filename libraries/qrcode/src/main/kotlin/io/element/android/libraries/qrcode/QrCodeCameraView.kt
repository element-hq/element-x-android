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

import android.content.Context
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Composable
fun QrCodeCameraView(
    onScanQrCode: (ByteArray) -> Unit,
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
        val coroutineScope = rememberCoroutineScope()
        val localContext = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current
        var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
        val previewUseCase = remember { Preview.Builder().build() }
        var lastFrame by remember { mutableStateOf<Bitmap?>(null) }
        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        LaunchedEffect(Unit) {
            cameraProvider = localContext.getCameraProvider()
        }

        suspend fun startQRCodeAnalysis(cameraProvider: ProcessCameraProvider, previewView: PreviewView, attempt: Int = 1) {
            lastFrame = null
            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()
            imageAnalysis.setAnalyzer(
                ContextCompat.getMainExecutor(previewView.context),
                QRCodeAnalyzer { result ->
                    result?.let {
                        Timber.d("QR code scanned!")
                        onScanQrCode(it)
                    }
                }
            )
            try {
                // Make sure we unbind all use cases before binding them again
                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    previewUseCase,
                    imageAnalysis
                )
                lastFrame = null
            } catch (e: Exception) {
                val maxAttempts = 3
                if (attempt > maxAttempts) {
                    Timber.e(e, "Use case binding failed after $maxAttempts attempts. Giving up.")
                } else {
                    Timber.e(e, "Use case binding failed (attempt #$attempt). Retrying after a delay...")
                    delay(100)
                    startQRCodeAnalysis(cameraProvider, previewView, attempt + 1)
                }
            }
        }

        fun stopQRCodeAnalysis(previewView: PreviewView) {
            // Stop analyzer
            imageAnalysis.clearAnalyzer()

            // Save last frame to display it as the 'frozen' preview
            if (lastFrame == null) {
                lastFrame = previewView.bitmap
                Timber.d("Saving last frame for frozen preview.")
            }

            // Unbind preview use case
            cameraProvider?.unbindAll()
        }

        Box(modifier.clipToBounds()) {
            AndroidView(
                factory = { context ->
                    val previewView = PreviewView(context)
                    previewUseCase.setSurfaceProvider(previewView.surfaceProvider)
                    previewView.previewStreamState.observe(lifecycleOwner) { state ->
                        previewView.alpha = if (state == PreviewView.StreamState.STREAMING) 1f else 0f
                    }
                    previewView
                },
                update = { previewView ->
                    if (renderPreview) {
                        cameraProvider?.let { provider ->
                            coroutineScope.launch { startQRCodeAnalysis(provider, previewView) }
                        }
                    } else {
                        stopQRCodeAnalysis(previewView)
                    }
                },
                onRelease = {
                    cameraProvider?.unbindAll()
                    cameraProvider = null
                },
            )
            lastFrame?.let {
                Image(bitmap = it.asImageBitmap(), contentDescription = null)
            }
        }
    }
}

@Suppress("BlockingMethodInNonBlockingContext")
private suspend fun Context.getCameraProvider(): ProcessCameraProvider =
    suspendCoroutine { continuation ->
        ProcessCameraProvider.getInstance(this).also { cameraProvider ->
            cameraProvider.addListener({
                continuation.resume(cameraProvider.get())
            }, ContextCompat.getMainExecutor(this))
        }
    }

/*
 * Copyright (c) 2023 New Vector Ltd
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

package io.element.android.libraries.mediaviewer.api.local.pdf

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import androidx.compose.runtime.Stable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

@Stable
class PdfPage(
    maxWidth: Int,
    val pageIndex: Int,
    private val mutex: Mutex,
    private val pdfRenderer: PdfRenderer,
    private val coroutineScope: CoroutineScope,
) {

    sealed interface State {
        data class Loading(val width: Int, val height: Int) : State
        data class Loaded(val bitmap: Bitmap) : State
    }

    private val renderWidth = maxWidth
    private val renderHeight: Int
    private var loadJob: Job? = null

    init {
        // We are just opening and closing the page to extract data so we can build the Loading state with the correct dimensions.
        pdfRenderer.openPage(pageIndex).use { page ->
            renderHeight = (page.height * (renderWidth.toFloat() / page.width)).toInt()
        }
    }

    private val mutableStateFlow = MutableStateFlow<State>(
        State.Loading(
            width = renderWidth,
            height = renderHeight
        )
    )
    val stateFlow: StateFlow<State> = mutableStateFlow

    fun load() {
        loadJob = coroutineScope.launch {
            val bitmap = mutex.withLock {
                withContext(Dispatchers.IO) {
                    pdfRenderer.openPageRenderAndClose(pageIndex, renderWidth, renderHeight)
                }
            }
            mutableStateFlow.value = State.Loaded(bitmap)
        }
    }

    fun close() {
        loadJob?.cancel()
        when (val loadingState = stateFlow.value) {
            is State.Loading -> return
            is State.Loaded -> {
                loadingState.bitmap.recycle()
                mutableStateFlow.value = State.Loading(
                    width = renderWidth,
                    height = renderHeight
                )
            }
        }
    }

    private fun PdfRenderer.openPageRenderAndClose(index: Int, bitmapWidth: Int, bitmapHeight: Int): Bitmap {
        fun createBitmap(bitmapWidth: Int, bitmapHeight: Int): Bitmap {
            val bitmap = Bitmap.createBitmap(
                bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE)
            canvas.drawBitmap(bitmap, 0f, 0f, null)
            return bitmap
        }
        return openPage(index).use { page ->
            createBitmap(bitmapWidth, bitmapHeight).apply {
                page.render(this, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            }
        }
    }
}




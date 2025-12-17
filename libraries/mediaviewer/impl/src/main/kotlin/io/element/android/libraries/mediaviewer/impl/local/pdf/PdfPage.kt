/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.local.pdf

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
                bitmapWidth,
                bitmapHeight,
                Bitmap.Config.ARGB_8888
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

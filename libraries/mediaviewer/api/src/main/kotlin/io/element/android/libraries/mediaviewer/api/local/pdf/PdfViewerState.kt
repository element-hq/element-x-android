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

import android.content.Context
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CoroutineScope
import me.saket.telephoto.zoomable.ZoomableState
import me.saket.telephoto.zoomable.rememberZoomableState

@Stable
class PdfViewerState(
    private val model: Any?,
    private val coroutineScope: CoroutineScope,
    private val context: Context,
    val zoomableState: ZoomableState,
    val lazyListState: LazyListState,
) {
    var isLoaded by mutableStateOf(false)
    private var pdfRendererManager by mutableStateOf<PdfRendererManager?>(null)

    @Composable
    fun getPages(): List<PdfPage> {
        return pdfRendererManager?.run {
            pdfPages.collectAsState().value
        } ?: emptyList()
    }

    fun openForWidth(maxWidth: Int) {
        ParcelFileDescriptorFactory(context).create(model)
            .onSuccess {
                pdfRendererManager = PdfRendererManager(it, maxWidth, coroutineScope).apply {
                    open()
                }
                isLoaded = true
            }
    }

    fun close() {
        pdfRendererManager?.close()
        isLoaded = false
    }
}

@Composable
fun rememberPdfViewerState(
    model: Any?,
    zoomableState: ZoomableState = rememberZoomableState(),
    lazyListState: LazyListState = rememberLazyListState(),
    context: Context = LocalContext.current,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
): PdfViewerState {
    return remember(model) {
        PdfViewerState(
            model = model,
            coroutineScope = coroutineScope,
            context = context,
            zoomableState = zoomableState,
            lazyListState = lazyListState
        )
    }
}

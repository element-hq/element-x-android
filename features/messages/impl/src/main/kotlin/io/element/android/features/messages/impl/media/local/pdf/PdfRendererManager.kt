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

package io.element.android.features.messages.impl.media.local.pdf

import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class PdfRendererManager(
    private val parcelFileDescriptor: ParcelFileDescriptor,
    private val width: Int,
    private val coroutineScope: CoroutineScope,
) {

    private val mutex = Mutex()
    private var pdfRenderer: PdfRenderer? = null
    private val mutablePdfPages = MutableStateFlow<List<PdfPage>>(emptyList())
    val pdfPages: StateFlow<List<PdfPage>> = mutablePdfPages

    fun open() {
        coroutineScope.launch {
            mutex.withLock {
                withContext(Dispatchers.IO) {
                    pdfRenderer = PdfRenderer(parcelFileDescriptor).apply {
                        (0 until pageCount).map { pageIndex ->
                            PdfPage(width, pageIndex, mutex, this, coroutineScope)
                        }.also {
                            mutablePdfPages.value = it
                        }
                    }
                }
            }
        }
    }

    fun close() {
        coroutineScope.launch {
            mutex.withLock {
                mutablePdfPages.value.forEach { pdfPage ->
                    pdfPage.close()
                }
                pdfRenderer?.close()
                parcelFileDescriptor.close()
            }
        }
    }
}

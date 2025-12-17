/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.local.pdf

import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.core.extensions.runCatchingExceptions
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
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
    private val mutablePdfPages = MutableStateFlow<AsyncData<ImmutableList<PdfPage>>>(AsyncData.Uninitialized)
    val pdfPages: StateFlow<AsyncData<ImmutableList<PdfPage>>> = mutablePdfPages

    fun open() {
        coroutineScope.launch {
            mutex.withLock {
                withContext(Dispatchers.IO) {
                    pdfRenderer = runCatchingExceptions {
                        PdfRenderer(parcelFileDescriptor)
                    }.fold(
                        onSuccess = { pdfRenderer ->
                            pdfRenderer.apply {
                                // Preload just 3 pages so we can render faster
                                val firstPages = loadPages(from = 0, to = 3)
                                mutablePdfPages.value = AsyncData.Success(firstPages.toImmutableList())
                                val nextPages = loadPages(from = 3, to = pageCount)
                                mutablePdfPages.value = AsyncData.Success((firstPages + nextPages).toImmutableList())
                            }
                        },
                        onFailure = {
                            mutablePdfPages.value = AsyncData.Failure(it)
                            null
                        }
                    )
                }
            }
        }
    }

    fun close() {
        coroutineScope.launch {
            mutex.withLock {
                mutablePdfPages.value.dataOrNull()?.forEach { pdfPage ->
                    pdfPage.close()
                }
                pdfRenderer?.close()
                parcelFileDescriptor.close()
            }
        }
    }

    private fun PdfRenderer.loadPages(from: Int, to: Int): List<PdfPage> {
        return (from until minOf(to, pageCount)).map { pageIndex ->
            PdfPage(width, pageIndex, mutex, this, coroutineScope)
        }
    }
}

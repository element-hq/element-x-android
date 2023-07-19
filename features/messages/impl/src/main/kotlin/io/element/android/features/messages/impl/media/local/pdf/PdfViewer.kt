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

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.text.roundToPx
import io.element.android.libraries.designsystem.text.toDp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import me.saket.telephoto.zoomable.zoomable

@Composable
fun PdfViewer(
    pdfViewerState: PdfViewerState,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier.zoomable(pdfViewerState.zoomableState),
        contentAlignment = Alignment.Center
    ) {
        val maxWidthInPx = maxWidth.roundToPx()
        DisposableEffect(pdfViewerState) {
            pdfViewerState.openForWidth(maxWidthInPx)
            onDispose {
                pdfViewerState.close()
            }
        }
        val pdfPages = pdfViewerState.getPages()
        PdfPagesView(pdfPages.toImmutableList(), pdfViewerState.lazyListState)
    }
}

@Composable
private fun PdfPagesView(
    pdfPages: ImmutableList<PdfPage>,
    lazyListState: LazyListState,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = lazyListState,
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically)

    ) {
        items(pdfPages.size) { index ->
            val pdfPage = pdfPages[index]
            PdfPageView(pdfPage)
        }
    }
}

@Composable
private fun PdfPageView(
    pdfPage: PdfPage,
    modifier: Modifier = Modifier,
) {
    val pdfPageState by pdfPage.stateFlow.collectAsState()
    DisposableEffect(pdfPage) {
        pdfPage.load()
        onDispose {
            pdfPage.close()
        }
    }
    when (val state = pdfPageState) {
        is PdfPage.State.Loaded -> {
            Image(
                bitmap = state.bitmap.asImageBitmap(),
                contentDescription = "Page ${pdfPage.pageIndex}",
                contentScale = ContentScale.FillWidth,
                modifier = modifier.fillMaxWidth()
            )
        }
        is PdfPage.State.Loading -> {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .height(state.height.toDp())
                    .background(color = Color.White)
            )
        }
    }
}

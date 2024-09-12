/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.api.local.pdf

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.text.roundToPx
import io.element.android.libraries.designsystem.text.toDp
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import me.saket.telephoto.zoomable.zoomable

@Composable
fun PdfViewer(
    pdfViewerState: PdfViewerState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier
            .zoomable(
                state = pdfViewerState.zoomableState,
                onClick = { onClick() }
            ),
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
        PdfPagesView(
            pdfPages = pdfPages.toImmutableList(),
            lazyListState = pdfViewerState.lazyListState,
        )
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
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
    ) {
        // Add a fake item to the top so that the first item is not at the top of the screen.
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
        items(pdfPages.size) { index ->
            val pdfPage = pdfPages[index]
            PdfPageView(pdfPage)
        }
    }
}

@Composable
private fun PdfPageView(
    pdfPage: PdfPage,
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
                contentDescription = stringResource(id = CommonStrings.a11y_page_n, pdfPage.pageIndex),
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth()
            )
        }
        is PdfPage.State.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(state.height.toDp())
                    .background(color = Color.White)
            )
        }
    }
}

/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.local.pdf

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.text.roundToPx
import io.element.android.libraries.designsystem.text.toDp
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.mediaviewer.impl.viewer.topAppBarHeight
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.ImmutableList
import me.saket.telephoto.zoomable.zoomable
import java.io.IOException

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
            pdfPages = pdfPages,
            lazyListState = pdfViewerState.lazyListState,
        )
    }
}

@Composable
private fun PdfPagesView(
    pdfPages: AsyncData<ImmutableList<PdfPage>>,
    lazyListState: LazyListState,
    modifier: Modifier = Modifier,
) {
    when (pdfPages) {
        is AsyncData.Uninitialized,
        is AsyncData.Loading -> Unit
        is AsyncData.Failure -> PdfPagesErrorView(
            pdfPages.error,
            modifier,
        )
        is AsyncData.Success -> PdfPagesContentView(
            pdfPages = pdfPages.data,
            lazyListState = lazyListState,
            modifier = modifier
        )
    }
}

@Composable
private fun PdfPagesErrorView(
    error: Throwable,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = buildString {
                append(stringResource(id = CommonStrings.error_unknown))
                append("\n\n")
                append(error.localizedMessage)
            },
            textAlign = TextAlign.Center,
            style = ElementTheme.typography.fontBodyLgRegular,
        )
    }
}

@Composable
private fun PdfPagesContentView(
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
            Spacer(modifier = Modifier.height(topAppBarHeight))
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

@PreviewsDayNight
@Composable
internal fun PdfPagesErrorViewPreview() = ElementPreview {
    PdfPagesErrorView(
        error = IOException("file not in PDF format or corrupted"),
    )
}

/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.local.pdf

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.element.android.libraries.mediaviewer.api.local.LocalMedia
import io.element.android.libraries.mediaviewer.impl.local.LocalMediaViewState

@Composable
fun MediaPdfView(
    localMediaViewState: LocalMediaViewState,
    localMedia: LocalMedia?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pdfViewerState = rememberPdfViewerState(
        model = localMedia?.uri,
        zoomableState = localMediaViewState.zoomableState,
    )
    localMediaViewState.isReady = pdfViewerState.isLoaded
    PdfViewer(
        pdfViewerState = pdfViewerState,
        onClick = onClick,
        modifier = modifier,
    )
}

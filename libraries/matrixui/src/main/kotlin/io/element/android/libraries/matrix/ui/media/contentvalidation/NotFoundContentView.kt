/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.media.contentvalidation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.ui.strings.CommonStrings

/**
 * A view to display when the media content can't be fetched.
 */
@Composable
fun NotFoundContentView(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues.Zero,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
) {
    ContentErrorView(
        title = stringResource(CommonStrings.content_scanner_not_found_title),
        message = stringResource(CommonStrings.content_scanner_not_found),
        modifier = modifier,
        contentPadding = contentPadding,
        onTextLayout = onTextLayout,
    )
}

@PreviewsDayNight
@Composable
internal fun NotFoundContentViewPreview() = ElementPreview {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        NotFoundContentView()
    }
}

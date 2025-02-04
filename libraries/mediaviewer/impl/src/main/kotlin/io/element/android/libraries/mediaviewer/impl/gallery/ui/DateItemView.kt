/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.gallery.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.mediaviewer.impl.model.MediaItem

@Composable
fun DateItemView(
    item: MediaItem.DateSeparator,
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        text = item.formattedDate,
        textAlign = TextAlign.Center,
        style = ElementTheme.typography.fontBodyMdMedium,
        color = ElementTheme.colors.textPrimary,
    )
}

@PreviewsDayNight
@Composable
internal fun DateItemViewPreview(
    @PreviewParameter(MediaItemDateSeparatorProvider::class) date: MediaItem.DateSeparator,
) = ElementPreview {
    DateItemView(date)
}

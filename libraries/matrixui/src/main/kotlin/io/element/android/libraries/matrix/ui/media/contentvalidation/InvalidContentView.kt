/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.media.contentvalidation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight

@Composable
fun InvalidContentView(
    contentHasPreview: Boolean,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues.Zero,
    onTextLayout: (TextLayoutResult) -> Unit = {},
) {
    Row(
        modifier = modifier
            .background(color = ElementTheme.colors.bgCriticalSubtle)
            .padding(contentPadding),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = CompoundIcons.Error(),
            contentDescription = null,
            tint = ElementTheme.colors.iconCriticalPrimary
        )
        Column(Modifier.weight(1f)) {
            Text(
                text = "The file is not safe",
                color = ElementTheme.colors.textCriticalPrimary,
                style = ElementTheme.typography.fontBodyLgMedium,
            )
            val text = if (contentHasPreview) "Download and preview have been disabled" else "Download has been disabled."
            val textMeasurer = rememberTextMeasurer()
            LaunchedEffect(text) {
                onTextLayout(textMeasurer.measure(text))
            }
            Text(
                text = text,
                color = ElementTheme.colors.textSecondary,
                style = ElementTheme.typography.fontBodyMdRegular,
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun InvalidContentViewPreview() = ElementPreview {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        InvalidContentView(contentHasPreview = true)
        InvalidContentView(contentHasPreview = false)
    }
}

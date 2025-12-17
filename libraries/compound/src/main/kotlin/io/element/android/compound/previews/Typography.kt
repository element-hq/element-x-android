/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.compound.previews

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme

@Preview
@Composable
internal fun TypographyPreview() = ElementTheme {
    Surface {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            with(ElementTheme.materialTypography) {
                TypographyTokenPreview(displayLarge, "Display large")
                TypographyTokenPreview(displayMedium, "Display medium")
                TypographyTokenPreview(displaySmall, "Display small")
                TypographyTokenPreview(headlineLarge, "Headline large")
                TypographyTokenPreview(headlineMedium, "Headline medium")
                TypographyTokenPreview(headlineSmall, "Headline small")
                TypographyTokenPreview(titleLarge, "Title large")
                TypographyTokenPreview(titleMedium, "Title medium")
                TypographyTokenPreview(titleSmall, "Title small")
                TypographyTokenPreview(bodyLarge, "Body large")
                TypographyTokenPreview(bodyMedium, "Body medium")
                TypographyTokenPreview(bodySmall, "Body small")
                TypographyTokenPreview(labelLarge, "Label large")
                TypographyTokenPreview(labelMedium, "Label medium")
                TypographyTokenPreview(labelSmall, "Label small")
            }
        }
    }
}

@Composable
private fun TypographyTokenPreview(style: TextStyle, text: String) {
    Text(text = text, style = style)
}

/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.compound.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

/**
 * Data class to hold avatar colors.
 */
data class AvatarColors(
    /** Background color for the avatar. */
    val background: Color,
    /** Foreground color for the avatar. */
    val foreground: Color,
)

/**
 * Avatar colors using semantic tokens.
 */
@Composable
fun avatarColors(): List<AvatarColors> {
    return listOf(
        AvatarColors(background = ElementTheme.colors.bgDecorative1, foreground = ElementTheme.colors.textDecorative1),
        AvatarColors(background = ElementTheme.colors.bgDecorative2, foreground = ElementTheme.colors.textDecorative2),
        AvatarColors(background = ElementTheme.colors.bgDecorative3, foreground = ElementTheme.colors.textDecorative3),
        AvatarColors(background = ElementTheme.colors.bgDecorative4, foreground = ElementTheme.colors.textDecorative4),
        AvatarColors(background = ElementTheme.colors.bgDecorative5, foreground = ElementTheme.colors.textDecorative5),
        AvatarColors(background = ElementTheme.colors.bgDecorative6, foreground = ElementTheme.colors.textDecorative6),
    )
}

@Preview
@Composable
internal fun AvatarColorsPreviewLight() {
    ElementTheme {
        val chunks = avatarColors().chunked(4)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for (chunk in chunks) {
                AvatarColorRow(chunk.toImmutableList())
            }
        }
    }
}

@Preview
@Composable
internal fun AvatarColorsPreviewDark() {
    ElementTheme(darkTheme = true) {
        val chunks = avatarColors().chunked(4)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for (chunk in chunks) {
                AvatarColorRow(chunk.toImmutableList())
            }
        }
    }
}

@Composable
private fun AvatarColorRow(colors: ImmutableList<AvatarColors>) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        colors.forEach { color ->
            Box(
                modifier = Modifier.size(48.dp)
                    .background(color.background),
            ) {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = "A",
                    color = color.foreground,
                )
            }
        }
    }
}

/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.media

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import coil3.Bitmap
import io.element.android.compound.theme.AvatarColors
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.SemanticColors
import io.element.android.compound.tokens.generated.compoundColorsDark
import io.element.android.compound.tokens.generated.compoundColorsLight
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Text

/**
 * Generates a bitmap for an initials avatar based on the provided [AvatarData].
 */
class InitialsAvatarBitmapGenerator(
    useDarkTheme: Boolean = false,
    private val fontSizePercentage: Float = 0.5f,
) {
    private val compoundColors: SemanticColors = if (useDarkTheme) {
        compoundColorsDark
    } else {
        compoundColorsLight
    }

    // List of predefined avatar colors to use for initials avatars, in light mode
    private val allAvatarColors: List<AvatarColors> = listOf(
        AvatarColors(
            background = compoundColors.bgDecorative1,
            foreground = compoundColors.textDecorative1,
        ),
        AvatarColors(
            background = compoundColors.bgDecorative2,
            foreground = compoundColors.textDecorative2,
        ),
        AvatarColors(
            background = compoundColors.bgDecorative3,
            foreground = compoundColors.textDecorative3,
        ),
        AvatarColors(
            background = compoundColors.bgDecorative4,
            foreground = compoundColors.textDecorative4,
        ),
        AvatarColors(
            background = compoundColors.bgDecorative5,
            foreground = compoundColors.textDecorative5,
        ),
        AvatarColors(
            background = compoundColors.bgDecorative6,
            foreground = compoundColors.textDecorative6,
        ),
    )

    /**
     * Generates a bitmap for an avatar with no URL, using the initials from the [AvatarData].
     * @param size The size of the bitmap to generate, in pixels.
     * @param avatarData The [AvatarData] containing the initials and other information.
     */
    fun generateBitmap(size: Int, avatarData: AvatarData): Bitmap? {
        if (avatarData.url != null) {
            // This generator is only for initials avatars, not for avatars with URLs
            return null
        }

        // Get the color pair to use for the initials avatar
        val avatarColors = allAvatarColors[avatarData.id.sumOf { it.code } % allAvatarColors.size]

        val bitmap = createBitmap(size, size)
        Canvas(bitmap).run {
            drawColor(avatarColors.background.toArgb())
            val letter = avatarData.initialLetter

            val textPaint = Paint().apply {
                color = avatarColors.foreground.toArgb()
                textSize = size * fontSizePercentage // Adjust text size relative to the avatar size
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            }
            val bounds = Rect()
            textPaint.getTextBounds(letter, 0, letter.length, bounds)
            drawText(
                letter,
                size / 2f,
                size.toFloat() / 2 - (textPaint.descent() + textPaint.ascent()) / 2,
                textPaint
            )
        }

        return bitmap
    }
}

@Composable
@PreviewsDayNight
internal fun InitialsAvatarBitmapGeneratorPreview() = ElementPreview {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(6) { index ->
            val avatarData = remember { AvatarData(id = index.toString(), name = Char('0'.code + index).toString(), size = AvatarSize.IncomingCall) }
            val isLightTheme = ElementTheme.isLightTheme
            val bitmap = remember(isLightTheme) {
                val generator = InitialsAvatarBitmapGenerator(useDarkTheme = !isLightTheme)
                generator.generateBitmap(512, avatarData)?.asImageBitmap()
            }

            bitmap?.let {
                Image(bitmap = it, contentDescription = null, modifier = Modifier.size(48.dp))
            } ?: Text("No avatar generated")
        }
    }
}

/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import coil3.request.allowHardware
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.avatar.AvatarType
import io.element.android.libraries.designsystem.components.avatar.avatarShape
import io.element.android.libraries.designsystem.components.avatar.internal.ImageAvatar
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight

/**
 * Variants of location pin markers.
 */
sealed interface PinVariant {
    data class UserLocation(
        val avatarData: AvatarData,
        val isLive: Boolean,
    ) : PinVariant

    data object PinnedLocation : PinVariant
    data object StaleLocation : PinVariant
}

private val PIN_MARKER_WIDTH = 42.dp
private val PIN_MARKER_HEIGHT = (PIN_MARKER_WIDTH * 1.2f)
private val DOT_RADIUS = 6.dp
private val CONTENT_OFFSET = 5.dp

/**
 * A location pin composable that supports multiple variants.
 *
 * Based on Figma design: https://www.figma.com/design/G1xy0HDZKJf5TCRFmKb5d5/Compound-Android-Components?node-id=4665-2890&m=dev
 */
@Composable
fun LocationPin(
    variant: PinVariant,
    modifier: Modifier = Modifier,
    allowHardwareBitmapRendering: Boolean = true,
) {
    val colors = LocationPinColors.fromVariant(variant)
    Box(
        modifier = modifier.size(width = PIN_MARKER_WIDTH, height = PIN_MARKER_HEIGHT),
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawPinShape(
                fillColor = colors.fill,
                strokeColor = colors.stroke,
                strokeWidth = 1.dp.toPx(),
            )
        }
        val avatarSize = PIN_MARKER_WIDTH - CONTENT_OFFSET * 2
        val contentModifier = Modifier
            .align(Alignment.TopCenter)
            .offset(y = CONTENT_OFFSET)

        when (variant) {
            is PinVariant.UserLocation -> {
                val avatarShape = AvatarType.User.avatarShape()
                ImageAvatar(
                    avatarData = variant.avatarData,
                    forcedAvatarSize = avatarSize,
                    avatarShape = avatarShape,
                    modifier = contentModifier
                        .border(width = 1.dp, color = colors.avatarStoke, shape = avatarShape),
                    configureRequest = { builder ->
                        builder.allowHardware(allowHardwareBitmapRendering)
                    }
                )
            }
            PinVariant.PinnedLocation, PinVariant.StaleLocation -> {
                Canvas(
                    modifier = contentModifier.size(avatarSize)
                ) {
                    drawCircle(
                        color = colors.dotColor,
                        radius = DOT_RADIUS.toPx(),
                        center = center,
                    )
                }
            }
        }
    }
}

private data class LocationPinColors(
    val fill: Color,
    val stroke: Color,
    val dotColor: Color,
    val avatarStoke: Color,
) {
    companion object {
        @Composable
        fun fromVariant(variant: PinVariant): LocationPinColors {
            return when (variant) {
                is PinVariant.UserLocation ->
                    if (variant.isLive) {
                        LocationPinColors(
                            fill = ElementTheme.colors.iconAccentPrimary,
                            stroke = ElementTheme.colors.bgCanvasDefault,
                            dotColor = Color.Transparent,
                            avatarStoke = ElementTheme.colors.bgCanvasDefault,
                        )
                    } else {
                        LocationPinColors(
                            fill = ElementTheme.colors.bgCanvasDefault,
                            stroke = ElementTheme.colors.iconQuaternaryAlpha,
                            dotColor = Color.Transparent,
                            avatarStoke = ElementTheme.colors.iconQuaternaryAlpha,
                        )
                    }
                PinVariant.PinnedLocation -> LocationPinColors(
                    fill = ElementTheme.colors.bgCanvasDefault,
                    stroke = ElementTheme.colors.iconSecondaryAlpha,
                    dotColor = ElementTheme.colors.iconPrimary,
                    avatarStoke = Color.Transparent,
                )
                PinVariant.StaleLocation -> LocationPinColors(
                    fill = ElementTheme.colors.bgSubtleSecondary,
                    stroke = ElementTheme.colors.iconDisabled,
                    dotColor = ElementTheme.colors.iconDisabled,
                    avatarStoke = Color.Transparent,
                )
            }
        }
    }
}

/**
 * Draws a teardrop-shaped pin with smooth curves.
 *
 * Based on SVG path with dimensions 40x48 (ratio 1:1.2).
 * Scales automatically to fit the canvas size.
 */
private fun DrawScope.drawPinShape(
    fillColor: Color,
    strokeColor: Color,
    strokeWidth: Float,
) {
    val svgWidth = 40f
    val svgHeight = 48f
    val inset = strokeWidth / 2
    val scaleX = (size.width - strokeWidth) / svgWidth
    val scaleY = (size.height - strokeWidth) / svgHeight

    val path = Path().apply {
        moveTo(20f, 48f)
        cubicTo(19.4167f, 48f, 18.8333f, 47.8965f, 18.25f, 47.6895f)
        cubicTo(17.6667f, 47.4825f, 17.1458f, 47.1721f, 16.6875f, 46.7581f)
        cubicTo(13.9792f, 44.2743f, 11.5833f, 41.8525f, 9.5f, 39.4929f)
        cubicTo(7.41667f, 37.1332f, 5.67708f, 34.8461f, 4.28125f, 32.6313f)
        cubicTo(2.88542f, 30.4166f, 1.82292f, 28.2846f, 1.09375f, 26.2354f)
        cubicTo(0.364583f, 24.1863f, 0f, 22.2303f, 0f, 20.3674f)
        cubicTo(0f, 14.1578f, 2.01042f, 9.21087f, 6.03125f, 5.52652f)
        cubicTo(10.0521f, 1.84217f, 14.7083f, 0f, 20f, 0f)
        cubicTo(25.2917f, 0f, 29.9479f, 1.84217f, 33.9688f, 5.52652f)
        cubicTo(37.9896f, 9.21087f, 40f, 14.1578f, 40f, 20.3674f)
        cubicTo(40f, 22.2303f, 39.6354f, 24.1863f, 38.9062f, 26.2354f)
        cubicTo(38.1771f, 28.2846f, 37.1146f, 30.4166f, 35.7188f, 32.6313f)
        cubicTo(34.3229f, 34.8461f, 32.5833f, 37.1332f, 30.5f, 39.4929f)
        cubicTo(28.4167f, 41.8525f, 26.0208f, 44.2743f, 23.3125f, 46.7581f)
        cubicTo(22.8542f, 47.1721f, 22.3333f, 47.4825f, 21.75f, 47.6895f)
        cubicTo(21.1667f, 47.8965f, 20.5833f, 48f, 20f, 48f)
        close()

        transform(Matrix().apply {
            scale(scaleX, scaleY)
            translate(inset / scaleX, inset / scaleY)
        })
    }

    drawPath(path = path, color = fillColor, style = Fill)
    drawPath(path = path, color = strokeColor, style = Stroke(width = strokeWidth))
}

@PreviewsDayNight
@Composable
internal fun LocationPinPreview() = ElementPreview {
    val sampleAvatarData = AvatarData(
        id = "@alice:matrix.org",
        name = "Alice",
        url = null,
        size = AvatarSize.SelectedUser
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            LocationPin(
                variant = PinVariant.UserLocation(avatarData = sampleAvatarData, isLive = false),
            )
            LocationPin(
                variant = PinVariant.UserLocation(avatarData = sampleAvatarData, isLive = true),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            LocationPin(
                variant = PinVariant.PinnedLocation,
            )
            LocationPin(
                variant = PinVariant.StaleLocation,
            )
        }
    }
}

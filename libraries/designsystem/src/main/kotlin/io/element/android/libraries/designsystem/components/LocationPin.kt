/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import androidx.core.graphics.withSave
import coil3.Image
import coil3.SingletonImageLoader
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.toBitmap
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.colors.AvatarColorsProvider
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
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

/**
 * A location pin composable that supports multiple variants.
 *
 * Based on Figma design: https://www.figma.com/design/G1xy0HDZKJf5TCRFmKb5d5/Compound-Android-Components?node-id=4665-2890&m=dev
 */
@Composable
fun LocationPin(
    variant: PinVariant,
    modifier: Modifier = Modifier,
) {
    val image = rememberLocationPinBitmap(variant)
    Canvas(modifier = modifier.size(PIN_WIDTH, PIN_HEIGHT)) {
        if (image != null) {
            drawImage(image)
        }
    }
}

/**
 * Renders a location pin to an [ImageBitmap] using Canvas operations.
 * @param variant The pin variant to render
 * @return The rendered [ImageBitmap], or null if still loading
 */
@Composable
fun rememberLocationPinBitmap(variant: PinVariant): ImageBitmap? {
    val context = LocalContext.current
    val density = LocalDensity.current
    val colors = pinColors(variant)
    return produceState<ImageBitmap?>(initialValue = null, variant, colors) {
        val renderer = LocationPinRenderer(context, density)
        val bitmap = renderer.renderPin(variant, colors)
        value = bitmap.asImageBitmap()
    }.value
}

private val PIN_WIDTH = 42.dp
private val PIN_HEIGHT = PIN_WIDTH * 1.2f
private val AVATAR_SIZE = PIN_WIDTH - 10.dp
private val CONTENT_OFFSET = 5.dp
private val DOT_RADIUS = 6.dp
private val STROKE_WIDTH = 1.dp

@Composable
private fun pinColors(variant: PinVariant): PinColors {
    return when (variant) {
        is PinVariant.UserLocation -> {
            val avatarColors = AvatarColorsProvider.provide(variant.avatarData.id)
            if (variant.isLive) {
                PinColors(
                    fill = ElementTheme.colors.iconAccentPrimary,
                    stroke = Color.Transparent,
                    dot = Color.Transparent,
                    avatarStroke = ElementTheme.colors.bgCanvasDefault,
                    avatarBackground = avatarColors.background,
                    avatarForeground = avatarColors.foreground,
                )
            } else {
                PinColors(
                    fill = ElementTheme.colors.bgCanvasDefault,
                    stroke = ElementTheme.colors.iconQuaternaryAlpha,
                    dot = Color.Transparent,
                    avatarStroke = ElementTheme.colors.iconQuaternaryAlpha,
                    avatarBackground = avatarColors.background,
                    avatarForeground = avatarColors.foreground,
                )
            }
        }
        PinVariant.PinnedLocation -> PinColors(
            fill = ElementTheme.colors.bgCanvasDefault,
            stroke = ElementTheme.colors.iconSecondaryAlpha,
            avatarStroke = Color.Transparent,
            avatarBackground = Color.Transparent,
            avatarForeground = Color.Transparent,
            dot = ElementTheme.colors.iconPrimary,
        )
        PinVariant.StaleLocation -> PinColors(
            fill = ElementTheme.colors.bgSubtleSecondary,
            stroke = ElementTheme.colors.iconDisabled,
            avatarStroke = Color.Transparent,
            avatarBackground = Color.Transparent,
            avatarForeground = Color.Transparent,
            dot = ElementTheme.colors.iconDisabled,
        )
    }
}

/**
 * Color configuration for rendering a location pin.
 */
data class PinColors(
    val fill: Color,
    val stroke: Color,
    val dot: Color,
    val avatarStroke: Color,
    val avatarBackground: Color,
    val avatarForeground: Color,
)

/**
 * Renders location pins to bitmaps using Canvas operations.
 * Uses Coil for avatar loading with proper memory management.
 */
class LocationPinRenderer(
    private val context: Context,
    private val density: Density,
) {
    // Dimensions in pixels
    private val pinWidthPx = with(density) { PIN_WIDTH.toPx() }
    private val pinHeightPx = with(density) { PIN_HEIGHT.toPx() }
    private val avatarSizePx = with(density) { AVATAR_SIZE.toPx() }
    private val avatarOffsetPx = with(density) { CONTENT_OFFSET.toPx() }
    private val dotRadiusPx = with(density) { DOT_RADIUS.toPx() }
    private val strokeWidthPx = with(density) { STROKE_WIDTH.toPx() }

    /**
     * Renders a pin variant to bitmap. Suspending for async avatar loading.
     */
    suspend fun renderPin(
        variant: PinVariant,
        colors: PinColors,
    ): Bitmap {
        val bitmap = createBitmap(pinWidthPx.toInt(), pinHeightPx.toInt())
        val canvas = Canvas(bitmap)
        // Draw pin shape (fill + stroke)
        canvas.drawPinShape(colors.fill, colors.stroke)
        when (variant) {
            is PinVariant.UserLocation -> {
                val avatarImage = loadAvatarImage(variant.avatarData)
                canvas.drawAvatar(
                    avatarImage = avatarImage,
                    avatarData = variant.avatarData,
                    borderColor = colors.avatarStroke,
                    backgroundColor = colors.avatarBackground,
                    foregroundColor = colors.avatarForeground
                )
            }
            PinVariant.PinnedLocation,
            PinVariant.StaleLocation -> canvas.drawDot(colors.dot)
        }
        return bitmap
    }

    private fun Canvas.drawPinShape(fillColor: Color, strokeColor: Color) {
        val path = createPinPath()
        // Fill
        drawPath(path, Paint().apply {
            color = fillColor.toArgb()
            style = Paint.Style.FILL
            isAntiAlias = true
        })
        // Stroke
        drawPath(path, Paint().apply {
            color = strokeColor.toArgb()
            style = Paint.Style.STROKE
            strokeWidth = strokeWidthPx
            isAntiAlias = true
        })
    }

    /**
     * Creates the teardrop-shaped pin path.
     * Based on SVG path with dimensions 40x48 (ratio 1:1.2).
     * Scales automatically to fit the actual size.
     */
    private fun createPinPath(): Path {
        val svgWidth = 40f
        val svgHeight = 48f
        val inset = strokeWidthPx / 2
        val scaleX = (pinWidthPx - strokeWidthPx) / svgWidth
        val scaleY = (pinHeightPx - strokeWidthPx) / svgHeight

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
        }
        // Scale and translate the path
        val matrix = Matrix().apply {
            setScale(scaleX, scaleY)
            postTranslate(inset, inset)
        }
        path.transform(matrix)
        return path
    }

    private suspend fun loadAvatarImage(avatarData: AvatarData): Image? {
        val imageLoader = SingletonImageLoader.get(context)
        val request = ImageRequest.Builder(context)
            .data(avatarData)
            .size(avatarSizePx.toInt())
            // Disable hardware rendering for Canvas
            .allowHardware(false)
            .build()

        return imageLoader.execute(request).image
    }

    private fun Canvas.drawAvatar(
        avatarImage: Image?,
        avatarData: AvatarData,
        borderColor: Color,
        backgroundColor: Color,
        foregroundColor: Color,
    ) {
        val centerX = pinWidthPx / 2
        val avatarY = avatarOffsetPx
        val avatarRadius = avatarSizePx / 2

        withSave {
            val clipPath = Path().apply {
                addCircle(centerX, avatarY + avatarRadius, avatarRadius, Path.Direction.CW)
            }
            clipPath(clipPath)
            if (avatarImage != null) {
                // Draw the loaded avatar image
                val destRect = RectF(
                    centerX - avatarRadius,
                    avatarY,
                    centerX + avatarRadius,
                    avatarY + avatarSizePx
                )
                drawBitmap(avatarImage.toBitmap(), null, destRect, null)
            } else {
                // Fallback: draw initial letter circle
                drawInitialLetterAvatar(
                    avatarData = avatarData,
                    centerX = centerX,
                    centerY = avatarY + avatarRadius,
                    radius = avatarRadius,
                    foreground = foregroundColor.toArgb(),
                    background = backgroundColor.toArgb()
                )
            }
        }
        val paintBorder = Paint().apply {
            color = borderColor.toArgb()
            style = Paint.Style.STROKE
            strokeWidth = strokeWidthPx
            isAntiAlias = true
        }
        drawCircle(centerX, avatarY + avatarRadius, avatarRadius, paintBorder)
    }

    private fun Canvas.drawInitialLetterAvatar(
        avatarData: AvatarData,
        centerX: Float,
        centerY: Float,
        radius: Float,
        foreground: Int,
        background: Int,
    ) {
        // Draw background circle
        drawCircle(centerX, centerY, radius, Paint().apply {
            color = background
            style = Paint.Style.FILL
            isAntiAlias = true
        })
        // Draw initial letter
        val textPaint = Paint().apply {
            color = foreground
            textSize = radius * 1.2f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            isFakeBoldText = true
        }
        // Center text vertically
        val textBounds = Rect()
        textPaint.getTextBounds(avatarData.initialLetter, 0, 1, textBounds)
        val textY = centerY + textBounds.height() / 2f
        drawText(avatarData.initialLetter, centerX, textY, textPaint)
    }

    private fun Canvas.drawDot(dotColor: Color) {
        if (dotColor == Color.Transparent) return

        val centerX = pinWidthPx / 2
        // Position dot in the center of the circular part of the pin
        val centerY = avatarOffsetPx + avatarSizePx / 2

        drawCircle(centerX, centerY, dotRadiusPx, Paint().apply {
            color = dotColor.toArgb()
            style = Paint.Style.FILL
            isAntiAlias = true
        })
    }
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

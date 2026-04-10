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
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.withSave
import coil3.Image
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.asImage
import coil3.memory.MemoryCache
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.toBitmap
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.colors.AvatarColorsProvider
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.utils.CommonDrawables

private val PIN_WIDTH = 42.dp
private val PIN_HEIGHT = PIN_WIDTH * 1.2f
private val AVATAR_SIZE = PIN_WIDTH - 10.dp
private val CONTENT_OFFSET = 5.dp
private val DOT_RADIUS = 6.dp
private val STROKE_WIDTH = 1.dp

/**
 * Variants of location pin markers.
 */
@Immutable
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
    val cacheKey = rememberCacheKey(variant)
    val resources = LocalResources.current

    return if (LocalInspectionMode.current) {
        // In preview mode, skip async loading and return a simple placeholder image instead to avoid using ImageLoader
        val dimensions = PinDimensions(density)
        val avatarImage = ResourcesCompat.getDrawable(resources, CommonDrawables.sample_avatar, context.theme)?.toBitmap()?.asImage()
        LocationPinRenderer.renderPin(variant, colors, dimensions, avatarImage).asImageBitmap()
    } else {
        produceState<ImageBitmap?>(initialValue = null, cacheKey) {
            val imageLoader = SingletonImageLoader.get(context)
            val memoryCacheKey = MemoryCache.Key(cacheKey)
            val cached = imageLoader.memoryCache?.get(memoryCacheKey)
            if (cached != null) {
                value = cached.image.toBitmap().asImageBitmap()
            } else {
                val dimensions = PinDimensions(density)
                val bitmap = with(LocationPinRenderer) {
                    val avatarImage = loadAvatarImage(variant, context, imageLoader)
                    renderPin(variant, colors, dimensions, avatarImage)
                }
                imageLoader.memoryCache?.set(memoryCacheKey, MemoryCache.Value(bitmap.asImage()))
                value = bitmap.asImageBitmap()
            }
        }.value
    }
}

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
private data class PinColors(
    val fill: Color,
    val stroke: Color,
    val dot: Color,
    val avatarStroke: Color,
    val avatarBackground: Color,
    val avatarForeground: Color,
)

/**
 * Pre-calculated pixel dimensions for rendering a location pin.
 */
private class PinDimensions(density: Density) {
    val pinWidth = with(density) { PIN_WIDTH.toPx() }
    val pinHeight = with(density) { PIN_HEIGHT.toPx() }
    val avatarSize: Float = with(density) { AVATAR_SIZE.toPx() }
    val avatarOffset: Float = with(density) { CONTENT_OFFSET.toPx() }
    val dotRadius: Float = with(density) { DOT_RADIUS.toPx() }
    val strokeWidth: Float = with(density) { STROKE_WIDTH.toPx() }
}

/**
 * Renders location pins to bitmaps using Canvas operations.
 * Uses Coil for avatar loading.
 * Paint objects are shared across all renders.
 */
private object LocationPinRenderer {
    // Shared Paint objects to avoid allocations
    private val fillPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    private val strokePaint = Paint().apply {
        style = Paint.Style.STROKE
        isAntiAlias = true
    }
    private val textPaint = Paint().apply {
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
        isFakeBoldText = true
    }

    /**
     * Renders a pin variant to bitmap. Suspending for async avatar loading.
     */
    fun renderPin(
        variant: PinVariant,
        colors: PinColors,
        dimensions: PinDimensions,
        avatarImage: Image?,
    ): Bitmap {
        val bitmap = createBitmap(dimensions.pinWidth.toInt(), dimensions.pinHeight.toInt())
        val canvas = Canvas(bitmap)
        canvas.drawPinShape(colors.fill, colors.stroke, dimensions)
        when (variant) {
            is PinVariant.UserLocation -> {
                canvas.drawAvatar(
                    avatarImage = avatarImage,
                    avatarData = variant.avatarData,
                    borderColor = colors.avatarStroke,
                    backgroundColor = colors.avatarBackground,
                    foregroundColor = colors.avatarForeground,
                    dimensions = dimensions,
                )
            }
            PinVariant.PinnedLocation,
            PinVariant.StaleLocation -> canvas.drawDot(colors.dot, dimensions)
        }
        return bitmap
    }

    private fun Canvas.drawPinShape(fillColor: Color, strokeColor: Color, dimensions: PinDimensions) {
        val path = createPinPath(dimensions)
        fillPaint.color = fillColor.toArgb()
        drawPath(path, fillPaint)
        strokePaint.color = strokeColor.toArgb()
        strokePaint.strokeWidth = dimensions.strokeWidth
        drawPath(path, strokePaint)
    }

    /**
     * Updates the teardrop-shaped pin path to match dimensions.
     * Based on SVG path with dimensions 40x48 (ratio 1:1.2).
     */
    private fun createPinPath(dimensions: PinDimensions): Path {
        val svgWidth = 40f
        val svgHeight = 48f
        val inset = dimensions.strokeWidth / 2
        val scaleX = (dimensions.pinWidth - dimensions.strokeWidth) / svgWidth
        val scaleY = (dimensions.pinHeight - dimensions.strokeWidth) / svgHeight

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
        val matrix = Matrix().apply {
            setScale(scaleX, scaleY)
            postTranslate(inset, inset)
        }
        path.transform(matrix)
        return path
    }

    suspend fun loadAvatarImage(
        variant: PinVariant,
        context: Context,
        imageLoader: ImageLoader,
    ): Image? {
        val avatarData = when (variant) {
            is PinVariant.UserLocation -> variant.avatarData
            else -> return null
        }
        val request = ImageRequest.Builder(context)
            .data(avatarData)
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
        dimensions: PinDimensions,
    ) {
        val centerX = dimensions.pinWidth / 2
        val avatarY = dimensions.avatarOffset
        val avatarRadius = dimensions.avatarSize / 2

        withSave {
            if (avatarImage != null) {
                val bitmap = avatarImage.toBitmap()
                // Calculate centered square crop (ContentScale.Crop behavior)
                val srcSize = minOf(bitmap.width, bitmap.height)
                val srcX = (bitmap.width - srcSize) / 2
                val srcY = (bitmap.height - srcSize) / 2
                val srcRect = Rect(srcX, srcY, srcX + srcSize, srcY + srcSize)
                val destRect = RectF(
                    centerX - avatarRadius,
                    avatarY,
                    centerX + avatarRadius,
                    avatarY + dimensions.avatarSize
                )
                val clipPath = Path().apply {
                    addCircle(centerX, avatarY + avatarRadius, avatarRadius, Path.Direction.CW)
                }
                clipPath(clipPath)
                drawBitmap(bitmap, srcRect, destRect, null)
            } else {
                drawInitialLetterAvatar(
                    initialLetter = avatarData.initialLetter,
                    centerX = centerX,
                    centerY = avatarY + avatarRadius,
                    radius = avatarRadius,
                    foreground = foregroundColor.toArgb(),
                    background = backgroundColor.toArgb()
                )
            }
        }
        strokePaint.color = borderColor.toArgb()
        strokePaint.strokeWidth = dimensions.strokeWidth
        drawCircle(centerX, avatarY + avatarRadius, avatarRadius, strokePaint)
    }

    private fun Canvas.drawInitialLetterAvatar(
        initialLetter: String,
        centerX: Float,
        centerY: Float,
        radius: Float,
        foreground: Int,
        background: Int,
    ) {
        fillPaint.color = background
        drawCircle(centerX, centerY, radius, fillPaint)
        textPaint.color = foreground
        textPaint.textSize = radius * 1.2f
        val textBounds = Rect()
        textPaint.getTextBounds(initialLetter, 0, 1, textBounds)
        val textY = centerY + textBounds.height() / 2f
        drawText(initialLetter, centerX, textY, textPaint)
    }

    private fun Canvas.drawDot(dotColor: Color, dimensions: PinDimensions) {
        if (dotColor == Color.Transparent) return
        val centerX = dimensions.pinWidth / 2
        val centerY = dimensions.avatarOffset + dimensions.avatarSize / 2
        fillPaint.color = dotColor.toArgb()
        drawCircle(centerX, centerY, dimensions.dotRadius, fillPaint)
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

@Composable
private fun rememberCacheKey(variant: PinVariant): String {
    val isLightTheme = ElementTheme.isLightTheme
    val density = LocalDensity.current.density
    return remember(isLightTheme, density, variant) {
        val pinVariant = when (variant) {
            PinVariant.PinnedLocation -> "pin_pinned"
            PinVariant.StaleLocation -> "pin_stale"
            is PinVariant.UserLocation -> "pin_user_${variant.avatarData.id}_${variant.isLive}"
        }
        "${pinVariant}_{$isLightTheme}_{$density}"
    }
}

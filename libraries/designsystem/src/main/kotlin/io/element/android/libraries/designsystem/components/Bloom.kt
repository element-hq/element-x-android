/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components

import android.graphics.Bitmap
import android.graphics.Typeface
import android.os.Build
import android.text.TextPaint
import androidx.annotation.FloatRange
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RadialGradientShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.toOffset
import androidx.compose.ui.unit.toSize
import coil3.SingletonImageLoader
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.toBitmap
import com.airbnb.android.showkase.annotation.ShowkaseComposable
import com.vanniktech.blurhash.BlurHash
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.colors.AvatarColorsProvider
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewGroup
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.text.toDp
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.MediumTopAppBar
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.utils.CommonDrawables
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Default bloom configuration values.
 */
object BloomDefaults {
    /**
     * Number of components to use with BlurHash to generate the blur effect.
     * Larger values mean more detailed blurs.
     */
    const val HASH_COMPONENTS = 4
    const val ENCODE_SIZE_PX = 20
    const val DECODE_SIZE_PX = 5

    /** Default bloom layers. */
    @Composable
    fun defaultLayers() = persistentListOf(
        // Bottom layer
        if (ElementTheme.isLightTheme) {
            BloomLayer(0.2f, BlendMode.Hardlight)
        } else {
            BloomLayer(0.5f, BlendMode.Exclusion)
        },
        // Top layer
        BloomLayer(if (ElementTheme.isLightTheme) 0.8f else 0.2f, BlendMode.Color),
    )
}

/**
 * Bloom layer configuration.
 * @param alpha The alpha value to apply to the layer.
 * @param blendMode The blend mode to apply to the layer.
 */
data class BloomLayer(
    val alpha: Float,
    val blendMode: BlendMode,
)

/**
 * Bloom effect modifier. Applies a bloom effect to the component.
 * @param hash The BlurHash to use as the bloom source.
 * @param background The background color to use for the bloom effect. Since we use blend modes it must be non-transparent.
 * @param blurSize The size of the bloom effect. If not specified the bloom effect will be the size of the component.
 * @param offset The offset to use for the bloom effect. If not specified the bloom effect will be centered on the component.
 * @param clipToSize The size to use for clipping the bloom effect. If not specified the bloom effect will not be clipped.
 * @param layerConfiguration The configuration for the bloom layers. If not specified the default layers configuration will be used.
 * @param bottomSoftEdgeColor The color to use for the bottom soft edge. If not specified the [background] color will be used.
 * @param bottomSoftEdgeHeight The height of the bottom soft edge. If not specified the bottom soft edge will not be drawn.
 * @param bottomSoftEdgeAlpha The alpha value to apply to the bottom soft edge.
 * @param alpha The alpha value to apply to the bloom effect.
 */
@SuppressWarnings("ModifierComposed")
fun Modifier.bloom(
    hash: String?,
    background: Color,
    blurSize: DpSize = DpSize.Unspecified,
    offset: DpOffset = DpOffset.Unspecified,
    clipToSize: DpSize = DpSize.Unspecified,
    layerConfiguration: ImmutableList<BloomLayer>? = null,
    bottomSoftEdgeColor: Color = background,
    bottomSoftEdgeHeight: Dp = 40.dp,
    @FloatRange(from = 0.0, to = 1.0)
    bottomSoftEdgeAlpha: Float = 1.0f,
    @FloatRange(from = 0.0, to = 1.0)
    alpha: Float = 1f,
) = composed {
    val defaultLayers = BloomDefaults.defaultLayers()
    val layers = layerConfiguration ?: defaultLayers
    // Bloom only works on API 29+
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return@composed this
    if (hash == null) return@composed this

    val hashedBitmap = remember(hash) {
        BlurHash.decode(
            blurHash = hash,
            width = BloomDefaults.DECODE_SIZE_PX,
            height = BloomDefaults.DECODE_SIZE_PX,
        )?.asImageBitmap()
    } ?: return@composed this
    val density = LocalDensity.current
    val pixelSize = remember(blurSize, density) { blurSize.toIntSize(density) }
    val clipToPixelSize = remember(clipToSize, density) { clipToSize.toIntSize(density) }
    val bottomSoftEdgeHeightPixels = remember(bottomSoftEdgeHeight, density) { with(density) { bottomSoftEdgeHeight.roundToPx() } }
    val isRTL = LocalLayoutDirection.current == LayoutDirection.Rtl
    drawWithCache {
        val dstSize = if (pixelSize != IntSize.Zero) {
            pixelSize
        } else {
            IntSize(size.width.toInt(), size.height.toInt())
        }
        // Calculate where to place the center of the bloom effect
        val centerOffset = if (offset.isSpecified) {
            if (isRTL) {
                IntOffset(
                    size.width.roundToInt() - offset.x.roundToPx(),
                    size.height.roundToInt() - offset.y.roundToPx(),
                )
            } else {
                IntOffset(
                    offset.x.roundToPx(),
                    offset.y.roundToPx(),
                )
            }
        } else {
            IntOffset(
                size.center.x.toInt(),
                size.center.y.toInt(),
            )
        }
        // Calculate the offset to draw the different layers and apply clipping
        // This offset is applied to place the top left corner of the bloom effect
        val layersOffset = if (offset.isSpecified) {
            // Offsets the layers so the center of the bloom effect is at the provided offset value
            IntOffset(
                centerOffset.x - dstSize.width / 2,
                centerOffset.y - dstSize.height / 2,
            )
        } else {
            // Places the layers at the center of the component
            IntOffset.Zero
        }
        val radius = max(dstSize.width, dstSize.height).toFloat() / 2
        val circularGradientShader = RadialGradientShader(
            centerOffset.toOffset(),
            radius,
            listOf(Color.Red, Color.Transparent),
            listOf(0f, 1f)
        )
        val circularGradientBrush = ShaderBrush(circularGradientShader)
        val bottomEdgeGradient = LinearGradientShader(
            from = IntOffset(0, clipToPixelSize.height - bottomSoftEdgeHeightPixels).toOffset(),
            to = IntOffset(0, clipToPixelSize.height).toOffset(),
            listOf(Color.Transparent, bottomSoftEdgeColor),
            listOf(0f, 1f)
        )
        val bottomEdgeGradientBrush = ShaderBrush(bottomEdgeGradient)
        onDrawBehind {
            if (dstSize != IntSize.Zero) {
                val circleClipPath = Path().apply {
                    addOval(Rect(centerOffset.toOffset(), radius - 1))
                }
                // Clip the external radius of bloom gradient too, otherwise we have a 1px border
                clipPath(circleClipPath, clipOp = ClipOp.Intersect) {
                    // Draw the bloom layers
                    drawWithLayer {
                        // Clip rect to the provided size if needed
                        if (clipToPixelSize != IntSize.Zero) {
                            drawContext.canvas.clipRect(Rect(Offset.Zero, clipToPixelSize.toSize()), ClipOp.Intersect)
                        }
                        // Draw background color for blending
                        drawRect(background, size = pixelSize.toSize())
                        // Draw layers
                        for (layer in layers) {
                            drawImage(
                                hashedBitmap,
                                srcSize = IntSize(BloomDefaults.HASH_COMPONENTS, BloomDefaults.HASH_COMPONENTS),
                                dstSize = dstSize,
                                dstOffset = layersOffset,
                                alpha = layer.alpha * alpha,
                                blendMode = layer.blendMode,
                            )
                        }
                        // Mask the layers erasing the outer radius using the gradient brush
                        drawCircle(
                            circularGradientBrush,
                            radius,
                            centerOffset.toOffset(),
                            blendMode = BlendMode.DstIn
                        )
                    }
                }
                // Draw the bottom soft edge
                drawRect(
                    bottomEdgeGradientBrush,
                    topLeft = IntOffset(0, clipToPixelSize.height - bottomSoftEdgeHeight.roundToPx()).toOffset(),
                    size = IntSize(pixelSize.width, bottomSoftEdgeHeight.roundToPx()).toSize(),
                    alpha = bottomSoftEdgeAlpha
                )
            }
        }
    }
}

/**
 * Bloom effect modifier for avatars. Applies a bloom effect to the component.
 * @param avatarData The avatar data to use as the bloom source.
 *  If the avatar data has a URL it will be used as the bloom source, otherwise the initials will be used.
 * @param background The background color to use for the bloom effect. Since we use blend modes it must be non-transparent.
 * @param blurSize The size of the bloom effect. If not specified the bloom effect will be the size of the component.
 * @param offset The offset to use for the bloom effect. If not specified the bloom effect will be centered on the component.
 * @param clipToSize The size to use for clipping the bloom effect. If not specified the bloom effect will not be clipped.
 * @param bottomSoftEdgeColor The color to use for the bottom soft edge. If not specified the [background] color will be used.
 * @param bottomSoftEdgeHeight The height of the bottom soft edge. If not specified the bottom soft edge will not be drawn.
 * @param bottomSoftEdgeAlpha The alpha value to apply to the bottom soft edge.
 * @param alpha The alpha value to apply to the bloom effect.
 */
@SuppressWarnings("ModifierComposed")
fun Modifier.avatarBloom(
    avatarData: AvatarData,
    background: Color,
    blurSize: DpSize = DpSize.Unspecified,
    offset: DpOffset = DpOffset.Unspecified,
    clipToSize: DpSize = DpSize.Unspecified,
    bottomSoftEdgeColor: Color = background,
    bottomSoftEdgeHeight: Dp = 40.dp,
    @FloatRange(from = 0.0, to = 1.0)
    bottomSoftEdgeAlpha: Float = 1.0f,
    @FloatRange(from = 0.0, to = 1.0)
    alpha: Float = 1f,
) = composed {
    // Bloom only works on API 29+
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return@composed this

    // Request the avatar contents to use as the bloom source
    val context = LocalContext.current
    if (avatarData.url != null) {
        val painterRequest = remember(avatarData) {
            ImageRequest.Builder(context)
                .data(avatarData)
                // Allow cache and default dispatchers
                .defaults(ImageRequest.Defaults())
                // Needed to be able to read pixels from the Bitmap for the hash
                .allowHardware(false)
                // Reduce size so it loads faster for large avatars
                .size(BloomDefaults.ENCODE_SIZE_PX, BloomDefaults.ENCODE_SIZE_PX)
                .build()
        }

        // By making it saveable, we'll 'cache' the previous bloom effect until a new one is loaded
        var blurHash by rememberSaveable(avatarData) { mutableStateOf<String?>(null) }
        LaunchedEffect(avatarData) {
            withContext(Dispatchers.IO) {
                val bitmap = SingletonImageLoader.get(context)
                    .execute(painterRequest)
                    .image
                    ?.toBitmap()
                    ?: return@withContext
                blurHash = BlurHash.encode(
                    bitmap = bitmap,
                    componentX = BloomDefaults.HASH_COMPONENTS,
                    componentY = BloomDefaults.HASH_COMPONENTS,
                )
            }
        }

        bloom(
            hash = blurHash,
            background = background,
            blurSize = blurSize,
            offset = offset,
            clipToSize = clipToSize,
            bottomSoftEdgeColor = bottomSoftEdgeColor,
            bottomSoftEdgeHeight = bottomSoftEdgeHeight,
            bottomSoftEdgeAlpha = bottomSoftEdgeAlpha,
            alpha = alpha,
        )
    } else {
        // There is no URL so we'll generate an avatar with the initials and use that as the bloom source
        val avatarColors = AvatarColorsProvider.provide(avatarData.id)
        val initialsBitmap = initialsBitmap(
            width = BloomDefaults.ENCODE_SIZE_PX.toDp(),
            height = BloomDefaults.ENCODE_SIZE_PX.toDp(),
            text = avatarData.initialLetter,
            textColor = avatarColors.foreground,
            backgroundColor = avatarColors.background,
        )
        val hash = remember(avatarData, avatarColors) {
            BlurHash.encode(
                bitmap = initialsBitmap.asAndroidBitmap(),
                componentX = BloomDefaults.HASH_COMPONENTS,
                componentY = BloomDefaults.HASH_COMPONENTS,
            )
        }
        bloom(
            hash = hash,
            background = background,
            blurSize = blurSize,
            offset = offset,
            clipToSize = clipToSize,
            bottomSoftEdgeColor = bottomSoftEdgeColor,
            bottomSoftEdgeHeight = bottomSoftEdgeHeight,
            bottomSoftEdgeAlpha = bottomSoftEdgeAlpha,
            alpha = alpha,
        )
    }
}

// Used to create a Bitmap version of the initials avatar
@Composable
private fun initialsBitmap(
    text: String,
    backgroundColor: Color,
    textColor: Color,
    width: Dp = 32.dp,
    height: Dp = 32.dp,
): ImageBitmap = with(LocalDensity.current) {
    val backgroundPaint = remember(backgroundColor) {
        Paint().also { it.color = backgroundColor }
    }
    val resolver: FontFamily.Resolver = LocalFontFamilyResolver.current
    val fontSize = remember { height.toSp() / 2 }
    val typeface: Typeface = remember(resolver) {
        resolver.resolve(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Normal,
        )
    }.value as Typeface
    val textPaint = remember(textColor, typeface) {
        TextPaint().apply {
            color = textColor.toArgb()
            textSize = fontSize.toPx()
            this.typeface = typeface
        }
    }
    val textMeasurer = rememberTextMeasurer()
    val result = remember(text) { textMeasurer.measure(text, TextStyle.Default.copy(fontSize = fontSize)) }
    val centerPx = remember(width, height) { IntOffset(width.roundToPx() / 2, height.roundToPx() / 2) }
    remember(text, width, height, backgroundColor, textColor) {
        val bitmap = Bitmap.createBitmap(width.roundToPx(), height.roundToPx(), Bitmap.Config.ARGB_8888).asImageBitmap()
        androidx.compose.ui.graphics.Canvas(bitmap).also { canvas ->
            canvas.drawCircle(centerPx.toOffset(), width.toPx() / 2, backgroundPaint)
            canvas.nativeCanvas.drawText(text, centerPx.x.toFloat() - result.size.width / 2, centerPx.y * 2f - result.size.height / 2 - 4, textPaint)
        }
        bitmap
    }
}

// Translates DP sizes into pixel sizes, taking into account unspecified values
private fun DpSize.toIntSize(density: Density) = with(density) {
    if (isSpecified) {
        IntSize(width.roundToPx(), height.roundToPx())
    } else {
        IntSize.Zero
    }
}

/**
 * Helper to draw to a canvas using layers. This allows us to apply clipping to those layers only.
 */
fun DrawScope.drawWithLayer(block: DrawScope.() -> Unit) {
    with(drawContext.canvas.nativeCanvas) {
        val checkPoint = saveLayer(null, null)
        block()
        restoreToCount(checkPoint)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@PreviewsDayNight
@ShowkaseComposable(group = PreviewGroup.Bloom)
@Composable
internal fun BloomPreview() {
    val blurhash = "eePn{tI?xExEja}ooKWWodjtNJoKR,j@a|sBWpS3WDbGazoKWWWWj@"
    var topAppBarHeight by remember { mutableIntStateOf(-1) }
    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(topAppBarState)
    ElementPreview(
        drawableFallbackForImages = CommonDrawables.sample_avatar,
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                Box {
                    MediumTopAppBar(
                        modifier = Modifier
                            .onSizeChanged { size ->
                                topAppBarHeight = size.height
                            }
                            .bloom(
                                hash = blurhash,
                                background = ElementTheme.colors.bgCanvasDefault,
                                blurSize = DpSize(430.dp, 430.dp),
                                offset = DpOffset(24.dp, 24.dp),
                                clipToSize = if (topAppBarHeight > 0) DpSize(430.dp, topAppBarHeight.toDp()) else DpSize.Zero,
                            ),
                        colors = TopAppBarDefaults.largeTopAppBarColors(
                            containerColor = Color.Transparent,
                            scrolledContainerColor = Color.Black.copy(alpha = 0.05f),
                        ),
                        navigationIcon = {
                            Avatar(
                                avatarData = AvatarData(
                                    id = "sample-avatar",
                                    name = "sample",
                                    url = "aURL",
                                    size = AvatarSize.CurrentUserTopBar,
                                ),
                            )
                        },
                        actions = {
                            IconButton(onClick = {}) {
                                Icon(
                                    imageVector = CompoundIcons.ShareAndroid(),
                                    contentDescription = null,
                                )
                            }
                        },
                        title = {
                            Text("Title")
                        },
                        scrollBehavior = scrollBehavior,
                    )
                }
            },
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .consumeWindowInsets(paddingValues)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                repeat(20) {
                    Text("Content", modifier = Modifier.padding(vertical = 20.dp))
                }
            }
        }
    }
}

class InitialsColorIntProvider : PreviewParameterProvider<Int> {
    override val values: Sequence<Int>
        get() = sequenceOf(0, 1, 2, 3, 4, 5, 6, 7)
}

@PreviewsDayNight
@Composable
@ShowkaseComposable(group = PreviewGroup.Bloom)
internal fun BloomInitialsPreview(@PreviewParameter(InitialsColorIntProvider::class) color: Int) {
    ElementPreview {
        val avatarColors = AvatarColorsProvider.provide("$color")
        val bitmap = initialsBitmap(text = "F", backgroundColor = avatarColors.background, textColor = avatarColors.foreground)
        val hash = BlurHash.encode(
            bitmap = bitmap.asAndroidBitmap(),
            componentX = BloomDefaults.HASH_COMPONENTS,
            componentY = BloomDefaults.HASH_COMPONENTS,
        )
        Box(
            modifier = Modifier
                .size(256.dp)
                .bloom(
                    hash = hash,
                    background = if (ElementTheme.isLightTheme) {
                        // Workaround to display a very subtle bloom for avatars with very soft colors
                        Color(0xFFF9F9F9)
                    } else {
                        ElementTheme.colors.bgCanvasDefault
                    },
                    bottomSoftEdgeColor = ElementTheme.colors.bgCanvasDefault,
                    blurSize = DpSize(256.dp, 256.dp),
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape),
                painter = BitmapPainter(bitmap),
                contentDescription = null
            )
        }
    }
}

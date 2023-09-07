/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.libraries.designsystem.components

import android.graphics.Bitmap
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.text.TextPaint
import androidx.annotation.FloatRange
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toOffset
import androidx.compose.ui.unit.toSize
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.vanniktech.blurhash.BlurHash
import io.element.android.libraries.designsystem.R
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.preview.DayNightPreviews
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.text.roundToPx
import io.element.android.libraries.designsystem.text.toDp
import io.element.android.libraries.designsystem.theme.components.MediumTopAppBar
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.theme.ElementTheme
import kotlin.math.max

@Composable
fun Bloom(
    bitmap: Bitmap,
    background: Color,
    modifier: Modifier = Modifier,
    size: DpSize = DpSize.Zero,
    content: @Composable () -> Unit,
) {
    val hash = remember(bitmap) { BlurHash.encode(bitmap, blurHashComponents, blurHashComponents) }
    Bloom(
        hash = hash,
        background = background,
        blurSize = size,
        content = content,
        modifier = modifier
    )
}

@Composable
fun Bloom(
    hash: String,
    background: Color,
    modifier: Modifier = Modifier,
    blurSize: DpSize = DpSize.Zero,
    clipToSize: DpSize = DpSize.Zero,
    drawContents: Boolean = true,
    @FloatRange(from = 0.0, to = 1.0) alpha: Float = 1f,
    content: @Composable () -> Unit = {},
) {
    val hashedBitmap = remember(hash) {
        BlurHash.decode(hash, blurHashComponents, blurHashComponents)?.asImageBitmap()
    } ?: return
    val pixelSize = blurSize.toIntSize()
    val clipToPixelSize = clipToSize.toIntSize()
    val topLayerOpacity = if (isSystemInDarkTheme()) 0.2f else 0.75f
    val bottomLayerOpacity = if (isSystemInDarkTheme()) 0.15f else 0.2f
    val topLayerBlendMode = if (isSystemInDarkTheme()) BlendMode.SrcOver else BlendMode.Saturation
    val bottomLayerBlendMode = BlendMode.Hardlight
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .drawWithCache {
                val dstSize = if (blurSize == DpSize.Zero) {
                    IntSize(size.width.toInt(), size.height.toInt())
                } else {
                    pixelSize
                }
                val centerOffset = IntOffset(size.center.x.toInt(), size.center.y.toInt())
                val radius = max(dstSize.width, dstSize.height).toFloat() / 2
                val shader = RadialGradientShader(
                    centerOffset.toOffset(),
                    radius,
                    listOf(Color.Red, Color.Transparent),
                    listOf(0f, 1f)
                )
                val brush = ShaderBrush(shader)
                val dstOffset = if (blurSize == DpSize.Zero) IntOffset.Zero else IntOffset(
                    centerOffset.x - pixelSize.width / 2,
                    centerOffset.y - pixelSize.height / 2
                )
                onDrawWithContent {
                    if (dstSize != IntSize.Zero) {
                        val circleClipPath = Path().apply {
                            addOval(Rect(drawContext.size.center, radius - 1))
                        }
                        if (clipToPixelSize != IntSize.Zero) {
                            val path = Path().apply {
                                addRect(Rect(Offset.Zero, clipToPixelSize.toSize()))
                            }
                            drawContext.canvas.clipPath(path, ClipOp.Intersect)
                        }
                        // Clip external path if needed
                        clipPath(circleClipPath, clipOp = ClipOp.Intersect) {
                            drawWithLayer {
                                // Draw background color for blending
                                drawRect(background, size = pixelSize.toSize())
                                // 25% opacity, hard light blend mode
                                drawImage(
                                    hashedBitmap,
                                    srcSize = IntSize(blurHashComponents, blurHashComponents),
                                    dstSize = dstSize,
                                    dstOffset = dstOffset,
                                    alpha = bottomLayerOpacity * alpha,
                                    blendMode = bottomLayerBlendMode,
                                )
                                // 25% opacity, default blend mode
                                drawImage(
                                    hashedBitmap,
                                    srcSize = IntSize(blurHashComponents, blurHashComponents),
                                    dstSize = dstSize,
                                    dstOffset = dstOffset,
                                    alpha = topLayerOpacity * alpha,
                                    blendMode = topLayerBlendMode,
                                )
                                // Erase the outer radius using the gradient brush
                                drawCircle(brush, radius, blendMode = BlendMode.DstIn)
                            }
                        }
                        if (drawContents) {
                            drawContent()
                        }
                    }
                }
            }
    ) {
        content()
    }
}

fun DrawScope.drawWithLayer(block: DrawScope.() -> Unit) {
    with(drawContext.canvas.nativeCanvas) {
        val checkPoint = saveLayer(null, null)
        block()
        restoreToCount(checkPoint)
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
private fun initialsBitmap(
    text: String,
    backgroundColor: Color,
    textColor: Color,
    width: Dp = 32.dp,
    height: Dp = 32.dp,
): ImageBitmap = with(LocalDensity.current) {
    val backgroundPaint = Paint().also { it.color = backgroundColor }
    val resolver: FontFamily.Resolver = LocalFontFamilyResolver.current

    val style = ElementTheme.typography.fontBodyLgMedium
    val typeface: Typeface = remember(resolver, style) {
        resolver.resolve(
            fontFamily = style.fontFamily,
            fontWeight = style.fontWeight ?: FontWeight.Normal,
            fontStyle = style.fontStyle ?: FontStyle.Normal,
            fontSynthesis = style.fontSynthesis ?: FontSynthesis.All,
        )
    }.value as Typeface
    val textPaint = TextPaint().apply {
        color = textColor.toArgb()
        textSize = 16.sp.toPx()
        this.typeface = typeface
    }
    val textMeasurer = rememberTextMeasurer()
    val result = textMeasurer.measure(text, TextStyle.Default.copy(fontSize = 16.sp))
    val centerPx = IntOffset(width.roundToPx()/2, height.roundToPx()/2)
    remember {
        val bitmap = Bitmap.createBitmap(width.roundToPx(), height.roundToPx(), Bitmap.Config.ARGB_8888).asImageBitmap()
        androidx.compose.ui.graphics.Canvas(bitmap).also { canvas ->
            canvas.drawCircle(centerPx.toOffset(), width.toPx() / 2, backgroundPaint)
            canvas.nativeCanvas.drawText(text, centerPx.x.toFloat() - (result.size.width)/2, centerPx.y * 2f - (result.size.height/2) - 4, textPaint)
        }
        bitmap
    }
}

fun Modifier.asyncBloom(
    avatarData: AvatarData?,
    background: Color,
    blurSize: DpSize = DpSize.Unspecified,
    offset: DpOffset = DpOffset.Unspecified,
    clipToSize: DpSize = DpSize.Unspecified,
    bottomEdgeMaskHeight: Dp = 40.dp,
    @FloatRange(from = 0.0, to = 1.0)
    bottomEdgeMaskAlpha: Float = 1.0f,
    @FloatRange(from = 0.0, to = 1.0)
    alpha: Float = 1f,
) = composed {
    avatarData ?: return@composed this

    val painter = rememberAsyncImagePainter(
        ImageRequest.Builder(LocalContext.current)
            .data(avatarData)
            .allowHardware(false)
            .build()
    )
    var blurHash by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(avatarData) {
        val drawable = painter.imageLoader.execute(painter.request).drawable ?: return@LaunchedEffect
        val bitmap = (drawable as? BitmapDrawable)?.bitmap ?: return@LaunchedEffect
        blurHash = BlurHash.encode(bitmap, blurHashComponents, blurHashComponents)
    }

    bloom(
        hash = blurHash,
        background = background,
        blurSize = blurSize,
        offset = offset,
        clipToSize = clipToSize,
        bottomEdgeMaskHeight = bottomEdgeMaskHeight,
        bottomEdgeMaskAlpha = bottomEdgeMaskAlpha,
        alpha = alpha,
    )
}

fun Modifier.bloom(
    hash: String?,
    background: Color,
    blurSize: DpSize = DpSize.Unspecified,
    offset: DpOffset = DpOffset.Unspecified,
    clipToSize: DpSize = DpSize.Unspecified,
    bottomEdgeMaskHeight: Dp = 40.dp,
    @FloatRange(from = 0.0, to = 1.0)
    bottomEdgeMaskAlpha: Float = 1.0f,
    @FloatRange(from = 0.0, to = 1.0)
    alpha: Float = 1f,
) = composed {
    if (hash == null) return@composed this

    val hashedBitmap = remember(hash) {
        BlurHash.decode(hash, blurHashComponents, blurHashComponents)?.asImageBitmap()
    } ?: return@composed this
    val pixelSize = blurSize.toIntSize()
    val clipToPixelSize = clipToSize.toIntSize()
    val topLayerOpacity = if (isSystemInDarkTheme()) 0.2f else 0.8f
    val bottomLayerOpacity = if (isSystemInDarkTheme()) 0.5f else 0.2f
    val topLayerBlendMode = BlendMode.Color
    val bottomLayerBlendMode = if (isSystemInDarkTheme()) BlendMode.Exclusion else BlendMode.Hardlight
    val bottomEdgeMaskHeightPixels = bottomEdgeMaskHeight.roundToPx()
    drawWithCache {
        val dstSize = if (pixelSize != IntSize.Zero) {
            pixelSize
        } else {
            IntSize(size.width.toInt(), size.height.toInt())
        }
        val centerOffset = if (offset != DpOffset.Unspecified) {
            IntOffset(
                offset.x.roundToPx() - dstSize.width / 2,
                offset.y.roundToPx() - dstSize.height / 2
            )
        } else {
            IntOffset.Zero
        }
        val dstOffset = if (offset != DpOffset.Unspecified) {
            IntOffset(
                offset.x.roundToPx(),
                offset.y.roundToPx(),
            )
        } else {
            IntOffset(
                size.center.x.toInt(),
                size.center.y.toInt(),
            )
        }
        val radius = max(dstSize.width, dstSize.height).toFloat() / 2
        val circularGradientShader = RadialGradientShader(
            dstOffset.toOffset(),
            radius,
            listOf(Color.Red, Color.Transparent),
            listOf(0f, 1f)
        )
        val circularGradientBrush = ShaderBrush(circularGradientShader)
        val bottomEdgeGradient = LinearGradientShader(
            from = IntOffset(0, clipToPixelSize.height - bottomEdgeMaskHeightPixels).toOffset(),
            to = IntOffset(0, clipToPixelSize.height).toOffset(),
            listOf(Color.Transparent, background),
            listOf(0f, 1f)
        )
        val bottomEdgeGradientBrush = ShaderBrush(bottomEdgeGradient)
        onDrawWithContent {
            if (dstSize != IntSize.Zero) {
                val circleClipPath = Path().apply {
                    addOval(Rect(dstOffset.toOffset(), radius - 1))
                }
                if (clipToPixelSize != IntSize.Zero) {
                    val path = Path().apply {
                        addRect(Rect(Offset.Zero, clipToPixelSize.toSize()))
                    }
                    drawContext.canvas.clipPath(path, ClipOp.Intersect)
                }
                // Clip external path if needed
                clipPath(circleClipPath, clipOp = ClipOp.Intersect) {
                    drawWithLayer {
                        // Draw background color for blending
                        drawRect(background, size = pixelSize.toSize())
                        // 25% opacity, hard light blend mode
                        drawImage(
                            hashedBitmap,
                            srcSize = IntSize(blurHashComponents, blurHashComponents),
                            dstSize = dstSize,
                            dstOffset = centerOffset,
                            alpha = bottomLayerOpacity * alpha,
                            blendMode = bottomLayerBlendMode,
                        )
                        // 25% opacity, default blend mode
                        drawImage(
                            hashedBitmap,
                            srcSize = IntSize(blurHashComponents, blurHashComponents),
                            dstSize = dstSize,
                            dstOffset = centerOffset,
                            alpha = topLayerOpacity * alpha,
                            blendMode = topLayerBlendMode,
                        )
                        // Erase the outer radius using the gradient brush
                        drawCircle(
                            circularGradientBrush,
                            radius,
                            dstOffset.toOffset(),
                            blendMode = BlendMode.DstIn
                        )
                    }
                }
                drawRect(
                    bottomEdgeGradientBrush,
                    topLeft = IntOffset(0, clipToPixelSize.height - bottomEdgeMaskHeight.roundToPx()).toOffset(),
                    size = IntSize(pixelSize.width, bottomEdgeMaskHeight.roundToPx()).toSize(),
                    alpha = bottomEdgeMaskAlpha
                )
            }
            drawContent()
        }
    }
}

@Composable
private fun DpSize.toIntSize() = with(LocalDensity.current) {
    if (isSpecified) {
        IntSize(width.roundToPx(), height.roundToPx())
    } else {
        IntSize.Zero
    }
}

private val blurHashComponents = 5

@OptIn(ExperimentalMaterial3Api::class)
@DayNightPreviews
@Composable
internal fun BloomPreview() {
    val manuBlurhash = "eVG[A{wJGbS6\$LuPR%ozX9n3C8Se-TxFI;RQsCvzoJozskW=ROWBW?"
    val nadBlurhash = "eePn{tI?xExEja}ooKWWodjtNJoKR,j@a|sBWpS3WDbGazoKWWWWj@"
    var topAppBarHeight by remember { mutableIntStateOf(-1) }
    val topAppBarState = rememberTopAppBarState(
        //    initialHeightOffsetLimit = -52.dp.toPx(), initialHeightOffset = -52.dp.toPx()
    )
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(topAppBarState)
    ElementPreview {
        Scaffold(
            modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                Box {
                    Bloom(
                        modifier = Modifier.size(48.dp),
                        hash = nadBlurhash,
                        background = ElementTheme.materialColors.background,
                        blurSize = DpSize(192.dp, 192.dp),
                        clipToSize = if (topAppBarHeight > 0) DpSize(192.dp, topAppBarHeight.toDp()) else DpSize.Zero,
//            drawContents = false,
//            clipToSize = DpSize(192.dp, 64.dp),
                    )
                    MediumTopAppBar(
                        modifier = Modifier.onSizeChanged { size ->
                            topAppBarHeight = size.height
                        },
                        colors = TopAppBarDefaults.largeTopAppBarColors(
                            containerColor = Color.Transparent,
                            scrolledContainerColor = Color.Black.copy(alpha = 0.05f),
                        ),
                        navigationIcon = {
                            Image(
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .size(32.dp)
                                    .clip(CircleShape),
                                painter = painterResource(id = R.drawable.sample_avatar),
                                contentScale = ContentScale.Crop,
                                contentDescription = null
                            )
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
                for (i in 0..20) {
                    Text("Content", modifier = Modifier.padding(vertical = 20.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@DayNightPreviews
@Composable
internal fun BloomModifierPreview() {
    val manuBlurhash = "eVG[A{wJGbS6\$LuPR%ozX9n3C8Se-TxFI;RQsCvzoJozskW=ROWBW?"
    val nadBlurhash = "eePn{tI?xExEja}ooKWWodjtNJoKR,j@a|sBWpS3WDbGazoKWWWWj@"
    var topAppBarHeight by remember { mutableIntStateOf(-1) }
    val topAppBarState = rememberTopAppBarState(
        //    initialHeightOffsetLimit = -52.dp.toPx(), initialHeightOffset = -52.dp.toPx()
    )
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(topAppBarState)
    ElementPreview {
        Scaffold(
            modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                Box {
                    MediumTopAppBar(
                        modifier = Modifier
                            .onSizeChanged { size ->
                                topAppBarHeight = size.height
                            }
                            .bloom(
                                hash = nadBlurhash,
                                background = ElementTheme.materialColors.background,
                                blurSize = DpSize(256.dp, 256.dp),
                                offset = DpOffset(24.dp, 24.dp),
                                clipToSize = if (topAppBarHeight > 0) DpSize(256.dp, topAppBarHeight.toDp()) else DpSize.Zero,
                            ),
                        colors = TopAppBarDefaults.largeTopAppBarColors(
                            containerColor = Color.Transparent,
                            scrolledContainerColor = Color.Black.copy(alpha = 0.05f),
                        ),
                        navigationIcon = {
                            Image(
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .size(32.dp)
                                    .clip(CircleShape),
                                painter = painterResource(id = R.drawable.sample_avatar),
                                contentScale = ContentScale.Crop,
                                contentDescription = null
                            )
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
                for (i in 0..20) {
                    Text("Content", modifier = Modifier.padding(vertical = 20.dp))
                }
            }
        }
    }
}

@DayNightPreviews
@Composable
internal fun BloomInitialsPreview() {
    ElementPreview {
        val bitmap = initialsBitmap(text = "F", backgroundColor = Color(0xFFFFDFC8), textColor = Color(0xFF850000))
        Bloom(
            bitmap = bitmap.asAndroidBitmap(),
            background = ElementTheme.materialColors.background,
            modifier = Modifier.size(256.dp),
//            blurSize = DpSize(128.dp, 128.dp)
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

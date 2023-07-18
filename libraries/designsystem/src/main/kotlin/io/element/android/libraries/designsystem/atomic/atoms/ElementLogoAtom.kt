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

package io.element.android.libraries.designsystem.atomic.atoms

import android.graphics.BlurMaskFilter
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.R
import io.element.android.libraries.designsystem.preview.DayNightPreviews
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.theme.ElementTheme

@Composable
fun ElementLogoAtom(
    size: ElementLogoAtomSize,
    modifier: Modifier = Modifier,
    darkTheme: Boolean = isSystemInDarkTheme(),
) {
    val blur = if (darkTheme) 160.dp else 24.dp
    //box-shadow: 0px 6.075949668884277px 24.30379867553711px 0px #1B1D2280;
    val shadowColor = if (darkTheme) Color.Black.copy(alpha = 0.4f) else Color(0x401B1D22)
    val backgroundColor = if (darkTheme) Color.White.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.4f)
    val borderColor = if (darkTheme) Color.White.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.4f)
    Box(
        modifier = modifier
            .size(size.outerSize)
            .border(size.borderWidth, borderColor, RoundedCornerShape(size.cornerRadius)),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .size(size.outerSize)
                .shapeShadow(
                    color = shadowColor,
                    cornerRadius = size.cornerRadius,
                    blurRadius = 32.dp,
                    offsetY = 8.dp,
                )
        )
        Box(
            Modifier
                .clip(RoundedCornerShape(size.cornerRadius))
                .size(size.outerSize)
                .background(backgroundColor)
                .blur(blur)
        )
        Image(
            modifier = Modifier.size(size.logoSize),
            painter = painterResource(id = R.drawable.element_logo),
            contentDescription = null
        )
    }
}

sealed class ElementLogoAtomSize(
    val outerSize: Dp,
    val logoSize: Dp,
    val cornerRadius: Dp,
    val borderWidth: Dp,
) {
    object Medium : ElementLogoAtomSize(
        outerSize = 120.dp,
        logoSize = 83.5.dp,
        cornerRadius = 33.dp,
        borderWidth = 0.38.dp,
    )

    object Large : ElementLogoAtomSize(
        outerSize = 158.dp,
        logoSize = 110.dp,
        cornerRadius = 44.dp,
        borderWidth = 1.dp,
    )
}

fun Modifier.shapeShadow(
    color: Color = Color.Black,
    cornerRadius: Dp = 0.dp,
    offsetX: Dp = 0.dp,
    offsetY: Dp = 0.dp,
    blurRadius: Dp = 0.dp,
) = then(
    drawBehind {
        drawIntoCanvas { canvas ->
            val path = Path().apply {
                addRoundRect(RoundRect(Rect(Offset.Zero, size), CornerRadius(cornerRadius.toPx())))
            }

            clipPath(path, ClipOp.Difference) {
                val paint = Paint()
                val frameworkPaint = paint.asFrameworkPaint()
                if (blurRadius != 0.dp) {
                    frameworkPaint.maskFilter = (BlurMaskFilter(blurRadius.toPx(), BlurMaskFilter.Blur.NORMAL))
                }
                frameworkPaint.color = color.toArgb()

                val leftPixel = offsetX.toPx()
                val topPixel = offsetY.toPx()
                val rightPixel = size.width + topPixel
                val bottomPixel = size.height + leftPixel

                canvas.drawRect(
                    left = leftPixel,
                    top = topPixel,
                    right = rightPixel,
                    bottom = bottomPixel,
                    paint = paint,
                )
            }
        }
    }
)

@Composable
@DayNightPreviews
internal fun ElementLogoAtomMediumPreview() {
    ContentToPreview(ElementLogoAtomSize.Medium)
}

@Composable
@DayNightPreviews
internal fun ElementLogoAtomLargePreview() {
    ContentToPreview(ElementLogoAtomSize.Large)
}

@Composable
private fun ContentToPreview(elementLogoAtomSize: ElementLogoAtomSize) {
    ElementPreview {
        Box(
            Modifier
                .size(elementLogoAtomSize.outerSize + 64.dp)
                .background(ElementTheme.colors.bgSubtlePrimary),
            contentAlignment = Alignment.Center
        ) {
            ElementLogoAtom(elementLogoAtomSize)
        }
    }
}

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
) {
    val outerSize = when (size) {
        ElementLogoAtomSize.Large -> 158.dp
        ElementLogoAtomSize.Medium -> 120.dp
    }
    val logoSize = when (size) {
        ElementLogoAtomSize.Large -> 110.dp
        ElementLogoAtomSize.Medium -> 83.5.dp
    }
    val cornerRadius = when (size) {
        ElementLogoAtomSize.Large -> 44.dp
        ElementLogoAtomSize.Medium -> 33.dp
    }
    val borderWidth = when (size) {
        ElementLogoAtomSize.Large -> 1.dp
        ElementLogoAtomSize.Medium -> 0.38.dp
    }
    val blur = if (isSystemInDarkTheme()) {
        160.dp
    } else {
        24.dp
    }
    //box-shadow: 0px 6.075949668884277px 24.30379867553711px 0px #1B1D2280;
    val shadowColor = if (isSystemInDarkTheme()) {
        Color.Black.copy(alpha = 0.4f)
    } else {
        Color(0x401B1D22)
    }
    val backgroundColor = if (isSystemInDarkTheme()) Color.White.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.4f)
    val borderColor = if (isSystemInDarkTheme()) Color.White.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.4f)
    Box(
        modifier = modifier
            .size(outerSize)
            .border(borderWidth, borderColor, RoundedCornerShape(cornerRadius)),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .size(outerSize)
                .shapeShadow(
                    color = shadowColor,
                    cornerRadius = cornerRadius,
                    blurRadius = 32.dp,
                    offsetY = 8.dp,
                )
        )
        Box(
            Modifier
                .clip(RoundedCornerShape(cornerRadius))
                .size(outerSize)
                .background(backgroundColor)
                .blur(blur)
        )
        Image(
            modifier = Modifier.size(logoSize),
            painter = painterResource(id = R.drawable.element_logo),
            contentDescription = null
        )
    }
}

enum class ElementLogoAtomSize {
    Medium,
    Large
}

@Composable
@DayNightPreviews
internal fun ElementLogoAtomPreview() {
    ElementPreview {
        Box(
            Modifier
                .size(180.dp)
                .background(ElementTheme.colors.bgSubtlePrimary),
            contentAlignment = Alignment.Center
        ) {
            ElementLogoAtom(ElementLogoAtomSize.Large)
        }
    }
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

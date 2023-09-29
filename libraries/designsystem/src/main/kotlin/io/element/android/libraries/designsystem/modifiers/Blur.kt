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

package io.element.android.libraries.designsystem.modifiers

import android.graphics.BlurMaskFilter
import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * @return true if the blur modifier is supported on the current OS version.
 *
 * The docs say the `blur` modifier is only supported on Android 12+:
 * https://developer.android.com/reference/kotlin/androidx/compose/ui/Modifier#(androidx.compose.ui.Modifier).blur(androidx.compose.ui.unit.Dp,androidx.compose.ui.draw.BlurredEdgeTreatment)
 * */
@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S)
fun canUseBlur(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

@Composable
fun canUseBlurMaskFilter() = !LocalView.current.isHardwareAccelerated

fun Modifier.blurredShapeShadow(
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

            // Draw the blurred shadow, then cut out the shape from it
            clipPath(path, ClipOp.Difference) {
                val paint = Paint()
                val frameworkPaint = paint.asFrameworkPaint()
                if (blurRadius != 0.dp) {
                    frameworkPaint.maskFilter = BlurMaskFilter(blurRadius.toPx(), BlurMaskFilter.Blur.NORMAL)
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

fun Modifier.blurCompat(
    radius: Dp,
    edgeTreatment: BlurredEdgeTreatment = BlurredEdgeTreatment.Rectangle
): Modifier {
    return when {
        radius.value == 0f -> this
        canUseBlur() -> blur(radius, edgeTreatment)
        else -> this // Added in case we find a way to make this work on older devices
    }
}

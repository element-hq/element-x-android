/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.libraries.designsystem.components.button

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.RadialGradientShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import io.element.android.compound.annotations.CoreColorToken
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.compound.tokens.generated.internal.LightColorTokens
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon

@OptIn(CoreColorToken::class)
@Composable
fun GradientFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(25),
    content: @Composable () -> Unit,
) {
    val linearShaderBrush = remember {
        object : ShaderBrush() {
            override fun createShader(size: Size): Shader {
                return LinearGradientShader(
                    from = Offset(size.width, size.height),
                    to = Offset(size.width, 0f),
                    colors = listOf(
                        LightColorTokens.colorBlue900,
                        LightColorTokens.colorGreen700,
                    ),
                )
            }
        }
    }
    val radialShaderBrush = remember {
        object : ShaderBrush() {
            override fun createShader(size: Size): Shader {
                return RadialGradientShader(
                    center = size.center,
                    radius = size.width / 2,
                    colors = listOf(
                        LightColorTokens.colorGreen700,
                        LightColorTokens.colorBlue900,
                    )
                )
            }
        }
    }

    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .graphicsLayer(shape = shape, clip = false)
            .clip(shape)
            .drawBehind {
                drawRect(brush = radialShaderBrush, alpha = 0.4f)
                drawRect(brush = linearShaderBrush)
            }
            .clickable(
                enabled = true,
                onClick = onClick,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = Color.White)
            ),
        contentAlignment = Alignment.Center
    ) {
        CompositionLocalProvider(LocalContentColor provides Color.White) {
            content()
        }
    }
}

@PreviewsDayNight
@Composable
internal fun GradientFloatingActionButtonPreview() {
    ElementPreview {
        Box(modifier = Modifier.padding(20.dp)) {
            GradientFloatingActionButton(
                modifier = Modifier.size(48.dp),
                onClick = {},
            ) {
                Icon(imageVector = CompoundIcons.ChatNew(), contentDescription = null)
            }
        }
    }
}

@PreviewsDayNight
@Composable
internal fun GradientFloatingActionButtonCircleShapePreview() {
    ElementPreview {
        Box(modifier = Modifier.padding(20.dp)) {
            GradientFloatingActionButton(
                shape = CircleShape,
                modifier = Modifier.size(48.dp),
                onClick = {},
            ) {
                Icon(
                    modifier = Modifier.padding(start = 2.dp),
                    imageVector = CompoundIcons.SendSolid(),
                    contentDescription = null
                )
            }
        }
    }
}

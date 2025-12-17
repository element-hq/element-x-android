/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
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
import androidx.compose.ui.graphics.BlendMode
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
import io.element.android.libraries.designsystem.colors.gradientActionColors
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
    val colors = gradientActionColors()
    val linearShaderBrush = remember {
        object : ShaderBrush() {
            override fun createShader(size: Size): Shader {
                return LinearGradientShader(
                    from = Offset(size.width, size.height),
                    to = Offset(size.width, 0f),
                    colors = colors,
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
                    colors = colors,
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
                drawRect(brush = linearShaderBrush)
                drawRect(brush = radialShaderBrush, alpha = 0.4f, blendMode = BlendMode.Overlay)
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

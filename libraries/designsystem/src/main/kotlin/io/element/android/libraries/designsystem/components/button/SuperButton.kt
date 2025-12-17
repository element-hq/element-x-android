/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.button

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.colors.gradientActionColors
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.ButtonSize
import io.element.android.libraries.designsystem.theme.components.HorizontalDivider
import io.element.android.libraries.designsystem.theme.components.lowHorizontalPaddingValue

@Composable
fun SuperButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(50),
    buttonSize: ButtonSize = ButtonSize.Large,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val contentPadding = remember(buttonSize) {
        when (buttonSize) {
            ButtonSize.Large -> PaddingValues(horizontal = 24.dp, vertical = 13.dp)
            ButtonSize.LargeLowPadding -> PaddingValues(horizontal = lowHorizontalPaddingValue, vertical = 13.dp)
            ButtonSize.Medium -> PaddingValues(horizontal = 20.dp, vertical = 9.dp)
            ButtonSize.MediumLowPadding -> PaddingValues(horizontal = lowHorizontalPaddingValue, vertical = 9.dp)
            ButtonSize.Small -> PaddingValues(horizontal = 16.dp, vertical = 5.dp)
        }
    }
    val colors = gradientActionColors()
    val shaderBrush = remember(colors) {
        object : ShaderBrush() {
            override fun createShader(size: Size): Shader {
                return LinearGradientShader(
                    from = Offset(0f, 0f),
                    to = Offset(0f, size.height),
                    colors = colors,
                )
            }
        }
    }
    val border = if (enabled) {
        BorderStroke(1.dp, shaderBrush)
    } else {
        BorderStroke(1.dp, ElementTheme.colors.borderDisabled)
    }
    val backgroundColor = ElementTheme.colors.bgCanvasDefault
    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .graphicsLayer(shape = shape, clip = false)
            .clip(shape)
            .border(border, shape)
            .drawBehind {
                drawRect(backgroundColor)
                drawRect(brush = shaderBrush, alpha = 0.04f)
            }
            .clickable(
                enabled = enabled,
                onClick = onClick,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple()
            )
            .padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        CompositionLocalProvider(
            LocalContentColor provides if (enabled) ElementTheme.colors.textPrimary else ElementTheme.colors.textDisabled,
            LocalTextStyle provides ElementTheme.typography.fontBodyLgMedium,
        ) {
            content()
        }
    }
}

@PreviewsDayNight
@Composable
internal fun SuperButtonPreview() {
    ElementPreview {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            SuperButton(
                modifier = Modifier.padding(10.dp),
                buttonSize = ButtonSize.Large,
                onClick = {},
            ) {
                Text("Super button!")
            }

            SuperButton(
                modifier = Modifier.padding(10.dp),
                buttonSize = ButtonSize.LargeLowPadding,
                onClick = {},
            ) {
                Text("Super LargeLowPadding")
            }

            SuperButton(
                modifier = Modifier.padding(10.dp),
                buttonSize = ButtonSize.Medium,
                onClick = {},
            ) {
                Text("Super button!")
            }

            SuperButton(
                modifier = Modifier.padding(10.dp),
                buttonSize = ButtonSize.MediumLowPadding,
                onClick = {},
            ) {
                Text("Super MediumLowPadding")
            }

            SuperButton(
                modifier = Modifier.padding(10.dp),
                buttonSize = ButtonSize.Small,
                onClick = {},
            ) {
                Text("Super button!")
            }

            HorizontalDivider()

            SuperButton(
                modifier = Modifier.padding(10.dp),
                buttonSize = ButtonSize.Large,
                enabled = false,
                onClick = {},
            ) {
                Text("Super button!")
            }

            SuperButton(
                modifier = Modifier.padding(10.dp),
                buttonSize = ButtonSize.Medium,
                enabled = false,
                onClick = {},
            ) {
                Text("Super button!")
            }

            SuperButton(
                modifier = Modifier.padding(10.dp),
                buttonSize = ButtonSize.Small,
                enabled = false,
                onClick = {},
            ) {
                Text("Super button!")
            }
        }
    }
}

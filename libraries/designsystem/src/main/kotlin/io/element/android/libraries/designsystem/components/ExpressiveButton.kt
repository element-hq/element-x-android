/*
 * Copyright 2024 Element
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

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.theme.motion.ExpressiveInteraction
import io.element.android.libraries.designsystem.theme.motion.MotionTokens

/**
 * Material 3 Expressive Button with responsive scale animation on press.
 * 
 * Features:
 * - Spring-based scale animation (0.98x when pressed)
 * - Responsive elevation feedback
 * - Material 3 motion specifications
 * 
 * @param onClick Callback when button is clicked
 * @param modifier Modifier to apply
 * @param enabled Whether button is enabled
 * @param variant Button style variant (Primary, Secondary, Elevated, Filled Tonal, Outlined, Text)
 * @param content Button content (typically Text)
 */
@Composable
fun ExpressiveButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: ButtonVariant = ButtonVariant.Primary,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable RowScope.() -> Unit,
) {
    val isPressed = remember { mutableStateOf(false) }
    
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> isPressed.value = true
                is PressInteraction.Release -> isPressed.value = false
                is PressInteraction.Cancel -> isPressed.value = false
            }
        }
    }

    val scaleAnimationState = ExpressiveInteraction.scaleOnPress(
        isPressed = isPressed.value && enabled,
        pressedScale = 0.98f,
        durationMillis = MotionTokens.durationShort,
        dampingRatio = 0.6f,
        stiffness = 500f
    )

    val buttonContent: @Composable RowScope.() -> Unit = {
        content()
    }

    when (variant) {
        ButtonVariant.Primary -> Button(
            onClick = onClick,
            modifier = modifier
                .scale(scaleAnimationState.value)
                .defaultMinSize(minHeight = 40.dp),
            enabled = enabled,
            interactionSource = interactionSource,
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp),
            content = buttonContent,
        )
        ButtonVariant.Secondary -> ElevatedButton(
            onClick = onClick,
            modifier = modifier
                .scale(scaleAnimationState.value)
                .defaultMinSize(minHeight = 40.dp),
            enabled = enabled,
            interactionSource = interactionSource,
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp),
            content = buttonContent,
        )
        ButtonVariant.FilledTonal -> FilledTonalButton(
            onClick = onClick,
            modifier = modifier
                .scale(scaleAnimationState.value)
                .defaultMinSize(minHeight = 40.dp),
            enabled = enabled,
            interactionSource = interactionSource,
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp),
            content = buttonContent,
        )
        ButtonVariant.Outlined -> OutlinedButton(
            onClick = onClick,
            modifier = modifier
                .scale(scaleAnimationState.value)
                .defaultMinSize(minHeight = 40.dp),
            enabled = enabled,
            interactionSource = interactionSource,
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp),
            content = buttonContent,
        )
        ButtonVariant.Text -> TextButton(
            onClick = onClick,
            modifier = modifier
                .scale(scaleAnimationState.value)
                .defaultMinSize(minHeight = 40.dp),
            enabled = enabled,
            interactionSource = interactionSource,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
            content = buttonContent,
        )
    }
}

/**
 * Expressive Button with icon and text.
 * 
 * @param onClick Callback when button is clicked
 * @param modifier Modifier to apply
 * @param enabled Whether button is enabled
 * @param variant Button style variant
 * @param icon Icon composable (usually Icon composable)
 * @param text Text content
 */
@Composable
fun ExpressiveButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: ButtonVariant = ButtonVariant.Primary,
    icon: @Composable (() -> Unit)? = null,
    text: String,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    ExpressiveButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        variant = variant,
        interactionSource = interactionSource,
    ) {
        if (icon != null) {
            icon()
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text = text)
    }
}

/**
 * Expressive Button with text only.
 * 
 * @param onClick Callback when button is clicked
 * @param text Button text
 * @param modifier Modifier to apply
 * @param enabled Whether button is enabled
 * @param variant Button style variant
 */
@Composable
fun ExpressiveButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: ButtonVariant = ButtonVariant.Primary,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    ExpressiveButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        variant = variant,
        interactionSource = interactionSource,
    ) {
        Text(text = text)
    }
}

/**
 * Button variants following Material 3 design system.
 */
enum class ButtonVariant {
    /** Filled button with solid color background - for primary actions */
    Primary,
    
    /** Elevated button with shadow - for secondary elevated actions */
    Secondary,
    
    /** Filled tonal button with tonal color - for secondary actions */
    FilledTonal,
    
    /** Outlined button with border only - for tertiary actions */
    Outlined,
    
    /** Text button minimal style - for least important actions */
    Text,
}

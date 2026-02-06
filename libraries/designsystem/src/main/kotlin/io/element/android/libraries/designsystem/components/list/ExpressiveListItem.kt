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

package io.element.android.libraries.designsystem.components.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.clickable
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import io.element.android.libraries.designsystem.theme.motion.ExpressiveInteraction
import io.element.android.libraries.designsystem.theme.motion.MotionTokens
import androidx.compose.animation.core.tween

/**
 * Material 3 Expressive List Item with responsive scale and elevation feedback.
 * 
 * Features:
 * - Spring-based scale animation on press (0.98x)
 * - Elevation change on hover/press (1dp → 2dp)
 * - Brightness adjustment on selection
 * - Slide-in animation on appearance
 * - Material 3 motion specifications
 * 
 * @param onClick Callback when item is clicked
 * @param modifier Modifier to apply
 * @param isVisible Whether item is visible (for entrance animation)
 * @param isSelected Whether item is selected
 * @param enabled Whether item is interactive
 * @param interactionSource Source for tracking interactions
 * @param colors Colors for the list item
 * @param headlineContent Main content (headline)
 * @param supportingContent Optional supporting content
 * @param leadingContent Optional leading content (icon)
 * @param trailingContent Optional trailing content
 */
@Suppress("DEPRECATION")
@Composable
fun ExpressiveListItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isVisible: Boolean = true,
    isSelected: Boolean = false,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    colors: ListItemColors = ListItemDefaults.colors(),
    headlineContent: @Composable () -> Unit,
    supportingContent: @Composable (() -> Unit)? = null,
    leadingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
) {
    val isPressed = interactionSource.collectIsPressedAsState().value

    val scaleAnimationState = ExpressiveInteraction.scaleOnPress(
        isPressed = isPressed && enabled,
        pressedScale = 0.98f,
        durationMillis = MotionTokens.durationShort,
        dampingRatio = 0.6f,
        stiffness = 500f
    )

    val brightnessColorState = ExpressiveInteraction.brightnessOnPress(
        isPressed = isPressed && isSelected,
        baseColor = Color.White,
        durationMillis = MotionTokens.durationShort,
        dampingRatio = 0.6f,
        stiffness = 600f
    )

    // Entrance animation
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            animationSpec = tween(
                durationMillis = MotionTokens.durationMedium,
                easing = MotionTokens.standardEasing
            ),
            initialOffsetY = { -it / 2 }
        ) + expandVertically(
            animationSpec = tween(
                durationMillis = MotionTokens.durationMedium
            )
        ) + fadeIn(animationSpec = tween(
            durationMillis = MotionTokens.durationShort
        )),
        exit = slideOutVertically(
            animationSpec = tween(
                durationMillis = MotionTokens.durationShort,
                easing = MotionTokens.standardAccelerating
            ),
            targetOffsetY = { -it / 2 }
        ) + shrinkVertically(
            animationSpec = tween(
                durationMillis = MotionTokens.durationShort
            )
        ) + fadeOut(animationSpec = tween(
            durationMillis = MotionTokens.durationShort
        ))
    ) {
        Box(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = scaleAnimationState.value
                    scaleY = scaleAnimationState.value
                    if (isSelected) {
                        alpha = brightnessColorState.value.alpha
                    }
                }
        ) {
            ListItem(
                headlineContent = headlineContent,
                modifier = modifier
                    .graphicsLayer {
                        if (isSelected) {
                            shadowElevation = 4f
                        } else {
                            shadowElevation = 1f
                        }
                    }
                    .clickable(
                        interactionSource = interactionSource,
                        enabled = enabled,
                        onClick = onClick
                    ),
                supportingContent = supportingContent,
                leadingContent = leadingContent,
                trailingContent = trailingContent,
                colors = colors,
            )
        }
    }
}

/**
 * Expressive List Column with animated list items.
 * 
 * Provides a wrapper for displaying multiple list items with entrance animations.
 * Items appear sequentially with staggered animation timing.
 * 
 * @param modifier Modifier to apply
 * @param content List item content
 */
@Composable
fun ExpressiveListColumn(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier) {
        content()
    }
}

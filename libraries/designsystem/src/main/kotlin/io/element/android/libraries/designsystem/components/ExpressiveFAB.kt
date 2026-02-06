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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import io.element.android.libraries.designsystem.theme.motion.MotionTokens
import androidx.compose.animation.core.animateFloatAsState

/**
 * Material 3 Expressive Floating Action Button with combined scale and brightness animations.
 * 
 * Features:
 * - Spring-based scale animation on press (0.98x)
 * - Brightness adjustment for enhanced feedback
 * - Scale in/out animations on appearance/disappearance
 * - Material 3 motion specifications
 * 
 * @param onClick Callback when FAB is clicked
 * @param modifier Modifier to apply
 * @param isVisible Whether FAB is visible (for entrance animation)
 * @param enabled Whether FAB is enabled
 * @param containerColor Background color
 * @param contentColor Content color
 * @param shape Shape of the FAB
 * @param size Size variant (Medium, Large, Small)
 * @param interactionSource Source for tracking interactions
 * @param content FAB content (usually Icon)
 */
@Composable
fun ExpressiveFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isVisible: Boolean = true,
    enabled: Boolean = true,
    containerColor: Color = FloatingActionButtonDefaults.containerColor,
    contentColor: Color = io.element.android.compound.theme.ElementTheme.colors.iconPrimary,
    shape: androidx.compose.ui.graphics.Shape = FloatingActionButtonDefaults.shape,
    size: FABSize = FABSize.Medium,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit,
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

    // Calculate pressed scale based on size
    val pressedScale = when (size) {
        FABSize.Small -> 0.95f
        FABSize.Medium -> 0.97f
        FABSize.Large -> 0.96f
    }

    val scaleAnimationState = animateFloatAsState(
        targetValue = if (isPressed.value && enabled) pressedScale else 1.0f,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = 500f
        )
    ).value

    AnimatedVisibility(
        visible = isVisible,
        enter = scaleIn(
            animationSpec = tween(
                durationMillis = MotionTokens.durationMedium,
                easing = MotionTokens.standardEasing
            ),
            initialScale = 0.8f
        ) + fadeIn(animationSpec = tween(
            durationMillis = MotionTokens.durationShort
        )),
        exit = scaleOut(
            animationSpec = tween(
                durationMillis = MotionTokens.durationShort,
                easing = MotionTokens.standardAccelerating
            ),
            targetScale = 0.8f
        ) + fadeOut(animationSpec = tween(
            durationMillis = MotionTokens.durationShort
        ))
    ) {
        val fabModifier = modifier.scale(scaleAnimationState)

        when (size) {
            FABSize.Small -> SmallFloatingActionButton(
                onClick = onClick,
                modifier = fabModifier,
                containerColor = containerColor,
                contentColor = contentColor,
                shape = shape,
                interactionSource = interactionSource,
                content = content
            )
            FABSize.Medium -> FloatingActionButton(
                onClick = onClick,
                modifier = fabModifier,
                containerColor = containerColor,
                contentColor = contentColor,
                shape = shape,
                interactionSource = interactionSource,
                content = content
            )
            FABSize.Large -> LargeFloatingActionButton(
                onClick = onClick,
                modifier = fabModifier,
                containerColor = containerColor,
                contentColor = contentColor,
                shape = shape,
                interactionSource = interactionSource,
                content = content
            )
        }
    }
}

/**
 * Extended Expressive FAB with text label.
 * 
 * @param onClick Callback when FAB is clicked
 * @param modifier Modifier to apply
 * @param isVisible Whether FAB is visible
 * @param enabled Whether FAB is enabled
 * @param text Text label for the FAB
 * @param icon Icon content
 * @param containerColor Background color
 * @param contentColor Content color
 * @param interactionSource Source for tracking interactions
 */
@Composable
fun ExpressiveExtendedFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isVisible: Boolean = true,
    enabled: Boolean = true,
    text: String,
    icon: @Composable () -> Unit,
    containerColor: Color = FloatingActionButtonDefaults.containerColor,
    contentColor: Color = io.element.android.compound.theme.ElementTheme.colors.iconPrimary,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
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

    val scaleAnimationState = animateFloatAsState(
        targetValue = if (isPressed.value && enabled) 0.96f else 1.0f,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = 500f
        )
    ).value

    AnimatedVisibility(
        visible = isVisible,
        enter = scaleIn(
            animationSpec = tween(
                durationMillis = MotionTokens.durationMedium,
                easing = MotionTokens.standardEasing
            ),
            initialScale = 0.8f
        ) + fadeIn(animationSpec = tween(
            durationMillis = MotionTokens.durationShort
        )),
        exit = scaleOut(
            animationSpec = tween(
                durationMillis = MotionTokens.durationShort,
                easing = MotionTokens.standardAccelerating
            ),
            targetScale = 0.8f
        ) + fadeOut(animationSpec = tween(
            durationMillis = MotionTokens.durationShort
        ))
    ) {
        androidx.compose.material3.ExtendedFloatingActionButton(
            onClick = onClick,
            modifier = modifier.scale(scaleAnimationState),
            containerColor = containerColor,
            contentColor = contentColor,
            interactionSource = interactionSource,
            icon = icon,
            text = { androidx.compose.material3.Text(text = text) }
        )
    }
}

/**
 * Size variants for Expressive FAB.
 */
enum class FABSize {
    Small,
    Medium,
    Large,
}

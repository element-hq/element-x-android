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

package io.element.android.libraries.designsystem.components.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.animation.core.tween
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.theme.motion.MotionTokens

/**
 * Material 3 Expressive Bottom Sheet with smooth slide + fade animations.
 * 
 * Features:
 * - Slide up animation on appearance (300ms)
 * - Slide down animation on dismissal (200ms)
 * - Fade effects for smooth visual transition
 * - Scrim (background) with fade
 * - Material 3 motion specifications
 * 
 * This is a wrapper around [ModalBottomSheet] that adds expressive animations.
 * 
 * @param onDismissRequest Callback when the sheet should be dismissed
 * @param modifier Modifier to apply to the entire sheet
 * @param sheetState State of the modal bottom sheet
 * @param scrimColor Color of the scrim background
 * @param containerColor Color of the sheet container
 * @param contentColor Color of the sheet content
 * @param tonalElevation Elevation of the sheet surface
 * @param sheetContent Content inside the bottom sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpressiveModalBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    scrimColor: Color = Color.Black.copy(alpha = 0.32f),
    containerColor: Color = ElementTheme.colors.bgSubtlePrimary,
    contentColor: Color = ElementTheme.colors.textPrimary,
    tonalElevation: androidx.compose.ui.unit.Dp = 16.dp,
    sheetContent: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = modifier
    ) {
        // Animated scrim background
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(animationSpec = tween(
                durationMillis = MotionTokens.durationMedium
            )),
            exit = fadeOut(animationSpec = tween(
                durationMillis = MotionTokens.durationShort
            ))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(scrimColor)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismissRequest
                    )
            )
        }

        // Animated sheet content
        AnimatedVisibility(
            visible = true,
            enter = slideInVertically(
                animationSpec = tween(
                    durationMillis = MotionTokens.durationLong,
                    easing = MotionTokens.standardEasing
                ),
                initialOffsetY = { it / 2 }
            ) + fadeIn(animationSpec = tween(
                durationMillis = MotionTokens.durationMedium
            )),
            exit = slideOutVertically(
                animationSpec = tween(
                    durationMillis = MotionTokens.durationMedium,
                    easing = MotionTokens.standardAccelerating
                ),
                targetOffsetY = { it / 2 }
            ) + fadeOut(animationSpec = tween(
                durationMillis = MotionTokens.durationShort
            ))
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .systemBarsPadding(),
                color = containerColor,
                contentColor = contentColor,
                tonalElevation = tonalElevation,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(
                    topStart = 28.dp,
                    topEnd = 28.dp
                ),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    content = sheetContent
                )
            }
        }
    }
}

/**
 * Simplified Expressive Bottom Sheet wrapper.
 * 
 * @param isVisible Whether the sheet is visible
 * @param onDismiss Callback when sheet should be dismissed
 * @param modifier Modifier to apply
 * @param sheetState State of the sheet
 * @param content Content inside the sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpressiveBottomSheetScaffold(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    content: @Composable ColumnScope.() -> Unit,
) {
    if (isVisible) {
        ExpressiveModalBottomSheet(
            onDismissRequest = onDismiss,
            modifier = modifier,
            sheetState = sheetState,
            sheetContent = content
        )
    }
}

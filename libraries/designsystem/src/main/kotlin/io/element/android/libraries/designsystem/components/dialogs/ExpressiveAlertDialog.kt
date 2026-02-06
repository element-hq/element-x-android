/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.DialogProperties
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.theme.motion.MotionTokens

/**
 * Expressive Alert Dialog with Material 3 motion and more dynamic animations.
 * Provides better feedback and visual hierarchy compared to standard dialogs.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpressiveAlertDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    properties: DialogProperties = DialogProperties(),
    content: @Composable () -> Unit,
) {
    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        properties = properties,
        content = {
            // Scrim with fade animation
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.32f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismissRequest
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Content with scale and fade animation
                AnimatedVisibility(
                    visible = true,
                    enter = scaleIn(
                        animationSpec = tween(
                            durationMillis = MotionTokens.durationMedium,
                            easing = MotionTokens.standardEasing
                        ),
                        initialScale = 0.8f
                    ) + fadeIn(
                        animationSpec = tween(
                            durationMillis = MotionTokens.durationMedium,
                            easing = MotionTokens.standardDecelerating
                        )
                    ),
                    exit = scaleOut(
                        animationSpec = tween(
                            durationMillis = MotionTokens.durationShort,
                            easing = MotionTokens.standardAccelerating
                        ),
                        targetScale = 0.95f
                    ) + fadeOut(
                        animationSpec = tween(
                            durationMillis = MotionTokens.durationShort,
                            easing = MotionTokens.standardAccelerating
                        )
                    )
                ) {
                    content()
                }
            }
        }
    )
}

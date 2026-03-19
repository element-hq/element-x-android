/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.animation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.EaseInBack
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalContext
import io.element.android.libraries.androidutils.system.areAnimationsEnabled

/**
 * Material 3 motion utilities for consistent animation behavior across the app.
 *
 * M3 motion uses emphasized easing for enter/exit and standard easing for in-place changes.
 */
object M3Motion {
    // M3 duration tokens
    const val DURATION_SHORT = 150
    const val DURATION_MEDIUM = 300
    const val DURATION_LONG = 500
    const val DURATION_EXTRA_LONG = 700

    // M3 enter transition: fade + slide from bottom
    val enterTransition: EnterTransition
        get() = fadeIn(
            animationSpec = tween(
                durationMillis = DURATION_MEDIUM,
                easing = LinearOutSlowInEasing
            )
        ) + slideInVertically(
            animationSpec = tween(
                durationMillis = DURATION_MEDIUM,
                easing = LinearOutSlowInEasing
            ),
            initialOffsetY = { it / 8 }
        )

    // M3 exit transition: fade + slide to bottom
    val exitTransition: ExitTransition
        get() = fadeOut(
            animationSpec = tween(
                durationMillis = DURATION_SHORT,
                easing = FastOutSlowInEasing
            )
        ) + slideOutVertically(
            animationSpec = tween(
                durationMillis = DURATION_SHORT,
                easing = FastOutSlowInEasing
            ),
            targetOffsetY = { it / 8 }
        )

    // Simple fade enter (for less prominent elements)
    val fadeEnter: EnterTransition
        get() = fadeIn(
            animationSpec = tween(
                durationMillis = DURATION_SHORT,
                easing = LinearOutSlowInEasing
            )
        )

    // Simple fade exit
    val fadeExit: ExitTransition
        get() = fadeOut(
            animationSpec = tween(
                durationMillis = DURATION_SHORT,
                easing = FastOutSlowInEasing
            )
        )
}

/**
 * Returns whether reduce motion is enabled (system "Remove animations" setting).
 */
val isReduceMotionEnabled: Boolean
    @Composable
    @ReadOnlyComposable
    get() = !LocalContext.current.areAnimationsEnabled()

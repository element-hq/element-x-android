/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.animation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import io.element.android.libraries.androidutils.system.areAnimationsEnabled

/**
 * Material 3 Expressive motion utilities powered by [MaterialTheme.motionScheme].
 *
 * Spring-physics based animations replace the previous tween-based constants.
 * All transitions respect the system "Remove animations" accessibility setting.
 */
object M3Motion {
    /**
     * M3 Expressive enter transition: fade + slide from bottom using MotionScheme springs.
     */
    val enterTransition: EnterTransition
        @OptIn(ExperimentalMaterial3ExpressiveApi::class)
        @Composable
        get() {
            if (isReduceMotionEnabled) {
                return fadeIn(animationSpec = snap())
            }
            val spatialSpec = MaterialTheme.motionScheme.defaultSpatialSpec<IntOffset>()
            val effectsSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()
            return fadeIn(animationSpec = effectsSpec) + slideInVertically(
                animationSpec = spatialSpec,
                initialOffsetY = { it / 8 }
            )
        }

    /**
     * M3 Expressive exit transition: fade + slide to bottom using MotionScheme springs.
     */
    val exitTransition: ExitTransition
        @OptIn(ExperimentalMaterial3ExpressiveApi::class)
        @Composable
        get() {
            if (isReduceMotionEnabled) {
                return fadeOut(animationSpec = snap())
            }
            val spatialSpec = MaterialTheme.motionScheme.defaultSpatialSpec<IntOffset>()
            val effectsSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()
            return fadeOut(animationSpec = effectsSpec) + slideOutVertically(
                animationSpec = spatialSpec,
                targetOffsetY = { it / 8 }
            )
        }

    /**
     * Simple fade enter using MotionScheme effects spec.
     */
    val fadeEnter: EnterTransition
        @OptIn(ExperimentalMaterial3ExpressiveApi::class)
        @Composable
        get() {
            if (isReduceMotionEnabled) {
                return fadeIn(animationSpec = snap())
            }
            return fadeIn(animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec())
        }

    /**
     * Lightweight animation spec for list item animations (animateItem).
     * Uses tween instead of spring to avoid per-frame spring solver overhead during scrolling.
     * Respects the system "Remove animations" accessibility setting.
     */
    @Composable
    @ReadOnlyComposable
    fun <T> listItemSpec(): FiniteAnimationSpec<T> =
        if (isReduceMotionEnabled) snap() else tween(durationMillis = 200, easing = FastOutSlowInEasing)

    /**
     * For animate*AsState calls (color, float, dp). Uses MotionScheme effects spec (spring-physics).
     * Respects the system "Remove animations" accessibility setting.
     */
    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun <T> defaultValueSpec(): FiniteAnimationSpec<T> =
        if (isReduceMotionEnabled) snap() else MaterialTheme.motionScheme.fastEffectsSpec()

    /**
     * For animateContentSize. Uses MotionScheme spatial spec (spring-physics).
     * Respects the system "Remove animations" accessibility setting.
     */
    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun defaultContentSizeSpec(): FiniteAnimationSpec<IntSize> =
        if (isReduceMotionEnabled) snap() else MaterialTheme.motionScheme.defaultSpatialSpec()

    /**
     * Simple fade exit using MotionScheme effects spec.
     */
    val fadeExit: ExitTransition
        @OptIn(ExperimentalMaterial3ExpressiveApi::class)
        @Composable
        get() {
            if (isReduceMotionEnabled) {
                return fadeOut(animationSpec = snap())
            }
            return fadeOut(animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec())
        }
}

/**
 * Returns whether reduce motion is enabled (system "Remove animations" setting).
 */
val isReduceMotionEnabled: Boolean
    @Composable
    @ReadOnlyComposable
    get() = !LocalContext.current.areAnimationsEnabled()

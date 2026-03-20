/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.animation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.IntOffset

/**
 * Reusable M3 Expressive transition composables powered by [MaterialTheme.motionScheme].
 * When reduce-motion is enabled the transitions collapse to [snap].
 */
object ExpressiveTransitions {
    /**
     * Shared-axis enter transition (horizontal slide + fade) using MotionScheme spatial spec.
     */
    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun enterTransition(): EnterTransition {
        if (isReduceMotionEnabled) {
            return fadeIn(animationSpec = snap())
        }
        val spatialSpec = MaterialTheme.motionScheme.defaultSpatialSpec<IntOffset>()
        val effectsSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()
        return fadeIn(animationSpec = effectsSpec) + slideInHorizontally(
            animationSpec = spatialSpec,
            initialOffsetX = { it / 5 }
        )
    }

    /**
     * Shared-axis exit transition (horizontal slide + fade) using MotionScheme spatial spec.
     */
    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun exitTransition(): ExitTransition {
        if (isReduceMotionEnabled) {
            return fadeOut(animationSpec = snap())
        }
        val spatialSpec = MaterialTheme.motionScheme.defaultSpatialSpec<IntOffset>()
        val effectsSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()
        return fadeOut(animationSpec = effectsSpec) + slideOutHorizontally(
            animationSpec = spatialSpec,
            targetOffsetX = { -it / 5 }
        )
    }

    /**
     * Vertical enter transition (slide from bottom + fade) using MotionScheme.
     */
    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun verticalEnterTransition(): EnterTransition {
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
     * Vertical exit transition (slide to bottom + fade) using MotionScheme.
     */
    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun verticalExitTransition(): ExitTransition {
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
    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun fadeEnterTransition(): EnterTransition {
        if (isReduceMotionEnabled) {
            return fadeIn(animationSpec = snap())
        }
        return fadeIn(animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec())
    }

    /**
     * Simple fade exit using MotionScheme effects spec.
     */
    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun fadeExitTransition(): ExitTransition {
        if (isReduceMotionEnabled) {
            return fadeOut(animationSpec = snap())
        }
        return fadeOut(animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec())
    }
}

/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.theme.motion

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing

/**
 * Material 3 Motion tokens for expressive animations.
 * These easing functions create more dynamic and expressive motion
 * that feels natural and responsive.
 *
 * Based on: https://m3.material.io/styles/motion/easing-and-duration/easing
 */
object MotionTokens {
    /**
     * Standard easing: Used for elements that enter from the edge or
     * expand within the component. Fast beginning with gradual deceleration.
     */
    val standardEasing: Easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)

    /**
     * Standard decelerate: Used for elements that are already visible
     * and remain in the viewport. Creates a settling motion.
     */
    val standardDecelerating: Easing = CubicBezierEasing(0.0f, 0.0f, 0.0f, 1.0f)

    /**
     * Standard accelerate: Used for elements that exit the viewport
     * or become hidden. Fast beginning with rapid acceleration.
     */
    val standardAccelerating: Easing = CubicBezierEasing(0.3f, 0.0f, 1.0f, 1.0f)

    /**
     * Emphasized easing: Used for important transitions and state changes.
     * More expressive motion that draws attention.
     */
    val emphasizedEasing: Easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)

    /**
     * Emphasized decelerate: Creates a smooth settle with emphasis.
     */
    val emphasizedDecelerating: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)

    /**
     * Emphasized accelerate: Strong, expressive exit motion.
     */
    val emphasizedAccelerating: Easing = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)

    // Duration tokens (in milliseconds)
    
    /**
     * Extra short duration: 50ms
     * Used for microinteractions and immediate feedback
     */
    const val durationExtraShort: Int = 50

    /**
     * Short duration: 100ms
     * Used for simple transitions and hover states
     */
    const val durationShort: Int = 100

    /**
     * Medium duration: 200ms
     * Used for standard transitions
     */
    const val durationMedium: Int = 200

    /**
     * Long duration: 300ms
     * Used for important transitions and animations
     */
    const val durationLong: Int = 300

    /**
     * Extra long duration: 500ms
     * Used for complex animations and sequential transitions
     */
    const val durationExtraLong: Int = 500

    /**
     * Delayed duration: 1000ms
     * Used for entrance animations and loading indicators
     */
    const val durationDelayed: Int = 1000
}

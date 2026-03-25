/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.architecture.animation

import android.provider.Settings
import androidx.compose.animation.core.snap
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import com.bumble.appyx.core.navigation.transition.ModifierTransitionHandler
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.transitionhandler.rememberBackstackFader
import com.bumble.appyx.navmodel.backstack.transitionhandler.rememberBackstackSlider

private val isReduceMotionEnabled: Boolean
    @Composable
    @ReadOnlyComposable
    get() = Settings.Global.getFloat(
        LocalContext.current.contentResolver,
        Settings.Global.ANIMATOR_DURATION_SCALE,
        1.0f
    ) == 0f

/**
 * M3 Expressive shared-axis horizontal transition for screen navigation.
 * Uses MotionScheme spatial defaults for spring physics.
 * Respects the system "Remove animations" accessibility setting.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun <NavTarget> rememberDefaultTransitionHandler(): ModifierTransitionHandler<NavTarget, BackStack.State> {
    if (isReduceMotionEnabled) {
        return rememberBackstackFader(transitionSpec = { snap() })
    }
    val effectsSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Offset>()
    return rememberBackstackSlider(
        transitionSpec = { effectsSpec },
    )
}

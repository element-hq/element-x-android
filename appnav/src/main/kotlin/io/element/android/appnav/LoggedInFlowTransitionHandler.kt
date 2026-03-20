/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.appnav

import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.snap
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import com.bumble.appyx.core.navigation.transition.ModifierTransitionHandler
import com.bumble.appyx.core.navigation.transition.TransitionDescriptor
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.transitionhandler.rememberBackstackFader
import com.bumble.appyx.navmodel.backstack.transitionhandler.rememberBackstackSlider
import io.element.android.libraries.designsystem.animation.isReduceMotionEnabled

/**
 * A TransitionHandler that uses fade transition when Placeholder is being removed,
 * and slide transition for all other cases.
 */
class LoggedInFlowTransitionHandler(
    private val backstack: BackStack<LoggedInFlowNode.NavTarget>,
    private val slider: ModifierTransitionHandler<LoggedInFlowNode.NavTarget, BackStack.State>,
    private val fader: ModifierTransitionHandler<LoggedInFlowNode.NavTarget, BackStack.State>,
) : ModifierTransitionHandler<LoggedInFlowNode.NavTarget, BackStack.State>() {
    override fun createModifier(
        modifier: Modifier,
        transition: Transition<BackStack.State>,
        descriptor: TransitionDescriptor<LoggedInFlowNode.NavTarget, BackStack.State>
    ): Modifier {
        val isPlaceholderBeingRemoved = backstack.elements.value.any { element ->
            element.key.navTarget == LoggedInFlowNode.NavTarget.Placeholder &&
                element.targetState != BackStack.State.ACTIVE
        }
        val handler = if (isPlaceholderBeingRemoved) fader else slider
        return handler.createModifier(modifier, transition, descriptor)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun rememberLoggedInFlowTransitionHandler(
    backstack: BackStack<LoggedInFlowNode.NavTarget>,
): ModifierTransitionHandler<LoggedInFlowNode.NavTarget, BackStack.State> {
    val reduceMotion = isReduceMotionEnabled
    val slider = if (reduceMotion) {
        rememberBackstackSlider<LoggedInFlowNode.NavTarget>(transitionSpec = { snap() })
    } else {
        val effectsSliderSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Offset>()
        rememberBackstackSlider<LoggedInFlowNode.NavTarget>(transitionSpec = { effectsSliderSpec })
    }
    val fader = if (reduceMotion) {
        rememberBackstackFader<LoggedInFlowNode.NavTarget>(transitionSpec = { snap() })
    } else {
        val effectsSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()
        rememberBackstackFader<LoggedInFlowNode.NavTarget>(transitionSpec = { effectsSpec })
    }
    return remember(backstack, slider, fader) {
        LoggedInFlowTransitionHandler(backstack, slider, fader)
    }
}

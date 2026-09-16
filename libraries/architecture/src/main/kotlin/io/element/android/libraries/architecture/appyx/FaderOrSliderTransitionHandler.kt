/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.architecture.appyx

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.navigation.transition.ModifierTransitionHandler
import com.bumble.appyx.core.navigation.transition.TransitionDescriptor
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.NewRoot
import com.bumble.appyx.navmodel.backstack.operation.Replace
import com.bumble.appyx.navmodel.backstack.transitionhandler.rememberBackstackFader
import com.bumble.appyx.navmodel.backstack.transitionhandler.rememberBackstackSlider

/**
 * A TransitionHandler that uses fade transition when the operation is Replace or NewRoot,
 * and slide transition for all other cases.
 */
private class FaderOrSliderTransitionHandler<NavTarget>(
    private val slider: ModifierTransitionHandler<NavTarget, BackStack.State>,
    private val fader: ModifierTransitionHandler<NavTarget, BackStack.State>,
) : ModifierTransitionHandler<NavTarget, BackStack.State>() {
    override fun createModifier(
        modifier: Modifier,
        transition: Transition<BackStack.State>,
        descriptor: TransitionDescriptor<NavTarget, BackStack.State>
    ): Modifier {
        val operation = descriptor.operation
        val useFader = operation is Replace || operation is NewRoot
        val handler = if (useFader) fader else slider
        return handler.createModifier(modifier, transition, descriptor)
    }
}

@Composable
fun <NavTarget> rememberFaderOrSliderTransitionHandler(): ModifierTransitionHandler<NavTarget, BackStack.State> {
    val slider = rememberBackstackSlider<NavTarget>(
        transitionSpec = { spring(stiffness = Spring.StiffnessMediumLow) },
    )
    val fader = rememberBackstackFader<NavTarget>(
        transitionSpec = { spring(stiffness = Spring.StiffnessMediumLow) },
    )
    return rememberDelegateTransitionHandler {
        FaderOrSliderTransitionHandler(slider, fader)
    }
}

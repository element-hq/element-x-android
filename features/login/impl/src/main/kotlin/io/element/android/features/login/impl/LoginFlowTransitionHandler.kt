/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.navigation.transition.ModifierTransitionHandler
import com.bumble.appyx.core.navigation.transition.TransitionDescriptor
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.Replace
import com.bumble.appyx.navmodel.backstack.transitionhandler.rememberBackstackFader
import com.bumble.appyx.navmodel.backstack.transitionhandler.rememberBackstackSlider
import io.element.android.libraries.architecture.appyx.rememberDelegateTransitionHandler

/**
 * A TransitionHandler that uses fade transition when OnBoarding is replacing the current screen,
 * and slide transition for all other cases.
 */
private class LoginFlowTransitionHandler(
    private val slider: ModifierTransitionHandler<LoginFlowNode.NavTarget, BackStack.State>,
    private val fader: ModifierTransitionHandler<LoginFlowNode.NavTarget, BackStack.State>,
) : ModifierTransitionHandler<LoginFlowNode.NavTarget, BackStack.State>() {
    override fun createModifier(
        modifier: Modifier,
        transition: Transition<BackStack.State>,
        descriptor: TransitionDescriptor<LoginFlowNode.NavTarget, BackStack.State>
    ): Modifier {
        val useFader = descriptor.element is LoginFlowNode.NavTarget.OnBoarding &&
            descriptor.operation is Replace
        val handler = if (useFader) fader else slider
        return handler.createModifier(modifier, transition, descriptor)
    }
}

@Composable
fun rememberLoginFlowTransitionHandler(): ModifierTransitionHandler<LoginFlowNode.NavTarget, BackStack.State> {
    val slider = rememberBackstackSlider<LoginFlowNode.NavTarget>(
        transitionSpec = { spring(stiffness = Spring.StiffnessMediumLow) },
    )
    val fader = rememberBackstackFader<LoginFlowNode.NavTarget>(
        transitionSpec = { spring(stiffness = Spring.StiffnessMediumLow) },
    )
    return rememberDelegateTransitionHandler {
        LoginFlowTransitionHandler(slider, fader)
    }
}

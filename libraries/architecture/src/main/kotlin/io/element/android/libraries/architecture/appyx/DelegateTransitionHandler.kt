/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.architecture.appyx

import android.annotation.SuppressLint
import androidx.compose.animation.core.Transition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.navigation.transition.ModifierTransitionHandler
import com.bumble.appyx.core.navigation.transition.TransitionDescriptor

/**
 * A [ModifierTransitionHandler] that delegates the creation of the modifier to another handler
 * based on the [NavTarget]. The idea is to allow different transitions for different [NavTarget]s.
 */
class DelegateTransitionHandler<NavTarget, State>(
    private val handlerProvider: (NavTarget) -> ModifierTransitionHandler<NavTarget, State>,
) : ModifierTransitionHandler<NavTarget, State>() {
    @SuppressLint("ModifierFactoryExtensionFunction")
    override fun createModifier(modifier: Modifier, transition: Transition<State>, descriptor: TransitionDescriptor<NavTarget, State>): Modifier {
        return handlerProvider(descriptor.element).createModifier(modifier, transition, descriptor)
    }
}

@Composable
fun <NavTarget, State> rememberDelegateTransitionHandler(
    handlerProvider: (NavTarget) -> ModifierTransitionHandler<NavTarget, State>,
): ModifierTransitionHandler<NavTarget, State> =
    remember(handlerProvider) { DelegateTransitionHandler(handlerProvider = handlerProvider) }

/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.architecture.overlay

import com.bumble.appyx.core.navigation.BaseNavModel
import com.bumble.appyx.core.navigation.NavElements
import com.bumble.appyx.core.navigation.backpresshandlerstrategies.BackPressHandlerStrategy
import com.bumble.appyx.core.navigation.onscreen.OnScreenStateResolver
import com.bumble.appyx.core.navigation.operationstrategies.ExecuteImmediately
import com.bumble.appyx.core.navigation.operationstrategies.OperationStrategy
import com.bumble.appyx.core.state.SavedStateMap
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.BackStackOnScreenResolver

class Overlay<NavTarget : Any>(
    savedStateMap: SavedStateMap?,
    key: String = requireNotNull(Overlay::class.qualifiedName),
    backPressHandler: BackPressHandlerStrategy<NavTarget, BackStack.State> = HideOverlayBackPressHandler(),
    operationStrategy: OperationStrategy<NavTarget, BackStack.State> = ExecuteImmediately(),
    screenResolver: OnScreenStateResolver<BackStack.State> = BackStackOnScreenResolver,
) : BaseNavModel<NavTarget, BackStack.State>(
    backPressHandler = backPressHandler,
    screenResolver = screenResolver,
    operationStrategy = operationStrategy,
    finalState = BackStack.State.DESTROYED,
    savedStateMap = savedStateMap,
    key = key,
) {
    override val initialElements: NavElements<NavTarget, BackStack.State>
        get() = emptyList()
}

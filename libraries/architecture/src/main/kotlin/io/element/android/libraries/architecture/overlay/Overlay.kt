/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

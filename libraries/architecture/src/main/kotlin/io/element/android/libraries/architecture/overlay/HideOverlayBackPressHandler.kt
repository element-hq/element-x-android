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

import com.bumble.appyx.core.navigation.backpresshandlerstrategies.BaseBackPressHandlerStrategy
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.BackStackElements
import io.element.android.libraries.architecture.overlay.operation.Hide
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HideOverlayBackPressHandler<NavTarget : Any> :
    BaseBackPressHandlerStrategy<NavTarget, BackStack.State>() {
    override val canHandleBackPressFlow: Flow<Boolean> by lazy {
        navModel.elements.map(::areThereElements)
    }

    private fun areThereElements(elements: BackStackElements<NavTarget>) =
        elements.isNotEmpty()

    override fun onBackPressed() {
        navModel.accept(Hide())
    }
}

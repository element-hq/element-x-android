/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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

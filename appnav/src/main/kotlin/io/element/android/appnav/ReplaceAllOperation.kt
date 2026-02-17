/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.appnav

import com.bumble.appyx.core.navigation.NavElements
import com.bumble.appyx.core.navigation.Operation
import com.bumble.appyx.navmodel.backstack.BackStack
import kotlinx.parcelize.Parcelize

/**
 * Replaces all the current elements with the provided [navElements], keeping their [BackStack.State] too.
 */
@Parcelize
class ReplaceAllOperation<NavTarget : Any>(
    private val navElements: NavElements<NavTarget, BackStack.State>
) : Operation<NavTarget, BackStack.State> {
    override fun isApplicable(elements: NavElements<NavTarget, BackStack.State>): Boolean {
        return true
    }

    override fun invoke(existing: NavElements<NavTarget, BackStack.State>): NavElements<NavTarget, BackStack.State> {
        return navElements
    }
}

/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.architecture.overlay.operation

import com.bumble.appyx.core.navigation.NavKey
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.BackStackElement
import com.bumble.appyx.navmodel.backstack.BackStackElements
import com.bumble.appyx.navmodel.backstack.activeElement
import io.element.android.libraries.architecture.overlay.Overlay
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class Show<T : Any>(
    private val element: @RawValue T
) : OverlayOperation<T> {
    override fun isApplicable(elements: BackStackElements<T>): Boolean =
        element != elements.activeElement

    override fun invoke(elements: BackStackElements<T>): BackStackElements<T> = listOf(
        BackStackElement(
            key = NavKey(element),
            fromState = BackStack.State.CREATED,
            targetState = BackStack.State.ACTIVE,
            operation = this
        )
    )
}

fun <T : Any> Overlay<T>.show(element: T) {
    accept(Show(element))
}

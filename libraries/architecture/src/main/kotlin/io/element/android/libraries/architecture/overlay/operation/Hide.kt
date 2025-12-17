/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.architecture.overlay.operation

import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.BackStackElements
import com.bumble.appyx.navmodel.backstack.activeIndex
import io.element.android.libraries.architecture.overlay.Overlay
import kotlinx.parcelize.Parcelize

@Parcelize
class Hide<T : Any> : OverlayOperation<T> {
    override fun isApplicable(elements: BackStackElements<T>): Boolean =
        elements.any { it.targetState == BackStack.State.ACTIVE }

    override fun invoke(
        elements: BackStackElements<T>
    ): BackStackElements<T> {
        val hideIndex = elements.activeIndex
        require(hideIndex != -1) { "Nothing to hide, state=$elements" }
        return elements.mapIndexed { index, element ->
            when (index) {
                hideIndex -> element.transitionTo(
                    newTargetState = BackStack.State.DESTROYED,
                    operation = this
                )
                else -> element
            }
        }
    }

    override fun equals(other: Any?): Boolean = this.javaClass == other?.javaClass

    override fun hashCode(): Int = this.javaClass.hashCode()
}

fun <T : Any> Overlay<T>.hide() {
    accept(Hide())
}

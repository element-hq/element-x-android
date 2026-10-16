/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.appnav

import com.bumble.appyx.core.navigation.NavKey
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.BackStackElement
import com.bumble.appyx.navmodel.backstack.BackStackElements
import com.bumble.appyx.navmodel.backstack.operation.BackStackOperation
import com.bumble.appyx.navmodel.backstack.operation.NewRoot
import com.bumble.appyx.navmodel.backstack.operation.Remove
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

/**
 * Don't process NewRoot if the nav target already exists in the stack.
 */
fun <T : Any> BackStack<T>.safeRoot(element: T) {
    val containsRoot = elements.value.any {
        it.key.navTarget == element
    }
    if (containsRoot) return
    accept(NewRoot(element))
}

/**
 * Remove the last element on the backstack equals to the given one.
 */
fun <T : Any> BackStack<T>.removeLast(element: T) {
    val lastExpectedNavElement = elements.value.lastOrNull {
        it.key.navTarget == element
    } ?: return
    accept(Remove(lastExpectedNavElement.key))
}

/**
 * Replace the root element of the backstack, and keep the elements on top of it, contrary to [safeRoot] which removes all the elements.
 */
fun <T : Any> BackStack<T>.replaceRoot(element: T) {
    accept(ReplaceRoot(element))
}

/**
 * Replace the root element of the backstack, keeping the elements on top of it.
 *
 * Operation:
 *
 * [A, B, C] + ReplaceRoot(D) = [D, B, C]
 * [A] + ReplaceRoot(D) = [D]
 */
@Parcelize
internal data class ReplaceRoot<T : Any>(
    private val element: @RawValue T
) : BackStackOperation<T> {
    override fun isApplicable(elements: BackStackElements<T>): Boolean =
        elements.rootElement()?.key?.navTarget != element

    override fun invoke(elements: BackStackElements<T>): BackStackElements<T> {
        val root = elements.rootElement() ?: return elements
        if (root.targetState == BackStack.State.ACTIVE) {
            // Nothing on top of the root, so this is the same as NewRoot
            return NewRoot(element).invoke(elements)
        }
        // The root is stashed below other elements: drop it, as Remove does for a non active element,
        // and add the new root, stashed, in place of it.
        val newRoot = BackStackElement(
            key = NavKey(element),
            fromState = BackStack.State.STASHED,
            targetState = BackStack.State.STASHED,
            operation = this,
        )
        return elements.map { if (it == root) newRoot else it }
    }

    private fun BackStackElements<T>.rootElement() = firstOrNull { it.targetState != BackStack.State.DESTROYED }
}

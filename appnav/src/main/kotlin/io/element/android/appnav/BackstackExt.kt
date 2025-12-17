/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.appnav

import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.NewRoot
import com.bumble.appyx.navmodel.backstack.operation.Remove

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

/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.architecture.appyx

import com.bumble.appyx.navmodel.backstack.BackStack

fun <T : Any> BackStack<T>.canPop(): Boolean {
    val elements = elements.value
    return elements.any { it.targetState == BackStack.State.ACTIVE } &&
        elements.any { it.targetState == BackStack.State.STASHED }
}

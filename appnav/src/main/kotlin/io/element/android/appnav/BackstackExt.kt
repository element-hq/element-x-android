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

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

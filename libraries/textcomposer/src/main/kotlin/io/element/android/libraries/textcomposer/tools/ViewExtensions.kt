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

package io.element.android.libraries.textcomposer.tools

import android.view.ViewGroup
import androidx.transition.ChangeBounds
import androidx.transition.Fade
import androidx.transition.Transition
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet

fun ViewGroup.animateLayoutChange(animationDuration: Long, transitionComplete: (() -> Unit)? = null) {
    val transition = TransitionSet().apply {
        ordering = TransitionSet.ORDERING_SEQUENTIAL
        addTransition(ChangeBounds())
        addTransition(Fade(Fade.IN))
        duration = animationDuration
        addListener(object : SimpleTransitionListener() {
            override fun onTransitionEnd(transition: Transition) {
                transitionComplete?.invoke()
            }
        })
    }
    TransitionManager.beginDelayedTransition((parent as? ViewGroup ?: this), transition)
}

package io.element.android.x.textcomposer.tools

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

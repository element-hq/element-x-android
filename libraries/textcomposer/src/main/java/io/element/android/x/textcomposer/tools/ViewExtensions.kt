package io.element.android.x.textcomposer.tools

import android.view.ViewGroup
import androidx.transition.*

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

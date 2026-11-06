/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

private const val STIFFNESS = Spring.StiffnessMediumLow
private const val DAMPING_RATIO = Spring.DampingRatioNoBouncy

/**
 * Animates the placement of a timeline item in a `reverseLayout` LazyColumn, including when the item itself is resized.
 *
 * [LazyItemScope.animateItem] animates the offset of the start edge of the items, which is their *bottom* edge in a `reverseLayout` list. When an
 * item is resized (i.e. its read receipts move to a newer message, a group of events is expanded, the typing notification appears...), the items
 * above it smoothly animate to their new position, but its own top edge jumps by the size difference immediately, making it overlap the item above
 * or bounce. [compensateResizeInReverseLayout] negates that jump, see [ReverseLayoutResizeNode].
 */
internal fun Modifier.animateTimelineItem(scope: LazyItemScope): Modifier = with(scope) {
    this@animateTimelineItem
        .animateItem(placementSpec = spring(dampingRatio = DAMPING_RATIO, stiffness = STIFFNESS, visibilityThreshold = IntOffset.VisibilityThreshold))
        .compensateResizeInReverseLayout(spring(dampingRatio = DAMPING_RATIO, stiffness = STIFFNESS, visibilityThreshold = 1f))
}

/**
 * When the height of the item changes, offsets its content by the height difference so its top edge stays where it was, then animates that
 * offset back to 0 with [animationSpec]. Using the same spec as the placement animation of the items, the top edge of this item follows the
 * bottom edge of the item above it.
 */
internal fun Modifier.compensateResizeInReverseLayout(animationSpec: FiniteAnimationSpec<Float>): Modifier =
    this then ReverseLayoutResizeElement(animationSpec)

private data class ReverseLayoutResizeElement(
    val animationSpec: FiniteAnimationSpec<Float>,
) : ModifierNodeElement<ReverseLayoutResizeNode>() {
    override fun create() = ReverseLayoutResizeNode(animationSpec)

    override fun update(node: ReverseLayoutResizeNode) {
        node.animationSpec = animationSpec
    }
}

/**
 * The animation mirrors what `LazyLayoutItemAnimation.animatePlacementDelta` does for the placement of the items, so when several resizes and
 * placement changes happen in a row, this item and its neighbours keep moving together.
 */
private class ReverseLayoutResizeNode(
    var animationSpec: FiniteAnimationSpec<Float>,
) : Modifier.Node(), LayoutModifierNode, DrawModifierNode {
    private var lastHeight = UNSET
    private val offsetAnimation = Animatable(0f)

    // The offset currently applied to the content: positive values move it down.
    private var offset by mutableFloatStateOf(0f)

    override fun MeasureScope.measure(measurable: Measurable, constraints: Constraints): MeasureResult {
        val placeable = measurable.measure(constraints)
        val previousHeight = lastHeight
        lastHeight = placeable.height
        if (previousHeight != UNSET && previousHeight != placeable.height) {
            // The top edge moved by the height difference: move the content by the same amount in the opposite direction
            animateOffset(delta = (placeable.height - previousHeight).toFloat())
        }
        return layout(placeable.width, placeable.height) {
            // Only read the offset in the layer block, so the animation doesn't trigger new measure or placement passes
            placeable.placeWithLayer(0, 0) { translationY = offset }
        }
    }

    override fun ContentDrawScope.draw() {
        if (offset > 0f) {
            // The content is moved down, don't let it overlap the item below this one
            clipRect(left = -size.width, top = -size.height, right = 2 * size.width, bottom = size.height) {
                this@draw.drawContent()
            }
        } else {
            drawContent()
        }
    }

    private fun animateOffset(delta: Float) {
        val totalOffset = offset + delta
        // Apply it in the current frame, the animation will only start in the next one
        offset = totalOffset
        coroutineScope.launch {
            try {
                if (!offsetAnimation.isRunning) {
                    offsetAnimation.snapTo(totalOffset)
                }
                // If an animation is running, keep its current value and velocity, so the item keeps moving together with its neighbours
                val target = offsetAnimation.value - totalOffset
                offsetAnimation.animateTo(target, animationSpec) {
                    offset = value - target
                }
            } catch (_: CancellationException) {
                // A new animation was started, it takes over
            }
        }
    }

    override fun onReset() {
        // The node is reused for another item
        lastHeight = UNSET
        offset = 0f
    }

    override fun onDetach() {
        lastHeight = UNSET
        offset = 0f
    }

    private companion object {
        const val UNSET = -1
    }
}

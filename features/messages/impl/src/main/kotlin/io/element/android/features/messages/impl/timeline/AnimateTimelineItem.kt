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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private const val STIFFNESS = Spring.StiffnessMediumLow
private const val DAMPING_RATIO = Spring.DampingRatioNoBouncy
private val placementSpec = spring(dampingRatio = DAMPING_RATIO, stiffness = STIFFNESS, visibilityThreshold = IntOffset.VisibilityThreshold)

// Same spring as [placementSpec], so the offsets applied here move together with the placement animations of the items
private val offsetSpec = spring(dampingRatio = DAMPING_RATIO, stiffness = STIFFNESS, visibilityThreshold = 1f)

/**
 * Animates the placement of a timeline item in a `reverseLayout` LazyColumn, including when the item itself is resized or inserted at the bottom.
 *
 * [LazyItemScope.animateItem] animates the offset of the start edge of the items, which is their *bottom* edge in a `reverseLayout` list:
 * - When an item is resized (i.e. its read receipts move to a newer message, a group of events is expanded, the typing notification appears...),
 * the items above it smoothly animate to their new position, but its own top edge jumps by the size difference immediately, making it overlap the
 * item above or bounce. [ReverseLayoutResizeNode] negates that jump.
 * - When new items are inserted at the bottom, the items above them slide up, but the new items are directly displayed at their final position, so
 * the items sliding up overlap them. Instead of fading them in, [insertion] makes them slide in from the bottom together with the items above.
 *
 * @param scope the scope of the item in the LazyColumn.
 * @param animationScope the scope to run the animations in. It must be a composition scope, like the one `LazyColumn` uses for the placement animations
 * (i.e. from [rememberCoroutineScope]), so they start in the same frame: otherwise they get out of sync when frames are dropped.
 * @param insertion the insertion this item is part of, if it has just been inserted at the bottom of the timeline. See [rememberTimelineItemsInsertion].
 */
internal fun Modifier.animateTimelineItem(
    scope: LazyItemScope,
    animationScope: CoroutineScope,
    insertion: TimelineItemsInsertion? = null,
): Modifier = with(scope) {
    this@animateTimelineItem
        .animateItem(
            fadeInSpec = if (insertion != null) null else spring(stiffness = STIFFNESS),
            placementSpec = placementSpec,
        )
        .then(ReverseLayoutResizeElement(offsetSpec, animationScope, insertion))
}

/**
 * Keeps track of the timeline items inserted at the bottom of the timeline (i.e. new messages) in a single update, so they can slide in together.
 */
@Stable
internal class TimelineItemsInsertion(
    val keys: Set<Any>,
    private val coroutineScope: CoroutineScope,
) {
    private var totalHeight = 0f
    private var isAnimationScheduled = false
    private var isAnimationStarted = false

    /** The offset to apply to the inserted items: positive values move them down. */
    var offset by mutableFloatStateOf(0f)
        private set

    /**
     * Called when an inserted item is first measured. All the inserted items which are visible are measured in the same frame, and they start
     * below their final position by their total height: that's also how much the items above them have to move.
     */
    fun onItemMeasured(height: Int) {
        // Items measured later, i.e. when scrolled into view, are displayed directly at their final position
        if (isAnimationStarted) return
        totalHeight += height
        offset = totalHeight
        if (!isAnimationScheduled) {
            isAnimationScheduled = true
            // The animation starts in the next frame, once all the inserted items have been measured
            coroutineScope.launch {
                isAnimationStarted = true
                try {
                    // Same steps as the placement animations, so the animation starts in the same frame
                    val animation = Animatable(0f)
                    animation.snapTo(totalHeight)
                    animation.animateTo(0f, offsetSpec) { offset = value }
                } finally {
                    offset = 0f
                }
            }
        }
    }
}

/**
 * Returns a [TimelineItemsInsertion] when the last update of [timelineItems] inserted new items at the bottom of the timeline, below the previously
 * newest event. Returns null for the initial items, paginated items, or when the timeline is replaced.
 */
@Composable
internal fun rememberTimelineItemsInsertion(
    timelineItems: ImmutableList<TimelineItem>,
    animationScope: CoroutineScope,
): TimelineItemsInsertion? {
    val previousItems = remember { arrayOfNulls<List<TimelineItem>>(1) }
    return remember(timelineItems) {
        val keys = keysInsertedAtBottom(previous = previousItems[0], current = timelineItems)
        previousItems[0] = timelineItems
        keys.takeIf { it.isNotEmpty() }?.let { TimelineItemsInsertion(it, animationScope) }
    }
}

internal fun keysInsertedAtBottom(previous: List<TimelineItem>?, current: List<TimelineItem>): Set<Any> {
    if (previous == null) return emptySet()
    val previousNewestEventIndex = previous.indexOfFirst { it !is TimelineItem.Virtual }
    if (previousNewestEventIndex == -1) return emptySet()
    val previousNewestEventKey = previous[previousNewestEventIndex].identifier()
    val newestEventIndex = current.indexOfFirst { it.identifier() == previousNewestEventKey }
    if (newestEventIndex <= 0) return emptySet()
    // Items which were already below the newest event, i.e. the typing notification, are not new
    val previousKeysBelow = previous.subList(0, previousNewestEventIndex).mapTo(HashSet()) { it.identifier() }
    return current.subList(0, newestEventIndex)
        .map { it.identifier() }
        .filterNotTo(HashSet()) { it in previousKeysBelow }
}

private data class ReverseLayoutResizeElement(
    val animationSpec: FiniteAnimationSpec<Float>,
    val animationScope: CoroutineScope,
    val insertion: TimelineItemsInsertion?,
) : ModifierNodeElement<ReverseLayoutResizeNode>() {
    override fun create() = ReverseLayoutResizeNode(animationSpec, animationScope, insertion)

    override fun update(node: ReverseLayoutResizeNode) {
        node.animationSpec = animationSpec
        node.animationScope = animationScope
        node.updateInsertion(insertion)
    }
}

/**
 * Only clips the content below [bottom], leaving room for the content drawn outside of the other edges.
 */
private data class BottomEdgeClipShape(private val bottom: Float) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        return Outline.Rectangle(Rect(left = -size.width, top = -size.height, right = 2 * size.width, bottom = bottom))
    }
}

/**
 * When the height of the item changes, offsets its content by the height difference so its top edge stays where it was, then animates that
 * offset back to 0 with [animationSpec]. Using the same spec as the placement animation of the items, the top edge of this item follows the
 * bottom edge of the item above it. While the content is moved down, it's clipped so it doesn't overlap the item below.
 *
 * The animation mirrors what `LazyLayoutItemAnimation.animatePlacementDelta` does for the placement of the items, so when several resizes and
 * placement changes happen in a row, this item and its neighbours keep moving together.
 */
private class ReverseLayoutResizeNode(
    var animationSpec: FiniteAnimationSpec<Float>,
    var animationScope: CoroutineScope,
    private var insertion: TimelineItemsInsertion?,
) : Modifier.Node(), LayoutModifierNode {
    private var lastHeight = UNSET
    private var animationJob: Job? = null

    fun updateInsertion(insertion: TimelineItemsInsertion?) {
        // Keep the insertion the item was measured with: a later timeline update must not stop its slide in animation
        if (lastHeight == UNSET) {
            this.insertion = insertion
        }
    }
    private val offsetAnimation = Animatable(0f)

    // The offset applied to the content to compensate a resize: positive values move it down.
    private var resizeOffset by mutableFloatStateOf(0f)

    override fun MeasureScope.measure(measurable: Measurable, constraints: Constraints): MeasureResult {
        val placeable = measurable.measure(constraints)
        val previousHeight = lastHeight
        lastHeight = placeable.height
        if (previousHeight == UNSET) {
            insertion?.onItemMeasured(placeable.height)
        } else if (previousHeight != placeable.height) {
            // The top edge moved by the height difference: move the content by the same amount in the opposite direction
            animateResizeOffset(delta = (placeable.height - previousHeight).toFloat())
        }
        return layout(placeable.width, placeable.height) {
            // Only read the offsets in the layer block, so the animations don't trigger new measure or placement passes
            placeable.placeWithLayer(0, 0) {
                val resizeOffset = resizeOffset
                translationY = resizeOffset + (insertion?.offset ?: 0f)
                // When the item grew, its content is moved down: don't let it overlap the item below.
                // The clip is in the coordinates of the layer, which is translated with the content.
                clip = resizeOffset > 0f
                if (clip) {
                    shape = BottomEdgeClipShape(bottom = placeable.height - resizeOffset)
                }
            }
        }
    }

    private fun animateResizeOffset(delta: Float) {
        val totalOffset = resizeOffset + delta
        // Apply it in the current frame, the animation will only start in the next one
        resizeOffset = totalOffset
        // Not the node's coroutine scope: see `animationScope` in [animateTimelineItem]
        animationJob = animationScope.launch {
            try {
                if (!offsetAnimation.isRunning) {
                    offsetAnimation.snapTo(totalOffset)
                }
                // If an animation is running, keep its current value and velocity, so the item keeps moving together with its neighbours
                val target = offsetAnimation.value - totalOffset
                offsetAnimation.animateTo(target, animationSpec) {
                    resizeOffset = value - target
                }
            } catch (_: CancellationException) {
                // A new animation was started, it takes over
            }
        }
    }

    override fun onReset() {
        // The node is reused for another item
        stopAnimation()
        insertion = null
    }

    override fun onDetach() {
        stopAnimation()
    }

    private fun stopAnimation() {
        // The animation scope outlives this node
        animationJob?.cancel()
        animationJob = null
        lastHeight = UNSET
        resizeOffset = 0f
    }

    private companion object {
        const val UNSET = -1
    }
}

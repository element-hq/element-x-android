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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.layout
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
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
 * - When an item is resized, the items above it which come into view while they move, i.e. when a group of events is collapsed, are displayed at
 * their final position by the LazyColumn, while the items next to them are still moving. [resizes] makes them move with the other items.
 *
 * @param scope the scope of the item in the LazyColumn.
 * @param animationScope the scope to run the animations in. It must be a composition scope, like the one `LazyColumn` uses for the placement animations
 * (i.e. from [rememberCoroutineScope]), so they start in the same frame: otherwise they get out of sync when frames are dropped.
 * @param resizes the resizes of the items of the LazyColumn, see [rememberTimelineItemResizes].
 * @param key the key of the item.
 * @param index the index of the item.
 * @param insertion the insertion this item is part of, if it has just been inserted at the bottom of the timeline. See [rememberTimelineItemsInsertion].
 */
internal fun Modifier.animateTimelineItem(
    scope: LazyItemScope,
    animationScope: CoroutineScope,
    resizes: TimelineItemResizes,
    key: Any,
    index: Int,
    insertion: TimelineItemsInsertion? = null,
): Modifier = with(scope) {
    this@animateTimelineItem
        .animateItem(
            fadeInSpec = if (insertion != null) null else spring(stiffness = STIFFNESS),
            placementSpec = placementSpec,
        )
        .then(ReverseLayoutResizeElement(offsetSpec, animationScope, resizes, key, index, insertion))
}

/**
 * The resizes of the items of a `reverseLayout` LazyColumn which are being animated by [animateTimelineItem].
 *
 * When an item is resized, the LazyColumn animates the placement of the visible items above it from their previous position: they are moved
 * by the same offset as the content of the resized item. The other items above it, which may come into view while the visible ones move (i.e.
 * when a group of events is collapsed), are not animated by the LazyColumn, so they have to apply that offset themselves.
 */
@Stable
internal class TimelineItemResizes(private val lazyListState: LazyListState) {
    private val running = mutableStateListOf<ItemResize>()
    private var pendingGrowths = 0

    /**
     * How much the viewport of the LazyColumn is extended above its top edge, see [extendViewportAbove].
     *
     * When an item grows, the items above it move up, and the LazyColumn stops displaying the ones which end up out of its viewport right away,
     * even though their placement animation starts from their previous, visible, position. While the items grow, the viewport is extended
     * enough to keep them, so they keep moving until they leave the screen.
     */
    var extraHeight by mutableIntStateOf(0)
        private set

    /** Called when an item is about to grow by [delta], in the next frame, once the viewport is extended. */
    internal fun onGrowthPending(delta: Int) {
        pendingGrowths++
        val runningGrowths = Snapshot.withoutReadObservation { running.sumOf { it.offset.coerceAtLeast(0f).toDouble() } }
        extraHeight = maxOf(extraHeight, runningGrowths.toInt() + delta)
    }

    internal fun onGrowthApplied() {
        pendingGrowths--
    }

    internal fun onResizeStarted(resize: ItemResize) {
        if (resize !in running) {
            // Called while the LazyColumn is measured: its layout info is still the one of the previous frame, and the LazyColumn starts the
            // placement animations of the items which were visible in it
            resize.animatedKeys = Snapshot.withoutReadObservation {
                lazyListState.layoutInfo.visibleItemsInfo.mapTo(HashSet()) { it.key }
            }
            running.add(resize)
        }
    }

    internal fun onResizeFinished(resize: ItemResize) {
        running.remove(resize)
        if (running.isEmpty() && pendingGrowths == 0) {
            extraHeight = 0
        }
    }

    /**
     * The offset to apply to the item with the given [key] and [index] for the resizes of the items below it, when the LazyColumn doesn't
     * animate its placement for them.
     */
    internal fun offsetOf(key: Any, index: Int): Float {
        var offset = 0f
        running.forEach { resize ->
            if (resize.index < index && key !in resize.animatedKeys) {
                offset += resize.offset
            }
        }
        return offset
    }
}

/**
 * Returns the [TimelineItemResizes] of the items of the LazyColumn with the given [lazyListState].
 */
@Composable
internal fun rememberTimelineItemResizes(lazyListState: LazyListState): TimelineItemResizes {
    return remember(lazyListState) { TimelineItemResizes(lazyListState) }
}

/**
 * Extends the viewport of the LazyColumn above its top edge by [TimelineItemResizes.extraHeight], without changing its size or what's displayed.
 * Must be used with [extendedAbove] for the content padding.
 */
internal fun Modifier.extendViewportAbove(resizes: TimelineItemResizes): Modifier = this
    .clipToBounds()
    .layout { measurable, constraints ->
        val extraHeight = resizes.extraHeight
        if (extraHeight == 0 || !constraints.hasBoundedHeight) {
            val placeable = measurable.measure(constraints)
            layout(placeable.width, placeable.height) { placeable.place(0, 0) }
        } else {
            val placeable = measurable.measure(
                constraints.copy(minHeight = constraints.minHeight + extraHeight, maxHeight = constraints.maxHeight + extraHeight)
            )
            layout(placeable.width, placeable.height - extraHeight) { placeable.place(0, -extraHeight) }
        }
    }

/**
 * Adds [TimelineItemResizes.extraHeight] to the top padding, so the content is laid out where it would be without [extendViewportAbove].
 */
@Composable
internal fun PaddingValues.extendedAbove(resizes: TimelineItemResizes): PaddingValues {
    val density = LocalDensity.current
    return remember(this, resizes, density) { ExtendedTopPaddingValues(this, resizes, density) }
}

@Stable
private class ExtendedTopPaddingValues(
    private val paddingValues: PaddingValues,
    private val resizes: TimelineItemResizes,
    private val density: Density,
) : PaddingValues by paddingValues {
    // Read while the LazyColumn is measured, so changes only trigger a new measure
    override fun calculateTopPadding(): Dp = paddingValues.calculateTopPadding() + with(density) { resizes.extraHeight.toDp() }
}

/**
 * The animation compensating the resizes of an item, see [ReverseLayoutResizeNode]. It keeps running when the item goes out of view, so the
 * items which follow it keep moving.
 *
 * The animation mirrors what `LazyLayoutItemAnimation.animatePlacementDelta` does for the placement of the items, so when several resizes and
 * placement changes happen in a row, this item and its neighbours keep moving together.
 *
 * @param index the index of the item.
 */
internal class ItemResize(var index: Int) {
    private val animation = Animatable(0f)

    // The keys of the items whose placement the LazyColumn animates for this resize
    var animatedKeys: Set<Any> = emptySet()

    /** The offset applied to the content of the item to compensate its resizes: positive values move it down. */
    var offset by mutableFloatStateOf(0f)
        private set

    fun animate(delta: Float, animationSpec: FiniteAnimationSpec<Float>, animationScope: CoroutineScope, onFinished: (ItemResize) -> Unit) {
        val totalOffset = offset + delta
        // Apply it in the current frame, the animation will only start in the next one
        offset = totalOffset
        animationScope.launch {
            try {
                if (!animation.isRunning) {
                    animation.snapTo(totalOffset)
                }
                // If an animation is running, keep its current value and velocity, so the item keeps moving together with its neighbours
                val target = animation.value - totalOffset
                animation.animateTo(target, animationSpec) {
                    offset = value - target
                }
                onFinished(this@ItemResize)
            } catch (_: CancellationException) {
                // A new animation was started, it takes over
            }
        }
    }
}

/**
 * Keeps track of the timeline items inserted at the bottom of the timeline (i.e. new messages) in a single update, so they can slide in together.
 *
 * @param keys the keys of the inserted items.
 * @param coroutineScope the scope to run the animation in, see `animationScope` in [animateTimelineItem].
 * @param previous the previous insertion, if its items are still sliding in. The new items are below them, so they also need to follow that slide.
 */
@Stable
internal class TimelineItemsInsertion(
    val keys: Set<Any>,
    private val coroutineScope: CoroutineScope,
    private val previous: TimelineItemsInsertion? = null,
) {
    private var totalHeight = 0f
    private var isAnimationScheduled = false
    private var isAnimationStarted = false

    // Whether the frame in which the items are inserted is over
    private var isInsertionFrameOver = false

    // The offset of this insertion only
    private var ownOffset by mutableFloatStateOf(0f)

    /**
     * The offset to apply to the inserted items: positive values move them down.
     *
     * The items of the previous insertion still apply their own offset, and the placement animation of the items above the new ones uses the same
     * spring as [ownOffset], so adding them up keeps all the items moving together.
     */
    val offset: Float
        get() = ownOffset + (previous?.offset ?: 0f)

    /** Whether the inserted items are still sliding in. */
    val isSlidingIn: Boolean by derivedStateOf { offset > 0f }

    /**
     * Called when an inserted item is first measured. All the inserted items which are visible are measured in the same frame, and they start
     * below their final position by their total height: that's also how much the items above them have to move.
     */
    fun onItemMeasured(height: Int) {
        // Items measured later, i.e. when scrolled into view, are displayed directly at their final position
        if (isInsertionFrameOver || isAnimationStarted) return
        totalHeight += height
        ownOffset = totalHeight
        if (!isAnimationScheduled) {
            isAnimationScheduled = true
            // The animation starts in the next frame, once all the inserted items have been measured
            coroutineScope.launch {
                isAnimationStarted = true
                try {
                    // Same steps as the placement animations, so the animation starts in the same frame
                    val animation = Animatable(0f)
                    animation.snapTo(totalHeight)
                    animation.animateTo(0f, offsetSpec) { ownOffset = value }
                } finally {
                    ownOffset = 0f
                }
            }
        }
    }

    /**
     * Waits for the end of the frame in which the items are inserted, after which the inserted items which are measured, i.e. when scrolled into
     * view, are displayed directly at their final position. Must be called once the composition in which the items are inserted is applied.
     */
    suspend fun awaitInsertionFrameOver() {
        try {
            withFrameNanos {}
        } finally {
            isInsertionFrameOver = true
        }
    }
}

/**
 * Creates the [TimelineItemsInsertion]s, chaining each one to the previous one while its items are still sliding in.
 */
@Stable
internal class TimelineItemsInsertions(private val coroutineScope: CoroutineScope) {
    private var last: TimelineItemsInsertion? = null

    /**
     * Creates the insertion of the items with the given [keys], if any. It has no side effects, so it can be called during composition:
     * [onInsertionApplied] must be called once the composition is applied.
     */
    fun create(keys: Set<Any>): TimelineItemsInsertion? {
        if (keys.isEmpty()) return null
        // Don't observe the offset: this is called during composition, it must not recompose on each frame of the animation
        val previous = last?.takeIf { Snapshot.withoutReadObservation { it.offset } != 0f }
        return TimelineItemsInsertion(keys, coroutineScope, previous)
    }

    fun onInsertionApplied(insertion: TimelineItemsInsertion) {
        last = insertion
    }
}

/**
 * Returns a [TimelineItemsInsertion] when the last update of [timelineItems] inserted new items at the bottom of the timeline, below the previously
 * newest event. Returns null for the initial items, paginated items, or when the timeline is replaced.
 */
@Composable
internal fun rememberTimelineItemsInsertion(
    timelineItems: ImmutableList<TimelineItem>,
    insertions: TimelineItemsInsertions,
): TimelineItemsInsertion? = rememberItemsInsertion(timelineItems, insertions, ::keysInsertedAtBottom)

/**
 * Returns a [TimelineItemsInsertion] when the last update of [items] inserted items, as computed by [insertedKeys] from the previous and the
 * current items.
 */
@Composable
internal fun <T : Any> rememberItemsInsertion(
    items: T,
    insertions: TimelineItemsInsertions,
    insertedKeys: (previous: T?, current: T) -> Set<Any>,
): TimelineItemsInsertion? {
    // The items of the last applied composition: the composition can be discarded, so it's only updated once applied
    val previousItems = remember { ItemsHolder<T>() }
    val insertion = remember(items) {
        insertions.create(insertedKeys(previousItems.items, items))
    }
    SideEffect {
        previousItems.items = items
    }
    if (insertion != null) {
        DisposableEffect(insertion) {
            insertions.onInsertionApplied(insertion)
            onDispose {}
        }
        LaunchedEffect(insertion) {
            insertion.awaitInsertionFrameOver()
        }
    }
    return insertion
}

private class ItemsHolder<T : Any> {
    var items: T? = null
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
    val resizes: TimelineItemResizes,
    val key: Any,
    val index: Int,
    val insertion: TimelineItemsInsertion?,
) : ModifierNodeElement<ReverseLayoutResizeNode>() {
    override fun create() = ReverseLayoutResizeNode(animationSpec, animationScope, resizes, key, index, insertion)

    override fun update(node: ReverseLayoutResizeNode) {
        node.animationSpec = animationSpec
        node.animationScope = animationScope
        node.resizes = resizes
        node.key = key
        node.updateIndex(index)
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
 * It also applies the offsets of the resizes of the items below it, when the LazyColumn doesn't animate its placement for them.
 */
private class ReverseLayoutResizeNode(
    var animationSpec: FiniteAnimationSpec<Float>,
    var animationScope: CoroutineScope,
    var resizes: TimelineItemResizes,
    var key: Any,
    private var index: Int,
    private var insertion: TimelineItemsInsertion?,
) : Modifier.Node(), LayoutModifierNode {
    private var lastHeight = UNSET

    // The resize of this item, if it's being animated
    private var resize: ItemResize? = null

    // Read while measuring, so the item is measured again once its growth can be applied
    private var growthState by mutableIntStateOf(NO_GROWTH)

    fun updateIndex(index: Int) {
        this.index = index
        resize?.index = index
    }

    fun updateInsertion(insertion: TimelineItemsInsertion?) {
        // Keep the insertion the item was measured with: a later timeline update must not stop its slide in animation
        if (lastHeight == UNSET) {
            this.insertion = insertion
        }
    }

    override fun MeasureScope.measure(measurable: Measurable, constraints: Constraints): MeasureResult {
        val placeable = measurable.measure(constraints)
        val previousHeight = lastHeight
        val height = placeable.height
        val isGrowthReady = growthState == GROWTH_READY
        if (previousHeight == UNSET) {
            lastHeight = height
            insertion?.onItemMeasured(height)
        } else if (height > previousHeight && !isGrowthReady) {
            // Keep the previous height in this frame, so the viewport can be extended before the items above it move up
            if (growthState == NO_GROWTH) {
                growthState = GROWTH_PENDING
                resizes.onGrowthPending(height - previousHeight)
                animationScope.launch {
                    withFrameNanos {}
                    if (growthState == GROWTH_PENDING) growthState = GROWTH_READY
                }
            }
        } else {
            if (growthState != NO_GROWTH) {
                growthState = NO_GROWTH
                resizes.onGrowthApplied()
            }
            if (height != previousHeight) {
                lastHeight = height
                // The top edge moved by the height difference: move the content by the same amount in the opposite direction
                val resize = resize ?: ItemResize(index).also { resize = it }
                resizes.onResizeStarted(resize)
                // Not the node's coroutine scope: see `animationScope` in [animateTimelineItem]
                resize.animate(
                    delta = (height - previousHeight).toFloat(),
                    animationSpec = animationSpec,
                    animationScope = animationScope,
                    onFinished = resizes::onResizeFinished,
                )
            }
        }
        val layoutHeight = lastHeight
        return layout(placeable.width, layoutHeight) {
            // While sliding in, the item is drawn below the other items, so the ones below it (i.e. the typing notification, which is opaque)
            // hide it until it reaches its position
            val zIndex = if (insertion?.isSlidingIn == true) -1f else 0f
            // Only read the offsets in the layer block, so the animations don't trigger new measure or placement passes
            placeable.placeWithLayer(0, 0, zIndex) {
                val resizeOffset = resize?.offset ?: 0f
                translationY = resizeOffset + resizes.offsetOf(key, index) + (insertion?.offset ?: 0f)
                // When the item grew, its content is moved down, or doesn't fit yet: don't let it overlap the item below.
                // The clip is in the coordinates of the layer, which is translated with the content.
                clip = resizeOffset > 0f || layoutHeight < placeable.height
                if (clip) {
                    shape = BottomEdgeClipShape(bottom = layoutHeight - resizeOffset)
                }
            }
        }
    }

    override fun onReset() {
        // The node is reused for another item
        reset()
        insertion = null
    }

    override fun onDetach() {
        reset()
    }

    private fun reset() {
        // The animations keep running in the animation scope, for the items following them
        lastHeight = UNSET
        resize = null
        if (growthState != NO_GROWTH) {
            growthState = NO_GROWTH
            resizes.onGrowthApplied()
        }
    }

    private companion object {
        const val UNSET = -1
        const val NO_GROWTH = 0
        const val GROWTH_PENDING = 1
        const val GROWTH_READY = 2
    }
}

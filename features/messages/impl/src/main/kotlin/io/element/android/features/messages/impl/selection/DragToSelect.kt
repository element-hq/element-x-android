/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.selection

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.event.isBulkSelectable
import io.element.android.libraries.matrix.api.core.EventId
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs

/** Height of the top/bottom band where holding the finger triggers auto-scroll. */
private val AUTO_SCROLL_EDGE_ZONE = 72.dp

/** Maximum per-frame scroll distance while the finger sits at the very edge. */
private val AUTO_SCROLL_MAX_STEP = 14.dp

/**
 * Each row reports its own on-screen bounds here; the drag gesture hit-tests the finger against
 * them instead of doing layout-offset math, so reverseLayout / contentPadding / variable heights
 * cannot drift. Bounds are in window space.
 */
class DragSelectRegistry {
    private val bounds = mutableStateMapOf<EventId, Rect>()

    fun put(id: EventId, rect: Rect) {
        bounds[id] = rect
    }

    fun remove(id: EventId) {
        bounds.remove(id)
    }

    /**
     * Event whose row contains [windowPoint], or null. Hit-tests on the Y axis only; falls back to
     * the nearest-centre row when the point is in a gap or content padding so the range keeps
     * growing, which also disambiguates momentary overlaps regardless of map iteration order.
     */
    fun eventAt(windowPoint: Offset): EventId? {
        if (bounds.isEmpty()) return null
        val y = windowPoint.y
        val containing = bounds.entries.filter { y >= it.value.top && y < it.value.bottom }
        val pool = if (containing.isNotEmpty()) containing else bounds.entries
        return pool.minByOrNull { abs((it.value.top + it.value.bottom) / 2f - y) }?.key
    }
}

/**
 * One-shot mutable cell carrying the long-press anchor from the per-row long-click handler to the
 * drag gesture. A plain holder rather than a MutableState so it passes through @Composable params
 * without tripping MutableParams and the write is visible to the gesture without a recomposition.
 */
class DragSelectAnchor {
    var eventId: EventId? = null
}

/**
 * Long-press-then-drag range selection. Anchor = the event the long-press fired on (pushed into
 * [anchor] by the per-row handler; cleared on every touch-down so a press in the gutter is
 * ignored). The moving finger is resolved through the [registry], not layout-offset math. Holding
 * near an edge auto-scrolls while the range grows.
 */
@Composable
fun Modifier.dragToSelectMessages(
    lazyListState: LazyListState,
    items: ImmutableList<TimelineItem>,
    currentSelection: ImmutableSet<EventId>?,
    enabled: Boolean,
    maxSelection: Int,
    reverseLayout: Boolean,
    anchor: DragSelectAnchor,
    registry: DragSelectRegistry?,
    onSelectionChange: (ImmutableSet<EventId>) -> Unit,
): Modifier {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val view = LocalView.current
    val latestItems by rememberUpdatedState(items)
    val latestBase by rememberUpdatedState(currentSelection ?: persistentSetOf())
    val latestOnChange by rememberUpdatedState(onSelectionChange)
    var listCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val edgePx = with(density) { AUTO_SCROLL_EDGE_ZONE.toPx() }
    val stepPx = with(density) { AUTO_SCROLL_MAX_STEP.toPx() }

    return this
        .onGloballyPositioned { listCoords = it }
        // Clear any stale anchor at the start of every touch, before the long-press timeout.
        .then(
            if (enabled) {
                Modifier.pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false)
                        anchor.eventId = null
                    }
                }
            } else {
                Modifier
            }
        )
        .pointerInput(enabled, registry) {
            if (!enabled || registry == null) return@pointerInput
            var anchorIndex = -1
            var targetIndex = -1
            var baseSelection: ImmutableSet<EventId> = persistentSetOf()
            var pointerLocal = Offset.Zero
            var autoScroll: Job? = null

            fun indexOfEvent(id: EventId?): Int =
                if (id == null) -1 else latestItems.indexOfFirst { (it as? TimelineItem.Event)?.eventId == id }

            // Window-space hit-test against per-row bounds.
            fun targetIndexAt(local: Offset): Int? {
                val coords = listCoords ?: return null
                val id = registry.eventAt(coords.localToWindow(local)) ?: return null
                return indexOfEvent(id).takeIf { it >= 0 }
            }

            fun emitRange() {
                if (anchorIndex < 0 || targetIndex < 0) return
                // Walk the swept run outward from the anchor so a take() cap drops the far end of
                // the drag, never the anchor.
                val sweep = if (targetIndex >= anchorIndex) anchorIndex..targetIndex else anchorIndex downTo targetIndex
                val range = sweep.asSequence()
                    .mapNotNull { latestItems.getOrNull(it) as? TimelineItem.Event }
                    .filter { it.content.isBulkSelectable() }
                    .mapNotNull { it.eventId }
                // Swept run first so it survives the cap; pre-existing taps fill the rest in
                // timeline order so the same set survives every recomposition.
                val combined = (
                    range + baseSelection.asSequence().sortedBy {
                        val i = indexOfEvent(it)
                        if (i < 0) Int.MAX_VALUE else i
                    }
                    ).distinct().toList()
                val capped = if (combined.size > maxSelection) combined.take(maxSelection) else combined
                latestOnChange(capped.toPersistentSet())
            }

            detectDragGesturesAfterLongPress(
                onDragStart = { offset ->
                    val anchorId = anchor.eventId
                    anchor.eventId = null // one-shot consume
                    val idx = indexOfEvent(anchorId)
                    if (anchorId != null && idx >= 0) {
                        anchorIndex = idx
                        targetIndex = idx
                        baseSelection = latestBase
                        pointerLocal = offset
                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                        emitRange()
                        autoScroll = scope.launch {
                            while (isActive) {
                                val h = size.height.toFloat()
                                val y = pointerLocal.y
                                val raw = when {
                                    y < edgePx -> -stepPx * (1f - y / edgePx).coerceIn(0f, 1f)
                                    y > h - edgePx -> stepPx * (1f - (h - y) / edgePx).coerceIn(0f, 1f)
                                    else -> 0f
                                }
                                val delta = if (reverseLayout) -raw else raw
                                if (delta != 0f) {
                                    lazyListState.scrollBy(delta)
                                    targetIndexAt(pointerLocal)?.let {
                                        if (it != targetIndex) {
                                            targetIndex = it
                                            emitRange()
                                        }
                                    }
                                }
                                delay(16)
                            }
                        }
                    } else {
                        // Gutter / stale anchor -> do not start a selection.
                        anchorIndex = -1
                        targetIndex = -1
                    }
                },
                onDrag = { change, _ ->
                    if (anchorIndex >= 0) {
                        pointerLocal = change.position
                        change.consume()
                        targetIndexAt(pointerLocal)?.let {
                            if (it != targetIndex) {
                                targetIndex = it
                                emitRange()
                            }
                        }
                    }
                },
                onDragEnd = {
                    autoScroll?.cancel()
                    autoScroll = null
                    anchorIndex = -1
                    targetIndex = -1
                },
                onDragCancel = {
                    autoScroll?.cancel()
                    autoScroll = null
                    anchorIndex = -1
                    targetIndex = -1
                },
            )
        }
}

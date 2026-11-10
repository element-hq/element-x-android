/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package io.element.android.features.messages.impl.timeline

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.messages.impl.typing.TypingNotificationView
import io.element.android.features.messages.impl.typing.aTypingNotificationState
import io.element.android.features.messages.impl.typing.aTypingRoomMember
import io.element.android.tests.testutils.robolectric.RobolectricTest
import org.junit.Test
import org.robolectric.annotation.GraphicsMode

/**
 * Checks what is actually drawn, which needs the native graphics of Robolectric.
 */
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AnimateTimelineItemDrawingTest : RobolectricTest() {
    private var isExpanded by mutableStateOf(false)
    private var keys by mutableStateOf(listOf(TYPING, BELOW))

    @Test
    fun `when an item expands, it is never drawn over the items next to it`() = runAndroidComposeUiTest<ComponentActivity> {
        setContent {
            val coroutineScope = rememberCoroutineScope()
            val keys = listOf(BELOW, EXPANDING, ABOVE)
            val lazyListState = rememberLazyListState()
            val resizes = rememberTimelineItemResizes(lazyListState)
            LazyColumn(
                state = lazyListState,
                modifier = Modifier.height(500.dp),
                reverseLayout = true,
            ) {
                itemsIndexed(keys, key = { _, key -> key }) { index, key ->
                    val modifier = Modifier
                        .animateTimelineItem(this, coroutineScope, resizes, key, index)
                        .fillMaxWidth()
                        .testTag(key)
                    if (key == EXPANDING) {
                        // Like a group of events
                        Box(modifier) {
                            Column(Modifier.background(EXPANDING_COLOR)) {
                                Box(Modifier.fillMaxWidth().height(50.dp))
                                if (isExpanded) {
                                    repeat(3) { Box(Modifier.fillMaxWidth().height(50.dp)) }
                                }
                            }
                        }
                    } else {
                        Box(modifier.height(100.dp).background(Color.Green))
                    }
                }
            }
        }

        mainClock.autoAdvance = false
        isExpanded = true
        repeat(30) { frame ->
            mainClock.advanceTimeByFrame()
            assertWithMessage("Item below in frame $frame").that(expandingColorRowsIn(BELOW)).isEqualTo(0)
            assertWithMessage("Item above in frame $frame").that(expandingColorRowsIn(ABOVE)).isEqualTo(0)
        }
    }

    @Test
    fun `when an item slides in above a visible typing notification, it is not drawn over it`() = runAndroidComposeUiTest<ComponentActivity> {
        setContent {
            ElementTheme {
                val currentKeys = keys
                val coroutineScope = rememberCoroutineScope()
                val insertions = remember { TimelineItemsInsertions(coroutineScope) }
                val lazyListState = rememberLazyListState()
                val resizes = rememberTimelineItemResizes(lazyListState)
                val insertion = rememberItemsInsertion(currentKeys, insertions) { _, current -> setOf(NEW).filterTo(HashSet()) { it in current } }
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.height(LIST_HEIGHT),
                    reverseLayout = true,
                ) {
                    itemsIndexed(currentKeys, key = { _, key -> key }) { index, key ->
                        val modifier = Modifier
                            .animateTimelineItem(this, coroutineScope, resizes, key, index, insertion?.takeIf { key in it.keys })
                            .fillMaxWidth()
                            .testTag(key)
                        when (key) {
                            // Like in the timeline, the typing notification is nested in the item
                            TYPING -> Box(modifier) {
                                TypingNotificationView(state = aTypingNotificationState(typingMembers = listOf(aTypingRoomMember())))
                            }
                            NEW -> Box(modifier.height(100.dp).background(EXPANDING_COLOR))
                            else -> Box(modifier.height(100.dp).background(Color.Green))
                        }
                    }
                }
            }
        }

        mainClock.autoAdvance = false
        keys = listOf(TYPING, NEW, BELOW)
        var displayedFrames = 0
        repeat(30) { frame ->
            mainClock.advanceTimeByFrame()
            if (onAllNodesWithTag(NEW).fetchSemanticsNodes().isNotEmpty()) {
                displayedFrames++
                assertWithMessage("Frame $frame").that(expandingColorRowsIn(TYPING)).isEqualTo(0)
            }
        }
        assertThat(displayedFrames).isGreaterThan(0)
        // Once it's in place, it's displayed
        mainClock.autoAdvance = true
        waitForIdle()
        assertThat(expandingColorRowsIn(NEW)).isGreaterThan(0)
    }

    @Test
    fun `when an item shrinks back, the items coming back into view above it are drawn next to the other ones`() =
        runAndroidComposeUiTest<ComponentActivity> {
            val keys = listOf(BELOW, EXPANDING, ABOVE) + List(10) { "older_$it" }
            var expandingHeight by mutableStateOf(50.dp)
            setContent {
                val coroutineScope = rememberCoroutineScope()
                val lazyListState = rememberLazyListState()
                val resizes = rememberTimelineItemResizes(lazyListState)
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.height(LIST_HEIGHT),
                    reverseLayout = true,
                ) {
                    itemsIndexed(keys, key = { _, key -> key }) { index, key ->
                        val color = when (key) {
                            ABOVE -> EXPANDING_COLOR
                            "older_0" -> OLDER_COLOR
                            else -> Color.Green
                        }
                        Box(
                            Modifier
                                .animateTimelineItem(this, coroutineScope, resizes, key, index)
                                .fillMaxWidth()
                                .height(if (key == EXPANDING) expandingHeight else 100.dp)
                                .background(color)
                        )
                    }
                }
            }
            // Like expanding then collapsing a group of events: the items above go out of view, except the one just above it, then come back into view
            expandingHeight = 300.dp
            waitForIdle()

            mainClock.autoAdvance = false
            expandingHeight = 50.dp
            var displayedFrames = 0
            repeat(30) { frame ->
                mainClock.advanceTimeByFrame()
                val aboveRows = rowsWithColor(EXPANDING_COLOR)
                val olderRows = rowsWithColor(OLDER_COLOR)
                if (olderRows.isNotEmpty()) {
                    displayedFrames++
                    // The item which comes back into view is drawn right above the one below it
                    assertWithMessage("Frame $frame").that(olderRows.last()).isAtMost(aboveRows.first())
                    assertWithMessage("Frame $frame").that(olderRows.last()).isAtLeast(aboveRows.first() - 2)
                }
            }
            assertThat(displayedFrames).isGreaterThan(0)
        }

    private fun AndroidComposeUiTest<*>.rowsWithColor(color: Color): List<Int> {
        val image = onRoot().captureToImage().toPixelMap()
        val x = image.width / 2
        return (0 until image.height).filter { y -> image[x, y] == color }
    }

    // Count the rows in the middle of the given item which have the color of the expanding item
    private fun AndroidComposeUiTest<*>.expandingColorRowsIn(tag: String): Int {
        val bounds = onNodeWithTag(tag).getUnclippedBoundsInRoot()
        return expandingColorRowsBetween(bounds.top, bounds.bottom)
    }

    private fun AndroidComposeUiTest<*>.expandingColorRowsBetween(top: Dp, bottom: Dp): Int {
        val image = onRoot().captureToImage().toPixelMap()
        val x = image.width / 2
        val topPx = (top.value * density.density).toInt().coerceIn(0, image.height - 1)
        val bottomPx = (bottom.value * density.density).toInt().coerceIn(0, image.height)
        return (topPx until bottomPx).count { y -> image[x, y] == EXPANDING_COLOR }
    }

    private companion object {
        const val BELOW = "below"
        const val EXPANDING = "expanding"
        const val ABOVE = "above"
        const val TYPING = "typing"
        const val NEW = "new"
        val LIST_HEIGHT = 500.dp
        val EXPANDING_COLOR = Color.Blue
        val OLDER_COLOR = Color.Red
    }
}

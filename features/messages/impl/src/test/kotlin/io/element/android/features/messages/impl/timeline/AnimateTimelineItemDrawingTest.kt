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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
            LazyColumn(
                modifier = Modifier.height(500.dp),
                reverseLayout = true,
            ) {
                items(listOf(BELOW, EXPANDING, ABOVE), key = { it }) { key ->
                    val modifier = Modifier
                        .animateTimelineItem(this, coroutineScope)
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
        assertInsertedItemIsNeverDrawnBelowTypingNotificationTop(typingNotificationHeight = 30.dp)
    }

    @Test
    fun `when an item slides in above an empty typing notification, it is not drawn in the padding below it`() =
        runAndroidComposeUiTest<ComponentActivity> {
            assertInsertedItemIsNeverDrawnBelowTypingNotificationTop(typingNotificationHeight = 0.dp)
        }

    private fun AndroidComposeUiTest<*>.assertInsertedItemIsNeverDrawnBelowTypingNotificationTop(typingNotificationHeight: Dp) {
        setContent {
            val currentKeys = keys
            val coroutineScope = rememberCoroutineScope()
            val insertions = remember { TimelineItemsInsertions(coroutineScope) }
            val insertion = remember(currentKeys) { insertions.onItemsInserted(setOf(NEW).filterTo(HashSet()) { it in currentKeys }) }
            LazyColumn(
                modifier = Modifier.height(LIST_HEIGHT),
                reverseLayout = true,
                contentPadding = PaddingValues(bottom = BOTTOM_PADDING),
            ) {
                items(currentKeys, key = { it }) { key ->
                    val modifier = Modifier
                        .animateTimelineItem(this, coroutineScope, insertion?.takeIf { key in it.keys })
                        .fillMaxWidth()
                        .testTag(key)
                    when (key) {
                        TYPING -> Box(
                            modifier
                                .hideItemsSlidingIn(insertions, Color.White, coveredHeightBelow = BOTTOM_PADDING)
                                .height(typingNotificationHeight)
                        )
                        NEW -> Box(modifier.height(100.dp).background(EXPANDING_COLOR))
                        else -> Box(modifier.height(100.dp).background(Color.Green))
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
                // From the top of the typing notification to the bottom of the list
                val top = onNodeWithTag(TYPING).getUnclippedBoundsInRoot().top
                assertWithMessage("Frame $frame").that(expandingColorRowsBetween(top, LIST_HEIGHT)).isEqualTo(0)
            }
        }
        assertThat(displayedFrames).isGreaterThan(0)
        // Once it's in place, it's displayed
        mainClock.autoAdvance = true
        waitForIdle()
        val bounds = onNodeWithTag(NEW).getUnclippedBoundsInRoot()
        assertThat(expandingColorRowsBetween(bounds.top, bounds.bottom)).isGreaterThan(0)
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
        val BOTTOM_PADDING = 8.dp
        val EXPANDING_COLOR = Color.Blue
    }
}

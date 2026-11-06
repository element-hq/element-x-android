/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package io.element.android.features.messages.impl.timeline

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.common.truth.Truth.assertThat
import io.element.android.tests.testutils.robolectric.RobolectricTest
import org.junit.Test

class AnimateTimelineItemTest : RobolectricTest() {
    private val itemHeights = mutableMapOf(TYPING to 0.dp, NEWEST to 100.dp, ABOVE to 100.dp, NEW to 80.dp)
    private var keys by mutableStateOf(listOf(TYPING, NEWEST, ABOVE))
    private var resizedItemHeight by mutableStateOf(100.dp)

    @Test
    fun `when an item shrinks, its top edge keeps following the item above it`() = runAndroidComposeUiTest<ComponentActivity> {
        setReverseLayoutList()
        assertTopEdgeFollowsItemAboveWhileResizingTo(50.dp)
    }

    @Test
    fun `when an item grows, its top edge keeps following the item above it`() = runAndroidComposeUiTest<ComponentActivity> {
        setReverseLayoutList()
        assertTopEdgeFollowsItemAboveWhileResizingTo(150.dp)
    }

    @Test
    fun `when an item is inserted at the bottom, it slides in together with the items above it`() = runAndroidComposeUiTest<ComponentActivity> {
        setReverseLayoutList()
        val initialNewestBottom = bottomOf(NEWEST)

        mainClock.autoAdvance = false
        keys = listOf(TYPING, NEW, NEWEST, ABOVE)
        // Advance until the frame where the new item is first displayed
        while (onAllNodesWithTag(NEW).fetchSemanticsNodes().isEmpty()) {
            mainClock.advanceTimeByFrame()
        }
        // Nothing moved yet, and the new item is just below the previously newest one, out of the list
        assertThat(bottomOf(NEWEST).value).isWithin(1f).of(initialNewestBottom.value)
        assertThat(topOf(NEW).value).isWithin(1f).of(initialNewestBottom.value)

        repeat(10) {
            mainClock.advanceTimeByFrame()
            assertThat(topOf(NEW).value).isWithin(1f).of(bottomOf(NEWEST).value)
        }

        mainClock.autoAdvance = true
        waitForIdle()
        assertThat(bottomOf(NEW).value).isWithin(1f).of(topOf(TYPING).value)
        assertThat(topOf(NEW).value).isWithin(1f).of(bottomOf(NEWEST).value)
        assertThat(bottomOf(NEWEST).value).isWithin(1f).of(initialNewestBottom.value - 80f)
    }

    private fun AndroidComposeUiTest<*>.assertTopEdgeFollowsItemAboveWhileResizingTo(newHeight: Dp) {
        val initialTop = topOf(NEWEST)
        assertThat(topOf(NEWEST)).isEqualTo(bottomOf(ABOVE))

        mainClock.autoAdvance = false
        resizedItemHeight = newHeight
        mainClock.advanceTimeByFrame()
        // Without the compensation, the top edge would immediately jump by the size difference
        assertThat(topOf(NEWEST).value).isWithin(1f).of(initialTop.value)

        repeat(10) {
            mainClock.advanceTimeByFrame()
            assertThat(topOf(NEWEST).value).isWithin(1f).of(bottomOf(ABOVE).value)
        }

        mainClock.autoAdvance = true
        waitForIdle()
        assertThat(topOf(NEWEST).value).isWithin(1f).of(initialTop.value + 100f - newHeight.value)
        assertThat(topOf(NEWEST).value).isWithin(1f).of(bottomOf(ABOVE).value)
    }

    private fun AndroidComposeUiTest<*>.setReverseLayoutList() {
        setContent {
            // Like the timeline items in TimelineView, only read the keys in the composition, not in the LazyColumn content
            val currentKeys = keys
            val coroutineScope = rememberCoroutineScope()
            val insertion = remember(currentKeys) {
                TimelineItemsInsertion(setOf(NEW), coroutineScope).takeIf { NEW in currentKeys }
            }
            LazyColumn(
                modifier = Modifier.height(500.dp),
                reverseLayout = true,
                // Same as the timeline
                contentPadding = PaddingValues(top = 64.dp, bottom = 8.dp),
            ) {
                items(currentKeys, key = { it }) { key ->
                    Box(
                        Modifier
                            .animateTimelineItem(this, coroutineScope, insertion?.takeIf { key in it.keys })
                            .fillMaxWidth()
                            .height(if (key == NEWEST) resizedItemHeight else itemHeights.getValue(key))
                    ) {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .testTag(key)
                        )
                    }
                }
            }
        }
    }

    private fun AndroidComposeUiTest<*>.topOf(tag: String) = onNodeWithTag(tag).getUnclippedBoundsInRoot().top

    private fun AndroidComposeUiTest<*>.bottomOf(tag: String) = onNodeWithTag(tag).getUnclippedBoundsInRoot().bottom

    private companion object {
        const val TYPING = "typing"
        const val NEW = "new"
        const val NEWEST = "newest"
        const val ABOVE = "above"
    }
}

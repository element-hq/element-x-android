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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.common.truth.Truth.assertThat
import io.element.android.tests.testutils.robolectric.RobolectricTest
import org.junit.Test

class AnimateTimelineItemTest : RobolectricTest() {
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

    private fun AndroidComposeUiTest<*>.assertTopEdgeFollowsItemAboveWhileResizingTo(newHeight: Dp) {
        val initialTop = resizedItemTop()
        assertThat(resizedItemTop()).isEqualTo(itemAboveBottom())

        mainClock.autoAdvance = false
        resizedItemHeight = newHeight
        mainClock.advanceTimeByFrame()
        // Without the compensation, the top edge would immediately jump by the size difference
        assertThat(resizedItemTop().value).isWithin(1f).of(initialTop.value)

        repeat(10) {
            mainClock.advanceTimeByFrame()
            assertThat(resizedItemTop().value).isWithin(1f).of(itemAboveBottom().value)
        }

        mainClock.autoAdvance = true
        waitForIdle()
        assertThat(resizedItemTop().value).isWithin(1f).of(initialTop.value + 100f - newHeight.value)
        assertThat(resizedItemTop().value).isWithin(1f).of(itemAboveBottom().value)
    }

    private fun AndroidComposeUiTest<*>.setReverseLayoutList() {
        setContent {
            LazyColumn(
                modifier = Modifier.height(500.dp),
                reverseLayout = true,
            ) {
                item(key = "resized") {
                    Box(
                        Modifier
                            .animateTimelineItem(this)
                            .fillMaxWidth()
                            .height(resizedItemHeight)
                    ) {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .testTag(RESIZED_ITEM)
                        )
                    }
                }
                item(key = "above") {
                    Box(
                        Modifier
                            .animateTimelineItem(this)
                            .fillMaxWidth()
                            .height(100.dp)
                            .testTag(ITEM_ABOVE)
                    )
                }
            }
        }
    }

    private fun AndroidComposeUiTest<*>.resizedItemTop() = onNodeWithTag(RESIZED_ITEM).getUnclippedBoundsInRoot().top

    private fun AndroidComposeUiTest<*>.itemAboveBottom() = onNodeWithTag(ITEM_ABOVE).getUnclippedBoundsInRoot().bottom

    private companion object {
        const val RESIZED_ITEM = "resized"
        const val ITEM_ABOVE = "above"
    }
}

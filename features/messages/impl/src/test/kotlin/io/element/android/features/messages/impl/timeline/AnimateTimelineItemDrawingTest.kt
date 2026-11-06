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
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.compose.ui.unit.dp
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

    // Count the rows in the middle of the given item which have the color of the expanding item
    private fun AndroidComposeUiTest<*>.expandingColorRowsIn(tag: String): Int {
        val image = onRoot().captureToImage().toPixelMap()
        val bounds = onNodeWithTag(tag).getUnclippedBoundsInRoot()
        val x = image.width / 2
        val top = (bounds.top.value * density.density).toInt().coerceIn(0, image.height - 1)
        val bottom = (bounds.bottom.value * density.density).toInt().coerceIn(0, image.height)
        return (top until bottom).count { y -> image[x, y] == EXPANDING_COLOR }
    }

    private companion object {
        const val BELOW = "below"
        const val EXPANDING = "expanding"
        const val ABOVE = "above"
        val EXPANDING_COLOR = Color.Blue
    }
}

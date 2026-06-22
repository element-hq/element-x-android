/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package io.element.android.features.messages.impl.timeline.components

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import io.element.android.features.messages.impl.timeline.aTimelineItemEvent
import io.element.android.features.messages.impl.timeline.aTimelineRoomInfo
import io.element.android.features.messages.impl.timeline.model.TimelineItemGroupPosition
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemTextContent
import io.element.android.libraries.preferences.api.store.TimelineLayoutMode
import io.element.android.libraries.testtags.TestTags
import io.element.android.tests.testutils.robolectric.RobolectricTest
import io.element.android.tests.testutils.setSafeContent
import org.junit.Test

class TimelineItemEventRowModernTest : RobolectricTest() {
    @Test
    fun `sender name visible for First group position in modern layout`() = runAndroidComposeUiTest<ComponentActivity> {
        setSafeContent {
            CompositionLocalProvider(LocalInspectionMode provides true) {
                ATimelineItemEventRow(
                    event = aTimelineItemEvent(
                        isMine = false,
                        content = aTimelineItemTextContent(body = "Hello"),
                        groupPosition = TimelineItemGroupPosition.First,
                    ),
                    timelineRoomInfo = aTimelineRoomInfo(timelineLayoutMode = TimelineLayoutMode.Modern),
                )
            }
        }
        onNodeWithTag(TestTags.timelineItemSenderName.value, useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun `avatar visible for First group position in modern layout`() = runAndroidComposeUiTest<ComponentActivity> {
        setSafeContent {
            CompositionLocalProvider(LocalInspectionMode provides true) {
                ATimelineItemEventRow(
                    event = aTimelineItemEvent(
                        isMine = false,
                        content = aTimelineItemTextContent(body = "Hello"),
                        groupPosition = TimelineItemGroupPosition.First,
                    ),
                    timelineRoomInfo = aTimelineRoomInfo(timelineLayoutMode = TimelineLayoutMode.Modern),
                )
            }
        }
        onNodeWithTag(TestTags.timelineItemSenderAvatar.value, useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun `avatar visible in DM mode in modern layout`() = runAndroidComposeUiTest<ComponentActivity> {
        setSafeContent {
            CompositionLocalProvider(LocalInspectionMode provides true) {
                ATimelineItemEventRow(
                    event = aTimelineItemEvent(
                        isMine = false,
                        content = aTimelineItemTextContent(body = "DM message"),
                        groupPosition = TimelineItemGroupPosition.First,
                    ),
                    timelineRoomInfo = aTimelineRoomInfo(
                        isDm = true,
                        timelineLayoutMode = TimelineLayoutMode.Modern,
                    ),
                )
            }
        }
        // Avatars are shown in DMs as well (both sender names visible)
        onNodeWithTag(TestTags.timelineItemSenderAvatar.value, useUnmergedTree = true)
            .assertIsDisplayed()
    }
}

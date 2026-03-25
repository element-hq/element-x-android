/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.features.messages.impl.timeline.aTimelineItemEvent
import io.element.android.features.messages.impl.timeline.aTimelineRoomInfo
import io.element.android.features.messages.impl.timeline.model.TimelineItemGroupPosition
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemTextContent
import io.element.android.libraries.preferences.api.store.TimelineLayoutMode
import io.element.android.libraries.testtags.TestTags
import io.element.android.tests.testutils.setSafeContent
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TimelineItemEventRowModernTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `sender name visible for First group position in modern layout`() {
        rule.setSafeContent {
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
        rule.onNodeWithTag(TestTags.timelineItemSenderName.value, useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun `sender name hidden for Middle group position in modern layout`() {
        rule.setSafeContent {
            CompositionLocalProvider(LocalInspectionMode provides true) {
                ATimelineItemEventRow(
                    event = aTimelineItemEvent(
                        isMine = false,
                        content = aTimelineItemTextContent(body = "Follow-up"),
                        groupPosition = TimelineItemGroupPosition.Middle,
                    ),
                    timelineRoomInfo = aTimelineRoomInfo(timelineLayoutMode = TimelineLayoutMode.Modern),
                )
            }
        }
        rule.onNodeWithTag(TestTags.timelineItemSenderName.value, useUnmergedTree = true)
            .assertDoesNotExist()
    }

    @Test
    fun `avatar visible for First group position in modern layout`() {
        rule.setSafeContent {
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
        rule.onNodeWithTag(TestTags.timelineItemSenderAvatar.value, useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun `avatar hidden in DM mode in modern layout`() {
        rule.setSafeContent {
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
        // Avatars are now shown in DMs (both sender names visible)
        rule.onNodeWithTag(TestTags.timelineItemSenderAvatar.value, useUnmergedTree = true)
            .assertExists()
    }
}

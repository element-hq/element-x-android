/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package io.element.android.features.announcement.impl.fullscreen

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.features.announcement.api.Announcement
import io.element.android.features.announcement.impl.AnnouncementEvent
import io.element.android.features.announcement.impl.AnnouncementState
import io.element.android.features.announcement.impl.anAnnouncementState
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.pressBackKey
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FullscreenAnnouncementViewTest {
    @Test
    fun `clicking on back sends a AnnouncementEvent`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<AnnouncementEvent>()
        setFullscreenAnnouncementView(
            anAnnouncementState(
                announcement = Announcement.Fullscreen.Space,
                eventSink = eventsRecorder,
            ),
        )
        pressBackKey()
        eventsRecorder.assertSingle(AnnouncementEvent.Continue(Announcement.Fullscreen.Space))
    }

    @Test
    fun `clicking on Continue sends a AnnouncementEvent`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<AnnouncementEvent>()
        setFullscreenAnnouncementView(
            anAnnouncementState(
                announcement = Announcement.Fullscreen.Space,
                eventSink = eventsRecorder,
            ),
        )
        clickOn(CommonStrings.action_continue)
        eventsRecorder.assertSingle(AnnouncementEvent.Continue(Announcement.Fullscreen.Space))
    }
}

private fun AndroidComposeUiTest<ComponentActivity>.setFullscreenAnnouncementView(
    state: AnnouncementState,
) {
    setContent {
        FullscreenAnnouncementView(
            state = state,
        )
    }
}

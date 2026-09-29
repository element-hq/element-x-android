/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package io.element.android.features.preferences.impl.moderation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import io.element.android.features.preferences.impl.R
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.media.MediaPreviewValue
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.pressBack
import io.element.android.tests.testutils.robolectric.RobolectricTest
import org.junit.Test
import org.robolectric.annotation.Config

class ModerationAndSafetyViewTest : RobolectricTest() {
    @Test
    fun `clicking on back invokes the expected callback`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<ModerationAndSafetyEvent>(expectEvents = false)
        ensureCalledOnce {
            setModerationAndSafetyView(
                state = aModerationAndSafetyState(
                    eventSink = eventsRecorder
                ),
                onBackClick = it
            )
            pressBack()
        }
    }

    @Test
    fun `clicking on Share presence emits the expected event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<ModerationAndSafetyEvent>()
        setModerationAndSafetyView(
            state = aModerationAndSafetyState(
                eventSink = eventsRecorder,
            ),
        )
        clickOn(R.string.screen_advanced_settings_share_presence)
        eventsRecorder.assertSingle(ModerationAndSafetyEvent.SetSharePresenceEnabled(true))
    }

    @Test
    @Config(qualifiers = "h1080dp")
    fun `clicking on hide invite avatars emits the expected event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<ModerationAndSafetyEvent>()
        setModerationAndSafetyView(
            state = aModerationAndSafetyState(
                eventSink = eventsRecorder,
                hideInviteAvatars = false
            ),
        )
        clickOn(R.string.screen_advanced_settings_hide_invite_avatars_toggle_title)
        eventsRecorder.assertSingle(ModerationAndSafetyEvent.SetHideInviteAvatars(true))
    }

    @Test
    @Config(qualifiers = "h1080dp")
    fun `clicking on timeline media preview always hide emits the expected event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<ModerationAndSafetyEvent>()
        setModerationAndSafetyView(
            state = aModerationAndSafetyState(
                eventSink = eventsRecorder,
                timelineMediaPreviewValue = MediaPreviewValue.On
            ),
        )
        clickOn(R.string.screen_advanced_settings_show_media_timeline_always_hide)
        eventsRecorder.assertSingle(ModerationAndSafetyEvent.SetTimelineMediaPreviewValue(MediaPreviewValue.Off))
    }

    @Test
    @Config(qualifiers = "h1080dp")
    fun `clicking on timeline media preview private rooms emits the expected event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<ModerationAndSafetyEvent>()
        setModerationAndSafetyView(
            state = aModerationAndSafetyState(
                eventSink = eventsRecorder,
                timelineMediaPreviewValue = MediaPreviewValue.On
            ),
        )
        clickOn(R.string.screen_advanced_settings_show_media_timeline_private_rooms)
        eventsRecorder.assertSingle(ModerationAndSafetyEvent.SetTimelineMediaPreviewValue(MediaPreviewValue.Private))
    }

    @Test
    @Config(qualifiers = "h1080dp")
    fun `clicking on timeline media preview always show emits the expected event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<ModerationAndSafetyEvent>()
        setModerationAndSafetyView(
            state = aModerationAndSafetyState(
                eventSink = eventsRecorder,
                timelineMediaPreviewValue = MediaPreviewValue.Off
            ),
        )
        clickOn(R.string.screen_advanced_settings_show_media_timeline_always_show)
        eventsRecorder.assertSingle(ModerationAndSafetyEvent.SetTimelineMediaPreviewValue(MediaPreviewValue.On))
    }

    @Test
    @Config(qualifiers = "h1080dp")
    fun `hide invite avatars toggle is disabled when action is loading`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<ModerationAndSafetyEvent>(expectEvents = false)
        setModerationAndSafetyView(
            state = aModerationAndSafetyState(
                eventSink = eventsRecorder,
                hideInviteAvatars = false,
                setHideInviteAvatarsAction = AsyncAction.Loading
            ),
        )
        // The toggle should be disabled, so clicking should not emit any events
        clickOn(R.string.screen_advanced_settings_hide_invite_avatars_toggle_title)
    }

    @Test
    @Config(qualifiers = "h1080dp")
    fun `timeline media preview options are disabled when action is loading`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<ModerationAndSafetyEvent>(expectEvents = false)
        setModerationAndSafetyView(
            state = aModerationAndSafetyState(
                eventSink = eventsRecorder,
                timelineMediaPreviewValue = MediaPreviewValue.On,
                setTimelineMediaPreviewAction = AsyncAction.Loading
            ),
        )
        // The options should be disabled, so clicking should not emit any events
        clickOn(R.string.screen_advanced_settings_show_media_timeline_always_hide)
        clickOn(R.string.screen_advanced_settings_show_media_timeline_private_rooms)
    }

    @Test
    fun `click on Blocked users invokes the expected callback`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<ModerationAndSafetyEvent>(expectEvents = false)
        ensureCalledOnce { callback ->
            setModerationAndSafetyView(
                state = aModerationAndSafetyState(
                    numberOfBlockedUsers = 1,
                    eventSink = eventsRecorder,
                ),
                onOpenBlockedUsers = callback,
            )
            val text = activity!!.getString(CommonStrings.common_blocked_users)
            onNode(hasText(text) and hasClickAction()).performScrollTo().performClick()
        }
    }

    @Test
    fun `when numberOfBlockedUsers is 0, Blocked users item is not shown`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<ModerationAndSafetyEvent>(expectEvents = false)
        setModerationAndSafetyView(
            state = aModerationAndSafetyState(
                numberOfBlockedUsers = 0,
                eventSink = eventsRecorder,
            ),
        )
        onNodeWithText(activity!!.getString(CommonStrings.common_blocked_users)).assertDoesNotExist()
    }
}

private fun AndroidComposeUiTest<ComponentActivity>.setModerationAndSafetyView(
    state: ModerationAndSafetyState,
    onBackClick: () -> Unit = EnsureNeverCalled(),
    onOpenBlockedUsers: () -> Unit = EnsureNeverCalled(),
) {
    setContent {
        ModerationAndSafetyView(
            state = state,
            onBackClick = onBackClick,
            onOpenBlockedUsers = onOpenBlockedUsers,
        )
    }
}

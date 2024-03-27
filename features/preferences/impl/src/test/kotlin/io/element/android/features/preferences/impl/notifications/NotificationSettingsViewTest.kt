/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.preferences.impl.notifications

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.features.preferences.impl.R
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EnsureNeverCalledWithParam
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.ensureCalledOnceWithParam
import io.element.android.tests.testutils.pressBack
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
class NotificationSettingsViewTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `clicking on back invokes the expected callback`() {
        val eventsRecorder = EventsRecorder<NotificationSettingsEvents>()
        ensureCalledOnce {
            rule.setNotificationSettingsView(
                state = aNotificationSettingsState(
                    eventSink = eventsRecorder
                ),
                onBackPressed = it
            )
            rule.pressBack()
        }
        eventsRecorder.assertSingle(NotificationSettingsEvents.RefreshSystemNotificationsEnabled)
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `clicking on troubleshoot notification invokes the expected callback`() {
        val eventsRecorder = EventsRecorder<NotificationSettingsEvents>()
        ensureCalledOnce {
            rule.setNotificationSettingsView(
                state = aNotificationSettingsState(
                    eventSink = eventsRecorder
                ),
                onTroubleshootNotificationsClicked = it
            )
            rule.clickOn(R.string.troubleshoot_notifications_entry_point_title)
        }
        eventsRecorder.assertSingle(NotificationSettingsEvents.RefreshSystemNotificationsEnabled)
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `clicking on group chats invokes the expected callback`() {
        val eventsRecorder = EventsRecorder<NotificationSettingsEvents>()
        ensureCalledOnceWithParam(false) {
            rule.setNotificationSettingsView(
                state = aNotificationSettingsState(
                    eventSink = eventsRecorder
                ),
                onOpenEditDefault = it
            )
            rule.clickOn(R.string.screen_notification_settings_group_chats)
        }
        eventsRecorder.assertSingle(NotificationSettingsEvents.RefreshSystemNotificationsEnabled)
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `clicking on direct chats invokes the expected callback`() {
        val eventsRecorder = EventsRecorder<NotificationSettingsEvents>()
        ensureCalledOnceWithParam(true) {
            rule.setNotificationSettingsView(
                state = aNotificationSettingsState(
                    eventSink = eventsRecorder
                ),
                onOpenEditDefault = it
            )
            rule.clickOn(R.string.screen_notification_settings_direct_chats)
        }
        eventsRecorder.assertSingle(NotificationSettingsEvents.RefreshSystemNotificationsEnabled)
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `clicking on disable notifications emits the expected events`() {
        testNotificationToggle(true)
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `clicking on enable notifications emits the expected events`() {
        testNotificationToggle(false)
    }

    private fun testNotificationToggle(initialState: Boolean) {
        val eventsRecorder = EventsRecorder<NotificationSettingsEvents>()
        rule.setNotificationSettingsView(
            state = aNotificationSettingsState(
                appNotificationEnabled = initialState,
                eventSink = eventsRecorder
            ),
        )
        rule.clickOn(R.string.screen_notification_settings_enable_notifications)
        eventsRecorder.assertList(
            listOf(
                NotificationSettingsEvents.RefreshSystemNotificationsEnabled,
                NotificationSettingsEvents.SetNotificationsEnabled(!initialState)
            )
        )
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `clicking on disable notify me on at room emits the expected events`() {
        testAtRoomToggle(true)
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `clicking on enable notify me on at room emits the expected events`() {
        testAtRoomToggle(false)
    }

    private fun testAtRoomToggle(initialState: Boolean) {
        val eventsRecorder = EventsRecorder<NotificationSettingsEvents>()
        rule.setNotificationSettingsView(
            state = aNotificationSettingsState(
                atRoomNotificationsEnabled = initialState,
                eventSink = eventsRecorder
            ),
        )
        rule.clickOn(R.string.screen_notification_settings_room_mention_label)
        eventsRecorder.assertList(
            listOf(
                NotificationSettingsEvents.RefreshSystemNotificationsEnabled,
                NotificationSettingsEvents.SetAtRoomNotificationsEnabled(!initialState)
            )
        )
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `clicking on disable notify me on invitation emits the expected events`() {
        testInvitationToggle(true)
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `clicking on enable notify me on invitation emits the expected events`() {
        testInvitationToggle(false)
    }

    private fun testInvitationToggle(initialState: Boolean) {
        val eventsRecorder = EventsRecorder<NotificationSettingsEvents>()
        rule.setNotificationSettingsView(
            state = aNotificationSettingsState(
                inviteForMeNotificationsEnabled = initialState,
                eventSink = eventsRecorder
            ),
        )
        rule.clickOn(R.string.screen_notification_settings_invite_for_me_label)
        eventsRecorder.assertList(
            listOf(
                NotificationSettingsEvents.RefreshSystemNotificationsEnabled,
                NotificationSettingsEvents.SetInviteForMeNotificationsEnabled(!initialState)
            )
        )
    }
}

private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setNotificationSettingsView(
    state: NotificationSettingsState,
    onOpenEditDefault: (isOneToOne: Boolean) -> Unit = EnsureNeverCalledWithParam(),
    onTroubleshootNotificationsClicked: () -> Unit = EnsureNeverCalled(),
    onBackPressed: () -> Unit = EnsureNeverCalled(),
) {
    setContent {
        NotificationSettingsView(
            state = state,
            onOpenEditDefault = onOpenEditDefault,
            onTroubleshootNotificationsClicked = onTroubleshootNotificationsClicked,
            onBackPressed = onBackPressed,
        )
    }
}

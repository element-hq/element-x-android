/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.notifications

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.features.preferences.impl.R
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.ui.strings.CommonStrings
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
                state = aValidNotificationSettingsState(
                    eventSink = eventsRecorder
                ),
                onBackClick = it
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
                state = aValidNotificationSettingsState(
                    eventSink = eventsRecorder
                ),
                onTroubleshootNotificationsClick = it
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
                state = aValidNotificationSettingsState(
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
                state = aValidNotificationSettingsState(
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
            state = aValidNotificationSettingsState(
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
            state = aValidNotificationSettingsState(
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
            state = aValidNotificationSettingsState(
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

    @Config(qualifiers = "h1024dp")
    @Test
    fun `with an error configuration, clicking on continue emits the expected events`() {
        val eventsRecorder = EventsRecorder<NotificationSettingsEvents>()
        rule.setNotificationSettingsView(
            state = aValidNotificationSettingsState(
                changeNotificationSettingAction = AsyncAction.Failure(AN_EXCEPTION),
                eventSink = eventsRecorder
            ),
        )
        rule.clickOn(CommonStrings.action_ok)
        eventsRecorder.assertList(
            listOf(
                NotificationSettingsEvents.RefreshSystemNotificationsEnabled,
                NotificationSettingsEvents.ClearNotificationChangeError
            )
        )
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `with invalid configuration, clicking on continue emits the expected events`() {
        val eventsRecorder = EventsRecorder<NotificationSettingsEvents>()
        rule.setNotificationSettingsView(
            state = aInvalidNotificationSettingsState(
                fixFailed = false,
                eventSink = eventsRecorder
            ),
        )
        rule.clickOn(CommonStrings.action_continue)
        eventsRecorder.assertList(
            listOf(
                NotificationSettingsEvents.RefreshSystemNotificationsEnabled,
                NotificationSettingsEvents.FixConfigurationMismatch
            )
        )
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `with invalid configuration and error, clicking on OK emits the expected events`() {
        val eventsRecorder = EventsRecorder<NotificationSettingsEvents>()
        rule.setNotificationSettingsView(
            state = aInvalidNotificationSettingsState(
                fixFailed = true,
                eventSink = eventsRecorder
            ),
        )
        rule.clickOn(CommonStrings.action_ok)
        eventsRecorder.assertList(
            listOf(
                NotificationSettingsEvents.RefreshSystemNotificationsEnabled,
                NotificationSettingsEvents.ClearConfigurationMismatchError
            )
        )
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `clicking on Push notification provider emits the expected event`() {
        val eventsRecorder = EventsRecorder<NotificationSettingsEvents>()
        rule.setNotificationSettingsView(
            state = aValidNotificationSettingsState(
                eventSink = eventsRecorder
            ),
        )
        rule.clickOn(R.string.screen_advanced_settings_push_provider_android)
        eventsRecorder.assertList(
            listOf(
                NotificationSettingsEvents.RefreshSystemNotificationsEnabled,
                NotificationSettingsEvents.ChangePushProvider,
            )
        )
    }

    @Test
    fun `clicking on a push provider emits the expected event`() {
        val eventsRecorder = EventsRecorder<NotificationSettingsEvents>()
        rule.setNotificationSettingsView(
            state = aValidNotificationSettingsState(
                eventSink = eventsRecorder,
                showChangePushProviderDialog = true,
                availablePushDistributors = listOf(aDistributor("P1"), aDistributor("P2"))
            ),
        )
        rule.onNodeWithText("P2").performClick()
        eventsRecorder.assertList(
            listOf(
                NotificationSettingsEvents.RefreshSystemNotificationsEnabled,
                NotificationSettingsEvents.SetPushProvider(1),
            )
        )
    }
}

private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setNotificationSettingsView(
    state: NotificationSettingsState,
    onOpenEditDefault: (isOneToOne: Boolean) -> Unit = EnsureNeverCalledWithParam(),
    onTroubleshootNotificationsClick: () -> Unit = EnsureNeverCalled(),
    onBackClick: () -> Unit = EnsureNeverCalled(),
) {
    setContent {
        NotificationSettingsView(
            state = state,
            onOpenEditDefault = onOpenEditDefault,
            onTroubleshootNotificationsClick = onTroubleshootNotificationsClick,
            onBackClick = onBackClick,
        )
    }
}

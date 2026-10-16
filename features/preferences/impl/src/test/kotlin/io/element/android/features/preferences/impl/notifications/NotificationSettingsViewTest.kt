/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package io.element.android.features.preferences.impl.notifications

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import com.google.testing.junit.testparameterinjector.TestParameter
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
import io.element.android.tests.testutils.robolectric.RobolectricTestParameter
import org.junit.Test
import org.robolectric.annotation.Config

class NotificationSettingsViewTest : RobolectricTestParameter() {
    @Test
    fun `clicking on back invokes the expected callback`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<NotificationSettingsEvent>()
        ensureCalledOnce {
            setNotificationSettingsView(
                state = aNotificationSettingsState(
                    eventSink = eventsRecorder
                ),
                onBackClick = it
            )
            pressBack()
        }
        eventsRecorder.assertSingle(NotificationSettingsEvent.RefreshSystemNotificationsEnabled)
    }

    @Config(qualifiers = "h1280dp")
    @Test
    fun `clicking on troubleshoot notification invokes the expected callback`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<NotificationSettingsEvent>()
        ensureCalledOnce {
            setNotificationSettingsView(
                state = aNotificationSettingsState(
                    eventSink = eventsRecorder
                ),
                onTroubleshootNotificationsClick = it
            )
            clickOn(R.string.troubleshoot_notifications_entry_point_title)
        }
        eventsRecorder.assertSingle(NotificationSettingsEvent.RefreshSystemNotificationsEnabled)
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `clicking on group chats invokes the expected callback`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<NotificationSettingsEvent>()
        ensureCalledOnceWithParam(false) {
            setNotificationSettingsView(
                state = aNotificationSettingsState(
                    eventSink = eventsRecorder
                ),
                onOpenEditDefault = it
            )
            clickOn(R.string.screen_notification_settings_group_chats)
        }
        eventsRecorder.assertSingle(NotificationSettingsEvent.RefreshSystemNotificationsEnabled)
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `clicking on direct chats invokes the expected callback`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<NotificationSettingsEvent>()
        ensureCalledOnceWithParam(true) {
            setNotificationSettingsView(
                state = aNotificationSettingsState(
                    eventSink = eventsRecorder
                ),
                onOpenEditDefault = it
            )
            clickOn(R.string.screen_notification_settings_direct_chats)
        }
        eventsRecorder.assertSingle(NotificationSettingsEvent.RefreshSystemNotificationsEnabled)
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `clicking on notifications toggle emits the expected events`(
        @TestParameter initialState: Boolean,
    ) = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<NotificationSettingsEvent>()
        setNotificationSettingsView(
            state = aNotificationSettingsState(
                appSettings = anAppSettings(appNotificationsEnabled = initialState),
                eventSink = eventsRecorder
            ),
        )
        clickOn(R.string.screen_notification_settings_enable_notifications)
        eventsRecorder.assertList(
            listOf(
                NotificationSettingsEvent.RefreshSystemNotificationsEnabled,
                NotificationSettingsEvent.SetNotificationsEnabled(!initialState)
            )
        )
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `clicking on notify me on at room toggle emits the expected events`(
        @TestParameter initialState: Boolean,
    ) = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<NotificationSettingsEvent>()
        setNotificationSettingsView(
            state = aNotificationSettingsState(
                matrixSettings = aValidMatrixSettings(atRoomNotificationsEnabled = initialState),
                eventSink = eventsRecorder
            ),
        )
        clickOn(R.string.screen_notification_settings_room_mention_label)
        eventsRecorder.assertList(
            listOf(
                NotificationSettingsEvent.RefreshSystemNotificationsEnabled,
                NotificationSettingsEvent.SetAtRoomNotificationsEnabled(!initialState)
            )
        )
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `clicking on notify me on invitation toggle emits the expected events`(
        @TestParameter initialState: Boolean,
    ) = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<NotificationSettingsEvent>()
        setNotificationSettingsView(
            state = aNotificationSettingsState(
                matrixSettings = aValidMatrixSettings(inviteForMeNotificationsEnabled = initialState),
                eventSink = eventsRecorder
            ),
        )
        clickOn(R.string.screen_notification_settings_invite_for_me_label)
        eventsRecorder.assertList(
            listOf(
                NotificationSettingsEvent.RefreshSystemNotificationsEnabled,
                NotificationSettingsEvent.SetInviteForMeNotificationsEnabled(!initialState)
            )
        )
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `clicking on show all activity toggle emits the expected events`(
        @TestParameter initialState: Boolean,
    ) = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<NotificationSettingsEvent>()
        setNotificationSettingsView(
            state = aNotificationSettingsState(
                showAllActivityInRoomList = initialState,
                eventSink = eventsRecorder
            ),
        )
        clickOn(R.string.screen_notification_settings_show_all_activity_title)
        eventsRecorder.assertList(
            listOf(
                NotificationSettingsEvent.RefreshSystemNotificationsEnabled,
                NotificationSettingsEvent.ToggleShowAllActivityInRoomList(!initialState),
            )
        )
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `with an error configuration, clicking on continue emits the expected events`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<NotificationSettingsEvent>()
        setNotificationSettingsView(
            state = aNotificationSettingsState(
                changeNotificationSettingAction = AsyncAction.Failure(AN_EXCEPTION),
                eventSink = eventsRecorder
            ),
        )
        clickOn(CommonStrings.action_ok)
        eventsRecorder.assertList(
            listOf(
                NotificationSettingsEvent.RefreshSystemNotificationsEnabled,
                NotificationSettingsEvent.ClearNotificationChangeError
            )
        )
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `with invalid configuration, clicking on continue emits the expected events`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<NotificationSettingsEvent>()
        setNotificationSettingsView(
            state = aNotificationSettingsState(
                matrixSettings = anInvalidMatrixSettings(),
                eventSink = eventsRecorder
            ),
        )
        clickOn(CommonStrings.action_continue)
        eventsRecorder.assertList(
            listOf(
                NotificationSettingsEvent.RefreshSystemNotificationsEnabled,
                NotificationSettingsEvent.FixConfigurationMismatch
            )
        )
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `with invalid configuration and error, clicking on OK emits the expected events`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<NotificationSettingsEvent>()
        setNotificationSettingsView(
            state = aNotificationSettingsState(
                matrixSettings = anInvalidMatrixSettings(fixFailed = true),
                eventSink = eventsRecorder
            ),
        )
        clickOn(CommonStrings.action_ok)
        eventsRecorder.assertList(
            listOf(
                NotificationSettingsEvent.RefreshSystemNotificationsEnabled,
                NotificationSettingsEvent.ClearConfigurationMismatchError
            )
        )
    }

    @Config(qualifiers = "h1280dp")
    @Test
    fun `clicking on Push notification provider emits the expected event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<NotificationSettingsEvent>()
        setNotificationSettingsView(
            state = aNotificationSettingsState(
                availablePushDistributors = listOf(
                    aDistributor("Firebase"),
                    aDistributor("ntfy"),
                ),
                eventSink = eventsRecorder
            ),
        )
        clickOn(R.string.screen_advanced_settings_push_provider_android)
        eventsRecorder.assertList(
            listOf(
                NotificationSettingsEvent.RefreshSystemNotificationsEnabled,
                NotificationSettingsEvent.ChangePushProvider,
            )
        )
    }

    @Test
    fun `clicking on a push provider emits the expected event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<NotificationSettingsEvent>()
        setNotificationSettingsView(
            state = aNotificationSettingsState(
                eventSink = eventsRecorder,
                showChangePushProviderDialog = true,
                availablePushDistributors = listOf(aDistributor("P1"), aDistributor("P2"))
            ),
        )
        onNodeWithText("P2").performClick()
        eventsRecorder.assertList(
            listOf(
                NotificationSettingsEvent.RefreshSystemNotificationsEnabled,
                NotificationSettingsEvent.SetPushProvider(1),
            )
        )
    }

    @Config(qualifiers = "h1280dp")
    @Test
    fun `sounds preference category renders rows with current display names`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<NotificationSettingsEvent>()
        setNotificationSettingsView(
            state = aNotificationSettingsState(
                eventSink = eventsRecorder,
                messageSound = aSoundChannelUiState(displayName = "Pixel notification"),
                callRingtone = aSoundChannelUiState(displayName = "Pixel ringtone"),
            ),
        )
        onNodeWithText("Sound").assertIsDisplayed()
        onNodeWithText("Message sound").assertIsDisplayed()
        onNodeWithText("Call ringtone").assertIsDisplayed()
        onNodeWithText("Pixel notification").assertIsDisplayed()
        onNodeWithText("Pixel ringtone").assertIsDisplayed()
    }

    @Config(qualifiers = "h1280dp")
    @Test
    fun `clicking the message sound row opens the preset dialog`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<NotificationSettingsEvent>()
        setNotificationSettingsView(
            state = aNotificationSettingsState(eventSink = eventsRecorder),
        )
        // The click now opens the in-app preset dialog instead of launching the system picker
        // directly; the picker only fires from the dialog's "Choose another sound..." option
        // (covered by Presenter tests + NotificationSoundPickerTest).
        onNodeWithText("Message sound").performClick()
        eventsRecorder.assertList(
            listOf(
                NotificationSettingsEvent.RefreshSystemNotificationsEnabled,
                NotificationSettingsEvent.ShowMessageSoundDialog,
            )
        )
    }
}

private fun AndroidComposeUiTest<ComponentActivity>.setNotificationSettingsView(
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

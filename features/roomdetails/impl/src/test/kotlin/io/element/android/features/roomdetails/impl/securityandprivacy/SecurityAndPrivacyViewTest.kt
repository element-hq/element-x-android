/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.securityandprivacy

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.features.roomdetails.impl.R
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.pressBack
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
class SecurityAndPrivacyViewTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `click on back invokes expected callback`() {
        ensureCalledOnce { callback ->
            rule.setSecurityAndPrivacyView(
                onBackClick = callback,
            )
            rule.pressBack()
        }
    }

    @Test
    fun `click on room access item emits the expected event`() {
        val recorder = EventsRecorder<SecurityAndPrivacyEvents>()
        val state = aSecurityAndPrivacyState(
            eventSink = recorder,
        )
        rule.setSecurityAndPrivacyView(state)
        rule.clickOn(R.string.screen_security_and_privacy_room_access_invite_only_option_title)
        recorder.assertSingle(SecurityAndPrivacyEvents.ChangeRoomAccess(SecurityAndPrivacyRoomAccess.InviteOnly))
    }

    @Test
    fun `click on disabled save doesn't emit event`() {
        val recorder = EventsRecorder<SecurityAndPrivacyEvents>(expectEvents = false)
        val state = aSecurityAndPrivacyState(eventSink = recorder)
        rule.setSecurityAndPrivacyView(state)
        rule.clickOn(CommonStrings.action_save)
        recorder.assertEmpty()
    }

    @Test
    fun `click on enabled save emits the expected event`() {
        val recorder = EventsRecorder<SecurityAndPrivacyEvents>()
        val state = aSecurityAndPrivacyState(
            eventSink = recorder,
            editedSettings = aSecurityAndPrivacySettings(
                roomAccess = SecurityAndPrivacyRoomAccess.Anyone,
            )
        )
        rule.setSecurityAndPrivacyView(state)
        rule.clickOn(CommonStrings.action_save)
        recorder.assertSingle(SecurityAndPrivacyEvents.Save)
    }

    @Test
    @Config(qualifiers = "h640dp")
    fun `click on room address item emits the expected event`() {
        val address = "@alias:matrix.org"
        val recorder = EventsRecorder<SecurityAndPrivacyEvents>()
        val state = aSecurityAndPrivacyState(
            eventSink = recorder,
            editedSettings = aSecurityAndPrivacySettings(
                address = address,
                roomAccess = SecurityAndPrivacyRoomAccess.Anyone,
            ),
        )
        rule.setSecurityAndPrivacyView(state)
        rule.onNodeWithText(address).performClick()
        recorder.assertSingle(SecurityAndPrivacyEvents.EditRoomAddress)
    }

    @Test
    @Config(qualifiers = "h640dp")
    fun `click on room visibility item emits the expected event`() {
        val recorder = EventsRecorder<SecurityAndPrivacyEvents>()
        val state = aSecurityAndPrivacyState(
            eventSink = recorder,
            editedSettings = aSecurityAndPrivacySettings(
                roomAccess = SecurityAndPrivacyRoomAccess.Anyone,
                isVisibleInRoomDirectory = AsyncData.Success(false),
            ),
        )
        rule.setSecurityAndPrivacyView(state)
        rule.clickOn(R.string.screen_security_and_privacy_room_directory_visibility_toggle_title)
        recorder.assertSingle(SecurityAndPrivacyEvents.ToggleRoomVisibility)
    }

    @Test
    @Config(qualifiers = "h640dp")
    fun `click on history visibility item emits the expected event`() {
        val recorder = EventsRecorder<SecurityAndPrivacyEvents>()
        val state = aSecurityAndPrivacyState(
            eventSink = recorder,
            editedSettings = aSecurityAndPrivacySettings(
                historyVisibility = SecurityAndPrivacyHistoryVisibility.SinceSelection,
            ),
        )
        rule.setSecurityAndPrivacyView(state)
        rule.clickOn(R.string.screen_security_and_privacy_room_history_since_selecting_option_title)
        recorder.assertSingle(SecurityAndPrivacyEvents.ChangeHistoryVisibility(SecurityAndPrivacyHistoryVisibility.SinceSelection))
    }

    @Test
    @Config(qualifiers = "h640dp")
    fun `click on encryption item emits the expected event`() {
        val recorder = EventsRecorder<SecurityAndPrivacyEvents>()
        val state = aSecurityAndPrivacyState(
            eventSink = recorder,
            savedSettings = aSecurityAndPrivacySettings(isEncrypted = false),
        )
        rule.setSecurityAndPrivacyView(state)
        rule.clickOn(R.string.screen_security_and_privacy_encryption_toggle_title)
        recorder.assertSingle(SecurityAndPrivacyEvents.ToggleEncryptionState)
    }

    @Test
    fun `click on encryption confirm emits the expected event`() {
        val recorder = EventsRecorder<SecurityAndPrivacyEvents>()
        val state = aSecurityAndPrivacyState(
            eventSink = recorder,
            showEncryptionConfirmation = true,
        )
        rule.setSecurityAndPrivacyView(state)
        rule.clickOn(R.string.screen_security_and_privacy_enable_encryption_alert_confirm_button_title)
        recorder.assertSingle(SecurityAndPrivacyEvents.ConfirmEnableEncryption)
    }
}

private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setSecurityAndPrivacyView(
    state: SecurityAndPrivacyState = aSecurityAndPrivacyState(
        eventSink = EventsRecorder(expectEvents = false),
    ),
    onBackClick: () -> Unit = EnsureNeverCalled(),
) {
    setContent {
        SecurityAndPrivacyView(
            state = state,
            onBackClick = onBackClick,
        )
    }
}

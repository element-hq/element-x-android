/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package io.element.android.features.securityandprivacy.impl.root

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.features.securityandprivacy.impl.R
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalledWithParam
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.pressBack
import kotlinx.collections.immutable.persistentListOf
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
class SecurityAndPrivacyViewTest {
    @Test
    fun `click on back invokes emits the expected event`() = runAndroidComposeUiTest {
        val recorder = EventsRecorder<SecurityAndPrivacyEvent>()
        val state = aSecurityAndPrivacyState(
            eventSink = recorder,
        )
        setSecurityAndPrivacyView(state)
        pressBack()
        recorder.assertSingle(SecurityAndPrivacyEvent.Exit)
    }

    @Test
    fun `discard cancellation emits the expected event`() = runAndroidComposeUiTest {
        val recorder = EventsRecorder<SecurityAndPrivacyEvent>()
        val state = aSecurityAndPrivacyState(
            saveAction = AsyncAction.ConfirmingCancellation,
            eventSink = recorder,
        )
        setSecurityAndPrivacyView(state)
        clickOn(CommonStrings.action_discard)
        recorder.assertSingle(SecurityAndPrivacyEvent.Exit)
    }

    @Test
    fun `save cancellation confirmation emits the expected event`() = runAndroidComposeUiTest {
        val recorder = EventsRecorder<SecurityAndPrivacyEvent>()
        val state = aSecurityAndPrivacyState(
            saveAction = AsyncAction.ConfirmingCancellation,
            eventSink = recorder,
        )
        setSecurityAndPrivacyView(state)
        clickOn(CommonStrings.action_save, inDialog = true)
        recorder.assertSingle(SecurityAndPrivacyEvent.Save)
    }

    @Test
    fun `click on room access item emits the expected event`() = runAndroidComposeUiTest {
        val recorder = EventsRecorder<SecurityAndPrivacyEvent>()
        val state = aSecurityAndPrivacyState(
            eventSink = recorder,
        )
        setSecurityAndPrivacyView(state)
        clickOn(R.string.screen_security_and_privacy_room_access_invite_only_option_title)
        recorder.assertSingle(SecurityAndPrivacyEvent.ChangeRoomAccess(SecurityAndPrivacyRoomAccess.InviteOnly))
    }

    @Test
    fun `click on disabled save doesn't emit event`() = runAndroidComposeUiTest {
        val recorder = EventsRecorder<SecurityAndPrivacyEvent>(expectEvents = false)
        val state = aSecurityAndPrivacyState(eventSink = recorder)
        setSecurityAndPrivacyView(state)
        clickOn(CommonStrings.action_save)
        recorder.assertEmpty()
    }

    @Test
    fun `click on enabled save emits the expected event`() = runAndroidComposeUiTest {
        val recorder = EventsRecorder<SecurityAndPrivacyEvent>()
        val state = aSecurityAndPrivacyState(
            eventSink = recorder,
            editedSettings = aSecurityAndPrivacySettings(
                roomAccess = SecurityAndPrivacyRoomAccess.Anyone,
            )
        )
        setSecurityAndPrivacyView(state)
        clickOn(CommonStrings.action_save)
        recorder.assertSingle(SecurityAndPrivacyEvent.Save)
    }

    @Test
    @Config(qualifiers = "h640dp")
    fun `click on room address item emits the expected event`() = runAndroidComposeUiTest {
        val address = "@alias:matrix.org"
        val recorder = EventsRecorder<SecurityAndPrivacyEvent>()
        val state = aSecurityAndPrivacyState(
            eventSink = recorder,
            editedSettings = aSecurityAndPrivacySettings(
                address = address,
                roomAccess = SecurityAndPrivacyRoomAccess.Anyone,
            ),
        )
        setSecurityAndPrivacyView(state)
        onNodeWithText(address).performClick()
        recorder.assertSingle(SecurityAndPrivacyEvent.EditRoomAddress)
    }

    @Test
    @Config(qualifiers = "h1024dp")
    fun `click on room visibility item emits the expected event`() = runAndroidComposeUiTest {
        val recorder = EventsRecorder<SecurityAndPrivacyEvent>()
        val state = aSecurityAndPrivacyState(
            eventSink = recorder,
            editedSettings = aSecurityAndPrivacySettings(
                roomAccess = SecurityAndPrivacyRoomAccess.Anyone,
                isVisibleInRoomDirectory = AsyncData.Success(false),
            ),
        )
        setSecurityAndPrivacyView(state)
        clickOn(R.string.screen_security_and_privacy_room_directory_visibility_toggle_title)
        recorder.assertSingle(SecurityAndPrivacyEvent.ToggleRoomVisibility)
    }

    @Test
    @Config(qualifiers = "h1024dp")
    fun `click on history visibility item emits the expected event`() = runAndroidComposeUiTest {
        val recorder = EventsRecorder<SecurityAndPrivacyEvent>()
        val state = aSecurityAndPrivacyState(
            eventSink = recorder,
            editedSettings = aSecurityAndPrivacySettings(
                historyVisibility = SecurityAndPrivacyHistoryVisibility.Invited,
            ),
        )
        setSecurityAndPrivacyView(state)
        clickOn(R.string.screen_security_and_privacy_room_history_since_invite_option_title)
        recorder.assertSingle(SecurityAndPrivacyEvent.ChangeHistoryVisibility(SecurityAndPrivacyHistoryVisibility.Invited))
    }

    @Test
    @Config(qualifiers = "h1024dp")
    fun `click on encryption item emits the expected event`() = runAndroidComposeUiTest {
        val recorder = EventsRecorder<SecurityAndPrivacyEvent>()
        val state = aSecurityAndPrivacyState(
            eventSink = recorder,
            savedSettings = aSecurityAndPrivacySettings(isEncrypted = false),
        )
        setSecurityAndPrivacyView(state)
        clickOn(R.string.screen_security_and_privacy_encryption_toggle_title)
        recorder.assertSingle(SecurityAndPrivacyEvent.ToggleEncryptionState)
    }

    @Test
    fun `click on encryption confirm emits the expected event`() = runAndroidComposeUiTest {
        val recorder = EventsRecorder<SecurityAndPrivacyEvent>()
        val state = aSecurityAndPrivacyState(
            eventSink = recorder,
            showEncryptionConfirmation = true,
        )
        setSecurityAndPrivacyView(state)
        clickOn(R.string.screen_security_and_privacy_enable_encryption_alert_confirm_button_title)
        recorder.assertSingle(SecurityAndPrivacyEvent.ConfirmEnableEncryption)
    }

    @Test
    @Config(qualifiers = "h1024dp")
    fun `click on space member access emits the expected event`() = runAndroidComposeUiTest {
        val recorder = EventsRecorder<SecurityAndPrivacyEvent>()
        val state = aSecurityAndPrivacyState(
            eventSink = recorder,
            spaceSelectionMode = SpaceSelectionMode.Single(A_ROOM_ID, null),
        )
        setSecurityAndPrivacyView(state)
        clickOn(R.string.screen_security_and_privacy_room_access_space_members_option_title)
        recorder.assertSingle(SecurityAndPrivacyEvent.SelectSpaceMemberAccess)
    }

    @Test
    @Config(qualifiers = "h1024dp")
    fun `click on ask to join with space members emits the expected event`() = runAndroidComposeUiTest {
        val recorder = EventsRecorder<SecurityAndPrivacyEvent>()
        val state = aSecurityAndPrivacyState(
            eventSink = recorder,
            spaceSelectionMode = SpaceSelectionMode.Single(A_ROOM_ID, null),
        )
        setSecurityAndPrivacyView(state)
        clickOn(R.string.screen_security_and_privacy_ask_to_join_option_title)
        recorder.assertSingle(SecurityAndPrivacyEvent.SelectAskToJoinWithSpaceMembersAccess)
    }

    @Test
    @Config(qualifiers = "h1024dp")
    fun `manage spaces footer is shown when space member access is selected`() = runAndroidComposeUiTest {
        val recorder = EventsRecorder<SecurityAndPrivacyEvent>(expectEvents = false)
        val state = aSecurityAndPrivacyState(
            eventSink = recorder,
            spaceSelectionMode = SpaceSelectionMode.Multiple,
            editedSettings = aSecurityAndPrivacySettings(
                roomAccess = SecurityAndPrivacyRoomAccess.SpaceMember(persistentListOf(A_ROOM_ID)),
            ),
        )
        setSecurityAndPrivacyView(state)
        // The footer text uses AnnotatedString with a link. Verify the footer text is displayed.
        val resources = activity!!.resources
        val actionFooterText = resources.getString(R.string.screen_security_and_privacy_room_access_footer_manage_spaces_action)
        val footerText = resources.getString(R.string.screen_security_and_privacy_room_access_footer, actionFooterText)
        onNodeWithText(footerText).assertExists()
    }
}

private fun AndroidComposeUiTest<ComponentActivity>.setSecurityAndPrivacyView(
    state: SecurityAndPrivacyState = aSecurityAndPrivacyState(
        eventSink = EventsRecorder(expectEvents = false),
    ),
    onLinkClick: (String) -> Unit = EnsureNeverCalledWithParam(),
) {
    setContent {
        SecurityAndPrivacyView(
            state = state,
            onLinkClick = onLinkClick,
        )
    }
}

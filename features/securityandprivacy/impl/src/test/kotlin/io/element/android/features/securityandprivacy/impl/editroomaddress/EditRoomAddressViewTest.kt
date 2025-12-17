/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securityandprivacy.impl.editroomaddress

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.ui.room.address.RoomAddressValidity
import io.element.android.libraries.testtags.TestTags
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

@RunWith(AndroidJUnit4::class)
class EditRoomAddressViewTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `click on back invokes expected callback`() {
        ensureCalledOnce { callback ->
            rule.setEditRoomAddressView(onBackClick = callback)
            rule.pressBack()
        }
    }

    @Test
    fun `click on disabled save doesn't emit event`() {
        val recorder = EventsRecorder<EditRoomAddressEvents>(expectEvents = false)
        val state = anEditRoomAddressState(eventSink = recorder)
        rule.setEditRoomAddressView(state)
        rule.clickOn(CommonStrings.action_save)
        recorder.assertEmpty()
    }

    @Test
    fun `click on enabled save emits the expected event`() {
        val recorder = EventsRecorder<EditRoomAddressEvents>()
        val state = anEditRoomAddressState(
            roomAddress = "room",
            roomAddressValidity = RoomAddressValidity.Valid,
            eventSink = recorder
        )
        rule.setEditRoomAddressView(state)
        rule.clickOn(CommonStrings.action_save)
        recorder.assertSingle(EditRoomAddressEvents.Save)
    }

    @Test
    fun `text changes on text field emits the expected event`() {
        val recorder = EventsRecorder<EditRoomAddressEvents>()
        val state = anEditRoomAddressState(
            roomAddress = "",
            eventSink = recorder
        )
        rule.setEditRoomAddressView(state)

        rule.onNodeWithTag(TestTags.roomAddressField.value).performTextInput("alias")
        recorder.assertSingle(EditRoomAddressEvents.RoomAddressChanged("alias"))
    }

    @Test
    fun `click on dismiss error emits the expected event`() {
        val recorder = EventsRecorder<EditRoomAddressEvents>()
        val state = anEditRoomAddressState(
            roomAddress = "",
            saveAction = AsyncAction.Failure(IllegalStateException()),
            eventSink = recorder
        )
        rule.setEditRoomAddressView(state)
        rule.clickOn(CommonStrings.action_cancel)
        recorder.assertSingle(EditRoomAddressEvents.DismissError)
    }

    @Test
    fun `click on retry error emits the expected event`() {
        val recorder = EventsRecorder<EditRoomAddressEvents>()
        val state = anEditRoomAddressState(
            roomAddress = "",
            saveAction = AsyncAction.Failure(IllegalStateException()),
            eventSink = recorder
        )
        rule.setEditRoomAddressView(state)
        rule.clickOn(CommonStrings.action_retry)
        recorder.assertSingle(EditRoomAddressEvents.Save)
    }
}

private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setEditRoomAddressView(
    state: EditRoomAddressState = anEditRoomAddressState(
        eventSink = EventsRecorder(expectEvents = false),
    ),
    onBackClick: () -> Unit = EnsureNeverCalled(),
) {
    setContent {
        EditRoomAddressView(
            state = state,
            onBackClick = onBackClick,
        )
    }
}

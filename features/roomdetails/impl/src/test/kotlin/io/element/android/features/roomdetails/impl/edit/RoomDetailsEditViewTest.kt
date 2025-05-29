/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.edit

import androidx.activity.ComponentActivity
import androidx.annotation.StringRes
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.isEditable
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.ui.media.AvatarAction
import io.element.android.libraries.testtags.TestTags
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.pressBack
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomDetailsEditViewTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `clicking on back invoke back callback`() {
        val eventsRecorder = EventsRecorder<RoomDetailsEditEvents>(expectEvents = false)
        ensureCalledOnce { callback ->
            rule.setRoomDetailsEditView(
                aRoomDetailsEditState(
                    eventSink = eventsRecorder
                ),
                onBackClick = callback,
            )
            rule.pressBack()
        }
    }

    @Test
    fun `when edition is successful, the expected callback is invoked`() {
        val eventsRecorder = EventsRecorder<RoomDetailsEditEvents>(expectEvents = false)
        ensureCalledOnce { callback ->
            rule.setRoomDetailsEditView(
                aRoomDetailsEditState(
                    eventSink = eventsRecorder,
                    saveAction = AsyncAction.Success(Unit)
                ),
                onRoomEdited = callback,
            )
        }
    }

    @Test
    fun `when name is changed, the expected Event is emitted`() {
        val eventsRecorder = EventsRecorder<RoomDetailsEditEvents>()
        rule.setRoomDetailsEditView(
            aRoomDetailsEditState(
                eventSink = eventsRecorder,
                roomRawName = "Marketing",
            ),
        )
        rule.onNodeWithText("Marketing").performTextInput("A")
        eventsRecorder.assertSingle(RoomDetailsEditEvents.UpdateRoomName("AMarketing"))
    }

    @Test
    fun `when user cannot change name, nothing happen`() {
        val eventsRecorder = EventsRecorder<RoomDetailsEditEvents>(expectEvents = false)
        rule.setRoomDetailsEditView(
            aRoomDetailsEditState(
                eventSink = eventsRecorder,
                roomRawName = "Marketing",
                canChangeName = false,
            ),
        )
        rule.onNodeWithText("Marketing").assert(!isEditable())
    }

    @Test
    fun `when topic is changed, the expected Event is emitted`() {
        val eventsRecorder = EventsRecorder<RoomDetailsEditEvents>()
        rule.setRoomDetailsEditView(
            aRoomDetailsEditState(
                eventSink = eventsRecorder,
                roomTopic = "My Topic",
            ),
        )
        rule.onNodeWithText("My Topic").performTextInput("A")
        eventsRecorder.assertSingle(RoomDetailsEditEvents.UpdateRoomTopic("AMy Topic"))
    }

    @Test
    fun `when user cannot change topic, nothing happen`() {
        val eventsRecorder = EventsRecorder<RoomDetailsEditEvents>(expectEvents = false)
        rule.setRoomDetailsEditView(
            aRoomDetailsEditState(
                eventSink = eventsRecorder,
                roomTopic = "My Topic",
                canChangeTopic = false,
            ),
        )
        rule.onNodeWithText("My Topic").assert(!isEditable())
    }

    @Ignore("This test is failing because the bottom sheet does not open")
    @Test
    fun `when avatar is changed with action to take photo, the expected Event is emitted`() {
        testAvatarChange(
            stringActionRes = CommonStrings.action_take_photo,
            expectedEvent = RoomDetailsEditEvents.HandleAvatarAction(AvatarAction.TakePhoto),
        )
    }

    @Ignore("This test is failing because the bottom sheet does not open")
    @Test
    fun `when avatar is changed with action to choose photo, the expected Event is emitted`() {
        testAvatarChange(
            stringActionRes = CommonStrings.action_choose_photo,
            expectedEvent = RoomDetailsEditEvents.HandleAvatarAction(AvatarAction.ChoosePhoto),
        )
    }

    @Ignore("This test is failing because the bottom sheet does not open")
    @Test
    fun `when avatar is changed with action to remove photo, the expected Event is emitted`() {
        testAvatarChange(
            stringActionRes = CommonStrings.action_remove,
            expectedEvent = RoomDetailsEditEvents.HandleAvatarAction(AvatarAction.Remove),
        )
    }

    private fun testAvatarChange(
        @StringRes stringActionRes: Int,
        expectedEvent: RoomDetailsEditEvents.HandleAvatarAction,
    ) {
        val eventsRecorder = EventsRecorder<RoomDetailsEditEvents>()
        rule.setRoomDetailsEditView(
            aRoomDetailsEditState(
                eventSink = eventsRecorder,
            ),
        )
        // Open the bottom sheet
        rule.onNode(hasTestTag(TestTags.editAvatar.value)).performClick()
        rule.onNodeWithText(rule.activity.getString(stringActionRes)).assertExists()
        rule.clickOn(stringActionRes)
        eventsRecorder.assertSingle(expectedEvent)
    }

    @Test
    fun `when user cannot change avatar, nothing happen`() {
        val eventsRecorder = EventsRecorder<RoomDetailsEditEvents>(expectEvents = false)
        rule.setRoomDetailsEditView(
            aRoomDetailsEditState(
                eventSink = eventsRecorder,
                canChangeAvatar = false,
            ),
        )
        rule.onNode(hasTestTag(TestTags.editAvatar.value)).performClick()
        rule.onNodeWithText(rule.activity.getString(CommonStrings.action_take_photo)).assertDoesNotExist()
    }

    @Test
    fun `when save is clicked, the expected Event is emitted`() {
        val eventsRecorder = EventsRecorder<RoomDetailsEditEvents>()
        rule.setRoomDetailsEditView(
            aRoomDetailsEditState(
                eventSink = eventsRecorder,
                saveButtonEnabled = true,
            ),
        )
        rule.clickOn(CommonStrings.action_save)
        eventsRecorder.assertSingle(RoomDetailsEditEvents.Save)
    }

    @Test
    fun `when save is clicked, but nothing need to be saved, nothing happens`() {
        val eventsRecorder = EventsRecorder<RoomDetailsEditEvents>(expectEvents = false)
        rule.setRoomDetailsEditView(
            aRoomDetailsEditState(
                eventSink = eventsRecorder,
                saveButtonEnabled = false,
            ),
        )
        rule.clickOn(CommonStrings.action_save)
    }

    @Test
    fun `when error is shown, closing the dialog emit the expected Event`() {
        val eventsRecorder = EventsRecorder<RoomDetailsEditEvents>()
        rule.setRoomDetailsEditView(
            aRoomDetailsEditState(
                eventSink = eventsRecorder,
                saveAction = AsyncAction.Failure(RuntimeException("Whelp")),
            ),
        )
        rule.clickOn(CommonStrings.action_ok)
        eventsRecorder.assertSingle(RoomDetailsEditEvents.CancelSaveChanges)
    }
}

private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setRoomDetailsEditView(
    state: RoomDetailsEditState,
    onBackClick: () -> Unit = EnsureNeverCalled(),
    onRoomEdited: () -> Unit = EnsureNeverCalled(),
) {
    setContent {
        RoomDetailsEditView(
            state = state,
            onBackClick = onBackClick,
            onRoomEditSuccess = onRoomEdited,
        )
    }
}

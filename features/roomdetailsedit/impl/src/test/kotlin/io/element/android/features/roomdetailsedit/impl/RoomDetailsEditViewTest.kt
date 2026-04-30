/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package io.element.android.features.roomdetailsedit.impl

import androidx.activity.ComponentActivity
import androidx.annotation.StringRes
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.isEditable
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
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
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomDetailsEditViewTest {
    @Test
    fun `clicking on back emits the expected Event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<RoomDetailsEditEvent>()
        setRoomDetailsEditView(
            aRoomDetailsEditState(
                eventSink = eventsRecorder
            ),
        )
        pressBack()
        eventsRecorder.assertSingle(RoomDetailsEditEvent.OnBackPress)
    }

    @Test
    fun `clicking on discard when confirming exit emits the expected Event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<RoomDetailsEditEvent>()
        setRoomDetailsEditView(
            aRoomDetailsEditState(
                saveAction = AsyncAction.ConfirmingCancellation,
                eventSink = eventsRecorder,
            ),
        )
        clickOn(CommonStrings.action_discard)
        eventsRecorder.assertSingle(RoomDetailsEditEvent.OnBackPress)
    }

    @Test
    fun `clicking on save when confirming exit emits the expected Event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<RoomDetailsEditEvent>()
        setRoomDetailsEditView(
            aRoomDetailsEditState(
                saveAction = AsyncAction.ConfirmingCancellation,
                eventSink = eventsRecorder,
            ),
        )
        clickOn(CommonStrings.action_save, inDialog = true)
        eventsRecorder.assertSingle(RoomDetailsEditEvent.Save)
    }

    @Test
    fun `when edition is successful, the expected callback is invoked`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<RoomDetailsEditEvent>(expectEvents = false)
        ensureCalledOnce { callback ->
            setRoomDetailsEditView(
                aRoomDetailsEditState(
                    eventSink = eventsRecorder,
                    saveAction = AsyncAction.Success(Unit)
                ),
                onDone = callback,
            )
        }
    }

    @Test
    fun `when name is changed, the expected Event is emitted`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<RoomDetailsEditEvent>()
        setRoomDetailsEditView(
            aRoomDetailsEditState(
                eventSink = eventsRecorder,
                roomRawName = "Marketing",
            ),
        )
        onNodeWithText("Marketing").performTextInput("A")
        eventsRecorder.assertSingle(RoomDetailsEditEvent.UpdateRoomName("AMarketing"))
    }

    @Test
    fun `when user cannot change name, nothing happen`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<RoomDetailsEditEvent>(expectEvents = false)
        setRoomDetailsEditView(
            aRoomDetailsEditState(
                eventSink = eventsRecorder,
                roomRawName = "Marketing",
                canChangeName = false,
            ),
        )
        onNodeWithText("Marketing").assert(!isEditable())
    }

    @Test
    fun `when topic is changed, the expected Event is emitted`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<RoomDetailsEditEvent>()
        setRoomDetailsEditView(
            aRoomDetailsEditState(
                eventSink = eventsRecorder,
                roomTopic = "My Topic",
            ),
        )
        onNodeWithText("My Topic").performTextInput("A")
        eventsRecorder.assertSingle(RoomDetailsEditEvent.UpdateRoomTopic("AMy Topic"))
    }

    @Test
    fun `when user cannot change topic, nothing happen`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<RoomDetailsEditEvent>(expectEvents = false)
        setRoomDetailsEditView(
            aRoomDetailsEditState(
                eventSink = eventsRecorder,
                roomTopic = "My Topic",
                canChangeTopic = false,
            ),
        )
        onNodeWithText("My Topic").assert(!isEditable())
    }

    @Ignore("This test is failing because the bottom sheet does not open")
    @Test
    fun `when avatar is changed with action to take photo, the expected Event is emitted`() {
        testAvatarChange(
            stringActionRes = CommonStrings.action_take_photo,
            expectedEvent = RoomDetailsEditEvent.HandleAvatarAction(AvatarAction.TakePhoto),
        )
    }

    @Ignore("This test is failing because the bottom sheet does not open")
    @Test
    fun `when avatar is changed with action to choose photo, the expected Event is emitted`() {
        testAvatarChange(
            stringActionRes = CommonStrings.action_choose_photo,
            expectedEvent = RoomDetailsEditEvent.HandleAvatarAction(AvatarAction.ChoosePhoto),
        )
    }

    @Ignore("This test is failing because the bottom sheet does not open")
    @Test
    fun `when avatar is changed with action to remove photo, the expected Event is emitted`() {
        testAvatarChange(
            stringActionRes = CommonStrings.action_remove,
            expectedEvent = RoomDetailsEditEvent.HandleAvatarAction(AvatarAction.Remove),
        )
    }

    private fun testAvatarChange(
        @StringRes stringActionRes: Int,
        expectedEvent: RoomDetailsEditEvent.HandleAvatarAction,
    ) = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<RoomDetailsEditEvent>()
        setRoomDetailsEditView(
            aRoomDetailsEditState(
                eventSink = eventsRecorder,
            ),
        )
        // Open the bottom sheet
        onNode(hasTestTag(TestTags.editAvatar.value)).performClick()
        onNodeWithText(activity!!.getString(stringActionRes)).assertExists()
        clickOn(stringActionRes)
        eventsRecorder.assertSingle(expectedEvent)
    }

    @Test
    fun `when user cannot change avatar, nothing happen`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<RoomDetailsEditEvent>(expectEvents = false)
        setRoomDetailsEditView(
            aRoomDetailsEditState(
                eventSink = eventsRecorder,
                canChangeAvatar = false,
            ),
        )
        onNode(hasTestTag(TestTags.editAvatar.value)).performClick()
        onNodeWithText(activity!!.getString(CommonStrings.action_take_photo)).assertDoesNotExist()
    }

    @Test
    fun `when save is clicked, the expected Event is emitted`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<RoomDetailsEditEvent>()
        setRoomDetailsEditView(
            aRoomDetailsEditState(
                eventSink = eventsRecorder,
                saveButtonEnabled = true,
            ),
        )
        clickOn(CommonStrings.action_save)
        eventsRecorder.assertSingle(RoomDetailsEditEvent.Save)
    }

    @Test
    fun `when save is clicked, but nothing need to be saved, nothing happens`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<RoomDetailsEditEvent>(expectEvents = false)
        setRoomDetailsEditView(
            aRoomDetailsEditState(
                eventSink = eventsRecorder,
                saveButtonEnabled = false,
            ),
        )
        clickOn(CommonStrings.action_save)
    }

    @Test
    fun `when error is shown, closing the dialog emit the expected Event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<RoomDetailsEditEvent>()
        setRoomDetailsEditView(
            aRoomDetailsEditState(
                eventSink = eventsRecorder,
                saveAction = AsyncAction.Failure(RuntimeException("Whelp")),
            ),
        )
        clickOn(CommonStrings.action_ok)
        eventsRecorder.assertSingle(RoomDetailsEditEvent.CloseDialog)
    }
}

private fun AndroidComposeUiTest<ComponentActivity>.setRoomDetailsEditView(
    state: RoomDetailsEditState,
    onDone: () -> Unit = EnsureNeverCalled(),
) {
    setContent {
        RoomDetailsEditView(
            state = state,
            onDone = onDone,
        )
    }
}

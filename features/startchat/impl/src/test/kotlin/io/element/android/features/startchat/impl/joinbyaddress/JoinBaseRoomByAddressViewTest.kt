/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package io.element.android.features.startchat.impl.joinbyaddress

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.features.startchat.impl.R
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.setSafeContent
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class JoinBaseRoomByAddressViewTest {
    @Test
    fun `entering text emits the expected event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<JoinRoomByAddressEvent>()
        setJoinRoomByAddressView(
            aJoinRoomByAddressState(
                eventSink = eventsRecorder,
            )
        )
        val text = activity!!.getString(R.string.screen_start_chat_join_room_by_address_action)
        onNodeWithText(text).performTextInput("#address:matrix.org")
        eventsRecorder.assertSingle(JoinRoomByAddressEvent.UpdateAddress("#address:matrix.org"))
    }

    @Test
    fun `clicking on continue emits the expected event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<JoinRoomByAddressEvent>()
        setJoinRoomByAddressView(
            aJoinRoomByAddressState(
                eventSink = eventsRecorder,
            )
        )
        clickOn(CommonStrings.action_continue)
        eventsRecorder.assertSingle(JoinRoomByAddressEvent.Continue)
    }
}

private fun AndroidComposeUiTest<ComponentActivity>.setJoinRoomByAddressView(
    state: JoinRoomByAddressState,
) {
    setSafeContent {
        JoinRoomByAddressView(state = state)
    }
}

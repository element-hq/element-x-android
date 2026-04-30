/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package io.element.android.libraries.troubleshoot.impl.history

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_FORMATTED_DATE
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PushHistoryViewTest {
    @Test
    fun `clicking on Reset sends a PushHistoryEvents`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<PushHistoryEvents>()
        setPushHistoryView(
            aPushHistoryState(
                pushCounter = 123,
                eventSink = eventsRecorder,
            ),
        )
        val menuContentDescription = activity!!.getString(CommonStrings.a11y_user_menu)
        onNodeWithContentDescription(menuContentDescription).performClick()
        clickOn(CommonStrings.action_reset)
        eventsRecorder.assertSingle(PushHistoryEvents.Reset(requiresConfirmation = true))
        // Also check that the push counter is rendered
        onNodeWithText("123").assertExists()
    }

    @Test
    fun `clicking on show only errors sends a PushHistoryEvents(true)`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<PushHistoryEvents>()
        setPushHistoryView(
            aPushHistoryState(
                showOnlyErrors = false,
                eventSink = eventsRecorder,
            ),
        )
        val menuContentDescription = activity!!.getString(CommonStrings.a11y_user_menu)
        onNodeWithContentDescription(menuContentDescription).performClick()
        onNodeWithText("Show only errors").performClick()
        eventsRecorder.assertSingle(PushHistoryEvents.SetShowOnlyErrors(showOnlyErrors = true))
    }

    @Test
    fun `clicking on show only errors sends a PushHistoryEvents(false)`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<PushHistoryEvents>()
        setPushHistoryView(
            aPushHistoryState(
                showOnlyErrors = true,
                eventSink = eventsRecorder,
            ),
        )
        val menuContentDescription = activity!!.getString(CommonStrings.a11y_user_menu)
        onNodeWithContentDescription(menuContentDescription).performClick()
        onNodeWithText("Show only errors").performClick()
        eventsRecorder.assertSingle(PushHistoryEvents.SetShowOnlyErrors(showOnlyErrors = false))
    }

    @Test
    fun `clicking on an invalid event has no effect`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<PushHistoryEvents>(expectEvents = false)
        setPushHistoryView(
            aPushHistoryState(
                pushHistoryItems = listOf(
                    aPushHistoryItem(
                        formattedDate = A_FORMATTED_DATE,
                    )
                ),
                eventSink = eventsRecorder,
            ),
        )
        onNodeWithText(A_FORMATTED_DATE).performClick()
        // No callback invoked
    }

    @Test
    fun `clicking on a valid event emits the expected Event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<PushHistoryEvents>()
        setPushHistoryView(
            aPushHistoryState(
                pushHistoryItems = listOf(
                    aPushHistoryItem(
                        formattedDate = A_FORMATTED_DATE,
                        eventId = AN_EVENT_ID,
                        roomId = A_ROOM_ID,
                        sessionId = A_SESSION_ID,
                    )
                ),
                eventSink = eventsRecorder,
            ),
        )
        onNodeWithText(A_FORMATTED_DATE).performClick()
        eventsRecorder.assertSingle(
            PushHistoryEvents.NavigateTo(
                sessionId = A_SESSION_ID,
                roomId = A_ROOM_ID,
                eventId = AN_EVENT_ID,
            )
        )
    }
}

private fun AndroidComposeUiTest<ComponentActivity>.setPushHistoryView(
    state: PushHistoryState,
    onBackClick: () -> Unit = EnsureNeverCalled(),
) {
    setContent {
        PushHistoryView(
            state = state,
            onBackClick = onBackClick,
        )
    }
}

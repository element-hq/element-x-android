/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.troubleshoot.impl.history

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_FORMATTED_DATE
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PushHistoryViewTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `clicking on Reset sends a PushHistoryEvents`() {
        val eventsRecorder = EventsRecorder<PushHistoryEvents>()
        rule.setPushHistoryView(
            aPushHistoryState(
                pushCounter = 123,
                eventSink = eventsRecorder,
            ),
        )
        val menuContentDescription = rule.activity.getString(CommonStrings.a11y_user_menu)
        rule.onNodeWithContentDescription(menuContentDescription).performClick()
        rule.clickOn(CommonStrings.action_reset)
        eventsRecorder.assertSingle(PushHistoryEvents.Reset(requiresConfirmation = true))
        // Also check that the push counter is rendered
        rule.onNodeWithText("123").assertExists()
    }

    @Test
    fun `clicking on show only errors sends a PushHistoryEvents(true)`() {
        val eventsRecorder = EventsRecorder<PushHistoryEvents>()
        rule.setPushHistoryView(
            aPushHistoryState(
                showOnlyErrors = false,
                eventSink = eventsRecorder,
            ),
        )
        val menuContentDescription = rule.activity.getString(CommonStrings.a11y_user_menu)
        rule.onNodeWithContentDescription(menuContentDescription).performClick()
        rule.onNodeWithText("Show only errors").performClick()
        eventsRecorder.assertSingle(PushHistoryEvents.SetShowOnlyErrors(showOnlyErrors = true))
    }

    @Test
    fun `clicking on show only errors sends a PushHistoryEvents(false)`() {
        val eventsRecorder = EventsRecorder<PushHistoryEvents>()
        rule.setPushHistoryView(
            aPushHistoryState(
                showOnlyErrors = true,
                eventSink = eventsRecorder,
            ),
        )
        val menuContentDescription = rule.activity.getString(CommonStrings.a11y_user_menu)
        rule.onNodeWithContentDescription(menuContentDescription).performClick()
        rule.onNodeWithText("Show only errors").performClick()
        eventsRecorder.assertSingle(PushHistoryEvents.SetShowOnlyErrors(showOnlyErrors = false))
    }

    @Test
    fun `clicking on an invalid event has no effect`() {
        val eventsRecorder = EventsRecorder<PushHistoryEvents>(expectEvents = false)
        rule.setPushHistoryView(
            aPushHistoryState(
                pushHistoryItems = listOf(
                    aPushHistoryItem(
                        formattedDate = A_FORMATTED_DATE,
                    )
                ),
                eventSink = eventsRecorder,
            ),
        )
        rule.onNodeWithText(A_FORMATTED_DATE).performClick()
        // No callback invoked
    }

    @Test
    fun `clicking on a valid event emits the expected Event`() {
        val eventsRecorder = EventsRecorder<PushHistoryEvents>()
        rule.setPushHistoryView(
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
        rule.onNodeWithText(A_FORMATTED_DATE).performClick()
        eventsRecorder.assertSingle(
            PushHistoryEvents.NavigateTo(
                sessionId = A_SESSION_ID,
                roomId = A_ROOM_ID,
                eventId = AN_EVENT_ID,
            )
        )
    }
}

private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setPushHistoryView(
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

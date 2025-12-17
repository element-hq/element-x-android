/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.poll.impl.history

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.features.poll.api.pollcontent.aPollContentState
import io.element.android.features.poll.impl.R
import io.element.android.features.poll.impl.history.model.PollHistoryFilter
import io.element.android.libraries.matrix.api.core.EventId
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
class PollHistoryViewTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `clicking on back invokes the expected callback`() {
        val eventsRecorder = EventsRecorder<PollHistoryEvents>(expectEvents = false)
        ensureCalledOnce {
            rule.setPollHistoryViewView(
                aPollHistoryState(
                    eventSink = eventsRecorder
                ),
                goBack = it
            )
            rule.pressBack()
        }
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `clicking on edit poll invokes the expected callback`() {
        val eventsRecorder = EventsRecorder<PollHistoryEvents>(expectEvents = false)
        val eventId = EventId("\$anEventId")
        val state = aPollHistoryState(
            currentItems = listOf(
                aPollHistoryItem(
                    state = aPollContentState(
                        eventId = eventId,
                        isMine = true,
                        isEnded = false,
                    )
                )
            ),
            eventSink = eventsRecorder
        )
        ensureCalledOnceWithParam(eventId) {
            rule.setPollHistoryViewView(
                state = state,
                onEditPoll = it
            )
            rule.clickOn(CommonStrings.action_edit_poll)
        }
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `clicking on poll end emits the expected Event`() {
        val eventsRecorder = EventsRecorder<PollHistoryEvents>()
        val eventId = EventId("\$anEventId")
        val state = aPollHistoryState(
            currentItems = listOf(
                aPollHistoryItem(
                    state = aPollContentState(
                        eventId = eventId,
                        isMine = true,
                        isEnded = false,
                        isPollEditable = false,
                    )
                )
            ),
            eventSink = eventsRecorder
        )
        rule.setPollHistoryViewView(
            state = state,
        )
        rule.clickOn(CommonStrings.action_end_poll)
        // Cancel the dialog
        rule.clickOn(CommonStrings.action_cancel)
        // Do it again, and confirm the dialog
        rule.clickOn(CommonStrings.action_end_poll)
        eventsRecorder.assertEmpty()
        rule.clickOn(CommonStrings.action_ok)
        eventsRecorder.assertSingle(
            PollHistoryEvents.EndPoll(eventId)
        )
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `clicking on poll answer emits the expected Event`() {
        val eventsRecorder = EventsRecorder<PollHistoryEvents>()
        val eventId = EventId("\$anEventId")
        val state = aPollHistoryState(
            currentItems = listOf(
                aPollHistoryItem(
                    state = aPollContentState(
                        eventId = eventId,
                        isMine = true,
                        isEnded = false,
                        isPollEditable = false,
                    )
                )
            ),
            eventSink = eventsRecorder
        )
        val answer = state.pollHistoryItems.ongoing.first().state.answerItems.first().answer
        rule.setPollHistoryViewView(
            state = state,
        )
        rule.onNodeWithText(
            text = answer.text,
            useUnmergedTree = true,
        ).performClick()
        eventsRecorder.assertSingle(
            PollHistoryEvents.SelectPollAnswer(eventId, answer.id)
        )
    }

    @Test
    fun `clicking on past tab emits the expected Event`() {
        val eventsRecorder = EventsRecorder<PollHistoryEvents>()
        rule.setPollHistoryViewView(
            aPollHistoryState(
                eventSink = eventsRecorder
            ),
        )
        rule.clickOn(R.string.screen_polls_history_filter_past)
        eventsRecorder.assertSingle(
            PollHistoryEvents.SelectFilter(filter = PollHistoryFilter.PAST)
        )
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `clicking on load more emits the expected Event`() {
        val eventsRecorder = EventsRecorder<PollHistoryEvents>()
        rule.setPollHistoryViewView(
            aPollHistoryState(
                hasMoreToLoad = true,
                eventSink = eventsRecorder,
            ),
        )
        rule.clickOn(CommonStrings.action_load_more)
        eventsRecorder.assertSingle(
            PollHistoryEvents.LoadMore
        )
    }
}

private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setPollHistoryViewView(
    state: PollHistoryState,
    onEditPoll: (EventId) -> Unit = EnsureNeverCalledWithParam(),
    goBack: () -> Unit = EnsureNeverCalled(),
) {
    setContent {
        PollHistoryView(
            state = state,
            onEditPoll = onEditPoll,
            goBack = goBack,
        )
    }
}

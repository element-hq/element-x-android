/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package io.element.android.features.messages.impl.pinned.list

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.features.messages.impl.actionlist.ActionListEvent
import io.element.android.features.messages.impl.actionlist.anActionListState
import io.element.android.features.messages.impl.timeline.aTimelineItemList
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemFileContent
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EnsureNeverCalledWithParam
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.ensureCalledOnceWithParam
import io.element.android.tests.testutils.pressBack
import io.element.android.tests.testutils.setSafeContent
import io.element.android.wysiwyg.link.Link
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PinnedMessagesListViewTest {
    @Test
    fun `clicking on back calls the expected callback`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<PinnedMessagesListEvent>(expectEvents = false)
        val state = aLoadedPinnedMessagesListState(
            eventSink = eventsRecorder
        )
        ensureCalledOnce { callback ->
            setPinnedMessagesListView(
                state = state,
                onBackClick = callback
            )
            pressBack()
        }
    }

    @Test
    fun `click on an event calls the expected callback`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<PinnedMessagesListEvent>(expectEvents = false)
        val content = aTimelineItemFileContent()
        val state = aLoadedPinnedMessagesListState(
            timelineItems = aTimelineItemList(content),
            eventSink = eventsRecorder
        )

        val event = state.timelineItems.first() as TimelineItem.Event
        ensureCalledOnceWithParam(event) { callback ->
            setPinnedMessagesListView(
                state = state,
                onEventClick = callback
            )
            onAllNodesWithText(content.filename).onFirst().performClick()
        }
    }

    @Test
    fun `long click on an event emits the expected event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<ActionListEvent>(expectEvents = true)
        val content = aTimelineItemFileContent()
        val state = aLoadedPinnedMessagesListState(
            timelineItems = aTimelineItemList(content),
            actionListState = anActionListState(eventSink = eventsRecorder)
        )

        setPinnedMessagesListView(
            state = state,
        )
        onAllNodesWithText(content.filename).onFirst()
            .performTouchInput {
                longClick()
            }
        val event = state.timelineItems.first() as TimelineItem.Event
        eventsRecorder.assertSingle(ActionListEvent.ComputeForMessage(event, state.userEventPermissions))
    }
}

private fun AndroidComposeUiTest<ComponentActivity>.setPinnedMessagesListView(
    state: PinnedMessagesListState,
    onBackClick: () -> Unit = EnsureNeverCalled(),
    onEventClick: (event: TimelineItem.Event) -> Unit = EnsureNeverCalledWithParam(),
    onUserDataClick: (MatrixUser) -> Unit = EnsureNeverCalledWithParam(),
    onLinkClick: (Link) -> Unit = EnsureNeverCalledWithParam(),
    onLinkLongClick: (Link) -> Unit = EnsureNeverCalledWithParam(),
) {
    setSafeContent(clearAndroidUiDispatcher = true) {
        PinnedMessagesListView(
            state = state,
            onBackClick = onBackClick,
            onEventClick = onEventClick,
            onUserDataClick = onUserDataClick,
            onLinkClick = onLinkClick,
            onLinkLongClick = onLinkLongClick,
        )
    }
}

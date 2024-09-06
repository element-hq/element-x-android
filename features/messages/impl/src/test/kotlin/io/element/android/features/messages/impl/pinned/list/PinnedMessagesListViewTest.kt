/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.messages.impl.pinned.list

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.features.messages.impl.actionlist.ActionListEvents
import io.element.android.features.messages.impl.actionlist.anActionListState
import io.element.android.features.messages.impl.timeline.aTimelineItemList
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemFileContent
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EnsureNeverCalledWithParam
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.ensureCalledOnceWithParam
import io.element.android.tests.testutils.pressBack
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PinnedMessagesListViewTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `clicking on back calls the expected callback`() {
        val eventsRecorder = EventsRecorder<PinnedMessagesListEvents>(expectEvents = false)
        val state = aLoadedPinnedMessagesListState(
            eventSink = eventsRecorder
        )
        ensureCalledOnce { callback ->
            rule.setPinnedMessagesListView(
                state = state,
                onBackClick = callback
            )
            rule.pressBack()
        }
    }

    @Test
    fun `click on an event calls the expected callback`() {
        val eventsRecorder = EventsRecorder<PinnedMessagesListEvents>(expectEvents = false)
        val content = aTimelineItemFileContent()
        val state = aLoadedPinnedMessagesListState(
            timelineItems = aTimelineItemList(content),
            eventSink = eventsRecorder
        )

        val event = state.timelineItems.first() as TimelineItem.Event
        ensureCalledOnceWithParam(event) { callback ->
            rule.setPinnedMessagesListView(
                state = state,
                onEventClick = callback
            )
            rule.onAllNodesWithText(content.body).onFirst().performClick()
        }
    }

    @Test
    fun `long click on an event emits the expected event`() {
        val eventsRecorder = EventsRecorder<ActionListEvents>(expectEvents = true)
        val content = aTimelineItemFileContent()
        val state = aLoadedPinnedMessagesListState(
            timelineItems = aTimelineItemList(content),
            actionListState = anActionListState(eventSink = eventsRecorder)
        )

        rule.setPinnedMessagesListView(
            state = state,
        )
        rule.onAllNodesWithText(content.body).onFirst()
            .performTouchInput {
                longClick()
            }
        val event = state.timelineItems.first() as TimelineItem.Event
        eventsRecorder.assertSingle(ActionListEvents.ComputeForMessage(event, state.userEventPermissions))
    }
}

private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setPinnedMessagesListView(
    state: PinnedMessagesListState,
    onBackClick: () -> Unit = EnsureNeverCalled(),
    onEventClick: (event: TimelineItem.Event) -> Unit = EnsureNeverCalledWithParam(),
    onUserDataClick: (UserId) -> Unit = EnsureNeverCalledWithParam(),
    onLinkClick: (String) -> Unit = EnsureNeverCalledWithParam(),
) {
    setContent {
        PinnedMessagesListView(
            state = state,
            onBackClick = onBackClick,
            onEventClick = onEventClick,
            onUserDataClick = onUserDataClick,
            onLinkClick = onLinkClick,
        )
    }
}

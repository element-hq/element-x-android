/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.messages.impl.timeline

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.virtual.TimelineItemLoadingIndicatorModel
import io.element.android.features.messages.impl.typing.TypingNotificationState
import io.element.android.features.messages.impl.typing.aTypingNotificationState
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EnsureNeverCalledWithParam
import io.element.android.tests.testutils.EnsureNeverCalledWithTwoParams
import io.element.android.tests.testutils.EventsRecorder
import kotlinx.collections.immutable.persistentListOf
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TimelineViewTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `reaching the end of the timeline with more events to load emits a LoadMore event`() {
        val eventsRecorder = EventsRecorder<TimelineEvents>()
        rule.setTimelineView(
            state = aTimelineState(
                timelineItems = persistentListOf<TimelineItem>(
                    TimelineItem.Virtual(
                        id = "backward_pagination",
                        model = TimelineItemLoadingIndicatorModel(Timeline.PaginationDirection.BACKWARDS, 0)
                    ),
                ),
                eventSink = eventsRecorder,
            ),
        )
        eventsRecorder.assertSingle(TimelineEvents.LoadMore(Timeline.PaginationDirection.BACKWARDS))
    }

    @Test
    fun `reaching the end of the timeline does not send a LoadMore event`() {
        val eventsRecorder = EventsRecorder<TimelineEvents>(expectEvents = false)
        rule.setTimelineView(
            state = aTimelineState(
                eventSink = eventsRecorder,
            ),
        )
    }

    @Test
    fun `scroll to bottom on live timeline does not emit the Event`() {
        val eventsRecorder = EventsRecorder<TimelineEvents>(expectEvents = false)
        rule.setTimelineView(
            state = aTimelineState(
                isLive = true,
                eventSink = eventsRecorder,
            ),
            forceJumpToBottomVisibility = true,
        )
        val contentDescription = rule.activity.getString(CommonStrings.a11y_jump_to_bottom)
        rule.onNodeWithContentDescription(contentDescription).performClick()
    }

    @Test
    fun `scroll to bottom on detached timeline emits the expected Event`() {
        val eventsRecorder = EventsRecorder<TimelineEvents>()
        rule.setTimelineView(
            state = aTimelineState(
                isLive = false,
                eventSink = eventsRecorder,
            ),
        )
        val contentDescription = rule.activity.getString(CommonStrings.a11y_jump_to_bottom)
        rule.onNodeWithContentDescription(contentDescription).performClick()
        eventsRecorder.assertSingle(TimelineEvents.JumpToLive)
    }
}

private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setTimelineView(
    state: TimelineState,
    typingNotificationState: TypingNotificationState = aTypingNotificationState(),
    onUserDataClick: (UserId) -> Unit = EnsureNeverCalledWithParam(),
    onLinkClick: (String) -> Unit = EnsureNeverCalledWithParam(),
    onMessageClick: (TimelineItem.Event) -> Unit = EnsureNeverCalledWithParam(),
    onMessageLongClick: (TimelineItem.Event) -> Unit = EnsureNeverCalledWithParam(),
    onTimestampClick: (TimelineItem.Event) -> Unit = EnsureNeverCalledWithParam(),
    onSwipeToReply: (TimelineItem.Event) -> Unit = EnsureNeverCalledWithParam(),
    onReactionClick: (emoji: String, TimelineItem.Event) -> Unit = EnsureNeverCalledWithTwoParams(),
    onReactionLongClick: (emoji: String, TimelineItem.Event) -> Unit = EnsureNeverCalledWithTwoParams(),
    onMoreReactionsClick: (TimelineItem.Event) -> Unit = EnsureNeverCalledWithParam(),
    onReadReceiptClick: (TimelineItem.Event) -> Unit = EnsureNeverCalledWithParam(),
    onJoinCallClick: () -> Unit = EnsureNeverCalled(),
    forceJumpToBottomVisibility: Boolean = false,
) {
    setContent {
        TimelineView(
            state = state,
            typingNotificationState = typingNotificationState,
            onUserDataClick = onUserDataClick,
            onLinkClick = onLinkClick,
            onMessageClick = onMessageClick,
            onMessageLongClick = onMessageLongClick,
            onTimestampClick = onTimestampClick,
            onSwipeToReply = onSwipeToReply,
            onReactionClick = onReactionClick,
            onReactionLongClick = onReactionLongClick,
            onMoreReactionsClick = onMoreReactionsClick,
            onReadReceiptClick = onReadReceiptClick,
            onJoinCallClick = onJoinCallClick,
            forceJumpToBottomVisibility = forceJumpToBottomVisibility,
        )
    }
}

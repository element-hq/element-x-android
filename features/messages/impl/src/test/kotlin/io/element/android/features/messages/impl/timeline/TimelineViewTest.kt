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
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.features.messages.impl.typing.aTypingNotificationState
import io.element.android.tests.testutils.EnsureNeverCalledWithParam
import io.element.android.tests.testutils.EnsureNeverCalledWithTwoParams
import io.element.android.tests.testutils.EventsRecorder
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TimelineViewTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `reaching the end of the timeline with more events to load emits a LoadMore event`() {
        val eventsRecorder = EventsRecorder<TimelineEvents>()
        rule.setContent {
            TimelineView(
                aTimelineState(
                    eventSink = eventsRecorder,
                    paginationState = aPaginationState(
                        hasMoreToLoadBackwards = true,
                    )
                ),
                typingNotificationState = aTypingNotificationState(),
                roomName = null,
                onUserDataClicked = EnsureNeverCalledWithParam(),
                onLinkClicked = EnsureNeverCalledWithParam(),
                onMessageClicked = EnsureNeverCalledWithParam(),
                onMessageLongClicked = EnsureNeverCalledWithParam(),
                onTimestampClicked = EnsureNeverCalledWithParam(),
                onSwipeToReply = EnsureNeverCalledWithParam(),
                onReactionClicked = EnsureNeverCalledWithTwoParams(),
                onReactionLongClicked = EnsureNeverCalledWithTwoParams(),
                onMoreReactionsClicked = EnsureNeverCalledWithParam(),
                onReadReceiptClick = EnsureNeverCalledWithParam(),
            )
        }
        eventsRecorder.assertSingle(TimelineEvents.LoadMore)
    }

    @Test
    fun `reaching the end of the timeline does not send a LoadMore event`() {
        val eventsRecorder = EventsRecorder<TimelineEvents>(expectEvents = false)
        rule.setContent {
            TimelineView(
                aTimelineState(
                    eventSink = eventsRecorder,
                    paginationState = aPaginationState(
                        hasMoreToLoadBackwards = false,
                    )
                ),
                typingNotificationState = aTypingNotificationState(),
                roomName = null,
                onUserDataClicked = EnsureNeverCalledWithParam(),
                onLinkClicked = EnsureNeverCalledWithParam(),
                onMessageClicked = EnsureNeverCalledWithParam(),
                onMessageLongClicked = EnsureNeverCalledWithParam(),
                onTimestampClicked = EnsureNeverCalledWithParam(),
                onSwipeToReply = EnsureNeverCalledWithParam(),
                onReactionClicked = EnsureNeverCalledWithTwoParams(),
                onReactionLongClicked = EnsureNeverCalledWithTwoParams(),
                onMoreReactionsClicked = EnsureNeverCalledWithParam(),
                onReadReceiptClick = EnsureNeverCalledWithParam(),
            )
        }
    }
}

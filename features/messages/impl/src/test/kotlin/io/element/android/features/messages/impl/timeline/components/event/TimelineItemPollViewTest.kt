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

package io.element.android.features.messages.impl.timeline.components.event

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.features.messages.impl.timeline.TimelineEvents
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemPollContent
import io.element.android.libraries.testtags.TestTags
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.pressTag
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TimelineItemPollViewTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `answering a poll with first answer should emit a PollAnswerSelected event`() {
        testAnswer(answerIndex = 0)
    }

    @Test
    fun `answering a poll with second answer should emit a PollAnswerSelected event`() {
        testAnswer(answerIndex = 1)
    }

    private fun testAnswer(answerIndex: Int) {
        val eventsRecorder = EventsRecorder<TimelineEvents.TimelineItemPollEvents>()
        val content = aTimelineItemPollContent()
        rule.setContent {
            TimelineItemPollView(
                content = content,
                eventSink = eventsRecorder
            )
        }
        val answer = content.answerItems[answerIndex].answer
        rule.onNode(hasText(answer.text)).performClick()
        eventsRecorder.assertSingle(TimelineEvents.SelectPollAnswer(content.eventId!!, answer.id))
    }

    @Test
    fun `editing a poll should emit a PollEditClicked event`() {
        val eventsRecorder = EventsRecorder<TimelineEvents.TimelineItemPollEvents>()
        val content = aTimelineItemPollContent(
            isMine = true,
            isEditable = true,
        )
        rule.setContent {
            TimelineItemPollView(
                content = content,
                eventSink = eventsRecorder
            )
        }
        rule.clickOn(CommonStrings.action_edit_poll)
        eventsRecorder.assertSingle(TimelineEvents.EditPoll(content.eventId!!))
    }

    @Test
    fun `closing a poll should emit a PollEndClicked event`() {
        val eventsRecorder = EventsRecorder<TimelineEvents.TimelineItemPollEvents>()
        val content = aTimelineItemPollContent(
            isMine = true,
        )
        rule.setContent {
            TimelineItemPollView(
                content = content,
                eventSink = eventsRecorder
            )
        }
        rule.clickOn(CommonStrings.action_end_poll)
        // A confirmation dialog should be shown
        eventsRecorder.assertEmpty()
        rule.pressTag(TestTags.dialogPositive.value)
        eventsRecorder.assertSingle(TimelineEvents.EndPoll(content.eventId!!))
    }
}

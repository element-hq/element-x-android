/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
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
        rule.onNode(
            matcher = hasText(answer.text),
            useUnmergedTree = true,
        ).performClick()
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

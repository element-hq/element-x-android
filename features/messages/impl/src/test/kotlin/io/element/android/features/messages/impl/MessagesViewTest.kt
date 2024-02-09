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

package io.element.android.features.messages.impl

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeRight
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.features.messages.impl.actionlist.ActionListEvents
import io.element.android.features.messages.impl.actionlist.ActionListState
import io.element.android.features.messages.impl.actionlist.anActionListState
import io.element.android.features.messages.impl.actionlist.model.TimelineItemAction
import io.element.android.features.messages.impl.attachments.Attachment
import io.element.android.features.messages.impl.messagecomposer.aMessageComposerState
import io.element.android.features.messages.impl.timeline.aTimelineItemEvent
import io.element.android.features.messages.impl.timeline.aTimelineItemList
import io.element.android.features.messages.impl.timeline.aTimelineItemReadReceipts
import io.element.android.features.messages.impl.timeline.aTimelineRoomInfo
import io.element.android.features.messages.impl.timeline.aTimelineState
import io.element.android.features.messages.impl.timeline.components.customreaction.CustomReactionEvents
import io.element.android.features.messages.impl.timeline.components.reactionsummary.ReactionSummaryEvents
import io.element.android.features.messages.impl.timeline.components.receipt.aReadReceiptData
import io.element.android.features.messages.impl.timeline.components.receipt.bottomsheet.ReadReceiptBottomSheetEvents
import io.element.android.features.messages.impl.timeline.components.retrysendmenu.RetrySendMenuEvents
import io.element.android.features.messages.impl.timeline.components.retrysendmenu.aRetrySendMenuState
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemTextContent
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.testtags.TestTags
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureCalledOnceWithParam
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EnsureNeverCalledWithParam
import io.element.android.tests.testutils.EnsureNeverCalledWithParamAndResult
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.ensureCalledOnceWithParam
import io.element.android.tests.testutils.pressBack
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MessagesViewTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `clicking on back invoke expected callback`() {
        val eventsRecorder = EventsRecorder<MessagesEvents>(expectEvents = false)
        val state = aMessagesState(
            eventSink = eventsRecorder
        )
        ensureCalledOnce { callback ->
            rule.setMessagesView(
                state = state,
                onBackPressed = callback,
            )
            rule.pressBack()
        }
    }

    @Test
    fun `clicking on room name invoke expected callback`() {
        val eventsRecorder = EventsRecorder<MessagesEvents>(expectEvents = false)
        val state = aMessagesState(
            eventSink = eventsRecorder
        )
        ensureCalledOnce { callback ->
            rule.setMessagesView(
                state = state,
                onRoomDetailsClicked = callback,
            )
            rule.onNodeWithText(state.roomName.dataOrNull().orEmpty()).performClick()
        }
    }

    @Test
    fun `clicking on join call invoke expected callback`() {
        val eventsRecorder = EventsRecorder<MessagesEvents>(expectEvents = false)
        val state = aMessagesState(
            eventSink = eventsRecorder
        )
        ensureCalledOnce { callback ->
            rule.setMessagesView(
                state = state,
                onJoinCallClicked = callback,
            )
            val joinCallContentDescription = rule.activity.getString(CommonStrings.a11y_start_call)
            rule.onNodeWithContentDescription(joinCallContentDescription).performClick()
        }
    }

    @Test
    fun `clicking on an Event invoke expected callback`() {
        val eventsRecorder = EventsRecorder<MessagesEvents>(expectEvents = false)
        val state = aMessagesState(
            eventSink = eventsRecorder
        )
        val timelineItem = state.timelineState.timelineItems.first()
        val callback = EnsureCalledOnceWithParam(
            expectedParam = timelineItem,
            result = true,
        )
        rule.setMessagesView(
            state = state,
            onEventClicked = callback,
        )
        // Cannot perform click on "Text", it's not detected. Use tag instead
        rule.onAllNodesWithTag(TestTags.messageBubble.value).onFirst().performClick()
        callback.assertSuccess()
    }

    @Test
    fun `clicking on an Event timestamp in error emits the expected Event`() {
        val eventsRecorder = EventsRecorder<RetrySendMenuEvents>()
        val state = aMessagesState(
            retrySendMenuState = aRetrySendMenuState(
                eventSink = eventsRecorder
            ),
        )
        val timelineItem = state.timelineState.timelineItems[1] as TimelineItem.Event
        rule.setMessagesView(
            state = state,
        )
        rule.onAllNodesWithText(timelineItem.sentTime)[1].performClick()
        eventsRecorder.assertSingle(RetrySendMenuEvents.EventSelected(timelineItem))
    }

    @Test
    fun `long clicking on an Event emits the expected Event userHasPermissionToSendMessage`() {
        `long clicking on an Event emits the expected Event`(userHasPermissionToSendMessage = true)
    }

    @Test
    fun `long clicking on an Event emits the expected Event userHasPermissionToRedactOwn`() {
        `long clicking on an Event emits the expected Event`(userHasPermissionToRedactOwn = true)
    }

    @Test
    fun `long clicking on an Event emits the expected Event userHasPermissionToRedactOther`() {
        `long clicking on an Event emits the expected Event`(userHasPermissionToRedactOther = true)
    }

    @Test
    fun `long clicking on an Event emits the expected Event userHasPermissionToSendReaction`() {
        `long clicking on an Event emits the expected Event`(userHasPermissionToSendReaction = true)
    }

    private fun `long clicking on an Event emits the expected Event`(
        userHasPermissionToSendMessage: Boolean = false,
        userHasPermissionToRedactOwn: Boolean = false,
        userHasPermissionToRedactOther: Boolean = false,
        userHasPermissionToSendReaction: Boolean = false,
    ) {
        val eventsRecorder = EventsRecorder<ActionListEvents>()
        val state = aMessagesState(
            actionListState = anActionListState(
                eventSink = eventsRecorder
            ),
            userHasPermissionToSendMessage = userHasPermissionToSendMessage,
            userHasPermissionToRedactOwn = userHasPermissionToRedactOwn,
            userHasPermissionToRedactOther = userHasPermissionToRedactOther,
            userHasPermissionToSendReaction = userHasPermissionToSendReaction,
        )
        val timelineItem = state.timelineState.timelineItems.first() as TimelineItem.Event
        rule.setMessagesView(
            state = state,
        )
        // Cannot perform click on "Text", it's not detected. Use tag instead
        rule.onAllNodesWithTag(TestTags.messageBubble.value).onFirst().performTouchInput { longClick() }
        eventsRecorder.assertSingle(
            ActionListEvents.ComputeForMessage(
                event = timelineItem,
                canRedactOwn = state.userHasPermissionToRedactOwn,
                canRedactOther = state.userHasPermissionToRedactOther,
                canSendMessage = state.userHasPermissionToSendMessage,
                canSendReaction = state.userHasPermissionToSendReaction,
            )
        )
    }

    @Test
    fun `clicking on a read receipt list emits the expected Event`() {
        val eventsRecorder = EventsRecorder<ReadReceiptBottomSheetEvents>()
        val state = aMessagesState(
            timelineState = aTimelineState(
                renderReadReceipts = true,
                timelineItems = persistentListOf(
                    aTimelineItemEvent(
                        readReceiptState = aTimelineItemReadReceipts(
                            receipts = listOf(
                                aReadReceiptData(0),
                            ),
                        ),
                    ),
                ),
            ),
            readReceiptBottomSheetState = aReadReceiptBottomSheetState(
                eventSink = eventsRecorder
            ),
        )
        val timelineItem = state.timelineState.timelineItems.first() as TimelineItem.Event
        rule.setMessagesView(
            state = state,
        )
        rule.onNodeWithTag(TestTags.messageReadReceipts.value).performClick()
        eventsRecorder.assertSingle(ReadReceiptBottomSheetEvents.EventSelected(timelineItem))
    }

    @Test
    fun `swiping on an Event emits the expected Event`() {
        swipeTest(userHasPermissionToSendMessage = true)
    }

    @Test
    fun `swiping on an Event emits no Event if user does not have permission to send message`() {
        swipeTest(userHasPermissionToSendMessage = false)
    }

    private fun swipeTest(userHasPermissionToSendMessage: Boolean) {
        val eventsRecorder = EventsRecorder<MessagesEvents>()
        val state = aMessagesState(
            timelineState = aTimelineState(
                timelineItems = aTimelineItemList(aTimelineItemTextContent()),
                timelineRoomInfo = aTimelineRoomInfo(
                    userHasPermissionToSendMessage = userHasPermissionToSendMessage
                ),
            ),
            eventSink = eventsRecorder,
        )
        rule.setMessagesView(
            state = state,
        )
        rule.onAllNodesWithTag(TestTags.messageBubble.value).onFirst().performTouchInput { swipeRight(endX = 200f) }
        if (userHasPermissionToSendMessage) {
            val timelineItem = state.timelineState.timelineItems.first() as TimelineItem.Event
            eventsRecorder.assertSingle(MessagesEvents.HandleAction(TimelineItemAction.Reply, timelineItem))
        } else {
            eventsRecorder.assertEmpty()
        }
    }

    @Test
    fun `clicking on send location invoke expected callback`() {
        val eventsRecorder = EventsRecorder<MessagesEvents>(expectEvents = false)
        val state = aMessagesState(
            composerState = aMessageComposerState(
                showAttachmentSourcePicker = true
            ),
            eventSink = eventsRecorder
        )
        ensureCalledOnce { callback ->
            rule.setMessagesView(
                state = state,
                onSendLocationClicked = callback,
            )
            rule.clickOn(R.string.screen_room_attachment_source_location)
        }
    }

    @Test
    fun `clicking on create poll invoke expected callback`() {
        val eventsRecorder = EventsRecorder<MessagesEvents>(expectEvents = false)
        val state = aMessagesState(
            composerState = aMessageComposerState(
                showAttachmentSourcePicker = true
            ),
            eventSink = eventsRecorder
        )
        ensureCalledOnce { callback ->
            rule.setMessagesView(
                state = state,
                onCreatePollClicked = callback,
            )
            // Then click on the poll action
            rule.clickOn(R.string.screen_room_attachment_source_poll)
        }
    }

    @Test
    fun `clicking on the sender of an Event invoke expected callback`() {
        val eventsRecorder = EventsRecorder<MessagesEvents>(expectEvents = false)
        val state = aMessagesState(
            eventSink = eventsRecorder
        )
        val timelineItem = state.timelineState.timelineItems.first()
        ensureCalledOnceWithParam(
            param = (timelineItem as TimelineItem.Event).senderId
        ) { callback ->
            rule.setMessagesView(
                state = state,
                onUserDataClicked = callback,
            )
            val senderName = (timelineItem as? TimelineItem.Event)?.senderDisplayName.orEmpty()
            rule.onNodeWithText(senderName).performClick()
        }
    }

    @Test
    fun `selecting a action on a message emits the expected Event`() {
        val eventsRecorder = EventsRecorder<MessagesEvents>()
        val state = aMessagesState(
            eventSink = eventsRecorder
        )
        val timelineItem = state.timelineState.timelineItems.first() as TimelineItem.Event
        val stateWithMessageAction = state.copy(
            actionListState = anActionListState(
                target = ActionListState.Target.Success(
                    event = timelineItem,
                    displayEmojiReactions = true,
                    actions = persistentListOf(TimelineItemAction.Edit),
                )
            ),
        )
        rule.setMessagesView(
            state = stateWithMessageAction,
        )
        rule.clickOn(CommonStrings.action_edit)
        // Give time for the close animation to complete
        rule.mainClock.advanceTimeBy(milliseconds = 1_000)
        eventsRecorder.assertSingle(MessagesEvents.HandleAction(TimelineItemAction.Edit, timelineItem))
    }

    @Test
    fun `clicking on a reaction emits the expected Event`() {
        val eventsRecorder = EventsRecorder<MessagesEvents>()
        val state = aMessagesState(
            eventSink = eventsRecorder
        )
        val timelineItem = state.timelineState.timelineItems.first() as TimelineItem.Event
        rule.setMessagesView(
            state = state,
        )
        rule.onAllNodesWithText("👍️").onFirst().performClick()
        eventsRecorder.assertSingle(MessagesEvents.ToggleReaction("👍️", timelineItem.eventId!!))
    }

    @Test
    fun `long clicking on a reaction emits the expected Event`() {
        val eventsRecorder = EventsRecorder<ReactionSummaryEvents>()
        val state = aMessagesState(
            reactionSummaryState = aReactionSummaryState(
                target = null,
                eventSink = eventsRecorder,
            ),
        )
        val timelineItem = state.timelineState.timelineItems.first() as TimelineItem.Event
        rule.setMessagesView(
            state = state,
        )
        rule.onAllNodesWithText("👍️").onFirst().performTouchInput { longClick() }
        eventsRecorder.assertSingle(ReactionSummaryEvents.ShowReactionSummary(timelineItem.eventId!!, timelineItem.reactionsState.reactions, "👍️"))
    }

    @Test
    fun `clicking on more reaction emits the expected Event`() {
        val eventsRecorder = EventsRecorder<CustomReactionEvents>()
        val state = aMessagesState(
            customReactionState = aCustomReactionState(
                eventSink = eventsRecorder,
            ),
        )
        val timelineItem = state.timelineState.timelineItems.first() as TimelineItem.Event
        rule.setMessagesView(
            state = state,
        )
        val moreReactionContentDescription = rule.activity.getString(R.string.screen_room_timeline_add_reaction)
        rule.onAllNodesWithContentDescription(moreReactionContentDescription).onFirst().performClick()
        eventsRecorder.assertSingle(CustomReactionEvents.ShowCustomReactionSheet(timelineItem))
    }
}

private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setMessagesView(
    state: MessagesState,
    onBackPressed: () -> Unit = EnsureNeverCalled(),
    onRoomDetailsClicked: () -> Unit = EnsureNeverCalled(),
    onEventClicked: (event: TimelineItem.Event) -> Boolean = EnsureNeverCalledWithParamAndResult(),
    onUserDataClicked: (UserId) -> Unit = EnsureNeverCalledWithParam(),
    onPreviewAttachments: (ImmutableList<Attachment>) -> Unit = EnsureNeverCalledWithParam(),
    onSendLocationClicked: () -> Unit = EnsureNeverCalled(),
    onCreatePollClicked: () -> Unit = EnsureNeverCalled(),
    onJoinCallClicked: () -> Unit = EnsureNeverCalled(),
) {
    setContent {
        // Cannot use the RichTextEditor, so simulate a LocalInspectionMode
        CompositionLocalProvider(LocalInspectionMode provides true) {
            MessagesView(
                state = state,
                onBackPressed = onBackPressed,
                onRoomDetailsClicked = onRoomDetailsClicked,
                onEventClicked = onEventClicked,
                onUserDataClicked = onUserDataClicked,
                onPreviewAttachments = onPreviewAttachments,
                onSendLocationClicked = onSendLocationClicked,
                onCreatePollClicked = onCreatePollClicked,
                onJoinCallClicked = onJoinCallClicked,
            )
        }
    }
}

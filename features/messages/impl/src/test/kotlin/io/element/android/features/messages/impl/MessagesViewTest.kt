/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
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
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.text.AnnotatedString
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.emojibasebindings.Emoji
import io.element.android.emojibasebindings.EmojibaseCategory
import io.element.android.emojibasebindings.EmojibaseStore
import io.element.android.features.messages.impl.actionlist.ActionListEvents
import io.element.android.features.messages.impl.actionlist.ActionListState
import io.element.android.features.messages.impl.actionlist.anActionListState
import io.element.android.features.messages.impl.actionlist.model.TimelineItemAction
import io.element.android.features.messages.impl.crypto.sendfailure.VerifiedUserSendFailure
import io.element.android.features.messages.impl.crypto.sendfailure.resolve.aChangedIdentitySendFailure
import io.element.android.features.messages.impl.messagecomposer.aMessageComposerState
import io.element.android.features.messages.impl.pinned.banner.PinnedMessagesBannerItem
import io.element.android.features.messages.impl.pinned.banner.aLoadedPinnedMessagesBannerState
import io.element.android.features.messages.impl.timeline.FOCUS_ON_PINNED_EVENT_DEBOUNCE_DURATION_IN_MILLIS
import io.element.android.features.messages.impl.timeline.TimelineEvents
import io.element.android.features.messages.impl.timeline.aTimelineItemEvent
import io.element.android.features.messages.impl.timeline.aTimelineItemList
import io.element.android.features.messages.impl.timeline.aTimelineItemReadReceipts
import io.element.android.features.messages.impl.timeline.aTimelineRoomInfo
import io.element.android.features.messages.impl.timeline.aTimelineState
import io.element.android.features.messages.impl.timeline.components.customreaction.CustomReactionEvents
import io.element.android.features.messages.impl.timeline.components.customreaction.CustomReactionState
import io.element.android.features.messages.impl.timeline.components.reactionsummary.ReactionSummaryEvents
import io.element.android.features.messages.impl.timeline.components.receipt.aReadReceiptData
import io.element.android.features.messages.impl.timeline.components.receipt.bottomsheet.ReadReceiptBottomSheetEvents
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemTextContent
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.tombstone.SuccessorRoom
import io.element.android.libraries.matrix.api.timeline.item.event.getAvatarUrl
import io.element.android.libraries.matrix.api.timeline.item.event.getDisplayName
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.testtags.TestTags
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureCalledOnceWithTwoParamsAndResult
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EnsureNeverCalledWithParam
import io.element.android.tests.testutils.EnsureNeverCalledWithTwoParams
import io.element.android.tests.testutils.EnsureNeverCalledWithTwoParamsAndResult
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.assertNoNodeWithText
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.pressBack
import io.element.android.tests.testutils.setSafeContent
import kotlinx.collections.immutable.persistentListOf
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import kotlin.time.Duration.Companion.milliseconds

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
                onBackClick = callback,
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
                onRoomDetailsClick = callback,
            )
            rule.onNodeWithText(state.roomName.orEmpty(), useUnmergedTree = true).performClick()
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
                onJoinCallClick = callback,
            )
            val joinCallContentDescription = rule.activity.getString(CommonStrings.a11y_start_call)
            rule.onNodeWithContentDescription(joinCallContentDescription).performClick()
        }
    }

    @Test
    fun `clicking on an Event invoke expected callback`() {
        val eventsRecorder = EventsRecorder<MessagesEvents>(expectEvents = false)
        val state = aMessagesState(
            timelineState = aTimelineState(
                timelineItems = aTimelineItemList(aTimelineItemTextContent()),
            ),
            eventSink = eventsRecorder
        )
        val timelineItem = state.timelineState.timelineItems.first()
        val callback = EnsureCalledOnceWithTwoParamsAndResult(
            expectedParam1 = true,
            expectedParam2 = timelineItem,
            result = true,
        )
        rule.setMessagesView(
            state = state,
            onEventClick = callback,
        )
        // Cannot perform click on "Text", it's not detected. Use tag instead
        rule.onAllNodesWithTag(TestTags.messageBubble.value).onFirst().performClick()
        callback.assertSuccess()
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
        userCanPinEvent: Boolean = false,
    ) {
        val eventsRecorder = EventsRecorder<ActionListEvents>()
        val state = aMessagesState(
            actionListState = anActionListState(
                eventSink = eventsRecorder
            ),
            userEventPermissions = UserEventPermissions(
                canSendMessage = userHasPermissionToSendMessage,
                canRedactOwn = userHasPermissionToRedactOwn,
                canRedactOther = userHasPermissionToRedactOther,
                canSendReaction = userHasPermissionToSendReaction,
                canPinUnpin = userCanPinEvent,
            ),
            timelineState = aTimelineState(
                timelineItems = aTimelineItemList(aTimelineItemTextContent()),
            ),
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
                userEventPermissions = state.userEventPermissions,
            )
        )
    }

    @Test
    @Config(qualifiers = "h1024dp")
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
        rule.onNodeWithTag(TestTags.messageReadReceipts.value, useUnmergedTree = true).performClick()
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
        val canBeRepliedEvent = aTimelineItemEvent(canBeRepliedTo = true)
        val cannotBeRepliedEvent = aTimelineItemEvent(canBeRepliedTo = false)
        val state = aMessagesState(
            timelineState = aTimelineState(
                timelineItems = persistentListOf(canBeRepliedEvent, cannotBeRepliedEvent),
                timelineRoomInfo = aTimelineRoomInfo(
                    userHasPermissionToSendMessage = userHasPermissionToSendMessage
                ),
            ),
            eventSink = eventsRecorder,
        )
        rule.setMessagesView(
            state = state,
        )
        rule.onAllNodesWithTag(TestTags.messageBubble.value).apply {
            onFirst().performTouchInput { swipeRight(endX = 200f) }
            onLast().performTouchInput { swipeRight(endX = 200f) }
        }
        if (userHasPermissionToSendMessage) {
            eventsRecorder.assertSingle(MessagesEvents.HandleAction(TimelineItemAction.Reply, canBeRepliedEvent))
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
                onSendLocationClick = callback,
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
                onCreatePollClick = callback,
            )
            // Then click on the poll action
            rule.clickOn(R.string.screen_room_attachment_source_poll)
        }
    }

    @Test
    @Config(qualifiers = "h1024dp")
    fun `clicking on the avatar of the sender of an Event emits the expected event`() {
        val eventsRecorder = EventsRecorder<MessagesEvents>()
        val state = aMessagesState(
            eventSink = eventsRecorder
        )
        val timelineEvent = state.timelineState.timelineItems.filterIsInstance<TimelineItem.Event>().first()
        rule.setMessagesView(state = state)
        rule.onNodeWithTag(TestTags.timelineItemSenderAvatar.value, useUnmergedTree = true).performClick()
        eventsRecorder.assertSingle(
            MessagesEvents.OnUserClicked(
                MatrixUser(
                    userId = timelineEvent.senderId,
                    displayName = timelineEvent.senderProfile.getDisplayName(),
                    avatarUrl = timelineEvent.senderProfile.getAvatarUrl()
                )
            )
        )
    }

    @Test
    @Config(qualifiers = "h1024dp")
    fun `clicking on the display name of the sender of an Event emits expected event`() {
        val eventsRecorder = EventsRecorder<MessagesEvents>()
        val state = aMessagesState(eventSink = eventsRecorder)
        val timelineEvent = state.timelineState.timelineItems.filterIsInstance<TimelineItem.Event>().first()
        rule.setMessagesView(state = state)
        rule.onNodeWithTag(TestTags.timelineItemSenderAvatar.value, useUnmergedTree = true).performClick()
        eventsRecorder.assertSingle(
            MessagesEvents.OnUserClicked(
                MatrixUser(
                    userId = timelineEvent.senderId,
                    displayName = timelineEvent.senderProfile.getDisplayName(),
                    avatarUrl = timelineEvent.senderProfile.getAvatarUrl()
                )
            )
        )
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
                    sentTimeFull = "",
                    displayEmojiReactions = true,
                    actions = persistentListOf(TimelineItemAction.Edit),
                    verifiedUserSendFailure = VerifiedUserSendFailure.None,
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
            timelineState = aTimelineState(
                timelineItems = aTimelineItemList(aTimelineItemTextContent()),
            ),
            eventSink = eventsRecorder,
        )
        val timelineItem = state.timelineState.timelineItems.first() as TimelineItem.Event
        rule.setMessagesView(
            state = state,
        )
        rule.onAllNodesWithText(
            text = "👍️",
            useUnmergedTree = true,
        ).onFirst().performClick()
        eventsRecorder.assertSingle(MessagesEvents.ToggleReaction("👍️", timelineItem.eventOrTransactionId))
    }

    @Test
    fun `long clicking on a reaction emits the expected Event`() {
        val eventsRecorder = EventsRecorder<ReactionSummaryEvents>()
        val state = aMessagesState(
            timelineState = aTimelineState(
                timelineItems = aTimelineItemList(aTimelineItemTextContent()),
            ),
            reactionSummaryState = aReactionSummaryState(
                target = null,
                eventSink = eventsRecorder,
            ),
        )
        val timelineItem = state.timelineState.timelineItems.first() as TimelineItem.Event
        rule.setMessagesView(
            state = state,
        )
        rule.onAllNodesWithText(
            text = "👍️",
            useUnmergedTree = true,
        ).onFirst().performTouchInput { longClick() }
        eventsRecorder.assertSingle(ReactionSummaryEvents.ShowReactionSummary(timelineItem.eventId!!, timelineItem.reactionsState.reactions, "👍️"))
    }

    @Test
    fun `clicking on more reaction emits the expected Event`() {
        val eventsRecorder = EventsRecorder<CustomReactionEvents>()
        val state = aMessagesState(
            timelineState = aTimelineState(
                timelineItems = aTimelineItemList(aTimelineItemTextContent()),
            ),
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

    @Test
    fun `clicking on more reaction from action list emits the expected Event`() {
        val eventsRecorder = EventsRecorder<CustomReactionEvents>()
        val state = aMessagesState(
            timelineState = aTimelineState(
                timelineItems = aTimelineItemList(aTimelineItemTextContent()),
            ),
        )
        val timelineItem = state.timelineState.timelineItems.first() as TimelineItem.Event
        val stateWithActionListState = state.copy(
            actionListState = anActionListState(
                target = ActionListState.Target.Success(
                    event = timelineItem,
                    sentTimeFull = "",
                    displayEmojiReactions = true,
                    verifiedUserSendFailure = VerifiedUserSendFailure.None,
                    actions = persistentListOf(TimelineItemAction.Edit),
                ),
            ),
            customReactionState = aCustomReactionState(
                eventSink = eventsRecorder
            ),
        )
        rule.setMessagesView(
            state = stateWithActionListState,
        )
        val moreReactionContentDescription = rule.activity.getString(CommonStrings.a11y_react_with_other_emojis)
        rule.onNodeWithContentDescription(moreReactionContentDescription).performClick()
        // Give time for the close animation to complete
        rule.mainClock.advanceTimeBy(milliseconds = 1_000)
        eventsRecorder.assertSingle(CustomReactionEvents.ShowCustomReactionSheet(timelineItem))
    }

    @Test
    fun `clicking on verified user send failure from action list emits the expected Event`() {
        val eventsRecorder = EventsRecorder<TimelineEvents>()
        val state = aMessagesState()
        val timelineItem = state.timelineState.timelineItems.first() as TimelineItem.Event
        val stateWithActionListState = state.copy(
            actionListState = anActionListState(
                target = ActionListState.Target.Success(
                    event = timelineItem,
                    sentTimeFull = "",
                    displayEmojiReactions = true,
                    verifiedUserSendFailure = aChangedIdentitySendFailure(),
                    actions = persistentListOf(),
                ),
            ),
            timelineState = aTimelineState(eventSink = eventsRecorder)
        )
        rule.setMessagesView(
            state = stateWithActionListState,
        )
        val verifiedUserSendFailure = rule.activity.getString(CommonStrings.screen_timeline_item_menu_send_failure_changed_identity, "Alice")
        rule.onNodeWithText(verifiedUserSendFailure).performClick()
        // Give time for the close animation to complete
        rule.mainClock.advanceTimeBy(milliseconds = 1_000)
        eventsRecorder.assertSingle(TimelineEvents.ComputeVerifiedUserSendFailure(timelineItem))
    }

    @Test
    fun `clicking on a custom emoji emits the expected Events`() {
        val aUnicode = "🙈"
        val customReactionStateEventsRecorder = EventsRecorder<CustomReactionEvents>()
        val eventsRecorder = EventsRecorder<MessagesEvents>()
        val state = aMessagesState(
            eventSink = eventsRecorder,
        )
        val timelineItem = state.timelineState.timelineItems.first() as TimelineItem.Event
        val stateWithCustomReactionState = state.copy(
            customReactionState = aCustomReactionState(
                target = CustomReactionState.Target.Success(
                    event = timelineItem,
                    emojibaseStore = EmojibaseStore(
                        categories = mapOf(
                            EmojibaseCategory.People to listOf(
                                Emoji(
                                    hexcode = "",
                                    label = "",
                                    tags = emptyList(),
                                    shortcodes = emptyList(),
                                    unicode = aUnicode,
                                    skins = null,
                                )
                            )
                        )
                    ),
                ),
                eventSink = customReactionStateEventsRecorder
            ),
        )
        rule.setMessagesView(
            state = stateWithCustomReactionState,
        )
        rule.onNodeWithText(aUnicode, useUnmergedTree = true).performClick()
        // Give time for the close animation to complete
        rule.mainClock.advanceTimeBy(milliseconds = 1_000)
        customReactionStateEventsRecorder.assertSingle(CustomReactionEvents.DismissCustomReactionSheet)
        eventsRecorder.assertSingle(MessagesEvents.ToggleReaction(aUnicode, timelineItem.eventOrTransactionId))
    }

    @Test
    fun `clicking on pinned messages banner emits the expected Event`() {
        val eventsRecorder = EventsRecorder<TimelineEvents>()
        val state = aMessagesState(
            timelineState = aTimelineState(eventSink = eventsRecorder),
            pinnedMessagesBannerState = aLoadedPinnedMessagesBannerState(
                knownPinnedMessagesCount = 2,
                currentPinnedMessageIndex = 0,
                currentPinnedMessage = PinnedMessagesBannerItem(
                    eventId = AN_EVENT_ID,
                    formatted = AnnotatedString("This is a pinned message")
                ),
            ),
        )
        rule.setMessagesView(state = state)
        rule.onNodeWithText("This is a pinned message").performClick()
        eventsRecorder.assertSingle(TimelineEvents.FocusOnEvent(AN_EVENT_ID, debounce = FOCUS_ON_PINNED_EVENT_DEBOUNCE_DURATION_IN_MILLIS.milliseconds))
    }

    @Test
    fun `clicking on successor room button emits expected event`() {
        val eventsRecorder = EventsRecorder<TimelineEvents>()
        val successorRoomId = RoomId("!successor:server.org")
        val state = aMessagesState(
            successorRoom = SuccessorRoom(
                roomId = successorRoomId,
                reason = "This room has been upgraded"
            ),
            timelineState = aTimelineState(eventSink = eventsRecorder)
        )
        rule.setMessagesView(state = state)
        val text = rule.activity.getString(R.string.screen_room_timeline_tombstoned_room_action)
        // The bottomsheet subcompose seems to make the node to appear twice
        rule.onAllNodesWithText(text).onFirst().performClick()
        eventsRecorder.assertSingle(TimelineEvents.NavigateToRoom(successorRoomId))
    }

    @Test
    fun `no banner shown when there is no successor room`() {
        val eventsRecorder = EventsRecorder<MessagesEvents>(expectEvents = false)
        val state = aMessagesState(
            successorRoom = null,
            eventSink = eventsRecorder
        )
        rule.setMessagesView(state = state)
        rule.assertNoNodeWithText(R.string.screen_room_timeline_tombstoned_room_message)
        rule.assertNoNodeWithText(R.string.screen_room_timeline_tombstoned_room_action)
    }
}

private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setMessagesView(
    state: MessagesState,
    onBackClick: () -> Unit = EnsureNeverCalled(),
    onRoomDetailsClick: () -> Unit = EnsureNeverCalled(),
    onEventClick: (isLive: Boolean, event: TimelineItem.Event) -> Boolean = EnsureNeverCalledWithTwoParamsAndResult(),
    onUserDataClick: (UserId) -> Unit = EnsureNeverCalledWithParam(),
    onLinkClick: (String, Boolean) -> Unit = EnsureNeverCalledWithTwoParams(),
    onSendLocationClick: () -> Unit = EnsureNeverCalled(),
    onCreatePollClick: () -> Unit = EnsureNeverCalled(),
    onJoinCallClick: () -> Unit = EnsureNeverCalled(),
    onViewAllPinnedMessagesClick: () -> Unit = EnsureNeverCalled(),
) {
    setSafeContent {
        // Cannot use the RichTextEditor, so simulate a LocalInspectionMode
        CompositionLocalProvider(LocalInspectionMode provides true) {
            MessagesView(
                state = state,
                onBackClick = onBackClick,
                onRoomDetailsClick = onRoomDetailsClick,
                onEventContentClick = onEventClick,
                onUserDataClick = onUserDataClick,
                onLinkClick = onLinkClick,
                onSendLocationClick = onSendLocationClick,
                onCreatePollClick = onCreatePollClick,
                onJoinCallClick = onJoinCallClick,
                onViewAllPinnedMessagesClick = onViewAllPinnedMessagesClick,
                knockRequestsBannerView = {},
            )
        }
    }
}

/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package io.element.android.features.messages.impl.timeline

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import io.element.android.features.messages.impl.timeline.components.MessageShieldData
import io.element.android.features.messages.impl.timeline.components.aCriticalShield
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemTextContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemUnknownContent
import io.element.android.features.messages.impl.timeline.model.virtual.TimelineItemLoadingIndicatorModel
import io.element.android.features.messages.impl.timeline.protection.TimelineProtectionState
import io.element.android.features.messages.impl.timeline.protection.aTimelineProtectionState
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UniqueId
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.api.timeline.item.event.MessageContent
import io.element.android.libraries.matrix.api.timeline.item.event.MessageShield
import io.element.android.libraries.matrix.api.timeline.item.event.TextMessageType
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.ui.messages.reply.InReplyToDetails
import io.element.android.libraries.matrix.ui.messages.reply.aProfileDetailsReady
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalledWithParam
import io.element.android.tests.testutils.EnsureNeverCalledWithTwoParams
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.robolectric.RobolectricTest
import io.element.android.tests.testutils.setSafeContent
import io.element.android.wysiwyg.link.Link
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import org.junit.Ignore
import org.junit.Test

class TimelineViewTest : RobolectricTest() {
    @Test
    fun `reaching the end of the timeline with more events to load emits a LoadMore event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<TimelineEvent>()
        setTimelineView(
            state = aTimelineState(
                timelineItems = persistentListOf<TimelineItem>(
                    TimelineItem.Virtual(
                        id = UniqueId("backward_pagination"),
                        model = TimelineItemLoadingIndicatorModel(Timeline.PaginationDirection.BACKWARDS, 0)
                    ),
                ),
                eventSink = eventsRecorder,
            ),
        )
        eventsRecorder.assertSingle(TimelineEvent.LoadMore(Timeline.PaginationDirection.BACKWARDS))
    }

    @Test
    fun `reaching the end of the timeline does not send a LoadMore event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<TimelineEvent>()
        setTimelineView(
            state = aTimelineState(
                timelineItems = persistentListOf(aTimelineItemEvent(content = aTimelineItemTextContent())),
                eventSink = eventsRecorder,
            ),
        )
        eventsRecorder.assertSingle(TimelineEvent.OnScrollFinished(firstIndex = 0))
    }

    @Test
    fun `scroll to bottom on live timeline does not emit the Event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<TimelineEvent>()
        setTimelineView(
            state = aTimelineState(
                timelineItems = persistentListOf(aTimelineItemEvent(content = aTimelineItemTextContent())),
                isLive = true,
                eventSink = eventsRecorder,
            ),
            forceJumpToBottomVisibility = true,
        )

        eventsRecorder.assertSingle(TimelineEvent.OnScrollFinished(firstIndex = 0))
        eventsRecorder.clear()

        val contentDescription = activity!!.getString(CommonStrings.a11y_jump_to_bottom)
        onNodeWithContentDescription(contentDescription).performClick()
    }

    @Test
    fun `scroll to bottom on detached timeline emits the expected Event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<TimelineEvent>()
        setTimelineView(
            state = aTimelineState(
                timelineItems = persistentListOf(aTimelineItemEvent(content = aTimelineItemTextContent())),
                isLive = false,
                eventSink = eventsRecorder,
            ),
        )

        eventsRecorder.assertSingle(TimelineEvent.OnScrollFinished(firstIndex = 0))
        eventsRecorder.clear()

        val contentDescription = activity!!.getString(CommonStrings.a11y_jump_to_bottom)
        onNodeWithContentDescription(contentDescription).performClick()
        eventsRecorder.assertSingle(TimelineEvent.JumpToLive)
    }

    @Test
    fun `an empty timeline triggers a prefetch`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<TimelineEvent>()
        setTimelineView(
            state = aTimelineState(
                timelineItems = persistentListOf(),
                eventSink = eventsRecorder,
            ),
        )

        eventsRecorder.assertSingle(TimelineEvent.LoadMore(Timeline.PaginationDirection.BACKWARDS))
    }

    @Test
    fun `show shield dialog`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<TimelineEvent>()
        setTimelineView(
            state = aTimelineState(
                timelineItems = persistentListOf<TimelineItem>(
                    aTimelineItemEvent(
                        // Do not use a Text because EditorStyledText cannot be used in UI test.
                        content = aTimelineItemTextContent(),
                        messageShield = MessageShield.UnverifiedIdentity(true),
                    ),
                ),
                eventSink = eventsRecorder,
            ),
        )
        val contentDescription = activity!!.getString(CommonStrings.a11y_encryption_details)
        onNodeWithContentDescription(contentDescription).performClick()
        eventsRecorder.assertList(
            listOf(
                TimelineEvent.OnScrollFinished(0),
                TimelineEvent.ShowShieldDialog(MessageShieldData(MessageShield.UnverifiedIdentity(true))),
            )
        )
    }

    @Test
    fun `hide shield dialog`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<TimelineEvent>()
        setTimelineView(
            state = aTimelineState(
                timelineItems = persistentListOf(aTimelineItemEvent(content = aTimelineItemTextContent())),
                isLive = false,
                eventSink = eventsRecorder,
                messageShield = aCriticalShield(),
            ),
        )
        eventsRecorder.assertSingle(TimelineEvent.OnScrollFinished(firstIndex = 0))
        eventsRecorder.clear()

        clickOn(CommonStrings.action_ok)
        eventsRecorder.assertSingle(TimelineEvent.HideShieldDialog)
    }

    @Test
    fun `clicking on a reply to a loaded event emits the expected Event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<TimelineEvent>()
        setTimelineView(
            state = aTimelineState(
                timelineItems = persistentListOf(
                    aTimelineItemEvent(
                        content = aTimelineItemTextContent(),
                        inReplyTo = InReplyToDetails.Ready(
                            eventId = AN_EVENT_ID,
                            senderId = A_USER_ID,
                            senderProfile = aProfileDetailsReady(),
                            eventContent = MessageContent(
                                body = "Replied message",
                                inReplyTo = null,
                                isEdited = false,
                                threadInfo = null,
                                type = TextMessageType("Replied message", null),
                            ),
                            textContent = "Replied message",
                        ),
                    ),
                ),
                eventSink = eventsRecorder,
            ),
        )
        eventsRecorder.clear()

        onAllNodesWithText("Replied message").onFirst().performClick()
        eventsRecorder.assertSingle(TimelineEvent.FocusOnEvent(AN_EVENT_ID))
    }

    @Test
    fun `a reply to an event which could not be loaded renders a placeholder and does not emit any Event when clicked`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<TimelineEvent>()
        setTimelineView(
            state = aTimelineState(
                timelineItems = persistentListOf(
                    aTimelineItemEvent(
                        content = aTimelineItemTextContent(),
                        inReplyTo = InReplyToDetails.Error(eventId = AN_EVENT_ID, message = "Failed to fetch the event"),
                    ),
                ),
                eventSink = eventsRecorder,
            ),
            onMessageClick = {},
        )
        eventsRecorder.assertSingle(TimelineEvent.OnScrollFinished(firstIndex = 0))
        eventsRecorder.clear()

        onNodeWithText("Failed to fetch the event").assertDoesNotExist()
        onNodeWithText(activity!!.getString(CommonStrings.error_message_not_found)).assertIsDisplayed().performClick()
        eventsRecorder.assertEmpty()
    }

    @Ignore(
        "performScrollToIndex in compose tests no longer sets LazyListState.isScrollInProgress to true, so the LoadMore event is not emitted." +
            "This needs to be reworked to use a different approach to check the LoadMore event was emitted."
    )
    @Test
    fun `scrolling near to the start of the loaded items triggers a pre-fetch`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<TimelineEvent>()
        val items = List<TimelineItem>(200) {
            aTimelineItemEvent(
                eventId = EventId("\$event_$it"),
                content = aTimelineItemUnknownContent(),
            )
        }.toImmutableList()

        setTimelineView(
            state = aTimelineState(
                timelineItems = items,
                eventSink = eventsRecorder,
                focusedEventIndex = -1,
                isLive = false,
            ),
        )

        onNodeWithTag("timeline").performScrollToIndex(180)

        mainClock.advanceTimeBy(1000)

        eventsRecorder.assertList(
            listOf(
                TimelineEvent.OnScrollFinished(firstIndex = 0),
                TimelineEvent.LoadMore(Timeline.PaginationDirection.BACKWARDS),
            )
        )
    }
}

private fun AndroidComposeUiTest<ComponentActivity>.setTimelineView(
    state: TimelineState,
    timelineProtectionState: TimelineProtectionState = aTimelineProtectionState(),
    onUserDataClick: (MatrixUser) -> Unit = EnsureNeverCalledWithParam(),
    onLinkClick: (Link) -> Unit = EnsureNeverCalledWithParam(),
    onMessageClick: (TimelineItem.Event) -> Unit = EnsureNeverCalledWithParam(),
    onMessageLongClick: (TimelineItem.Event) -> Unit = EnsureNeverCalledWithParam(),
    onSwipeToReply: (TimelineItem.Event) -> Unit = EnsureNeverCalledWithParam(),
    onReactionClick: (emoji: String, TimelineItem.Event) -> Unit = EnsureNeverCalledWithTwoParams(),
    onReactionLongClick: (emoji: String, TimelineItem.Event) -> Unit = EnsureNeverCalledWithTwoParams(),
    onMoreReactionsClick: (TimelineItem.Event) -> Unit = EnsureNeverCalledWithParam(),
    onReadReceiptClick: (TimelineItem.Event) -> Unit = EnsureNeverCalledWithParam(),
    onGalleryItemClick: (TimelineItem.Event, Int) -> Unit = EnsureNeverCalledWithTwoParams(),
    forceJumpToBottomVisibility: Boolean = false,
    onJoinCallClick: (isAudioCall: Boolean) -> Unit = EnsureNeverCalledWithParam(),
) {
    setSafeContent(clearAndroidUiDispatcher = true) {
        TimelineView(
            state = state,
            timelineProtectionState = timelineProtectionState,
            onUserDataClick = onUserDataClick,
            onLinkClick = onLinkClick,
            onContentClick = onMessageClick,
            onMessageLongClick = onMessageLongClick,
            onSwipeToReply = onSwipeToReply,
            onReactionClick = onReactionClick,
            onReactionLongClick = onReactionLongClick,
            onMoreReactionsClick = onMoreReactionsClick,
            onReadReceiptClick = onReadReceiptClick,
            onGalleryItemClick = onGalleryItemClick,
            forceJumpToBottomVisibility = forceJumpToBottomVisibility,
            onJoinCallClick = onJoinCallClick,
        )
    }
}

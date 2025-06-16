/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.features.messages.impl.timeline.components.aCriticalShield
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemImageContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemUnknownContent
import io.element.android.features.messages.impl.timeline.model.virtual.TimelineItemLoadingIndicatorModel
import io.element.android.features.messages.impl.timeline.protection.TimelineProtectionState
import io.element.android.features.messages.impl.timeline.protection.aTimelineProtectionState
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UniqueId
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.api.timeline.item.event.MessageShield
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EnsureNeverCalledWithParam
import io.element.android.tests.testutils.EnsureNeverCalledWithTwoParams
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.setSafeContent
import io.element.android.wysiwyg.link.Link
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
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
                        id = UniqueId("backward_pagination"),
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

    @Test
    fun `show shield dialog`() {
        val eventsRecorder = EventsRecorder<TimelineEvents>()
        rule.setTimelineView(
            state = aTimelineState(
                timelineItems = persistentListOf<TimelineItem>(
                    aTimelineItemEvent(
                        // Do not use a Text because EditorStyledText cannot be used in UI test.
                        content = aTimelineItemImageContent(),
                        messageShield = MessageShield.UnverifiedIdentity(true),
                    ),
                ),
                eventSink = eventsRecorder,
            ),
        )
        val contentDescription = rule.activity.getString(CommonStrings.event_shield_reason_unverified_identity)
        rule.onNodeWithContentDescription(contentDescription).performClick()
        eventsRecorder.assertList(
            listOf(
                TimelineEvents.OnScrollFinished(0),
                TimelineEvents.ShowShieldDialog(MessageShield.UnverifiedIdentity(true)),
            )
        )
    }

    @Test
    fun `hide shield dialog`() {
        val eventsRecorder = EventsRecorder<TimelineEvents>()
        rule.setTimelineView(
            state = aTimelineState(
                isLive = false,
                eventSink = eventsRecorder,
                messageShield = aCriticalShield(),
            ),
        )
        rule.clickOn(CommonStrings.action_ok)
        eventsRecorder.assertSingle(TimelineEvents.HideShieldDialog)
    }

    @Test
    fun `scrolling near to the start of the loaded items triggers a pre-fetch`() {
        val eventsRecorder = EventsRecorder<TimelineEvents>()
        val items = List<TimelineItem>(200) {
            aTimelineItemEvent(
                eventId = EventId("\$event_$it"),
                content = aTimelineItemUnknownContent(),
            )
        }.toPersistentList()

        rule.setTimelineView(
            state = aTimelineState(
                timelineItems = items,
                eventSink = eventsRecorder,
                focusedEventIndex = -1,
                isLive = false,
            ),
        )

        rule.onNodeWithTag("timeline").performScrollToIndex(180)

        rule.mainClock.advanceTimeBy(1000)

        eventsRecorder.assertList(
            listOf(
                TimelineEvents.OnScrollFinished(firstIndex = 0),
                TimelineEvents.LoadMore(Timeline.PaginationDirection.BACKWARDS),
            )
        )
    }
}

private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setTimelineView(
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
    onJoinCallClick: () -> Unit = EnsureNeverCalled(),
    forceJumpToBottomVisibility: Boolean = false,
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
            onJoinCallClick = onJoinCallClick,
            forceJumpToBottomVisibility = forceJumpToBottomVisibility,
        )
    }
}

/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.actionlist

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.messages.impl.aUserEventPermissions
import io.element.android.features.messages.impl.actionlist.model.TimelineItemAction
import io.element.android.features.messages.impl.actionlist.model.TimelineItemActionPostProcessor
import io.element.android.features.messages.impl.crypto.sendfailure.VerifiedUserSendFailure
import io.element.android.features.messages.impl.crypto.sendfailure.VerifiedUserSendFailureFactory
import io.element.android.features.messages.impl.fixtures.aMessageEvent
import io.element.android.features.messages.impl.timeline.aTimelineItemEvent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemCallNotifyContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemRedactedContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemTextContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemImageContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemPollContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemStateEventContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemVoiceContent
import io.element.android.features.poll.api.pollcontent.aPollAnswerItemList
import io.element.android.libraries.dateformatter.test.FakeDateFormatter
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.featureflag.test.FakeFeatureFlagService
import io.element.android.libraries.matrix.api.room.BaseRoom
import io.element.android.libraries.matrix.api.timeline.item.event.LocalEventSendState
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_CAPTION
import io.element.android.libraries.matrix.test.A_MESSAGE
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.room.FakeBaseRoom
import io.element.android.libraries.matrix.test.room.aRoomInfo
import io.element.android.libraries.preferences.test.InMemoryAppPreferencesStore
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.test
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@Suppress("LargeClass")
class ActionListPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val presenter = createActionListPresenter(isDeveloperModeEnabled = true, isPinFeatureEnabled = true)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.target).isEqualTo(ActionListState.Target.None)
        }
    }

    @Test
    fun `present - compute for message from me redacted`() = runTest {
        val presenter = createActionListPresenter(isDeveloperModeEnabled = true, isPinFeatureEnabled = true)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val messageEvent = aMessageEvent(isMine = true, isEditable = false, content = TimelineItemRedactedContent)
            initialState.eventSink.invoke(
                ActionListEvents.ComputeForMessage(
                    event = messageEvent,
                    userEventPermissions = aUserEventPermissions(
                        canRedactOwn = false,
                        canRedactOther = false,
                        canSendMessage = true,
                        canSendReaction = true,
                        canPinUnpin = true,
                    )
                )
            )
            val successState = awaitItem()
            assertThat(successState.target).isEqualTo(
                ActionListState.Target.Success(
                    event = messageEvent,
                    sentTimeFull = "0 Full true",
                    displayEmojiReactions = false,
                    verifiedUserSendFailure = VerifiedUserSendFailure.None,
                    actions = persistentListOf(
                        TimelineItemAction.ViewSource,
                    )
                )
            )
            initialState.eventSink.invoke(ActionListEvents.Clear)
            assertThat(awaitItem().target).isEqualTo(ActionListState.Target.None)
        }
    }

    @Test
    fun `present - compute for message from others redacted`() = runTest {
        val presenter = createActionListPresenter(isDeveloperModeEnabled = true, isPinFeatureEnabled = true)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val messageEvent = aMessageEvent(
                isMine = false,
                isEditable = false,
                content = TimelineItemRedactedContent
            )
            initialState.eventSink.invoke(
                ActionListEvents.ComputeForMessage(
                    event = messageEvent,
                    userEventPermissions = aUserEventPermissions(
                        canRedactOwn = false,
                        canRedactOther = false,
                        canSendMessage = true,
                        canSendReaction = true,
                        canPinUnpin = true,
                    )
                )
            )
            val successState = awaitItem()
            assertThat(successState.target).isEqualTo(
                ActionListState.Target.Success(
                    event = messageEvent,
                    sentTimeFull = "0 Full true",
                    displayEmojiReactions = false,
                    verifiedUserSendFailure = VerifiedUserSendFailure.None,
                    actions = persistentListOf(
                        TimelineItemAction.ViewSource,
                    )
                )
            )
            initialState.eventSink.invoke(ActionListEvents.Clear)
            assertThat(awaitItem().target).isEqualTo(ActionListState.Target.None)
        }
    }

    @Test
    fun `present - compute for others message`() = runTest {
        val presenter = createActionListPresenter(isDeveloperModeEnabled = true, isPinFeatureEnabled = true)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val messageEvent = aMessageEvent(
                isMine = false,
                isEditable = false,
                content = TimelineItemTextContent(body = A_MESSAGE, htmlDocument = null, isEdited = false, formattedBody = A_MESSAGE)
            )
            initialState.eventSink.invoke(
                ActionListEvents.ComputeForMessage(
                    event = messageEvent,
                    userEventPermissions = aUserEventPermissions(
                        canRedactOwn = false,
                        canRedactOther = false,
                        canSendMessage = true,
                        canSendReaction = true,
                        canPinUnpin = true,
                    )
                )
            )
            val successState = awaitItem()
            assertThat(successState.target).isEqualTo(
                ActionListState.Target.Success(
                    event = messageEvent,
                    sentTimeFull = "0 Full true",
                    displayEmojiReactions = true,
                    verifiedUserSendFailure = VerifiedUserSendFailure.None,
                    actions = persistentListOf(
                        TimelineItemAction.Reply,
                        TimelineItemAction.Forward,
                        TimelineItemAction.CopyLink,
                        TimelineItemAction.Pin,
                        TimelineItemAction.CopyText,
                        TimelineItemAction.ViewSource,
                        TimelineItemAction.ReportContent,
                    )
                )
            )
            initialState.eventSink.invoke(ActionListEvents.Clear)
            assertThat(awaitItem().target).isEqualTo(ActionListState.Target.None)
        }
    }

    @Test
    fun `present - compute for others message in a thread`() = runTest {
        val presenter = createActionListPresenter(isDeveloperModeEnabled = true, isPinFeatureEnabled = true)
        presenter.test {
            val initialState = awaitItem()
            val messageEvent = aMessageEvent(
                isMine = false,
                isEditable = false,
                isThreaded = true,
                content = TimelineItemTextContent(body = A_MESSAGE, htmlDocument = null, isEdited = false, formattedBody = A_MESSAGE)
            )
            initialState.eventSink.invoke(
                ActionListEvents.ComputeForMessage(
                    event = messageEvent,
                    userEventPermissions = aUserEventPermissions(
                        canRedactOwn = false,
                        canRedactOther = false,
                        canSendMessage = true,
                        canSendReaction = true,
                        canPinUnpin = true,
                    )
                )
            )
            val successState = awaitItem()
            assertThat(successState.target).isEqualTo(
                ActionListState.Target.Success(
                    event = messageEvent,
                    sentTimeFull = "0 Full true",
                    displayEmojiReactions = true,
                    verifiedUserSendFailure = VerifiedUserSendFailure.None,
                    actions = persistentListOf(
                        TimelineItemAction.ReplyInThread,
                        TimelineItemAction.Forward,
                        TimelineItemAction.CopyLink,
                        TimelineItemAction.Pin,
                        TimelineItemAction.CopyText,
                        TimelineItemAction.ViewSource,
                        TimelineItemAction.ReportContent,
                    )
                )
            )
            initialState.eventSink.invoke(ActionListEvents.Clear)
            assertThat(awaitItem().target).isEqualTo(ActionListState.Target.None)
        }
    }

    @Test
    fun `present - compute for others message cannot sent message`() = runTest {
        val presenter = createActionListPresenter(isDeveloperModeEnabled = true, isPinFeatureEnabled = true)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val messageEvent = aMessageEvent(
                isMine = false,
                isEditable = false,
                content = TimelineItemTextContent(body = A_MESSAGE, htmlDocument = null, isEdited = false, formattedBody = A_MESSAGE)
            )
            initialState.eventSink.invoke(
                ActionListEvents.ComputeForMessage(
                    event = messageEvent,
                    userEventPermissions = aUserEventPermissions(
                        canRedactOwn = true,
                        canRedactOther = false,
                        canSendMessage = false,
                        canSendReaction = true,
                        canPinUnpin = true,
                    )
                )
            )
            val successState = awaitItem()
            assertThat(successState.target).isEqualTo(
                ActionListState.Target.Success(
                    event = messageEvent,
                    sentTimeFull = "0 Full true",
                    displayEmojiReactions = true,
                    verifiedUserSendFailure = VerifiedUserSendFailure.None,
                    actions = persistentListOf(
                        TimelineItemAction.Forward,
                        TimelineItemAction.CopyLink,
                        TimelineItemAction.Pin,
                        TimelineItemAction.CopyText,
                        TimelineItemAction.ViewSource,
                        TimelineItemAction.ReportContent,
                    )
                )
            )
            initialState.eventSink.invoke(ActionListEvents.Clear)
            assertThat(awaitItem().target).isEqualTo(ActionListState.Target.None)
        }
    }

    @Test
    fun `present - compute for others message and can redact`() = runTest {
        val presenter = createActionListPresenter(isDeveloperModeEnabled = true, isPinFeatureEnabled = true)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val messageEvent = aMessageEvent(
                isMine = false,
                isEditable = false,
                content = TimelineItemTextContent(body = A_MESSAGE, htmlDocument = null, isEdited = false, formattedBody = A_MESSAGE)
            )
            initialState.eventSink.invoke(
                ActionListEvents.ComputeForMessage(
                    event = messageEvent,
                    userEventPermissions = aUserEventPermissions(
                        canRedactOwn = false,
                        canRedactOther = true,
                        canSendMessage = true,
                        canSendReaction = true,
                        canPinUnpin = true,
                    )
                )
            )
            val successState = awaitItem()
            assertThat(successState.target).isEqualTo(
                ActionListState.Target.Success(
                    event = messageEvent,
                    sentTimeFull = "0 Full true",
                    displayEmojiReactions = true,
                    verifiedUserSendFailure = VerifiedUserSendFailure.None,
                    actions = persistentListOf(
                        TimelineItemAction.Reply,
                        TimelineItemAction.Forward,
                        TimelineItemAction.CopyLink,
                        TimelineItemAction.Pin,
                        TimelineItemAction.CopyText,
                        TimelineItemAction.ViewSource,
                        TimelineItemAction.ReportContent,
                        TimelineItemAction.Redact,
                    )
                )
            )
            initialState.eventSink.invoke(ActionListEvents.Clear)
            assertThat(awaitItem().target).isEqualTo(ActionListState.Target.None)
        }
    }

    @Test
    fun `present - compute for others message and cannot send reaction`() = runTest {
        val presenter = createActionListPresenter(isDeveloperModeEnabled = true, isPinFeatureEnabled = true)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val messageEvent = aMessageEvent(
                isMine = false,
                isEditable = false,
                content = TimelineItemTextContent(body = A_MESSAGE, htmlDocument = null, isEdited = false, formattedBody = A_MESSAGE)
            )
            initialState.eventSink.invoke(
                ActionListEvents.ComputeForMessage(
                    event = messageEvent,
                    userEventPermissions = aUserEventPermissions(
                        canRedactOwn = false,
                        canRedactOther = true,
                        canSendMessage = true,
                        canSendReaction = false,
                        canPinUnpin = true,
                    )
                )
            )
            val successState = awaitItem()
            assertThat(successState.target).isEqualTo(
                ActionListState.Target.Success(
                    event = messageEvent,
                    sentTimeFull = "0 Full true",
                    displayEmojiReactions = false,
                    verifiedUserSendFailure = VerifiedUserSendFailure.None,
                    actions = persistentListOf(
                        TimelineItemAction.Reply,
                        TimelineItemAction.Forward,
                        TimelineItemAction.CopyLink,
                        TimelineItemAction.Pin,
                        TimelineItemAction.CopyText,
                        TimelineItemAction.ViewSource,
                        TimelineItemAction.ReportContent,
                        TimelineItemAction.Redact,
                    )
                )
            )
            initialState.eventSink.invoke(ActionListEvents.Clear)
            assertThat(awaitItem().target).isEqualTo(ActionListState.Target.None)
        }
    }

    @Test
    fun `present - compute for my message`() = runTest {
        val presenter = createActionListPresenter(isDeveloperModeEnabled = true, isPinFeatureEnabled = true)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val messageEvent = aMessageEvent(
                isMine = true,
                content = TimelineItemTextContent(body = A_MESSAGE, htmlDocument = null, isEdited = false, formattedBody = A_MESSAGE)
            )
            initialState.eventSink.invoke(
                ActionListEvents.ComputeForMessage(
                    event = messageEvent,
                    userEventPermissions = aUserEventPermissions(
                        canRedactOwn = true,
                        canRedactOther = false,
                        canSendMessage = true,
                        canSendReaction = true,
                        canPinUnpin = true,
                    )
                )
            )
            val successState = awaitItem()
            assertThat(successState.target).isEqualTo(
                ActionListState.Target.Success(
                    event = messageEvent,
                    sentTimeFull = "0 Full true",
                    displayEmojiReactions = true,
                    verifiedUserSendFailure = VerifiedUserSendFailure.None,
                    actions = persistentListOf(
                        TimelineItemAction.Reply,
                        TimelineItemAction.Forward,
                        TimelineItemAction.Edit,
                        TimelineItemAction.CopyLink,
                        TimelineItemAction.Pin,
                        TimelineItemAction.CopyText,
                        TimelineItemAction.ViewSource,
                        TimelineItemAction.Redact,
                    )
                )
            )
            initialState.eventSink.invoke(ActionListEvents.Clear)
            assertThat(awaitItem().target).isEqualTo(ActionListState.Target.None)
        }
    }

    @Test
    fun `present - compute for my message in a thread`() = runTest {
        val presenter = createActionListPresenter(isDeveloperModeEnabled = true, isPinFeatureEnabled = true)
        presenter.test {
            val initialState = awaitItem()
            val messageEvent = aMessageEvent(
                isMine = true,
                isThreaded = true,
                content = TimelineItemTextContent(body = A_MESSAGE, htmlDocument = null, isEdited = false, formattedBody = A_MESSAGE)
            )
            initialState.eventSink.invoke(
                ActionListEvents.ComputeForMessage(
                    event = messageEvent,
                    userEventPermissions = aUserEventPermissions(
                        canRedactOwn = true,
                        canRedactOther = false,
                        canSendMessage = true,
                        canSendReaction = true,
                        canPinUnpin = true,
                    )
                )
            )
            val successState = awaitItem()
            assertThat(successState.target).isEqualTo(
                ActionListState.Target.Success(
                    event = messageEvent,
                    sentTimeFull = "0 Full true",
                    displayEmojiReactions = true,
                    verifiedUserSendFailure = VerifiedUserSendFailure.None,
                    actions = persistentListOf(
                        TimelineItemAction.ReplyInThread,
                        TimelineItemAction.Forward,
                        TimelineItemAction.Edit,
                        TimelineItemAction.CopyLink,
                        TimelineItemAction.Pin,
                        TimelineItemAction.CopyText,
                        TimelineItemAction.ViewSource,
                        TimelineItemAction.Redact,
                    )
                )
            )
            initialState.eventSink.invoke(ActionListEvents.Clear)
            assertThat(awaitItem().target).isEqualTo(ActionListState.Target.None)
        }
    }

    @Test
    fun `present - compute for my message cannot redact`() = runTest {
        val presenter = createActionListPresenter(isDeveloperModeEnabled = true, isPinFeatureEnabled = true)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val messageEvent = aMessageEvent(
                isMine = true,
                content = TimelineItemTextContent(body = A_MESSAGE, htmlDocument = null, isEdited = false, formattedBody = A_MESSAGE)
            )
            initialState.eventSink.invoke(
                ActionListEvents.ComputeForMessage(
                    event = messageEvent,
                    userEventPermissions = aUserEventPermissions(
                        canRedactOwn = false,
                        canRedactOther = false,
                        canSendMessage = true,
                        canSendReaction = true,
                        canPinUnpin = true,
                    )
                )
            )
            val successState = awaitItem()
            assertThat(successState.target).isEqualTo(
                ActionListState.Target.Success(
                    event = messageEvent,
                    sentTimeFull = "0 Full true",
                    displayEmojiReactions = true,
                    verifiedUserSendFailure = VerifiedUserSendFailure.None,
                    actions = persistentListOf(
                        TimelineItemAction.Reply,
                        TimelineItemAction.Forward,
                        TimelineItemAction.Edit,
                        TimelineItemAction.CopyLink,
                        TimelineItemAction.Pin,
                        TimelineItemAction.CopyText,
                        TimelineItemAction.ViewSource,
                    )
                )
            )
            initialState.eventSink.invoke(ActionListEvents.Clear)
            assertThat(awaitItem().target).isEqualTo(ActionListState.Target.None)
        }
    }

    @Test
    fun `present - compute for my message no permission`() = runTest {
        val presenter = createActionListPresenter(isDeveloperModeEnabled = true, isPinFeatureEnabled = true)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val messageEvent = aMessageEvent(
                isMine = true,
                content = TimelineItemTextContent(body = A_MESSAGE, htmlDocument = null, isEdited = false, formattedBody = A_MESSAGE)
            )
            initialState.eventSink.invoke(
                ActionListEvents.ComputeForMessage(
                    event = messageEvent,
                    userEventPermissions = aUserEventPermissions(
                        canRedactOwn = false,
                        canRedactOther = false,
                        canSendMessage = false,
                        canSendReaction = false,
                        canPinUnpin = false,
                    )
                )
            )
            val successState = awaitItem()
            assertThat(successState.target).isEqualTo(
                ActionListState.Target.Success(
                    event = messageEvent,
                    sentTimeFull = "0 Full true",
                    displayEmojiReactions = false,
                    verifiedUserSendFailure = VerifiedUserSendFailure.None,
                    actions = persistentListOf(
                        TimelineItemAction.Forward,
                        TimelineItemAction.CopyLink,
                        TimelineItemAction.CopyText,
                        TimelineItemAction.ViewSource,
                    )
                )
            )
            initialState.eventSink.invoke(ActionListEvents.Clear)
            assertThat(awaitItem().target).isEqualTo(ActionListState.Target.None)
        }
    }

    @Test
    fun `present - compute for a media item`() = runTest {
        val presenter = createActionListPresenter(isDeveloperModeEnabled = true, isPinFeatureEnabled = true)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val messageEvent = aMessageEvent(
                isMine = true,
                isEditable = true,
                content = aTimelineItemImageContent(),
            )
            initialState.eventSink.invoke(
                ActionListEvents.ComputeForMessage(
                    event = messageEvent,
                    userEventPermissions = aUserEventPermissions(
                        canRedactOwn = true,
                        canRedactOther = false,
                        canSendMessage = true,
                        canSendReaction = true,
                        canPinUnpin = true,
                    ),
                )
            )
            val successState = awaitItem()
            assertThat(successState.target).isEqualTo(
                ActionListState.Target.Success(
                    event = messageEvent,
                    sentTimeFull = "0 Full true",
                    displayEmojiReactions = true,
                    verifiedUserSendFailure = VerifiedUserSendFailure.None,
                    actions = persistentListOf(
                        TimelineItemAction.Reply,
                        TimelineItemAction.Forward,
                        TimelineItemAction.AddCaption,
                        TimelineItemAction.CopyLink,
                        TimelineItemAction.Pin,
                        TimelineItemAction.ViewSource,
                        TimelineItemAction.Redact,
                    )
                )
            )
            initialState.eventSink.invoke(ActionListEvents.Clear)
            assertThat(awaitItem().target).isEqualTo(ActionListState.Target.None)
        }
    }

    @Test
    fun `present - compute for a media item - caption disabled`() = runTest {
        val presenter = createActionListPresenter(
            isDeveloperModeEnabled = true,
            isPinFeatureEnabled = true,
            allowCaption = false,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val messageEvent = aMessageEvent(
                isMine = true,
                isEditable = true,
                content = aTimelineItemImageContent(),
            )
            initialState.eventSink.invoke(
                ActionListEvents.ComputeForMessage(
                    event = messageEvent,
                    userEventPermissions = aUserEventPermissions(
                        canRedactOwn = true,
                        canRedactOther = false,
                        canSendMessage = true,
                        canSendReaction = true,
                        canPinUnpin = true,
                    ),
                )
            )
            val successState = awaitItem()
            assertThat(successState.target).isEqualTo(
                ActionListState.Target.Success(
                    event = messageEvent,
                    sentTimeFull = "0 Full true",
                    displayEmojiReactions = true,
                    verifiedUserSendFailure = VerifiedUserSendFailure.None,
                    actions = persistentListOf(
                        TimelineItemAction.Reply,
                        TimelineItemAction.Forward,
                        // Not here
                        // TimelineItemAction.AddCaption,
                        TimelineItemAction.CopyLink,
                        TimelineItemAction.Pin,
                        TimelineItemAction.ViewSource,
                        TimelineItemAction.Redact,
                    )
                )
            )
            initialState.eventSink.invoke(ActionListEvents.Clear)
            assertThat(awaitItem().target).isEqualTo(ActionListState.Target.None)
        }
    }

    @Test
    fun `present - compute for a media with caption item`() = runTest {
        val presenter = createActionListPresenter(isDeveloperModeEnabled = true, isPinFeatureEnabled = true)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val messageEvent = aMessageEvent(
                isMine = true,
                isEditable = true,
                content = aTimelineItemImageContent(
                    caption = A_CAPTION,
                ),
            )
            initialState.eventSink.invoke(
                ActionListEvents.ComputeForMessage(
                    event = messageEvent,
                    userEventPermissions = aUserEventPermissions(
                        canRedactOwn = true,
                        canRedactOther = false,
                        canSendMessage = true,
                        canSendReaction = true,
                        canPinUnpin = true,
                    ),
                )
            )
            val successState = awaitItem()
            assertThat(successState.target).isEqualTo(
                ActionListState.Target.Success(
                    event = messageEvent,
                    sentTimeFull = "0 Full true",
                    displayEmojiReactions = true,
                    verifiedUserSendFailure = VerifiedUserSendFailure.None,
                    actions = persistentListOf(
                        TimelineItemAction.Reply,
                        TimelineItemAction.Forward,
                        TimelineItemAction.EditCaption,
                        TimelineItemAction.CopyLink,
                        TimelineItemAction.Pin,
                        TimelineItemAction.CopyCaption,
                        TimelineItemAction.RemoveCaption,
                        TimelineItemAction.ViewSource,
                        TimelineItemAction.Redact,
                    )
                )
            )
            initialState.eventSink.invoke(ActionListEvents.Clear)
            assertThat(awaitItem().target).isEqualTo(ActionListState.Target.None)
        }
    }

    @Test
    fun `present - compute for a media with caption item - other user event`() = runTest {
        val presenter = createActionListPresenter(isDeveloperModeEnabled = true, isPinFeatureEnabled = true)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val messageEvent = aMessageEvent(
                isMine = false,
                isEditable = false,
                content = aTimelineItemImageContent(
                    caption = A_CAPTION,
                ),
            )
            initialState.eventSink.invoke(
                ActionListEvents.ComputeForMessage(
                    event = messageEvent,
                    userEventPermissions = aUserEventPermissions(
                        canRedactOwn = true,
                        canRedactOther = false,
                        canSendMessage = true,
                        canSendReaction = true,
                        canPinUnpin = true,
                    ),
                )
            )
            val successState = awaitItem()
            assertThat(successState.target).isEqualTo(
                ActionListState.Target.Success(
                    event = messageEvent,
                    sentTimeFull = "0 Full true",
                    displayEmojiReactions = true,
                    verifiedUserSendFailure = VerifiedUserSendFailure.None,
                    actions = persistentListOf(
                        TimelineItemAction.Reply,
                        TimelineItemAction.Forward,
                        TimelineItemAction.CopyLink,
                        TimelineItemAction.Pin,
                        TimelineItemAction.CopyCaption,
                        TimelineItemAction.ViewSource,
                        TimelineItemAction.ReportContent,
                    )
                )
            )
            initialState.eventSink.invoke(ActionListEvents.Clear)
            assertThat(awaitItem().target).isEqualTo(ActionListState.Target.None)
        }
    }

    @Test
    fun `present - compute for a state item in debug build`() = runTest {
        val presenter = createActionListPresenter(isDeveloperModeEnabled = true, isPinFeatureEnabled = true)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val stateEvent = aTimelineItemEvent(
                isMine = true,
                content = aTimelineItemStateEventContent(),
            )
            initialState.eventSink.invoke(
                ActionListEvents.ComputeForMessage(
                    event = stateEvent,
                    userEventPermissions = aUserEventPermissions(
                        canRedactOwn = false,
                        canRedactOther = false,
                        canSendMessage = true,
                        canSendReaction = true,
                        canPinUnpin = true,
                    )
                )
            )
            val successState = awaitItem()
            assertThat(successState.target).isEqualTo(
                ActionListState.Target.Success(
                    event = stateEvent,
                    sentTimeFull = "0 Full true",
                    displayEmojiReactions = false,
                    verifiedUserSendFailure = VerifiedUserSendFailure.None,
                    actions = persistentListOf(
                        TimelineItemAction.ViewSource,
                    )
                )
            )
            initialState.eventSink.invoke(ActionListEvents.Clear)
            assertThat(awaitItem().target).isEqualTo(ActionListState.Target.None)
        }
    }

    @Test
    fun `present - compute for a state item in non-debuggable build`() = runTest {
        val presenter = createActionListPresenter(isDeveloperModeEnabled = false, isPinFeatureEnabled = true)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val stateEvent = aTimelineItemEvent(
                isMine = true,
                content = aTimelineItemStateEventContent(),
            )
            initialState.eventSink.invoke(
                ActionListEvents.ComputeForMessage(
                    event = stateEvent,
                    userEventPermissions = aUserEventPermissions(
                        canRedactOwn = false,
                        canRedactOther = false,
                        canSendMessage = true,
                        canSendReaction = true,
                        canPinUnpin = true,
                    )
                )
            )
            assertThat(awaitItem().target).isEqualTo(ActionListState.Target.None)
        }
    }

    @Test
    fun `present - compute message in non-debuggable build`() = runTest {
        val presenter = createActionListPresenter(isDeveloperModeEnabled = false, isPinFeatureEnabled = true)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val messageEvent = aMessageEvent(
                isMine = true,
                content = TimelineItemTextContent(body = A_MESSAGE, htmlDocument = null, isEdited = false, formattedBody = A_MESSAGE)
            )
            initialState.eventSink.invoke(
                ActionListEvents.ComputeForMessage(
                    event = messageEvent,
                    userEventPermissions = aUserEventPermissions(
                        canRedactOwn = true,
                        canRedactOther = false,
                        canSendMessage = true,
                        canSendReaction = true,
                        canPinUnpin = true,
                    )
                )
            )
            val successState = awaitItem()
            assertThat(successState.target).isEqualTo(
                ActionListState.Target.Success(
                    event = messageEvent,
                    sentTimeFull = "0 Full true",
                    displayEmojiReactions = true,
                    verifiedUserSendFailure = VerifiedUserSendFailure.None,
                    actions = persistentListOf(
                        TimelineItemAction.Reply,
                        TimelineItemAction.Forward,
                        TimelineItemAction.Edit,
                        TimelineItemAction.CopyLink,
                        TimelineItemAction.Pin,
                        TimelineItemAction.CopyText,
                        TimelineItemAction.Redact,
                    )
                )
            )
            initialState.eventSink.invoke(ActionListEvents.Clear)
            assertThat(awaitItem().target).isEqualTo(ActionListState.Target.None)
        }
    }

    @Test
    fun `present - compute message when user can't pin`() = runTest {
        val presenter = createActionListPresenter(isDeveloperModeEnabled = true, isPinFeatureEnabled = true)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val messageEvent = aMessageEvent(
                isMine = true,
                content = TimelineItemTextContent(body = A_MESSAGE, htmlDocument = null, isEdited = false, formattedBody = A_MESSAGE)
            )
            initialState.eventSink.invoke(
                ActionListEvents.ComputeForMessage(
                    event = messageEvent,
                    userEventPermissions = aUserEventPermissions(
                        canRedactOwn = true,
                        canRedactOther = false,
                        canSendMessage = true,
                        canSendReaction = true,
                        canPinUnpin = false,
                    )
                )
            )
            val successState = awaitItem()
            assertThat(successState.target).isEqualTo(
                ActionListState.Target.Success(
                    event = messageEvent,
                    sentTimeFull = "0 Full true",
                    displayEmojiReactions = true,
                    verifiedUserSendFailure = VerifiedUserSendFailure.None,
                    actions = persistentListOf(
                        TimelineItemAction.Reply,
                        TimelineItemAction.Forward,
                        TimelineItemAction.Edit,
                        TimelineItemAction.CopyLink,
                        TimelineItemAction.CopyText,
                        TimelineItemAction.ViewSource,
                        TimelineItemAction.Redact,
                    )
                )
            )
            initialState.eventSink.invoke(ActionListEvents.Clear)
            assertThat(awaitItem().target).isEqualTo(ActionListState.Target.None)
        }
    }

    @Test
    fun `present - compute message when event is already pinned`() = runTest {
        val room = FakeBaseRoom().apply {
            givenRoomInfo(aRoomInfo(pinnedEventIds = listOf(AN_EVENT_ID)))
        }
        val presenter = createActionListPresenter(
            isDeveloperModeEnabled = true,
            isPinFeatureEnabled = true,
            room = room
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val messageEvent = aMessageEvent(
                isMine = true,
                content = TimelineItemTextContent(body = A_MESSAGE, htmlDocument = null, isEdited = false, formattedBody = A_MESSAGE)
            )
            initialState.eventSink.invoke(
                ActionListEvents.ComputeForMessage(
                    event = messageEvent,
                    userEventPermissions = aUserEventPermissions(
                        canRedactOwn = true,
                        canRedactOther = false,
                        canSendMessage = true,
                        canSendReaction = true,
                        canPinUnpin = true,
                    )
                )
            )
            val successState = awaitItem()
            assertThat(successState.target).isEqualTo(
                ActionListState.Target.Success(
                    event = messageEvent,
                    sentTimeFull = "0 Full true",
                    displayEmojiReactions = true,
                    verifiedUserSendFailure = VerifiedUserSendFailure.None,
                    actions = persistentListOf(
                        TimelineItemAction.Reply,
                        TimelineItemAction.Forward,
                        TimelineItemAction.Edit,
                        TimelineItemAction.CopyLink,
                        TimelineItemAction.Unpin,
                        TimelineItemAction.CopyText,
                        TimelineItemAction.ViewSource,
                        TimelineItemAction.Redact,
                    )
                )
            )
            initialState.eventSink.invoke(ActionListEvents.Clear)
            assertThat(awaitItem().target).isEqualTo(ActionListState.Target.None)
        }
    }

    @Test
    fun `present - compute message with no actions`() = runTest {
        val presenter = createActionListPresenter(isDeveloperModeEnabled = false, isPinFeatureEnabled = true)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val messageEvent = aMessageEvent(
                isMine = true,
                content = TimelineItemTextContent(body = A_MESSAGE, htmlDocument = null, isEdited = false, formattedBody = A_MESSAGE)
            )
            val redactedEvent = aMessageEvent(
                isMine = true,
                content = TimelineItemRedactedContent,
            )

            initialState.eventSink.invoke(
                ActionListEvents.ComputeForMessage(
                    event = messageEvent,
                    userEventPermissions = aUserEventPermissions(
                        canRedactOwn = false,
                        canRedactOther = false,
                        canSendMessage = true,
                        canSendReaction = true,
                        canPinUnpin = true,
                    )
                )
            )
            assertThat(awaitItem().target).isInstanceOf(ActionListState.Target.Success::class.java)

            initialState.eventSink.invoke(
                ActionListEvents.ComputeForMessage(
                    event = redactedEvent,
                    userEventPermissions = aUserEventPermissions(
                        canRedactOwn = false,
                        canRedactOther = false,
                        canSendMessage = true,
                        canSendReaction = true,
                    )
                )
            )
            awaitItem().run {
                assertThat(target).isEqualTo(ActionListState.Target.None)
            }
        }
    }

    @Test
    fun `present - compute not sent message`() = runTest {
        val presenter = createActionListPresenter(isDeveloperModeEnabled = false, isPinFeatureEnabled = true)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val messageEvent = aMessageEvent(
                // No event id, so it's not sent yet
                eventId = null,
                isMine = true,
                canBeRepliedTo = false,
                content = TimelineItemTextContent(body = A_MESSAGE, htmlDocument = null, isEdited = false, formattedBody = A_MESSAGE),
            )

            initialState.eventSink.invoke(
                ActionListEvents.ComputeForMessage(
                    event = messageEvent,
                    userEventPermissions = aUserEventPermissions(
                        canRedactOwn = true,
                        canRedactOther = false,
                        canSendMessage = true,
                        canSendReaction = true,
                        canPinUnpin = true,
                    )
                )
            )
            val successState = awaitItem()
            assertThat(successState.target).isEqualTo(
                ActionListState.Target.Success(
                    event = messageEvent,
                    sentTimeFull = "0 Full true",
                    displayEmojiReactions = true,
                    verifiedUserSendFailure = VerifiedUserSendFailure.None,
                    actions = persistentListOf(
                        TimelineItemAction.Edit,
                        TimelineItemAction.CopyText,
                        TimelineItemAction.Redact,
                    )
                )
            )
        }
    }

    @Test
    fun `present - compute for editable poll message`() = runTest {
        val presenter = createActionListPresenter(isDeveloperModeEnabled = false, isPinFeatureEnabled = true)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val messageEvent = aMessageEvent(
                isMine = true,
                isEditable = true,
                content = aTimelineItemPollContent(answerItems = aPollAnswerItemList(hasVotes = false)),
            )
            initialState.eventSink.invoke(
                ActionListEvents.ComputeForMessage(
                    event = messageEvent,
                    userEventPermissions = aUserEventPermissions(
                        canRedactOwn = true,
                        canRedactOther = false,
                        canSendMessage = true,
                        canSendReaction = true,
                        canPinUnpin = true,
                    )
                )
            )
            val successState = awaitItem()
            assertThat(successState.target).isEqualTo(
                ActionListState.Target.Success(
                    event = messageEvent,
                    sentTimeFull = "0 Full true",
                    displayEmojiReactions = true,
                    verifiedUserSendFailure = VerifiedUserSendFailure.None,
                    actions = persistentListOf(
                        TimelineItemAction.EndPoll,
                        TimelineItemAction.Reply,
                        TimelineItemAction.EditPoll,
                        TimelineItemAction.CopyLink,
                        TimelineItemAction.Pin,
                        TimelineItemAction.Redact,
                    )
                )
            )
        }
    }

    @Test
    fun `present - compute for non-editable poll message`() = runTest {
        val presenter = createActionListPresenter(isDeveloperModeEnabled = false, isPinFeatureEnabled = true)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val messageEvent = aMessageEvent(
                isMine = true,
                isEditable = false,
                content = aTimelineItemPollContent(answerItems = aPollAnswerItemList(hasVotes = true)),
            )
            initialState.eventSink.invoke(
                ActionListEvents.ComputeForMessage(
                    event = messageEvent,
                    userEventPermissions = aUserEventPermissions(
                        canRedactOwn = true,
                        canRedactOther = false,
                        canSendMessage = true,
                        canSendReaction = true,
                        canPinUnpin = true
                    )
                )
            )
            val successState = awaitItem()
            assertThat(successState.target).isEqualTo(
                ActionListState.Target.Success(
                    event = messageEvent,
                    sentTimeFull = "0 Full true",
                    displayEmojiReactions = true,
                    verifiedUserSendFailure = VerifiedUserSendFailure.None,
                    actions = persistentListOf(
                        TimelineItemAction.EndPoll,
                        TimelineItemAction.Reply,
                        TimelineItemAction.CopyLink,
                        TimelineItemAction.Pin,
                        TimelineItemAction.Redact,
                    )
                )
            )
        }
    }

    @Test
    fun `present - compute for ended poll message`() = runTest {
        val presenter = createActionListPresenter(isDeveloperModeEnabled = false, isPinFeatureEnabled = true)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val messageEvent = aMessageEvent(
                isMine = true,
                isEditable = false,
                content = aTimelineItemPollContent(isEnded = true),
            )
            initialState.eventSink.invoke(
                ActionListEvents.ComputeForMessage(
                    event = messageEvent,
                    userEventPermissions = aUserEventPermissions(
                        canRedactOwn = true,
                        canRedactOther = false,
                        canSendMessage = true,
                        canSendReaction = true,
                        canPinUnpin = true,
                    )
                )
            )
            val successState = awaitItem()
            assertThat(successState.target).isEqualTo(
                ActionListState.Target.Success(
                    event = messageEvent,
                    sentTimeFull = "0 Full true",
                    displayEmojiReactions = true,
                    verifiedUserSendFailure = VerifiedUserSendFailure.None,
                    actions = persistentListOf(
                        TimelineItemAction.Reply,
                        TimelineItemAction.CopyLink,
                        TimelineItemAction.Pin,
                        TimelineItemAction.Redact,
                    )
                )
            )
        }
    }

    @Test
    fun `present - compute for voice message`() = runTest {
        val presenter = createActionListPresenter(isDeveloperModeEnabled = false, isPinFeatureEnabled = true)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val messageEvent = aMessageEvent(
                isMine = true,
                isEditable = false,
                content = aTimelineItemVoiceContent(
                    caption = null,
                ),
            )
            initialState.eventSink.invoke(
                ActionListEvents.ComputeForMessage(
                    event = messageEvent,
                    userEventPermissions = aUserEventPermissions(
                        canRedactOwn = true,
                        canRedactOther = false,
                        canSendMessage = true,
                        canSendReaction = true,
                        canPinUnpin = true
                    )
                )
            )
            val successState = awaitItem()
            assertThat(successState.target).isEqualTo(
                ActionListState.Target.Success(
                    event = messageEvent,
                    sentTimeFull = "0 Full true",
                    displayEmojiReactions = true,
                    verifiedUserSendFailure = VerifiedUserSendFailure.None,
                    actions = persistentListOf(
                        TimelineItemAction.Reply,
                        TimelineItemAction.Forward,
                        TimelineItemAction.CopyLink,
                        TimelineItemAction.Pin,
                        TimelineItemAction.Redact,
                    )
                )
            )
        }
    }

    @Test
    fun `present - compute for call notify`() = runTest {
        val presenter = createActionListPresenter(isDeveloperModeEnabled = true, isPinFeatureEnabled = true)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val messageEvent = aMessageEvent(
                isMine = true,
                content = TimelineItemCallNotifyContent(),
            )
            initialState.eventSink.invoke(
                ActionListEvents.ComputeForMessage(
                    event = messageEvent,
                    userEventPermissions = aUserEventPermissions(
                        canRedactOwn = true,
                        canRedactOther = false,
                        canSendMessage = true,
                        canSendReaction = true,
                    )
                )
            )
            val successState = awaitItem()
            assertThat(successState.target).isEqualTo(
                ActionListState.Target.Success(
                    event = messageEvent,
                    sentTimeFull = "0 Full true",
                    displayEmojiReactions = false,
                    verifiedUserSendFailure = VerifiedUserSendFailure.None,
                    actions = persistentListOf(
                        TimelineItemAction.ViewSource
                    )
                )
            )
        }
    }

    @Test
    fun `present - compute for verified user send failure`() = runTest {
        val room = FakeBaseRoom(
            userDisplayNameResult = { Result.success("Alice") }
        )
        val presenter = createActionListPresenter(isDeveloperModeEnabled = false, isPinFeatureEnabled = false, room = room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val messageEvent = aMessageEvent(
                sendState = LocalEventSendState.Failed.VerifiedUserChangedIdentity(users = listOf(A_USER_ID)),
            )
            initialState.eventSink.invoke(
                ActionListEvents.ComputeForMessage(
                    event = messageEvent,
                    userEventPermissions = aUserEventPermissions(),
                )
            )
            skipItems(1)
            val successState = awaitItem()
            val target = successState.target as ActionListState.Target.Success
            assertThat(target.verifiedUserSendFailure).isEqualTo(VerifiedUserSendFailure.ChangedIdentity(userDisplayName = "Alice"))
        }
    }
}

private fun createActionListPresenter(
    isDeveloperModeEnabled: Boolean,
    isPinFeatureEnabled: Boolean,
    room: BaseRoom = FakeBaseRoom(),
    allowCaption: Boolean = true,
): ActionListPresenter {
    val preferencesStore = InMemoryAppPreferencesStore(isDeveloperModeEnabled = isDeveloperModeEnabled)
    return DefaultActionListPresenter(
        postProcessor = TimelineItemActionPostProcessor.Default,
        appPreferencesStore = preferencesStore,
        isPinnedMessagesFeatureEnabled = { isPinFeatureEnabled },
        room = room,
        userSendFailureFactory = VerifiedUserSendFailureFactory(room),
        featureFlagService = FakeFeatureFlagService(
            initialState = mapOf(
                FeatureFlags.MediaCaptionCreation.key to allowCaption,
            ),
        ),
        dateFormatter = FakeDateFormatter(),
    )
}

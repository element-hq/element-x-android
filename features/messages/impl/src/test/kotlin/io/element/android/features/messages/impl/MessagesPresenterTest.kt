/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.features.messages.impl

import androidx.lifecycle.Lifecycle
import com.google.common.truth.Truth.assertThat
import im.vector.app.features.analytics.plan.PinUnpinAction
import io.element.android.features.messages.impl.actionlist.ActionListEvents
import io.element.android.features.messages.impl.actionlist.ActionListState
import io.element.android.features.messages.impl.actionlist.anActionListState
import io.element.android.features.messages.impl.actionlist.model.TimelineItemAction
import io.element.android.features.messages.impl.crypto.identity.anIdentityChangeState
import io.element.android.features.messages.impl.fixtures.aMessageEvent
import io.element.android.features.messages.impl.link.aLinkState
import io.element.android.features.messages.impl.messagecomposer.MessageComposerEvents
import io.element.android.features.messages.impl.messagecomposer.MessageComposerState
import io.element.android.features.messages.impl.messagecomposer.aMessageComposerState
import io.element.android.features.messages.impl.pinned.banner.aLoadedPinnedMessagesBannerState
import io.element.android.features.messages.impl.timeline.TimelineController
import io.element.android.features.messages.impl.timeline.TimelineEvents
import io.element.android.features.messages.impl.timeline.aTimelineState
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemFileContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemImageContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemTextContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemVideoContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemImageContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemPollContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemTextContent
import io.element.android.features.messages.impl.timeline.protection.aTimelineProtectionState
import io.element.android.features.messages.impl.voicemessages.composer.aVoiceMessageComposerState
import io.element.android.features.messages.test.timeline.FakeHtmlConverterProvider
import io.element.android.features.roomcall.api.aStandByCallState
import io.element.android.features.roommembermoderation.api.RoomMemberModerationState
import io.element.android.libraries.androidutils.clipboard.FakeClipboardHelper
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.featureflag.test.FakeFeatureFlagService
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.encryption.identity.IdentityState
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.api.permalink.PermalinkParser
import io.element.android.libraries.matrix.api.room.MessageEventType
import io.element.android.libraries.matrix.api.room.RoomMembersState
import io.element.android.libraries.matrix.api.room.RoomMembershipState
import io.element.android.libraries.matrix.api.room.tombstone.SuccessorRoom
import io.element.android.libraries.matrix.api.timeline.item.TimelineItemDebugInfo
import io.element.android.libraries.matrix.api.timeline.item.event.EventOrTransactionId
import io.element.android.libraries.matrix.api.timeline.item.event.toEventOrTransactionId
import io.element.android.libraries.matrix.test.AN_AVATAR_URL
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.matrix.test.A_CAPTION
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID_2
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.A_USER_ID_2
import io.element.android.libraries.matrix.test.core.aBuildMeta
import io.element.android.libraries.matrix.test.encryption.FakeEncryptionService
import io.element.android.libraries.matrix.test.permalink.FakePermalinkParser
import io.element.android.libraries.matrix.test.room.FakeBaseRoom
import io.element.android.libraries.matrix.test.room.FakeJoinedRoom
import io.element.android.libraries.matrix.test.room.aRoomInfo
import io.element.android.libraries.matrix.test.room.aRoomMember
import io.element.android.libraries.matrix.test.sync.FakeSyncService
import io.element.android.libraries.matrix.test.timeline.FakeTimeline
import io.element.android.libraries.matrix.test.timeline.aTimelineItemDebugInfo
import io.element.android.libraries.matrix.ui.messages.reply.InReplyToDetails
import io.element.android.libraries.textcomposer.model.MessageComposerMode
import io.element.android.libraries.textcomposer.model.TextEditorState
import io.element.android.libraries.textcomposer.model.aTextEditorStateMarkdown
import io.element.android.services.analytics.test.FakeAnalyticsService
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.FakeLifecycleOwner
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.consumeItemsUntilPredicate
import io.element.android.tests.testutils.consumeItemsUntilTimeout
import io.element.android.tests.testutils.lambda.assert
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import io.element.android.tests.testutils.testCoroutineDispatchers
import io.element.android.tests.testutils.testWithLifecycleOwner
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

@Suppress("LargeClass")
class MessagesPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val presenter = createMessagesPresenter()
        presenter.testWithLifecycleOwner {
            val initialState = consumeItemsUntilTimeout().last()
            assertThat(initialState.roomId).isEqualTo(A_ROOM_ID)
            assertThat(initialState.roomName).isEqualTo("")
            assertThat(initialState.roomAvatar)
                .isEqualTo(AvatarData(id = A_ROOM_ID.value, name = "", url = AN_AVATAR_URL, size = AvatarSize.TimelineRoom))
            assertThat(initialState.userEventPermissions.canSendMessage).isTrue()
            assertThat(initialState.userEventPermissions.canRedactOwn).isTrue()
            assertThat(initialState.hasNetworkConnection).isTrue()
            assertThat(initialState.snackbarMessage).isNull()
            assertThat(initialState.inviteProgress).isEqualTo(AsyncData.Uninitialized)
            assertThat(initialState.showReinvitePrompt).isFalse()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `present - check that the room's unread flag is removed`() = runTest {
        val room = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
                canUserSendMessageResult = { _, _ -> Result.success(true) },
                canRedactOwnResult = { Result.success(true) },
                canRedactOtherResult = { Result.success(true) },
                canUserJoinCallResult = { Result.success(true) },
                canUserPinUnpinResult = { Result.success(true) },
                markAsReadResult = { lambdaError() }
            ),
            typingNoticeResult = { Result.success(Unit) },
        )
        val presenter = createMessagesPresenter(joinedRoom = room)
        presenter.testWithLifecycleOwner {
            runCurrent()
            assertThat(room.baseRoom.setUnreadFlagCalls).isEqualTo(listOf(false))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - handle toggling a reaction`() = runTest {
        val coroutineDispatchers = testCoroutineDispatchers(useUnconfinedTestDispatcher = true)
        val toggleReactionSuccess = lambdaRecorder { _: String, _: EventOrTransactionId -> Result.success(Unit) }
        val toggleReactionFailure =
            lambdaRecorder { _: String, _: EventOrTransactionId -> Result.failure<Unit>(IllegalStateException("Failed to send reaction")) }

        val timeline = FakeTimeline().apply {
            this.toggleReactionLambda = toggleReactionSuccess
        }
        val room = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
                canUserSendMessageResult = { _, _ -> Result.success(true) },
                canRedactOwnResult = { Result.success(true) },
                canRedactOtherResult = { Result.success(true) },
                canUserJoinCallResult = { Result.success(true) },
                canUserPinUnpinResult = { Result.success(true) },
            ),
            liveTimeline = timeline,
            typingNoticeResult = { Result.success(Unit) },
        )
        val presenter = createMessagesPresenter(joinedRoom = room, coroutineDispatchers = coroutineDispatchers)
        presenter.testWithLifecycleOwner {
            skipItems(1)
            val initialState = awaitItem()
            initialState.eventSink(MessagesEvents.ToggleReaction("👍", AN_EVENT_ID.toEventOrTransactionId()))
            advanceUntilIdle()
            assert(toggleReactionSuccess)
                .isCalledOnce()
                .with(value("👍"), value(AN_EVENT_ID.toEventOrTransactionId()))
            // No crashes when sending a reaction failed
            timeline.toggleReactionLambda = toggleReactionFailure
            initialState.eventSink(MessagesEvents.ToggleReaction("👍", AN_EVENT_ID.toEventOrTransactionId()))
            advanceUntilIdle()
            assert(toggleReactionFailure)
                .isCalledOnce()
                .with(value("👍"), value(AN_EVENT_ID.toEventOrTransactionId()))
        }
    }

    @Test
    fun `present - handle toggling a reaction twice`() = runTest {
        val coroutineDispatchers = testCoroutineDispatchers(useUnconfinedTestDispatcher = true)
        val toggleReactionSuccess = lambdaRecorder { _: String, _: EventOrTransactionId -> Result.success(Unit) }

        val timeline = FakeTimeline().apply {
            this.toggleReactionLambda = toggleReactionSuccess
        }
        val room = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
                canUserSendMessageResult = { _, _ -> Result.success(true) },
                canRedactOwnResult = { Result.success(true) },
                canRedactOtherResult = { Result.success(true) },
                canUserJoinCallResult = { Result.success(true) },
                canUserPinUnpinResult = { Result.success(true) },
            ),
            liveTimeline = timeline,
            typingNoticeResult = { Result.success(Unit) },
        )
        val presenter = createMessagesPresenter(joinedRoom = room, coroutineDispatchers = coroutineDispatchers)
        presenter.testWithLifecycleOwner {
            val initialState = awaitItem()
            initialState.eventSink(MessagesEvents.ToggleReaction("👍", AN_EVENT_ID.toEventOrTransactionId()))
            initialState.eventSink(MessagesEvents.ToggleReaction("👍", AN_EVENT_ID.toEventOrTransactionId()))
            advanceUntilIdle()
            assert(toggleReactionSuccess)
                .isCalledExactly(2)
                .withSequence(
                    listOf(value("👍"), value(AN_EVENT_ID.toEventOrTransactionId())),
                    listOf(value("👍"), value(AN_EVENT_ID.toEventOrTransactionId())),
                )
            skipItems(1)
        }
    }

    @Test
    fun `present - handle action forward`() = runTest {
        val onForwardEventClickLambda = lambdaRecorder<EventId, Unit> { }
        val navigator = FakeMessagesNavigator(
            onForwardEventClickLambda = onForwardEventClickLambda,
        )
        val presenter = createMessagesPresenter(navigator = navigator)
        presenter.testWithLifecycleOwner {
            val initialState = awaitItem()
            initialState.eventSink(MessagesEvents.HandleAction(TimelineItemAction.Forward, aMessageEvent()))
            assertThat(awaitItem().actionListState.target).isEqualTo(ActionListState.Target.None)
            onForwardEventClickLambda.assertions().isCalledOnce().with(value(AN_EVENT_ID))
        }
    }

    @Test
    fun `present - handle action copy`() = runTest {
        val clipboardHelper = FakeClipboardHelper()
        val event = aMessageEvent()
        val presenter = createMessagesPresenter(clipboardHelper = clipboardHelper)
        presenter.testWithLifecycleOwner {
            val initialState = awaitItem()
            initialState.eventSink(MessagesEvents.HandleAction(TimelineItemAction.CopyText, event))
            skipItems(2)
            assertThat(clipboardHelper.clipboardContents).isEqualTo((event.content as TimelineItemTextContent).body)
        }
    }

    @Test
    fun `present - handle action copy link`() = runTest {
        val clipboardHelper = FakeClipboardHelper()
        val event = aMessageEvent()
        val room = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
                canUserSendMessageResult = { _, _ -> Result.success(true) },
                canRedactOwnResult = { Result.success(true) },
                canRedactOtherResult = { Result.success(true) },
                canUserJoinCallResult = { Result.success(true) },
                canUserPinUnpinResult = { Result.success(true) },
                eventPermalinkResult = { Result.success("a link") },
            ),
            typingNoticeResult = { Result.success(Unit) },
        )
        val presenter = createMessagesPresenter(
            clipboardHelper = clipboardHelper,
            joinedRoom = room,
        )
        presenter.testWithLifecycleOwner {
            val initialState = awaitItem()
            initialState.eventSink(MessagesEvents.HandleAction(TimelineItemAction.CopyLink, event))
            skipItems(2)
            assertThat(clipboardHelper.clipboardContents).isEqualTo("a link")
        }
    }

    @Test
    fun `present - handle action reply`() = runTest {
        val composerRecorder = EventsRecorder<MessageComposerEvents>()
        val presenter = createMessagesPresenter(
            messageComposerPresenter = { aMessageComposerState(eventSink = composerRecorder) },
        )
        presenter.testWithLifecycleOwner {
            val initialState = awaitItem()
            initialState.eventSink(MessagesEvents.HandleAction(TimelineItemAction.Reply, aMessageEvent()))
            awaitItem()
            composerRecorder.assertSingle(
                MessageComposerEvents.SetMode(
                    composerMode = MessageComposerMode.Reply(
                        replyToDetails = InReplyToDetails.Loading(AN_EVENT_ID),
                        hideImage = false,
                    )
                )
            )
        }
    }

    @Test
    fun `present - handle action reply to an event with no id does nothing`() = runTest {
        val presenter = createMessagesPresenter()
        presenter.testWithLifecycleOwner {
            val initialState = awaitItem()
            initialState.eventSink(MessagesEvents.HandleAction(TimelineItemAction.Reply, aMessageEvent(eventId = null)))
            skipItems(1)
        }
    }

    @Test
    fun `present - handle action reply to an image media message`() = runTest {
        val composerRecorder = EventsRecorder<MessageComposerEvents>()
        val presenter = createMessagesPresenter(
            messageComposerPresenter = { aMessageComposerState(eventSink = composerRecorder) },
        )
        presenter.testWithLifecycleOwner {
            val initialState = awaitItem()
            val mediaMessage = aMessageEvent(
                content = TimelineItemImageContent(
                    filename = "image.jpg",
                    caption = null,
                    formattedCaption = null,
                    isEdited = false,
                    mediaSource = MediaSource(AN_AVATAR_URL),
                    thumbnailSource = null,
                    mimeType = MimeTypes.Jpeg,
                    blurhash = null,
                    width = 20,
                    height = 20,
                    thumbnailWidth = null,
                    thumbnailHeight = null,
                    aspectRatio = 1.0f,
                    fileExtension = "jpg",
                    formattedFileSize = "4MB"
                )
            )
            initialState.eventSink(MessagesEvents.HandleAction(TimelineItemAction.Reply, mediaMessage))
            awaitItem()
            composerRecorder.assertSingle(
                MessageComposerEvents.SetMode(
                    composerMode = MessageComposerMode.Reply(
                        replyToDetails = InReplyToDetails.Loading(AN_EVENT_ID),
                        hideImage = false,
                    )
                )
            )
        }
    }

    @Test
    fun `present - handle action reply to a video media message`() = runTest {
        val composerRecorder = EventsRecorder<MessageComposerEvents>()
        val presenter = createMessagesPresenter(
            messageComposerPresenter = { aMessageComposerState(eventSink = composerRecorder) },
        )
        presenter.testWithLifecycleOwner {
            val initialState = awaitItem()
            val mediaMessage = aMessageEvent(
                content = TimelineItemVideoContent(
                    filename = "video.mp4",
                    caption = null,
                    formattedCaption = null,
                    isEdited = false,
                    duration = 10.milliseconds,
                    mediaSource = MediaSource(AN_AVATAR_URL),
                    thumbnailSource = MediaSource(AN_AVATAR_URL),
                    mimeType = MimeTypes.Mp4,
                    blurHash = null,
                    width = 20,
                    height = 20,
                    thumbnailWidth = 20,
                    thumbnailHeight = 20,
                    aspectRatio = 1.0f,
                    fileExtension = "mp4",
                    formattedFileSize = "50MB"
                )
            )
            initialState.eventSink(MessagesEvents.HandleAction(TimelineItemAction.Reply, mediaMessage))
            awaitItem()
            composerRecorder.assertSingle(
                MessageComposerEvents.SetMode(
                    composerMode = MessageComposerMode.Reply(
                        replyToDetails = InReplyToDetails.Loading(AN_EVENT_ID),
                        hideImage = false,
                    )
                )
            )
        }
    }

    @Test
    fun `present - handle action reply to a file media message`() = runTest {
        val composerRecorder = EventsRecorder<MessageComposerEvents>()
        val presenter = createMessagesPresenter(
            messageComposerPresenter = { aMessageComposerState(eventSink = composerRecorder) },
        )
        presenter.testWithLifecycleOwner {
            val initialState = awaitItem()
            val mediaMessage = aMessageEvent(
                content = TimelineItemFileContent(
                    filename = "file.pdf",
                    caption = null,
                    isEdited = false,
                    formattedCaption = null,
                    mediaSource = MediaSource(AN_AVATAR_URL),
                    thumbnailSource = MediaSource(AN_AVATAR_URL),
                    formattedFileSize = "10 MB",
                    mimeType = MimeTypes.Pdf,
                    fileExtension = "pdf",
                )
            )
            initialState.eventSink(MessagesEvents.HandleAction(TimelineItemAction.Reply, mediaMessage))
            awaitItem()
            composerRecorder.assertSingle(
                MessageComposerEvents.SetMode(
                    composerMode = MessageComposerMode.Reply(
                        replyToDetails = InReplyToDetails.Loading(AN_EVENT_ID),
                        hideImage = false,
                    )
                )
            )
        }
    }

    @Test
    fun `present - handle action edit`() = runTest {
        val composerRecorder = EventsRecorder<MessageComposerEvents>()
        val presenter = createMessagesPresenter(
            messageComposerPresenter = { aMessageComposerState(eventSink = composerRecorder) },
        )
        presenter.testWithLifecycleOwner {
            val initialState = awaitItem()
            initialState.eventSink(MessagesEvents.HandleAction(TimelineItemAction.Edit, aMessageEvent()))
            awaitItem()
            composerRecorder.assertSingle(
                MessageComposerEvents.SetMode(
                    composerMode = MessageComposerMode.Edit(
                        eventOrTransactionId = AN_EVENT_ID.toEventOrTransactionId(),
                        content = (aMessageEvent().content as TimelineItemTextContent).body
                    )
                )
            )
        }
    }

    @Test
    fun `present - handle action edit poll`() = runTest {
        val onEditPollClickLambda = lambdaRecorder<EventId, Unit> { }
        val navigator = FakeMessagesNavigator(
            onEditPollClickLambda = onEditPollClickLambda
        )
        val presenter = createMessagesPresenter(navigator = navigator)
        presenter.testWithLifecycleOwner {
            val initialState = awaitItem()
            initialState.eventSink(MessagesEvents.HandleAction(TimelineItemAction.EditPoll, aMessageEvent(content = aTimelineItemPollContent())))
            awaitItem()
            onEditPollClickLambda.assertions().isCalledOnce().with(value(AN_EVENT_ID))
        }
    }

    @Test
    fun `present - handle action end poll`() = runTest {
        val timelineEventSink = EventsRecorder<TimelineEvents>()
        val presenter = createMessagesPresenter(timelineEventSink = timelineEventSink)
        presenter.testWithLifecycleOwner {
            val initialState = awaitItem()
            initialState.eventSink(MessagesEvents.HandleAction(TimelineItemAction.EndPoll, aMessageEvent(content = aTimelineItemPollContent())))
            delay(1)
            timelineEventSink.assertSingle(TimelineEvents.EndPoll(AN_EVENT_ID))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - handle action redact`() = runTest {
        val coroutineDispatchers = testCoroutineDispatchers(useUnconfinedTestDispatcher = true)

        val liveTimeline = FakeTimeline()
        val joinedRoom = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
                canUserSendMessageResult = { _, _ -> Result.success(true) },
                canRedactOwnResult = { Result.success(true) },
                canRedactOtherResult = { Result.success(true) },
                canUserJoinCallResult = { Result.success(true) },
                canUserPinUnpinResult = { Result.success(true) },
            ),
            liveTimeline = liveTimeline,
            typingNoticeResult = { Result.success(Unit) },
        )

        val redactEventLambda = lambdaRecorder { _: EventOrTransactionId, _: String? -> Result.success(Unit) }
        liveTimeline.redactEventLambda = redactEventLambda
        val presenter = createMessagesPresenter(
            joinedRoom = joinedRoom,
            coroutineDispatchers = coroutineDispatchers,
        )
        presenter.testWithLifecycleOwner {
            val initialState = awaitItem()
            val messageEvent = aMessageEvent()
            initialState.eventSink(MessagesEvents.HandleAction(TimelineItemAction.Redact, messageEvent))
            awaitItem()
            assert(redactEventLambda)
                .isCalledOnce()
                .with(value(messageEvent.eventOrTransactionId), value(null))
        }
    }

    @Test
    fun `present - handle action report content`() = runTest {
        val onReportContentClickLambda = lambdaRecorder { _: EventId, _: UserId -> }
        val navigator = FakeMessagesNavigator(
            onReportContentClickLambda = onReportContentClickLambda
        )
        val presenter = createMessagesPresenter(navigator = navigator)
        presenter.testWithLifecycleOwner {
            val initialState = awaitItem()
            initialState.eventSink(MessagesEvents.HandleAction(TimelineItemAction.ReportContent, aMessageEvent()))
            assertThat(awaitItem().actionListState.target).isEqualTo(ActionListState.Target.None)
            onReportContentClickLambda.assertions().isCalledOnce().with(value(AN_EVENT_ID), value(A_USER_ID))
        }
    }

    @Test
    fun `present - handle dismiss action`() = runTest {
        val presenter = createMessagesPresenter()
        presenter.testWithLifecycleOwner {
            val initialState = awaitItem()
            initialState.eventSink(MessagesEvents.Dismiss)
            assertThat(awaitItem().actionListState.target).isEqualTo(ActionListState.Target.None)
        }
    }

    @Test
    fun `present - handle action show developer info`() = runTest {
        val onShowEventDebugInfoClickLambda = lambdaRecorder { _: EventId?, _: TimelineItemDebugInfo -> }
        val navigator = FakeMessagesNavigator(
            onShowEventDebugInfoClickLambda = onShowEventDebugInfoClickLambda
        )
        val presenter = createMessagesPresenter(navigator = navigator)
        presenter.testWithLifecycleOwner {
            val initialState = awaitItem()
            initialState.eventSink(MessagesEvents.HandleAction(TimelineItemAction.ViewSource, aMessageEvent()))
            assertThat(awaitItem().actionListState.target).isEqualTo(ActionListState.Target.None)
            onShowEventDebugInfoClickLambda.assertions().isCalledOnce().with(value(AN_EVENT_ID), value(aTimelineItemDebugInfo()))
        }
    }

    @Test
    fun `present - shows prompt to reinvite users in DM`() = runTest {
        val room = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
                canUserSendMessageResult = { _, _ -> Result.success(true) },
                canRedactOwnResult = { Result.success(true) },
                canRedactOtherResult = { Result.success(true) },
                canUserJoinCallResult = { Result.success(true) },
                canUserPinUnpinResult = { Result.success(true) },
            ).apply {
                givenRoomInfo(aRoomInfo(isDirect = true, joinedMembersCount = 1, activeMembersCount = 1))
            },
            typingNoticeResult = { Result.success(Unit) },
        )
        val presenter = createMessagesPresenter(joinedRoom = room)
        presenter.testWithLifecycleOwner {
            val initialState = awaitItem()
            // Initially the composer doesn't have focus, so we don't show the alert
            assertThat(initialState.showReinvitePrompt).isFalse()
            // When the input field is focused we show the alert
            (initialState.composerState.textEditorState as TextEditorState.Markdown).state.hasFocus = true
            // Skip intermediate states
            skipItems(2)
            val focusedState = awaitItem()
            assertThat(focusedState.showReinvitePrompt).isTrue()
            // If it's dismissed then we stop showing the alert
            initialState.eventSink(MessagesEvents.InviteDialogDismissed(InviteDialogAction.Cancel))
            skipItems(1)
            val dismissedState = awaitItem()
            assertThat(dismissedState.showReinvitePrompt).isFalse()
        }
    }

    @Test
    fun `present - doesn't show reinvite prompt in non-direct room`() = runTest {
        val room = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
                canUserSendMessageResult = { _, _ -> Result.success(true) },
                canRedactOwnResult = { Result.success(true) },
                canRedactOtherResult = { Result.success(true) },
                canUserJoinCallResult = { Result.success(true) },
                canUserPinUnpinResult = { Result.success(true) },
            ).apply {
                givenRoomInfo(aRoomInfo(isDirect = false, joinedMembersCount = 1, activeMembersCount = 1))
            },
            typingNoticeResult = { Result.success(Unit) },
        )
        val presenter = createMessagesPresenter(joinedRoom = room)
        presenter.testWithLifecycleOwner {
            val initialState = awaitItem()
            assertThat(initialState.showReinvitePrompt).isFalse()
            (initialState.composerState.textEditorState as TextEditorState.Markdown).state.hasFocus = true
            // Skip intermediate events
            skipItems(1)
            val focusedState = awaitItem()
            assertThat(focusedState.showReinvitePrompt).isFalse()
        }
    }

    @Test
    fun `present - doesn't show reinvite prompt if other party is present`() = runTest {
        val room = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
                canUserSendMessageResult = { _, _ -> Result.success(true) },
                canRedactOwnResult = { Result.success(true) },
                canRedactOtherResult = { Result.success(true) },
                canUserJoinCallResult = { Result.success(true) },
                canUserPinUnpinResult = { Result.success(true) },
            ).apply {
                givenRoomInfo(aRoomInfo(isDirect = true, joinedMembersCount = 2, activeMembersCount = 2))
            },
            typingNoticeResult = { Result.success(Unit) },
        )
        val presenter = createMessagesPresenter(joinedRoom = room)
        presenter.testWithLifecycleOwner {
            val initialState = awaitItem()
            assertThat(initialState.showReinvitePrompt).isFalse()
            (initialState.composerState.textEditorState as TextEditorState.Markdown).state.hasFocus = true
            // Skip intermediate events
            skipItems(1)
            val focusedState = awaitItem()
            assertThat(focusedState.showReinvitePrompt).isFalse()
        }
    }

    @Test
    fun `present - handle reinviting other user when memberlist is ready`() = runTest {
        val inviteUserResult = lambdaRecorder { _: UserId -> Result.success(Unit) }
        val room = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
                canUserSendMessageResult = { _, _ -> Result.success(true) },
                canRedactOwnResult = { Result.success(true) },
                canRedactOtherResult = { Result.success(true) },
                canUserJoinCallResult = { Result.success(true) },
                canUserPinUnpinResult = { Result.success(true) },
            ),
            typingNoticeResult = { Result.success(Unit) },
            inviteUserResult = inviteUserResult,
        )
        room.givenRoomMembersState(
            RoomMembersState.Ready(
                persistentListOf(
                    aRoomMember(userId = A_SESSION_ID, membership = RoomMembershipState.JOIN),
                    aRoomMember(userId = A_SESSION_ID_2, membership = RoomMembershipState.LEAVE),
                )
            )
        )
        val presenter = createMessagesPresenter(joinedRoom = room)
        presenter.testWithLifecycleOwner {
            val initialState = consumeItemsUntilTimeout().last()
            initialState.eventSink(MessagesEvents.InviteDialogDismissed(InviteDialogAction.Invite))
            skipItems(1)
            val loadingState = awaitItem()
            assertThat(loadingState.inviteProgress.isLoading()).isTrue()
            val newState = awaitItem()
            assertThat(newState.inviteProgress.isSuccess()).isTrue()
            inviteUserResult.assertions().isCalledOnce().with(value(A_SESSION_ID_2))
        }
    }

    @Test
    fun `present - handle reinviting other user when memberlist is error`() = runTest {
        val inviteUserResult = lambdaRecorder { _: UserId -> Result.success(Unit) }
        val room = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
                canUserSendMessageResult = { _, _ -> Result.success(true) },
                canRedactOwnResult = { Result.success(true) },
                canRedactOtherResult = { Result.success(true) },
                canUserJoinCallResult = { Result.success(true) },
                canUserPinUnpinResult = { Result.success(true) },
            ),
            typingNoticeResult = { Result.success(Unit) },
            inviteUserResult = inviteUserResult,
        )
        room.givenRoomMembersState(
            RoomMembersState.Error(
                failure = Throwable(),
                prevRoomMembers = persistentListOf(
                    aRoomMember(userId = A_SESSION_ID, membership = RoomMembershipState.JOIN),
                    aRoomMember(userId = A_SESSION_ID_2, membership = RoomMembershipState.LEAVE),
                )
            )
        )
        val presenter = createMessagesPresenter(joinedRoom = room)
        presenter.testWithLifecycleOwner {
            val initialState = consumeItemsUntilTimeout().last()
            initialState.eventSink(MessagesEvents.InviteDialogDismissed(InviteDialogAction.Invite))
            skipItems(1)
            val loadingState = consumeItemsUntilPredicate { state ->
                state.inviteProgress.isLoading()
            }.last()
            assertThat(loadingState.inviteProgress.isLoading()).isTrue()
            val newState = awaitItem()
            assertThat(newState.inviteProgress.isSuccess()).isTrue()
            inviteUserResult.assertions().isCalledOnce().with(value(A_SESSION_ID_2))
        }
    }

    @Test
    fun `present - handle reinviting other user when memberlist is not ready`() = runTest {
        val room = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
                canUserSendMessageResult = { _, _ -> Result.success(true) },
                canRedactOwnResult = { Result.success(true) },
                canRedactOtherResult = { Result.success(true) },
                canUserJoinCallResult = { Result.success(true) },
                canUserPinUnpinResult = { Result.success(true) },
            ),
            typingNoticeResult = { Result.success(Unit) },
        )
        room.givenRoomMembersState(RoomMembersState.Unknown)
        val presenter = createMessagesPresenter(joinedRoom = room)
        presenter.testWithLifecycleOwner {
            val initialState = consumeItemsUntilTimeout().last()
            initialState.eventSink(MessagesEvents.InviteDialogDismissed(InviteDialogAction.Invite))
            skipItems(1)
            val loadingState = awaitItem()
            assertThat(loadingState.inviteProgress.isLoading()).isTrue()
            val newState = awaitItem()
            assertThat(newState.inviteProgress.isFailure()).isTrue()
        }
    }

    @Test
    fun `present - handle reinviting other user when inviting fails`() = runTest {
        val room = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
                canUserSendMessageResult = { _, _ -> Result.success(true) },
                canRedactOwnResult = { Result.success(true) },
                canRedactOtherResult = { Result.success(true) },
                canUserJoinCallResult = { Result.success(true) },
                canUserPinUnpinResult = { Result.success(true) },
            ),
            typingNoticeResult = { Result.success(Unit) },
            inviteUserResult = { Result.failure(RuntimeException("Oops!")) },
        )
        room.givenRoomMembersState(
            RoomMembersState.Ready(
                persistentListOf(
                    aRoomMember(userId = A_SESSION_ID, membership = RoomMembershipState.JOIN),
                    aRoomMember(userId = A_SESSION_ID_2, membership = RoomMembershipState.LEAVE),
                )
            )
        )
        val presenter = createMessagesPresenter(joinedRoom = room)
        presenter.testWithLifecycleOwner {
            val initialState = consumeItemsUntilTimeout().last()
            initialState.eventSink(MessagesEvents.InviteDialogDismissed(InviteDialogAction.Invite))

            val loadingState = consumeItemsUntilPredicate { state ->
                state.inviteProgress.isLoading()
            }.last()
            assertThat(loadingState.inviteProgress.isLoading()).isTrue()

            val failureState = consumeItemsUntilPredicate { state ->
                state.inviteProgress.isFailure()
            }.last()
            assertThat(failureState.inviteProgress.isFailure()).isTrue()
        }
    }

    @Test
    fun `present - permission to post`() = runTest {
        val room = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
                canRedactOwnResult = { Result.success(true) },
                canRedactOtherResult = { Result.success(true) },
                canUserJoinCallResult = { Result.success(true) },
                canUserPinUnpinResult = { Result.success(true) },
                canUserSendMessageResult = { _, messageEventType ->
                    when (messageEventType) {
                        MessageEventType.ROOM_MESSAGE -> Result.success(true)
                        MessageEventType.REACTION -> Result.success(true)
                        else -> lambdaError()
                    }
                },
            ),
            typingNoticeResult = { Result.success(Unit) },
        )
        val presenter = createMessagesPresenter(joinedRoom = room)
        presenter.testWithLifecycleOwner {
            skipItems(1)
            val state = awaitItem()
            assertThat(state.userEventPermissions.canSendMessage).isTrue()
        }
    }

    @Test
    fun `present - no permission to post`() = runTest {
        val room = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
                canRedactOwnResult = { Result.success(true) },
                canRedactOtherResult = { Result.success(true) },
                canUserJoinCallResult = { Result.success(true) },
                canUserPinUnpinResult = { Result.success(true) },
                canUserSendMessageResult = { _, messageEventType ->
                    when (messageEventType) {
                        MessageEventType.ROOM_MESSAGE -> Result.success(false)
                        MessageEventType.REACTION -> Result.success(false)
                        else -> lambdaError()
                    }
                },
            ),
            typingNoticeResult = { Result.success(Unit) },
        )
        val presenter = createMessagesPresenter(joinedRoom = room)
        presenter.testWithLifecycleOwner {
            // Default value
            assertThat(awaitItem().userEventPermissions.canSendMessage).isTrue()
            assertThat(awaitItem().userEventPermissions.canSendMessage).isFalse()
        }
    }

    @Test
    fun `present - permission to redact own`() = runTest {
        val joinedRoom = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
                canRedactOwnResult = { Result.success(true) },
                canUserSendMessageResult = { _, _ -> Result.success(true) },
                canRedactOtherResult = { Result.success(false) },
                canUserJoinCallResult = { Result.success(true) },
                canUserPinUnpinResult = { Result.success(true) },
            ),
            typingNoticeResult = { Result.success(Unit) },
        )
        val presenter = createMessagesPresenter(joinedRoom = joinedRoom)
        presenter.testWithLifecycleOwner {
            val initialState = consumeItemsUntilPredicate { it.userEventPermissions.canRedactOwn }.last()
            assertThat(initialState.userEventPermissions.canRedactOwn).isTrue()
            assertThat(initialState.userEventPermissions.canRedactOther).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - permission to redact other`() = runTest {
        val joinedRoom = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
                canRedactOtherResult = { Result.success(true) },
                canUserSendMessageResult = { _, _ -> Result.success(true) },
                canRedactOwnResult = { Result.success(false) },
                canUserJoinCallResult = { Result.success(true) },
                canUserPinUnpinResult = { Result.success(true) },
            ),
            typingNoticeResult = { Result.success(Unit) },
        )
        val presenter = createMessagesPresenter(joinedRoom = joinedRoom)
        presenter.testWithLifecycleOwner {
            val initialState = consumeItemsUntilPredicate { it.userEventPermissions.canRedactOther }.last()
            assertThat(initialState.userEventPermissions.canRedactOwn).isFalse()
            assertThat(initialState.userEventPermissions.canRedactOther).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - handle action reply to a poll`() = runTest {
        val composerRecorder = EventsRecorder<MessageComposerEvents>()
        val presenter = createMessagesPresenter(
            messageComposerPresenter = { aMessageComposerState(eventSink = composerRecorder) },
        )
        presenter.testWithLifecycleOwner {
            val initialState = awaitItem()
            val poll = aMessageEvent(
                content = aTimelineItemPollContent()
            )
            initialState.eventSink(MessagesEvents.HandleAction(TimelineItemAction.Reply, poll))
            skipItems(1)
            composerRecorder.assertSingle(
                MessageComposerEvents.SetMode(
                    composerMode = MessageComposerMode.Reply(
                        replyToDetails = InReplyToDetails.Loading(AN_EVENT_ID),
                        hideImage = false,
                    )
                )
            )
        }
    }

    @Test
    fun `present - handle action pin`() = runTest {
        val successPinEventLambda = lambdaRecorder { _: EventId -> Result.success(true) }
        val failurePinEventLambda = lambdaRecorder { _: EventId -> Result.failure<Boolean>(AN_EXCEPTION) }
        val analyticsService = FakeAnalyticsService()
        val timeline = FakeTimeline()
        val room = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
                canUserSendMessageResult = { _, _ -> Result.success(true) },
                canRedactOwnResult = { Result.success(true) },
                canRedactOtherResult = { Result.success(true) },
                canUserJoinCallResult = { Result.success(true) },
                canUserPinUnpinResult = { Result.success(true) },
            ),
            liveTimeline = timeline,
            typingNoticeResult = { Result.success(Unit) },
        )
        val presenter = createMessagesPresenter(joinedRoom = room, analyticsService = analyticsService)
        presenter.testWithLifecycleOwner {
            val messageEvent = aMessageEvent(
                content = aTimelineItemTextContent()
            )
            val initialState = awaitItem()

            timeline.pinEventLambda = successPinEventLambda
            initialState.eventSink(MessagesEvents.HandleAction(TimelineItemAction.Pin, messageEvent))
            assert(successPinEventLambda).isCalledOnce().with(value(messageEvent.eventId))

            timeline.pinEventLambda = failurePinEventLambda
            initialState.eventSink(MessagesEvents.HandleAction(TimelineItemAction.Pin, messageEvent))
            assert(failurePinEventLambda).isCalledOnce().with(value(messageEvent.eventId))
            skipItems(1)
            assertThat(awaitItem().snackbarMessage).isNotNull()
            assertThat(analyticsService.capturedEvents).containsExactly(
                PinUnpinAction(kind = PinUnpinAction.Kind.Pin, from = PinUnpinAction.From.Timeline),
                PinUnpinAction(kind = PinUnpinAction.Kind.Pin, from = PinUnpinAction.From.Timeline)
            )
        }
    }

    @Test
    fun `present - handle action unpin`() = runTest {
        val successUnpinEventLambda = lambdaRecorder { _: EventId -> Result.success(true) }
        val failureUnpinEventLambda = lambdaRecorder { _: EventId -> Result.failure<Boolean>(AN_EXCEPTION) }
        val timeline = FakeTimeline()
        val analyticsService = FakeAnalyticsService()
        val room = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
                canUserSendMessageResult = { _, _ -> Result.success(true) },
                canRedactOwnResult = { Result.success(true) },
                canRedactOtherResult = { Result.success(true) },
                canUserJoinCallResult = { Result.success(true) },
                canUserPinUnpinResult = { Result.success(true) },
            ),
            liveTimeline = timeline,
            typingNoticeResult = { Result.success(Unit) },
        )
        val presenter = createMessagesPresenter(joinedRoom = room, analyticsService = analyticsService)
        presenter.testWithLifecycleOwner {
            val messageEvent = aMessageEvent(
                content = aTimelineItemTextContent()
            )
            val initialState = awaitItem()

            timeline.unpinEventLambda = successUnpinEventLambda
            initialState.eventSink(MessagesEvents.HandleAction(TimelineItemAction.Unpin, messageEvent))
            assert(successUnpinEventLambda).isCalledOnce().with(value(messageEvent.eventId))

            timeline.unpinEventLambda = failureUnpinEventLambda
            initialState.eventSink(MessagesEvents.HandleAction(TimelineItemAction.Unpin, messageEvent))
            assert(failureUnpinEventLambda).isCalledOnce().with(value(messageEvent.eventId))
            skipItems(1)
            assertThat(awaitItem().snackbarMessage).isNotNull()
            assertThat(analyticsService.capturedEvents).containsExactly(
                PinUnpinAction(kind = PinUnpinAction.Kind.Unpin, from = PinUnpinAction.From.Timeline),
                PinUnpinAction(kind = PinUnpinAction.Kind.Unpin, from = PinUnpinAction.From.Timeline)
            )
        }
    }

    @Test
    fun `present - handle action edit caption`() = runTest {
        val messageEvent = aMessageEvent(
            content = aTimelineItemImageContent(
                caption = A_CAPTION,
            )
        )
        val composerRecorder = EventsRecorder<MessageComposerEvents>()
        val presenter = createMessagesPresenter(
            messageComposerPresenter = { aMessageComposerState(eventSink = composerRecorder) },
        )
        presenter.testWithLifecycleOwner {
            val initialState = awaitItem()
            initialState.eventSink(MessagesEvents.HandleAction(TimelineItemAction.EditCaption, messageEvent))
            awaitItem()
            composerRecorder.assertSingle(
                MessageComposerEvents.SetMode(
                    composerMode = MessageComposerMode.EditCaption(
                        eventOrTransactionId = AN_EVENT_ID.toEventOrTransactionId(),
                        content = A_CAPTION,
                        showCaptionCompatibilityWarning = true,
                    )
                )
            )
        }
    }

    @Test
    fun `present - handle action edit caption without warning`() = runTest {
        val messageEvent = aMessageEvent(
            content = aTimelineItemImageContent(
                caption = A_CAPTION,
            )
        )
        val composerRecorder = EventsRecorder<MessageComposerEvents>()
        val presenter = createMessagesPresenter(
            messageComposerPresenter = { aMessageComposerState(eventSink = composerRecorder) },
            featureFlagService = FakeFeatureFlagService(
                initialState = mapOf(FeatureFlags.MediaCaptionWarning.key to false)
            )
        )
        presenter.testWithLifecycleOwner {
            val initialState = awaitItem()
            initialState.eventSink(MessagesEvents.HandleAction(TimelineItemAction.EditCaption, messageEvent))
            awaitItem()
            composerRecorder.assertSingle(
                MessageComposerEvents.SetMode(
                    composerMode = MessageComposerMode.EditCaption(
                        eventOrTransactionId = AN_EVENT_ID.toEventOrTransactionId(),
                        content = A_CAPTION,
                        showCaptionCompatibilityWarning = false,
                    )
                )
            )
        }
    }

    @Test
    fun `present - handle action add caption`() = runTest {
        val composerRecorder = EventsRecorder<MessageComposerEvents>()
        val presenter = createMessagesPresenter(
            messageComposerPresenter = { aMessageComposerState(eventSink = composerRecorder) },
        )
        val messageEvent = aMessageEvent(
            content = aTimelineItemImageContent(
                caption = null,
            )
        )
        presenter.testWithLifecycleOwner {
            val initialState = awaitItem()
            initialState.eventSink(MessagesEvents.HandleAction(TimelineItemAction.AddCaption, messageEvent))
            awaitItem()
            composerRecorder.assertSingle(
                MessageComposerEvents.SetMode(
                    composerMode = MessageComposerMode.EditCaption(
                        eventOrTransactionId = AN_EVENT_ID.toEventOrTransactionId(),
                        content = "",
                        showCaptionCompatibilityWarning = true,
                    )
                )
            )
        }
    }

    @Test
    fun `present - handle action add caption without warning`() = runTest {
        val composerRecorder = EventsRecorder<MessageComposerEvents>()
        val presenter = createMessagesPresenter(
            messageComposerPresenter = { aMessageComposerState(eventSink = composerRecorder) },
            featureFlagService = FakeFeatureFlagService(
                initialState = mapOf(FeatureFlags.MediaCaptionWarning.key to false)
            )
        )
        val messageEvent = aMessageEvent(
            content = aTimelineItemImageContent(
                caption = null,
            )
        )
        presenter.testWithLifecycleOwner {
            val initialState = awaitItem()
            initialState.eventSink(MessagesEvents.HandleAction(TimelineItemAction.AddCaption, messageEvent))
            awaitItem()
            composerRecorder.assertSingle(
                MessageComposerEvents.SetMode(
                    composerMode = MessageComposerMode.EditCaption(
                        eventOrTransactionId = AN_EVENT_ID.toEventOrTransactionId(),
                        content = "",
                        showCaptionCompatibilityWarning = false,
                    )
                )
            )
        }
    }

    @Test
    fun `present - handle action remove caption`() = runTest {
        val messageEvent = aMessageEvent(
            content = aTimelineItemImageContent(
                caption = A_CAPTION,
            )
        )
        val editCaptionLambda = lambdaRecorder { _: EventOrTransactionId, _: String?, _: String? -> Result.success(Unit) }
        val timeline = FakeTimeline().apply {
            this.editCaptionLambda = editCaptionLambda
        }
        val room = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
                canUserSendMessageResult = { _, _ -> Result.success(true) },
                canRedactOwnResult = { Result.success(true) },
                canRedactOtherResult = { Result.success(true) },
                canUserJoinCallResult = { Result.success(true) },
                canUserPinUnpinResult = { Result.success(true) },
            ),
            liveTimeline = timeline,
            typingNoticeResult = { Result.success(Unit) },
        )
        val presenter = createMessagesPresenter(
            joinedRoom = room,
        )
        presenter.testWithLifecycleOwner {
            skipItems(1)
            val initialState = awaitItem()
            initialState.eventSink(MessagesEvents.HandleAction(TimelineItemAction.RemoveCaption, messageEvent))
            editCaptionLambda.assertions().isCalledOnce().with(value(AN_EVENT_ID.toEventOrTransactionId()), value(null), value(null))
        }
    }

    @Test
    fun `present - handle action view in timeline, it should have no effect`() = runTest {
        val messageEvent = aMessageEvent(
            content = aTimelineItemTextContent()
        )
        val presenter = createMessagesPresenter()
        presenter.testWithLifecycleOwner {
            skipItems(1)
            val initialState = awaitItem()
            initialState.eventSink(MessagesEvents.HandleAction(TimelineItemAction.ViewInTimeline, messageEvent))
            // No op!
        }
    }

    @Test
    fun `present - room with successor room includes successor info in state`() = runTest {
        val successorRoomId = RoomId("!successor:server.org")
        val successorReason = "This room has been moved to a new location"
        val room = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
                canUserSendMessageResult = { _, _ -> Result.success(true) },
                canRedactOwnResult = { Result.success(true) },
                canRedactOtherResult = { Result.success(true) },
                canUserJoinCallResult = { Result.success(true) },
                canUserPinUnpinResult = { Result.success(true) },
                initialRoomInfo = aRoomInfo(
                    successorRoom = SuccessorRoom(
                        roomId = successorRoomId,
                        reason = successorReason
                    )
                )
            ),
            typingNoticeResult = { Result.success(Unit) },
        )
        val presenter = createMessagesPresenter(joinedRoom = room)
        presenter.testWithLifecycleOwner {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.successorRoom).isNotNull()
            assertThat(initialState.successorRoom?.roomId).isEqualTo(successorRoomId)
            assertThat(initialState.successorRoom?.reason).isEqualTo(successorReason)
        }
    }

    @Test
    fun `present - room without successor room has null successor info in state`() = runTest {
        val room = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
                canUserSendMessageResult = { _, _ -> Result.success(true) },
                canRedactOwnResult = { Result.success(true) },
                canRedactOtherResult = { Result.success(true) },
                canUserJoinCallResult = { Result.success(true) },
                canUserPinUnpinResult = { Result.success(true) },
                initialRoomInfo = aRoomInfo(successorRoom = null)
            ),
            typingNoticeResult = { Result.success(Unit) },
        )
        val presenter = createMessagesPresenter(joinedRoom = room)
        presenter.testWithLifecycleOwner {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.successorRoom).isNull()
        }
    }

    @Test
    fun `present - when room is encrypted and a DM, the DM user's identity state is fetched onResume`() = runTest {
        val room = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
                sessionId = A_SESSION_ID,
                canUserSendMessageResult = { _, _ -> Result.success(true) },
                canRedactOwnResult = { Result.success(true) },
                canRedactOtherResult = { Result.success(true) },
                canUserJoinCallResult = { Result.success(true) },
                canUserPinUnpinResult = { Result.success(true) },
                initialRoomInfo = aRoomInfo(isDirect = true, isEncrypted = true)
            ).apply {
                givenRoomMembersState(RoomMembersState.Ready(persistentListOf(aRoomMember(userId = A_SESSION_ID), aRoomMember(userId = A_USER_ID_2))))
            },
            typingNoticeResult = { Result.success(Unit) },
        )
        val encryptionService = FakeEncryptionService(getUserIdentityResult = { Result.success(IdentityState.Verified) })

        val presenter = createMessagesPresenter(joinedRoom = room, encryptionService = encryptionService)
        val lifecycleOwner = FakeLifecycleOwner()
        presenter.testWithLifecycleOwner(lifecycleOwner) {
            val initialState = awaitItem()
            assertThat(initialState.dmUserVerificationState).isNull()

            skipItems(1)
            ensureAllEventsConsumed()

            lifecycleOwner.givenState(Lifecycle.State.RESUMED)
            assertThat(awaitItem().dmUserVerificationState).isEqualTo(IdentityState.Verified)
        }
    }

    private fun TestScope.createMessagesPresenter(
        coroutineDispatchers: CoroutineDispatchers = testCoroutineDispatchers(),
        joinedRoom: FakeJoinedRoom = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
                canUserSendMessageResult = { _, _ -> Result.success(true) },
                canRedactOwnResult = { Result.success(true) },
                canRedactOtherResult = { Result.success(true) },
                canUserJoinCallResult = { Result.success(true) },
                canUserPinUnpinResult = { Result.success(true) },
            ).apply {
                givenRoomInfo(aRoomInfo(id = roomId, name = ""))
            },
            liveTimeline = FakeTimeline(),
            typingNoticeResult = { Result.success(Unit) },
        ),
        navigator: FakeMessagesNavigator = FakeMessagesNavigator(),
        featureFlagService: FeatureFlagService = FakeFeatureFlagService(),
        clipboardHelper: FakeClipboardHelper = FakeClipboardHelper(),
        analyticsService: FakeAnalyticsService = FakeAnalyticsService(),
        timelineEventSink: (TimelineEvents) -> Unit = {},
        permalinkParser: PermalinkParser = FakePermalinkParser(),
        messageComposerPresenter: Presenter<MessageComposerState> = Presenter {
            aMessageComposerState(
                // Use TextEditorState.Markdown, so that we can request focus manually.
                textEditorState = aTextEditorStateMarkdown(initialText = "", initialFocus = false)
            )
        },
        roomMemberModerationPresenter: Presenter<RoomMemberModerationState> = Presenter {
            aRoomMemberModerationState()
        },
        encryptionService: FakeEncryptionService = FakeEncryptionService(),
        actionListEventSink: (ActionListEvents) -> Unit = {},
    ): MessagesPresenter {
        return MessagesPresenter(
            room = joinedRoom,
            composerPresenter = messageComposerPresenter,
            voiceMessageComposerPresenter = { aVoiceMessageComposerState() },
            timelinePresenter = { aTimelineState(eventSink = timelineEventSink) },
            timelineProtectionPresenter = { aTimelineProtectionState() },
            actionListPresenter = { anActionListState(eventSink = actionListEventSink) },
            customReactionPresenter = { aCustomReactionState() },
            reactionSummaryPresenter = { aReactionSummaryState() },
            readReceiptBottomSheetPresenter = { aReadReceiptBottomSheetState() },
            identityChangeStatePresenter = { anIdentityChangeState() },
            linkPresenter = { aLinkState() },
            pinnedMessagesBannerPresenter = { aLoadedPinnedMessagesBannerState() },
            roomCallStatePresenter = { aStandByCallState() },
            roomMemberModerationPresenter = roomMemberModerationPresenter,
            syncService = FakeSyncService(),
            snackbarDispatcher = SnackbarDispatcher(),
            navigator = navigator,
            clipboardHelper = clipboardHelper,
            featureFlagsService = featureFlagService,
            buildMeta = aBuildMeta(),
            dispatchers = coroutineDispatchers,
            htmlConverterProvider = FakeHtmlConverterProvider(),
            timelineController = TimelineController(joinedRoom),
            permalinkParser = permalinkParser,
            encryptionService = encryptionService,
            analyticsService = analyticsService,
        )
    }
}

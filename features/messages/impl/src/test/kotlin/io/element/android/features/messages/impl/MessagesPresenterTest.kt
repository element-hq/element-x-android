/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl

import android.net.Uri
import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import im.vector.app.features.analytics.plan.PinUnpinAction
import io.element.android.features.messages.impl.actionlist.ActionListState
import io.element.android.features.messages.impl.actionlist.FakeActionListPresenter
import io.element.android.features.messages.impl.actionlist.model.TimelineItemAction
import io.element.android.features.messages.impl.crypto.sendfailure.resolve.aResolveVerifiedUserSendFailureState
import io.element.android.features.messages.impl.draft.FakeComposerDraftService
import io.element.android.features.messages.impl.fixtures.aMessageEvent
import io.element.android.features.messages.impl.fixtures.aTimelineItemsFactoryCreator
import io.element.android.features.messages.impl.messagecomposer.DefaultMessageComposerContext
import io.element.android.features.messages.impl.messagecomposer.FakeRoomAliasSuggestionsDataSource
import io.element.android.features.messages.impl.messagecomposer.MessageComposerPresenter
import io.element.android.features.messages.impl.messagecomposer.TestRichTextEditorStateFactory
import io.element.android.features.messages.impl.messagecomposer.suggestions.SuggestionsProcessor
import io.element.android.features.messages.impl.pinned.banner.aLoadedPinnedMessagesBannerState
import io.element.android.features.messages.impl.timeline.TimelineController
import io.element.android.features.messages.impl.timeline.TimelineItemIndexer
import io.element.android.features.messages.impl.timeline.TimelinePresenter
import io.element.android.features.messages.impl.timeline.components.customreaction.CustomReactionPresenter
import io.element.android.features.messages.impl.timeline.components.customreaction.FakeEmojibaseProvider
import io.element.android.features.messages.impl.timeline.components.reactionsummary.ReactionSummaryPresenter
import io.element.android.features.messages.impl.timeline.components.receipt.bottomsheet.ReadReceiptBottomSheetPresenter
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemFileContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemImageContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemTextContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemVideoContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemPollContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemTextContent
import io.element.android.features.messages.impl.typing.aTypingNotificationState
import io.element.android.features.messages.impl.utils.FakeTextPillificationHelper
import io.element.android.features.messages.impl.voicemessages.composer.VoiceMessageComposerPlayer
import io.element.android.features.messages.impl.voicemessages.composer.VoiceMessageComposerPresenter
import io.element.android.features.messages.impl.voicemessages.timeline.FakeRedactedVoiceMessageManager
import io.element.android.features.messages.test.FakeMessageComposerContext
import io.element.android.features.messages.test.timeline.FakeHtmlConverterProvider
import io.element.android.features.networkmonitor.test.FakeNetworkMonitor
import io.element.android.features.poll.api.actions.EndPollAction
import io.element.android.features.poll.test.actions.FakeEndPollAction
import io.element.android.features.poll.test.actions.FakeSendPollResponseAction
import io.element.android.libraries.androidutils.clipboard.FakeClipboardHelper
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.featureflag.test.FakeFeatureFlagService
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.TransactionId
import io.element.android.libraries.matrix.api.core.UniqueId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.api.permalink.PermalinkParser
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.MatrixRoomMembersState
import io.element.android.libraries.matrix.api.room.MessageEventType
import io.element.android.libraries.matrix.api.room.RoomMembershipState
import io.element.android.libraries.matrix.test.AN_AVATAR_URL
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID_2
import io.element.android.libraries.matrix.test.A_THROWABLE
import io.element.android.libraries.matrix.test.A_UNIQUE_ID
import io.element.android.libraries.matrix.test.core.aBuildMeta
import io.element.android.libraries.matrix.test.permalink.FakePermalinkBuilder
import io.element.android.libraries.matrix.test.permalink.FakePermalinkParser
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.libraries.matrix.test.room.aRoomInfo
import io.element.android.libraries.matrix.test.room.aRoomMember
import io.element.android.libraries.matrix.test.timeline.FakeTimeline
import io.element.android.libraries.matrix.ui.messages.RoomMemberProfilesCache
import io.element.android.libraries.matrix.ui.messages.reply.InReplyToDetails
import io.element.android.libraries.mediapickers.test.FakePickerProvider
import io.element.android.libraries.mediaplayer.test.FakeMediaPlayer
import io.element.android.libraries.mediaupload.api.MediaSender
import io.element.android.libraries.mediaupload.test.FakeMediaPreProcessor
import io.element.android.libraries.mediaviewer.test.FakeLocalMediaFactory
import io.element.android.libraries.permissions.api.PermissionsPresenter
import io.element.android.libraries.permissions.test.FakePermissionsPresenter
import io.element.android.libraries.permissions.test.FakePermissionsPresenterFactory
import io.element.android.libraries.preferences.test.InMemorySessionPreferencesStore
import io.element.android.libraries.textcomposer.mentions.MentionSpanProvider
import io.element.android.libraries.textcomposer.model.MessageComposerMode
import io.element.android.libraries.voicerecorder.test.FakeVoiceRecorder
import io.element.android.services.analytics.test.FakeAnalyticsService
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.consumeItemsUntilPredicate
import io.element.android.tests.testutils.consumeItemsUntilTimeout
import io.element.android.tests.testutils.lambda.assert
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import io.element.android.tests.testutils.testCoroutineDispatchers
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

@Suppress("LargeClass")
class MessagesPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    private val mockMediaUrl: Uri = mockk("localMediaUri")

    @Test
    fun `present - initial state`() = runTest {
        val presenter = createMessagesPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = consumeItemsUntilTimeout().last()
            assertThat(initialState.roomId).isEqualTo(A_ROOM_ID)
            assertThat(initialState.roomName).isEqualTo(AsyncData.Success(""))
            assertThat(initialState.roomAvatar)
                .isEqualTo(AsyncData.Success(AvatarData(id = A_ROOM_ID.value, name = "", url = AN_AVATAR_URL, size = AvatarSize.TimelineRoom)))
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
        val room = FakeMatrixRoom(
            canUserSendMessageResult = { _, _ -> Result.success(true) },
            canRedactOwnResult = { Result.success(true) },
            canRedactOtherResult = { Result.success(true) },
            canUserJoinCallResult = { Result.success(true) },
            typingNoticeResult = { Result.success(Unit) },
            canUserPinUnpinResult = { Result.success(true) },
        )
        assertThat(room.markAsReadCalls).isEmpty()
        val presenter = createMessagesPresenter(matrixRoom = room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            runCurrent()
            assertThat(room.setUnreadFlagCalls).isEqualTo(listOf(false))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - call is disabled if user cannot join it even if there is an ongoing call`() = runTest {
        val room = FakeMatrixRoom(
            canUserJoinCallResult = { Result.success(false) },
            canUserSendMessageResult = { _, _ -> Result.success(true) },
            canRedactOwnResult = { Result.success(true) },
            canRedactOtherResult = { Result.success(true) },
            typingNoticeResult = { Result.success(Unit) },
            canUserPinUnpinResult = { Result.success(true) },
        ).apply {
            givenRoomInfo(aRoomInfo(hasRoomCall = true))
        }
        val presenter = createMessagesPresenter(matrixRoom = room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = consumeItemsUntilTimeout().last()
            assertThat(initialState.callState).isEqualTo(RoomCallState.DISABLED)
        }
    }

    @Test
    fun `present - handle toggling a reaction`() = runTest {
        val coroutineDispatchers = testCoroutineDispatchers(useUnconfinedTestDispatcher = true)
        val toggleReactionSuccess = lambdaRecorder { _: String, _: UniqueId -> Result.success(Unit) }
        val toggleReactionFailure = lambdaRecorder { _: String, _: UniqueId -> Result.failure<Unit>(IllegalStateException("Failed to send reaction")) }

        val timeline = FakeTimeline().apply {
            this.toggleReactionLambda = toggleReactionSuccess
        }
        val room = FakeMatrixRoom(
            liveTimeline = timeline,
            canUserSendMessageResult = { _, _ -> Result.success(true) },
            canRedactOwnResult = { Result.success(true) },
            canRedactOtherResult = { Result.success(true) },
            canUserJoinCallResult = { Result.success(true) },
            typingNoticeResult = { Result.success(Unit) },
            canUserPinUnpinResult = { Result.success(true) },
        )
        val presenter = createMessagesPresenter(matrixRoom = room, coroutineDispatchers = coroutineDispatchers)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            initialState.eventSink.invoke(MessagesEvents.ToggleReaction("👍", A_UNIQUE_ID))
            // No crashes when sending a reaction failed
            timeline.apply { toggleReactionLambda = toggleReactionFailure }
            initialState.eventSink.invoke(MessagesEvents.ToggleReaction("👍", A_UNIQUE_ID))
            assertThat(awaitItem().actionListState.target).isEqualTo(ActionListState.Target.None)

            assert(toggleReactionSuccess)
                .isCalledOnce()
                .with(value("👍"), value(A_UNIQUE_ID))
            assert(toggleReactionFailure)
                .isCalledOnce()
                .with(value("👍"), value(A_UNIQUE_ID))
        }
    }

    @Test
    fun `present - handle toggling a reaction twice`() = runTest {
        val coroutineDispatchers = testCoroutineDispatchers(useUnconfinedTestDispatcher = true)
        val toggleReactionSuccess = lambdaRecorder { _: String, _: UniqueId -> Result.success(Unit) }

        val timeline = FakeTimeline().apply {
            this.toggleReactionLambda = toggleReactionSuccess
        }
        val room = FakeMatrixRoom(
            liveTimeline = timeline,
            canUserSendMessageResult = { _, _ -> Result.success(true) },
            canRedactOwnResult = { Result.success(true) },
            canRedactOtherResult = { Result.success(true) },
            canUserJoinCallResult = { Result.success(true) },
            typingNoticeResult = { Result.success(Unit) },
            canUserPinUnpinResult = { Result.success(true) },
        )
        val presenter = createMessagesPresenter(matrixRoom = room, coroutineDispatchers = coroutineDispatchers)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            initialState.eventSink.invoke(MessagesEvents.ToggleReaction("👍", A_UNIQUE_ID))
            initialState.eventSink.invoke(MessagesEvents.ToggleReaction("👍", A_UNIQUE_ID))
            assert(toggleReactionSuccess)
                .isCalledExactly(2)
                .withSequence(
                    listOf(value("👍"), value(A_UNIQUE_ID)),
                    listOf(value("👍"), value(A_UNIQUE_ID)),
                )
        }
    }

    @Test
    fun `present - handle action forward`() = runTest {
        val navigator = FakeMessagesNavigator()
        val presenter = createMessagesPresenter(navigator = navigator)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            initialState.eventSink.invoke(MessagesEvents.HandleAction(TimelineItemAction.Forward, aMessageEvent()))
            assertThat(awaitItem().actionListState.target).isEqualTo(ActionListState.Target.None)
            assertThat(navigator.onForwardEventClickedCount).isEqualTo(1)
        }
    }

    @Test
    fun `present - handle action copy`() = runTest {
        val clipboardHelper = FakeClipboardHelper()
        val event = aMessageEvent()
        val presenter = createMessagesPresenter(clipboardHelper = clipboardHelper)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            initialState.eventSink.invoke(MessagesEvents.HandleAction(TimelineItemAction.Copy, event))
            assertThat(awaitItem().actionListState.target).isEqualTo(ActionListState.Target.None)
            assertThat(clipboardHelper.clipboardContents).isEqualTo((event.content as TimelineItemTextContent).body)
        }
    }

    @Test
    fun `present - handle action copy link`() = runTest {
        val clipboardHelper = FakeClipboardHelper()
        val event = aMessageEvent()
        val matrixRoom = FakeMatrixRoom(
            eventPermalinkResult = { Result.success("a link") },
            canUserSendMessageResult = { _, _ -> Result.success(true) },
            canRedactOwnResult = { Result.success(true) },
            canRedactOtherResult = { Result.success(true) },
            canUserJoinCallResult = { Result.success(true) },
            typingNoticeResult = { Result.success(Unit) },
            canUserPinUnpinResult = { Result.success(true) },
        )
        val presenter = createMessagesPresenter(
            clipboardHelper = clipboardHelper,
            matrixRoom = matrixRoom,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            initialState.eventSink.invoke(MessagesEvents.HandleAction(TimelineItemAction.CopyLink, event))
            assertThat(awaitItem().actionListState.target).isEqualTo(ActionListState.Target.None)
            assertThat(clipboardHelper.clipboardContents).isEqualTo("a link")
        }
    }

    @Test
    fun `present - handle action reply`() = runTest {
        val presenter = createMessagesPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            initialState.eventSink.invoke(MessagesEvents.HandleAction(TimelineItemAction.Reply, aMessageEvent()))
            val finalState = awaitItem()
            assertThat(finalState.composerState.mode).isInstanceOf(MessageComposerMode.Reply::class.java)
            assertThat(finalState.actionListState.target).isEqualTo(ActionListState.Target.None)
        }
    }

    @Test
    fun `present - handle action reply to an event with no id does nothing`() = runTest {
        val presenter = createMessagesPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            initialState.eventSink.invoke(MessagesEvents.HandleAction(TimelineItemAction.Reply, aMessageEvent(eventId = null)))
            assertThat(initialState.actionListState.target).isEqualTo(ActionListState.Target.None)
            // Otherwise we would have some extra items here
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - handle action reply to an image media message`() = runTest {
        val presenter = createMessagesPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            val mediaMessage = aMessageEvent(
                content = TimelineItemImageContent(
                    body = "image.jpg",
                    formatted = null,
                    filename = null,
                    mediaSource = MediaSource(AN_AVATAR_URL),
                    thumbnailSource = null,
                    mimeType = MimeTypes.Jpeg,
                    blurhash = null,
                    width = 20,
                    height = 20,
                    aspectRatio = 1.0f,
                    fileExtension = "jpg",
                    formattedFileSize = "4MB"
                )
            )
            initialState.eventSink.invoke(MessagesEvents.HandleAction(TimelineItemAction.Reply, mediaMessage))
            val finalState = awaitItem()
            assertThat(finalState.composerState.mode).isInstanceOf(MessageComposerMode.Reply::class.java)
            val replyMode = finalState.composerState.mode as MessageComposerMode.Reply
            assertThat(replyMode.replyToDetails).isInstanceOf(InReplyToDetails.Loading::class.java)
            assertThat(finalState.actionListState.target).isEqualTo(ActionListState.Target.None)
        }
    }

    @Test
    fun `present - handle action reply to a video media message`() = runTest {
        val presenter = createMessagesPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            val mediaMessage = aMessageEvent(
                content = TimelineItemVideoContent(
                    body = "video.mp4",
                    formatted = null,
                    filename = null,
                    duration = 10.milliseconds,
                    videoSource = MediaSource(AN_AVATAR_URL),
                    thumbnailSource = MediaSource(AN_AVATAR_URL),
                    mimeType = MimeTypes.Mp4,
                    blurHash = null,
                    width = 20,
                    height = 20,
                    aspectRatio = 1.0f,
                    fileExtension = "mp4",
                    formattedFileSize = "50MB"
                )
            )
            initialState.eventSink.invoke(MessagesEvents.HandleAction(TimelineItemAction.Reply, mediaMessage))
            val finalState = awaitItem()
            assertThat(finalState.composerState.mode).isInstanceOf(MessageComposerMode.Reply::class.java)
            val replyMode = finalState.composerState.mode as MessageComposerMode.Reply
            assertThat(replyMode.replyToDetails).isInstanceOf(InReplyToDetails.Loading::class.java)
            assertThat(finalState.actionListState.target).isEqualTo(ActionListState.Target.None)
        }
    }

    @Test
    fun `present - handle action reply to a file media message`() = runTest {
        val presenter = createMessagesPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            val mediaMessage = aMessageEvent(
                content = TimelineItemFileContent(
                    body = "file.pdf",
                    fileSource = MediaSource(AN_AVATAR_URL),
                    thumbnailSource = MediaSource(AN_AVATAR_URL),
                    formattedFileSize = "10 MB",
                    mimeType = MimeTypes.Pdf,
                    fileExtension = "pdf",
                )
            )
            initialState.eventSink.invoke(MessagesEvents.HandleAction(TimelineItemAction.Reply, mediaMessage))
            val finalState = awaitItem()
            assertThat(finalState.composerState.mode).isInstanceOf(MessageComposerMode.Reply::class.java)
            val replyMode = finalState.composerState.mode as MessageComposerMode.Reply
            assertThat(replyMode.replyToDetails).isInstanceOf(InReplyToDetails.Loading::class.java)
            assertThat(finalState.actionListState.target).isEqualTo(ActionListState.Target.None)
        }
    }

    @Test
    fun `present - handle action edit`() = runTest {
        val presenter = createMessagesPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            initialState.eventSink.invoke(MessagesEvents.HandleAction(TimelineItemAction.Edit, aMessageEvent()))
            val finalState = awaitItem()
            assertThat(finalState.composerState.mode).isInstanceOf(MessageComposerMode.Edit::class.java)
            assertThat(finalState.actionListState.target).isEqualTo(ActionListState.Target.None)
        }
    }

    @Test
    fun `present - handle action edit poll`() = runTest {
        val navigator = FakeMessagesNavigator()
        val presenter = createMessagesPresenter(navigator = navigator)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            initialState.eventSink.invoke(MessagesEvents.HandleAction(TimelineItemAction.Edit, aMessageEvent(content = aTimelineItemPollContent())))
            assertThat(navigator.onEditPollClickedCount).isEqualTo(1)
        }
    }

    @Test
    fun `present - handle action end poll`() = runTest {
        val endPollAction = FakeEndPollAction()
        val presenter = createMessagesPresenter(endPollAction = endPollAction)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            endPollAction.verifyExecutionCount(0)
            initialState.eventSink.invoke(MessagesEvents.HandleAction(TimelineItemAction.EndPoll, aMessageEvent(content = aTimelineItemPollContent())))
            delay(1)
            endPollAction.verifyExecutionCount(1)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - handle action redact`() = runTest {
        val coroutineDispatchers = testCoroutineDispatchers(useUnconfinedTestDispatcher = true)

        val liveTimeline = FakeTimeline()
        val matrixRoom = FakeMatrixRoom(
            liveTimeline = liveTimeline,
            canUserSendMessageResult = { _, _ -> Result.success(true) },
            canRedactOwnResult = { Result.success(true) },
            canRedactOtherResult = { Result.success(true) },
            canUserJoinCallResult = { Result.success(true) },
            typingNoticeResult = { Result.success(Unit) },
            canUserPinUnpinResult = { Result.success(true) },
        )

        val redactEventLambda = lambdaRecorder { _: EventId?, _: TransactionId?, _: String? -> Result.success(Unit) }
        liveTimeline.redactEventLambda = redactEventLambda

        val presenter = createMessagesPresenter(matrixRoom = matrixRoom, coroutineDispatchers = coroutineDispatchers)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            val messageEvent = aMessageEvent()
            initialState.eventSink.invoke(MessagesEvents.HandleAction(TimelineItemAction.Redact, messageEvent))
            assertThat(awaitItem().actionListState.target).isEqualTo(ActionListState.Target.None)
            assert(redactEventLambda)
                .isCalledOnce()
                .with(value(messageEvent.eventId), value(messageEvent.transactionId), value(null))
        }
    }

    @Test
    fun `present - handle action report content`() = runTest {
        val navigator = FakeMessagesNavigator()
        val presenter = createMessagesPresenter(navigator = navigator)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            initialState.eventSink.invoke(MessagesEvents.HandleAction(TimelineItemAction.ReportContent, aMessageEvent()))
            assertThat(awaitItem().actionListState.target).isEqualTo(ActionListState.Target.None)
            assertThat(navigator.onReportContentClickedCount).isEqualTo(1)
        }
    }

    @Test
    fun `present - handle dismiss action`() = runTest {
        val presenter = createMessagesPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            initialState.eventSink.invoke(MessagesEvents.Dismiss)
            assertThat(awaitItem().actionListState.target).isEqualTo(ActionListState.Target.None)
        }
    }

    @Test
    fun `present - handle action show developer info`() = runTest {
        val navigator = FakeMessagesNavigator()
        val presenter = createMessagesPresenter(navigator = navigator)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            initialState.eventSink.invoke(MessagesEvents.HandleAction(TimelineItemAction.ViewSource, aMessageEvent()))
            assertThat(awaitItem().actionListState.target).isEqualTo(ActionListState.Target.None)
            assertThat(navigator.onShowEventDebugInfoClickedCount).isEqualTo(1)
        }
    }

    @Test
    fun `present - shows prompt to reinvite users in DM`() = runTest {
        val room = FakeMatrixRoom(
            sessionId = A_SESSION_ID,
            isDirect = true,
            activeMemberCount = 1L,
            canUserSendMessageResult = { _, _ -> Result.success(true) },
            canRedactOwnResult = { Result.success(true) },
            canRedactOtherResult = { Result.success(true) },
            canUserJoinCallResult = { Result.success(true) },
            typingNoticeResult = { Result.success(Unit) },
            canUserPinUnpinResult = { Result.success(true) },
        )
        val presenter = createMessagesPresenter(matrixRoom = room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            // Initially the composer doesn't have focus, so we don't show the alert
            assertThat(initialState.showReinvitePrompt).isFalse()
            // When the input field is focused we show the alert
            initialState.composerState.textEditorState.requestFocus()
            val focusedState = consumeItemsUntilPredicate(timeout = 250.milliseconds) { state ->
                state.showReinvitePrompt
            }.last()
            assertThat(focusedState.showReinvitePrompt).isTrue()
            // If it's dismissed then we stop showing the alert
            initialState.eventSink(MessagesEvents.InviteDialogDismissed(InviteDialogAction.Cancel))
            val dismissedState = consumeItemsUntilPredicate(timeout = 250.milliseconds) { state ->
                !state.showReinvitePrompt
            }.last()
            assertThat(dismissedState.showReinvitePrompt).isFalse()
        }
    }

    @Test
    fun `present - doesn't show reinvite prompt in non-direct room`() = runTest {
        val room = FakeMatrixRoom(
            sessionId = A_SESSION_ID,
            isDirect = false,
            activeMemberCount = 1L,
            canUserSendMessageResult = { _, _ -> Result.success(true) },
            canRedactOwnResult = { Result.success(true) },
            canRedactOtherResult = { Result.success(true) },
            canUserJoinCallResult = { Result.success(true) },
            typingNoticeResult = { Result.success(Unit) },
            canUserPinUnpinResult = { Result.success(true) },
        )
        val presenter = createMessagesPresenter(matrixRoom = room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            assertThat(initialState.showReinvitePrompt).isFalse()
            initialState.composerState.textEditorState.requestFocus()
            val focusedState = awaitItem()
            assertThat(focusedState.showReinvitePrompt).isFalse()
        }
    }

    @Test
    fun `present - doesn't show reinvite prompt if other party is present`() = runTest {
        val room = FakeMatrixRoom(
            sessionId = A_SESSION_ID,
            isDirect = true,
            activeMemberCount = 2L,
            canUserSendMessageResult = { _, _ -> Result.success(true) },
            canRedactOwnResult = { Result.success(true) },
            canRedactOtherResult = { Result.success(true) },
            canUserJoinCallResult = { Result.success(true) },
            typingNoticeResult = { Result.success(Unit) },
            canUserPinUnpinResult = { Result.success(true) },
        )
        val presenter = createMessagesPresenter(matrixRoom = room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            assertThat(initialState.showReinvitePrompt).isFalse()
            initialState.composerState.textEditorState.requestFocus()
            val focusedState = awaitItem()
            assertThat(focusedState.showReinvitePrompt).isFalse()
        }
    }

    @Test
    fun `present - handle reinviting other user when memberlist is ready`() = runTest {
        val inviteUserResult = lambdaRecorder { _: UserId -> Result.success(Unit) }
        val room = FakeMatrixRoom(
            sessionId = A_SESSION_ID,
            inviteUserResult = inviteUserResult,
            canUserSendMessageResult = { _, _ -> Result.success(true) },
            canRedactOwnResult = { Result.success(true) },
            canRedactOtherResult = { Result.success(true) },
            canUserJoinCallResult = { Result.success(true) },
            typingNoticeResult = { Result.success(Unit) },
            canUserPinUnpinResult = { Result.success(true) },
        )
        room.givenRoomMembersState(
            MatrixRoomMembersState.Ready(
                persistentListOf(
                    aRoomMember(userId = A_SESSION_ID, membership = RoomMembershipState.JOIN),
                    aRoomMember(userId = A_SESSION_ID_2, membership = RoomMembershipState.LEAVE),
                )
            )
        )
        val presenter = createMessagesPresenter(matrixRoom = room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
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
        val room = FakeMatrixRoom(
            sessionId = A_SESSION_ID,
            inviteUserResult = inviteUserResult,
            canUserSendMessageResult = { _, _ -> Result.success(true) },
            canRedactOwnResult = { Result.success(true) },
            canRedactOtherResult = { Result.success(true) },
            canUserJoinCallResult = { Result.success(true) },
            typingNoticeResult = { Result.success(Unit) },
            canUserPinUnpinResult = { Result.success(true) },
        )
        room.givenRoomMembersState(
            MatrixRoomMembersState.Error(
                failure = Throwable(),
                prevRoomMembers = persistentListOf(
                    aRoomMember(userId = A_SESSION_ID, membership = RoomMembershipState.JOIN),
                    aRoomMember(userId = A_SESSION_ID_2, membership = RoomMembershipState.LEAVE),
                )
            )
        )
        val presenter = createMessagesPresenter(matrixRoom = room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
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
        val room = FakeMatrixRoom(
            sessionId = A_SESSION_ID,
            canUserSendMessageResult = { _, _ -> Result.success(true) },
            canRedactOwnResult = { Result.success(true) },
            canRedactOtherResult = { Result.success(true) },
            canUserJoinCallResult = { Result.success(true) },
            typingNoticeResult = { Result.success(Unit) },
            canUserPinUnpinResult = { Result.success(true) },
        )
        room.givenRoomMembersState(MatrixRoomMembersState.Unknown)
        val presenter = createMessagesPresenter(matrixRoom = room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
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
        val room = FakeMatrixRoom(
            sessionId = A_SESSION_ID,
            inviteUserResult = { Result.failure(Throwable("Oops!")) },
            canUserSendMessageResult = { _, _ -> Result.success(true) },
            canRedactOwnResult = { Result.success(true) },
            canRedactOtherResult = { Result.success(true) },
            canUserJoinCallResult = { Result.success(true) },
            typingNoticeResult = { Result.success(Unit) },
            canUserPinUnpinResult = { Result.success(true) },
        )
        room.givenRoomMembersState(
            MatrixRoomMembersState.Ready(
                persistentListOf(
                    aRoomMember(userId = A_SESSION_ID, membership = RoomMembershipState.JOIN),
                    aRoomMember(userId = A_SESSION_ID_2, membership = RoomMembershipState.LEAVE),
                )
            )
        )
        val presenter = createMessagesPresenter(matrixRoom = room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
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
        val matrixRoom = FakeMatrixRoom(
            canUserSendMessageResult = { _, messageEventType ->
                when (messageEventType) {
                    MessageEventType.ROOM_MESSAGE -> Result.success(true)
                    MessageEventType.REACTION -> Result.success(true)
                    else -> lambdaError()
                }
            },
            canRedactOwnResult = { Result.success(true) },
            canRedactOtherResult = { Result.success(true) },
            canUserJoinCallResult = { Result.success(true) },
            typingNoticeResult = { Result.success(Unit) },
            canUserPinUnpinResult = { Result.success(true) },
        )
        val presenter = createMessagesPresenter(matrixRoom = matrixRoom)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val state = awaitFirstItem()
            assertThat(state.userEventPermissions.canSendMessage).isTrue()
        }
    }

    @Test
    fun `present - no permission to post`() = runTest {
        val matrixRoom = FakeMatrixRoom(
            canUserSendMessageResult = { _, messageEventType ->
                when (messageEventType) {
                    MessageEventType.ROOM_MESSAGE -> Result.success(false)
                    MessageEventType.REACTION -> Result.success(false)
                    else -> lambdaError()
                }
            },
            canRedactOwnResult = { Result.success(true) },
            canRedactOtherResult = { Result.success(true) },
            canUserJoinCallResult = { Result.success(true) },
            typingNoticeResult = { Result.success(Unit) },
            canUserPinUnpinResult = { Result.success(true) },
        )
        val presenter = createMessagesPresenter(matrixRoom = matrixRoom)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            // Default value
            assertThat(awaitItem().userEventPermissions.canSendMessage).isTrue()
            skipItems(1)
            assertThat(awaitItem().userEventPermissions.canSendMessage).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - permission to redact own`() = runTest {
        val matrixRoom = FakeMatrixRoom(
            canRedactOwnResult = { Result.success(true) },
            canUserSendMessageResult = { _, _ -> Result.success(true) },
            canRedactOtherResult = { Result.success(false) },
            canUserJoinCallResult = { Result.success(true) },
            typingNoticeResult = { Result.success(Unit) },
            canUserPinUnpinResult = { Result.success(true) },
        )
        val presenter = createMessagesPresenter(matrixRoom = matrixRoom)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = consumeItemsUntilPredicate { it.userEventPermissions.canRedactOwn }.last()
            assertThat(initialState.userEventPermissions.canRedactOwn).isTrue()
            assertThat(initialState.userEventPermissions.canRedactOther).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - permission to redact other`() = runTest {
        val matrixRoom = FakeMatrixRoom(
            canRedactOtherResult = { Result.success(true) },
            canUserSendMessageResult = { _, _ -> Result.success(true) },
            canRedactOwnResult = { Result.success(false) },
            canUserJoinCallResult = { Result.success(true) },
            typingNoticeResult = { Result.success(Unit) },
            canUserPinUnpinResult = { Result.success(true) },
        )
        val presenter = createMessagesPresenter(matrixRoom = matrixRoom)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = consumeItemsUntilPredicate { it.userEventPermissions.canRedactOther }.last()
            assertThat(initialState.userEventPermissions.canRedactOwn).isFalse()
            assertThat(initialState.userEventPermissions.canRedactOther).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - handle action reply to a poll`() = runTest {
        val presenter = createMessagesPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            val poll = aMessageEvent(
                content = aTimelineItemPollContent()
            )
            initialState.eventSink.invoke(MessagesEvents.HandleAction(TimelineItemAction.Reply, poll))
            val finalState = awaitItem()
            assertThat(finalState.composerState.mode).isInstanceOf(MessageComposerMode.Reply::class.java)
            val replyMode = finalState.composerState.mode as MessageComposerMode.Reply

            assertThat(replyMode.replyToDetails).isInstanceOf(InReplyToDetails.Loading::class.java)
            assertThat(finalState.actionListState.target).isEqualTo(ActionListState.Target.None)
        }
    }

    @Test
    fun `present - handle action pin`() = runTest {
        val successPinEventLambda = lambdaRecorder { _: EventId -> Result.success(true) }
        val failurePinEventLambda = lambdaRecorder { _: EventId -> Result.failure<Boolean>(A_THROWABLE) }
        val analyticsService = FakeAnalyticsService()
        val timeline = FakeTimeline()
        val room = FakeMatrixRoom(
            liveTimeline = timeline,
            canUserSendMessageResult = { _, _ -> Result.success(true) },
            canRedactOwnResult = { Result.success(true) },
            canRedactOtherResult = { Result.success(true) },
            canUserJoinCallResult = { Result.success(true) },
            typingNoticeResult = { Result.success(Unit) },
            canUserPinUnpinResult = { Result.success(true) },
        )
        val presenter = createMessagesPresenter(matrixRoom = room, analyticsService = analyticsService)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val messageEvent = aMessageEvent(
                content = aTimelineItemTextContent()
            )
            val initialState = awaitFirstItem()

            timeline.pinEventLambda = successPinEventLambda
            initialState.eventSink.invoke(MessagesEvents.HandleAction(TimelineItemAction.Pin, messageEvent))
            assert(successPinEventLambda).isCalledOnce().with(value(messageEvent.eventId))

            timeline.pinEventLambda = failurePinEventLambda
            initialState.eventSink.invoke(MessagesEvents.HandleAction(TimelineItemAction.Pin, messageEvent))
            assert(failurePinEventLambda).isCalledOnce().with(value(messageEvent.eventId))
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
        val failureUnpinEventLambda = lambdaRecorder { _: EventId -> Result.failure<Boolean>(A_THROWABLE) }
        val timeline = FakeTimeline()
        val analyticsService = FakeAnalyticsService()
        val room = FakeMatrixRoom(
            liveTimeline = timeline,
            canUserSendMessageResult = { _, _ -> Result.success(true) },
            canRedactOwnResult = { Result.success(true) },
            canRedactOtherResult = { Result.success(true) },
            canUserJoinCallResult = { Result.success(true) },
            typingNoticeResult = { Result.success(Unit) },
            canUserPinUnpinResult = { Result.success(true) },
        )
        val presenter = createMessagesPresenter(matrixRoom = room, analyticsService = analyticsService)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val messageEvent = aMessageEvent(
                content = aTimelineItemTextContent()
            )
            val initialState = awaitFirstItem()

            timeline.unpinEventLambda = successUnpinEventLambda
            initialState.eventSink.invoke(MessagesEvents.HandleAction(TimelineItemAction.Unpin, messageEvent))
            assert(successUnpinEventLambda).isCalledOnce().with(value(messageEvent.eventId))

            timeline.unpinEventLambda = failureUnpinEventLambda
            initialState.eventSink.invoke(MessagesEvents.HandleAction(TimelineItemAction.Unpin, messageEvent))
            assert(failureUnpinEventLambda).isCalledOnce().with(value(messageEvent.eventId))
            assertThat(awaitItem().snackbarMessage).isNotNull()
            assertThat(analyticsService.capturedEvents).containsExactly(
                PinUnpinAction(kind = PinUnpinAction.Kind.Unpin, from = PinUnpinAction.From.Timeline),
                PinUnpinAction(kind = PinUnpinAction.Kind.Unpin, from = PinUnpinAction.From.Timeline)
            )
        }
    }

    private suspend fun <T> ReceiveTurbine<T>.awaitFirstItem(): T {
        // Skip 2 item if Mentions feature is enabled, else 1
        skipItems(if (FeatureFlags.Mentions.defaultValue(aBuildMeta())) 2 else 1)
        return awaitItem()
    }

    private fun TestScope.createMessagesPresenter(
        coroutineDispatchers: CoroutineDispatchers = testCoroutineDispatchers(),
        matrixRoom: MatrixRoom = FakeMatrixRoom(
            canUserSendMessageResult = { _, _ -> Result.success(true) },
            canRedactOwnResult = { Result.success(true) },
            canRedactOtherResult = { Result.success(true) },
            canUserJoinCallResult = { Result.success(true) },
            typingNoticeResult = { Result.success(Unit) },
            canUserPinUnpinResult = { Result.success(true) },
        ).apply {
            givenRoomInfo(aRoomInfo(id = roomId, name = ""))
        },
        navigator: FakeMessagesNavigator = FakeMessagesNavigator(),
        clipboardHelper: FakeClipboardHelper = FakeClipboardHelper(),
        analyticsService: FakeAnalyticsService = FakeAnalyticsService(),
        permissionsPresenter: PermissionsPresenter = FakePermissionsPresenter(),
        endPollAction: EndPollAction = FakeEndPollAction(),
        permalinkParser: PermalinkParser = FakePermalinkParser(),
    ): MessagesPresenter {
        val mediaSender = MediaSender(FakeMediaPreProcessor(), matrixRoom)
        val permissionsPresenterFactory = FakePermissionsPresenterFactory(permissionsPresenter)
        val sessionPreferencesStore = InMemorySessionPreferencesStore()
        val mentionSpanProvider = MentionSpanProvider(FakePermalinkParser())
        val messageComposerPresenter = MessageComposerPresenter(
            appCoroutineScope = this,
            room = matrixRoom,
            mediaPickerProvider = FakePickerProvider(),
            featureFlagService = FakeFeatureFlagService(mapOf(FeatureFlags.NotificationSettings.key to true)),
            sessionPreferencesStore = InMemorySessionPreferencesStore(),
            localMediaFactory = FakeLocalMediaFactory(mockMediaUrl),
            mediaSender = mediaSender,
            snackbarDispatcher = SnackbarDispatcher(),
            analyticsService = analyticsService,
            messageComposerContext = DefaultMessageComposerContext(),
            richTextEditorStateFactory = TestRichTextEditorStateFactory(),
            roomAliasSuggestionsDataSource = FakeRoomAliasSuggestionsDataSource(),
            permissionsPresenterFactory = permissionsPresenterFactory,
            permalinkParser = FakePermalinkParser(),
            permalinkBuilder = FakePermalinkBuilder(),
            timelineController = TimelineController(matrixRoom),
            draftService = FakeComposerDraftService(),
            mentionSpanProvider = mentionSpanProvider,
            pillificationHelper = FakeTextPillificationHelper(),
            roomMemberProfilesCache = RoomMemberProfilesCache(),
            suggestionsProcessor = SuggestionsProcessor(),
        ).apply {
            showTextFormatting = true
            isTesting = true
        }
        val voiceMessageComposerPresenter = VoiceMessageComposerPresenter(
            this,
            FakeVoiceRecorder(),
            analyticsService,
            mediaSender,
            player = VoiceMessageComposerPlayer(FakeMediaPlayer(), this),
            messageComposerContext = FakeMessageComposerContext(),
            permissionsPresenterFactory,
        )
        val timelinePresenter = TimelinePresenter(
            timelineItemsFactoryCreator = aTimelineItemsFactoryCreator(),
            room = matrixRoom,
            dispatchers = coroutineDispatchers,
            appScope = this,
            navigator = navigator,
            redactedVoiceMessageManager = FakeRedactedVoiceMessageManager(),
            endPollAction = endPollAction,
            sendPollResponseAction = FakeSendPollResponseAction(),
            sessionPreferencesStore = sessionPreferencesStore,
            timelineItemIndexer = TimelineItemIndexer(),
            timelineController = TimelineController(matrixRoom),
            resolveVerifiedUserSendFailurePresenter = { aResolveVerifiedUserSendFailureState() },
            typingNotificationPresenter = { aTypingNotificationState() },
        )
        val timelinePresenterFactory = object : TimelinePresenter.Factory {
            override fun create(navigator: MessagesNavigator): TimelinePresenter {
                return timelinePresenter
            }
        }
        val featureFlagService = FakeFeatureFlagService()
        val readReceiptBottomSheetPresenter = ReadReceiptBottomSheetPresenter()
        val customReactionPresenter = CustomReactionPresenter(emojibaseProvider = FakeEmojibaseProvider())
        val reactionSummaryPresenter = ReactionSummaryPresenter(room = matrixRoom)

        return MessagesPresenter(
            room = matrixRoom,
            composerPresenter = messageComposerPresenter,
            voiceMessageComposerPresenter = voiceMessageComposerPresenter,
            timelinePresenterFactory = timelinePresenterFactory,
            actionListPresenterFactory = FakeActionListPresenter.Factory,
            customReactionPresenter = customReactionPresenter,
            reactionSummaryPresenter = reactionSummaryPresenter,
            readReceiptBottomSheetPresenter = readReceiptBottomSheetPresenter,
            pinnedMessagesBannerPresenter = { aLoadedPinnedMessagesBannerState() },
            networkMonitor = FakeNetworkMonitor(),
            snackbarDispatcher = SnackbarDispatcher(),
            navigator = navigator,
            clipboardHelper = clipboardHelper,
            featureFlagsService = featureFlagService,
            buildMeta = aBuildMeta(),
            dispatchers = coroutineDispatchers,
            htmlConverterProvider = FakeHtmlConverterProvider(),
            timelineController = TimelineController(matrixRoom),
            permalinkParser = permalinkParser,
            analyticsService = analyticsService,
        )
    }
}

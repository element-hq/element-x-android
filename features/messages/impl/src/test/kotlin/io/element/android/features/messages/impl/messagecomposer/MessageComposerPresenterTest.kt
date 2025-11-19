/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.features.messages.impl.messagecomposer

import android.net.Uri
import androidx.compose.runtime.remember
import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import im.vector.app.features.analytics.plan.Composer
import im.vector.app.features.analytics.plan.Interaction
import io.element.android.features.location.api.LocationService
import io.element.android.features.location.test.FakeLocationService
import io.element.android.features.messages.impl.FakeMessagesNavigator
import io.element.android.features.messages.impl.MessagesNavigator
import io.element.android.features.messages.impl.attachments.Attachment
import io.element.android.features.messages.impl.draft.ComposerDraftService
import io.element.android.features.messages.impl.draft.FakeComposerDraftService
import io.element.android.features.messages.impl.messagecomposer.suggestions.SuggestionsProcessor
import io.element.android.features.messages.impl.timeline.TimelineController
import io.element.android.features.messages.impl.utils.FakeMentionSpanFormatter
import io.element.android.features.messages.impl.utils.FakeTextPillificationHelper
import io.element.android.features.messages.impl.utils.TextPillificationHelper
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.ThreadId
import io.element.android.libraries.matrix.api.media.ImageInfo
import io.element.android.libraries.matrix.api.media.VideoInfo
import io.element.android.libraries.matrix.api.permalink.PermalinkBuilder
import io.element.android.libraries.matrix.api.permalink.PermalinkParser
import io.element.android.libraries.matrix.api.room.IntentionalMention
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.room.RoomMembersState
import io.element.android.libraries.matrix.api.room.RoomMembershipState
import io.element.android.libraries.matrix.api.room.draft.ComposerDraft
import io.element.android.libraries.matrix.api.room.draft.ComposerDraftType
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.api.timeline.TimelineException
import io.element.android.libraries.matrix.api.timeline.item.event.EventOrTransactionId
import io.element.android.libraries.matrix.api.timeline.item.event.InReplyTo
import io.element.android.libraries.matrix.api.timeline.item.event.toEventOrTransactionId
import io.element.android.libraries.matrix.test.ANOTHER_MESSAGE
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_CAPTION
import io.element.android.libraries.matrix.test.A_MESSAGE
import io.element.android.libraries.matrix.test.A_REPLY
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_TRANSACTION_ID
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.A_USER_ID_2
import io.element.android.libraries.matrix.test.A_USER_ID_3
import io.element.android.libraries.matrix.test.A_USER_ID_4
import io.element.android.libraries.matrix.test.media.FakeMediaUploadHandler
import io.element.android.libraries.matrix.test.permalink.FakePermalinkBuilder
import io.element.android.libraries.matrix.test.permalink.FakePermalinkParser
import io.element.android.libraries.matrix.test.room.FakeBaseRoom
import io.element.android.libraries.matrix.test.room.FakeJoinedRoom
import io.element.android.libraries.matrix.test.room.aRoomInfo
import io.element.android.libraries.matrix.test.room.aRoomMember
import io.element.android.libraries.matrix.test.timeline.FakeTimeline
import io.element.android.libraries.matrix.ui.messages.reply.InReplyToDetails
import io.element.android.libraries.mediapickers.api.PickerProvider
import io.element.android.libraries.mediapickers.test.FakePickerProvider
import io.element.android.libraries.mediaupload.api.MediaOptimizationConfig
import io.element.android.libraries.mediaupload.api.MediaPreProcessor
import io.element.android.libraries.mediaupload.api.MediaSenderFactory
import io.element.android.libraries.mediaupload.api.MediaUploadInfo
import io.element.android.libraries.mediaupload.impl.DefaultMediaSender
import io.element.android.libraries.mediaupload.test.FakeMediaOptimizationConfigProvider
import io.element.android.libraries.mediaupload.test.FakeMediaPreProcessor
import io.element.android.libraries.mediaviewer.test.FakeLocalMediaFactory
import io.element.android.libraries.permissions.api.PermissionsPresenter
import io.element.android.libraries.permissions.test.FakePermissionsPresenter
import io.element.android.libraries.permissions.test.FakePermissionsPresenterFactory
import io.element.android.libraries.preferences.api.store.SessionPreferencesStore
import io.element.android.libraries.preferences.api.store.VideoCompressionPreset
import io.element.android.libraries.preferences.test.InMemorySessionPreferencesStore
import io.element.android.libraries.push.test.notifications.conversations.FakeNotificationConversationService
import io.element.android.libraries.textcomposer.mentions.MentionSpanProvider
import io.element.android.libraries.textcomposer.mentions.MentionSpanTheme
import io.element.android.libraries.textcomposer.mentions.ResolvedSuggestion
import io.element.android.libraries.textcomposer.model.MessageComposerMode
import io.element.android.libraries.textcomposer.model.Suggestion
import io.element.android.libraries.textcomposer.model.SuggestionType
import io.element.android.libraries.textcomposer.model.TextEditorState
import io.element.android.services.analytics.test.FakeAnalyticsService
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.lambda.any
import io.element.android.tests.testutils.lambda.assert
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import io.element.android.tests.testutils.test
import io.element.android.tests.testutils.waitForPredicate
import io.mockk.mockk
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import uniffi.wysiwyg_composer.MentionsState
import java.io.File

@Suppress("LargeClass")
@RunWith(RobolectricTestRunner::class)
class MessageComposerPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    private val pickerProvider = FakePickerProvider().apply {
        givenResult(mockk()) // Uri is not available in JVM, so the only way to have a non-null Uri is using Mockk
    }
    private val mediaPreProcessor = FakeMediaPreProcessor()
    private val snackbarDispatcher = SnackbarDispatcher()
    private val mockMediaUrl: Uri = mockk("localMediaUri")
    private val localMediaFactory = FakeLocalMediaFactory(mockMediaUrl)
    private val analyticsService = FakeAnalyticsService()
    private val notificationConversationService = FakeNotificationConversationService()

    @Test
    fun `present - initial state`() = runTest {
        val presenter = createPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            assertThat(initialState.isFullScreen).isFalse()
            assertThat(initialState.textEditorState.messageHtml()).isEqualTo("")
            assertThat(initialState.mode).isEqualTo(MessageComposerMode.Normal)
            assertThat(initialState.showAttachmentSourcePicker).isFalse()
            assertThat(initialState.canShareLocation).isTrue()
        }
    }

    @Test
    fun `present - toggle fullscreen`() = runTest {
        val presenter = createPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            initialState.eventSink.invoke(MessageComposerEvent.ToggleFullScreenState)
            val fullscreenState = awaitItem()
            assertThat(fullscreenState.isFullScreen).isTrue()
            fullscreenState.eventSink.invoke(MessageComposerEvent.ToggleFullScreenState)
            val notFullscreenState = awaitItem()
            assertThat(notFullscreenState.isFullScreen).isFalse()
        }
    }

    @Test
    fun `present - change message`() = runTest {
        val presenter = createPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            initialState.textEditorState.setHtml(A_MESSAGE)
            assertThat(initialState.textEditorState.messageHtml()).isEqualTo(A_MESSAGE)
            initialState.textEditorState.setHtml("")
            assertThat(initialState.textEditorState.messageHtml()).isEqualTo("")
        }
    }

    @Test
    fun `present - change mode to edit`() = runTest {
        val loadDraftLambda = lambdaRecorder { _: RoomId, _: ThreadId?, _: Boolean ->
            ComposerDraft(A_MESSAGE, A_MESSAGE, ComposerDraftType.NewMessage)
        }
        val updateDraftLambda = lambdaRecorder { _: RoomId, _: ThreadId?, _: ComposerDraft?, _: Boolean -> }
        val draftService = FakeComposerDraftService().apply {
            this.loadDraftLambda = loadDraftLambda
            this.saveDraftLambda = updateDraftLambda
        }
        val presenter = createPresenter(
            draftService = draftService,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            val state = presenter.present()
            remember(state, state.textEditorState.messageHtml()) { state }
        }.test {
            var state = awaitFirstItem()
            val mode = anEditMode(message = ANOTHER_MESSAGE)
            state.eventSink.invoke(MessageComposerEvent.SetMode(mode))
            state = awaitItem()
            assertThat(state.mode).isEqualTo(mode)
            assertThat(state.textEditorState.messageHtml()).isEqualTo(ANOTHER_MESSAGE)
            state = backToNormalMode(state)
            // The message that was being edited is cleared and volatile draft is loaded
            assertThat(state.textEditorState.messageHtml()).isEqualTo(A_MESSAGE)

            assert(loadDraftLambda)
                .isCalledExactly(2)
                .withSequence(
                    // Automatic load of draft
                    listOf(value(A_ROOM_ID), value(null), value(false)),
                    // Load of volatile draft when closing edit mode
                    listOf(value(A_ROOM_ID), value(null), value(true))
                )

            assert(updateDraftLambda)
                .isCalledOnce()
                .with(value(A_ROOM_ID), value(null), any(), value(true))
        }
    }

    @Test
    fun `present - change mode to edit caption`() = runTest {
        val loadDraftLambda = lambdaRecorder { _: RoomId, _: ThreadId?, _: Boolean ->
            ComposerDraft(A_MESSAGE, A_MESSAGE, ComposerDraftType.NewMessage)
        }
        val updateDraftLambda = lambdaRecorder { _: RoomId, _: ThreadId?, _: ComposerDraft?, _: Boolean -> }
        val draftService = FakeComposerDraftService().apply {
            this.loadDraftLambda = loadDraftLambda
            this.saveDraftLambda = updateDraftLambda
        }
        val presenter = createPresenter(
            draftService = draftService,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            val state = presenter.present()
            remember(state, state.textEditorState.messageHtml()) { state }
        }.test {
            var state = awaitFirstItem()
            val mode = anEditCaptionMode(caption = A_CAPTION)
            state.eventSink.invoke(MessageComposerEvent.SetMode(mode))
            state = awaitItem()
            assertThat(state.mode).isEqualTo(mode)
            assertThat(state.textEditorState.messageHtml()).isEqualTo(A_CAPTION)
            state = backToNormalMode(state)
            // The caption that was being edited is cleared and volatile draft is loaded
            assertThat(state.textEditorState.messageHtml()).isEqualTo(A_MESSAGE)

            assert(loadDraftLambda)
                .isCalledExactly(2)
                .withSequence(
                    // Automatic load of draft
                    listOf(value(A_ROOM_ID), value(null), value(false)),
                    // Load of volatile draft when closing edit mode
                    listOf(value(A_ROOM_ID), value(null), value(true))
                )
            assert(updateDraftLambda)
                .isCalledOnce()
                .with(value(A_ROOM_ID), value(null), any(), value(true))
        }
    }

    @Test
    fun `present - change mode to edit caption and send the caption`() = runTest {
        val editCaptionLambda = lambdaRecorder { _: EventOrTransactionId, _: String?, _: String? ->
            Result.success(Unit)
        }
        val timeline = FakeTimeline().apply {
            this.editCaptionLambda = editCaptionLambda
        }
        val joinedRoom = FakeJoinedRoom(
            liveTimeline = timeline,
            typingNoticeResult = { Result.success(Unit) }
        )
        val presenter = createPresenter(
            room = joinedRoom,
            isRichTextEditorEnabled = false,
        )
        val permalinkBuilder = FakePermalinkBuilder(permalinkForUserLambda = { Result.success("") })
        presenter.test {
            var state = awaitFirstItem()
            val mode = anEditCaptionMode(caption = A_CAPTION)
            state.eventSink.invoke(MessageComposerEvent.SetMode(mode))
            state = awaitItem()
            assertThat(state.mode).isEqualTo(mode)
            assertThat(state.textEditorState.messageMarkdown(permalinkBuilder)).isEqualTo(A_CAPTION)
            state.eventSink.invoke(MessageComposerEvent.SendMessage)
            val messageSentState = awaitItem()
            assertThat(messageSentState.textEditorState.messageMarkdown(permalinkBuilder)).isEqualTo("")
            waitForPredicate { analyticsService.capturedEvents.size == 1 }
            assertThat(analyticsService.capturedEvents).containsExactly(
                Composer(
                    inThread = false,
                    isEditing = true,
                    isReply = false,
                    messageType = Composer.MessageType.Text,
                )
            )
            assert(editCaptionLambda)
                .isCalledOnce()
                .with(value(AN_EVENT_ID.toEventOrTransactionId()), value(A_CAPTION), value(null))
        }
    }

    @Test
    fun `present - change mode to reply after edit`() = runTest {
        val loadDraftLambda = lambdaRecorder { _: RoomId, _: ThreadId?, _: Boolean ->
            ComposerDraft(A_MESSAGE, A_MESSAGE, ComposerDraftType.NewMessage)
        }
        val updateDraftLambda = lambdaRecorder { _: RoomId, _: ThreadId?, _: ComposerDraft?, _: Boolean -> }
        val draftService = FakeComposerDraftService().apply {
            this.loadDraftLambda = loadDraftLambda
            this.saveDraftLambda = updateDraftLambda
        }
        val presenter = createPresenter(
            draftService = draftService,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            val state = presenter.present()
            remember(state, state.textEditorState.messageHtml()) { state }
        }.test {
            var state = awaitFirstItem()
            val editMode = anEditMode(message = ANOTHER_MESSAGE)
            state.eventSink.invoke(MessageComposerEvent.SetMode(editMode))
            state = awaitItem()
            assertThat(state.mode).isEqualTo(editMode)
            assertThat(state.textEditorState.messageHtml()).isEqualTo(ANOTHER_MESSAGE)

            val replyMode = aReplyMode()
            state.eventSink.invoke(MessageComposerEvent.SetMode(replyMode))
            state = awaitItem()
            assertThat(state.mode).isEqualTo(replyMode)
            assertThat(state.textEditorState.messageHtml()).isEmpty()

            assert(loadDraftLambda)
                .isCalledOnce()
                .with(value(A_ROOM_ID), value(null), value(false))

            assert(updateDraftLambda)
                .isCalledOnce()
                .with(value(A_ROOM_ID), value(null), any(), value(true))
        }
    }

    @Test
    fun `present - change mode to reply`() = runTest {
        val presenter = createPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            var state = awaitFirstItem()
            val mode = aReplyMode()
            state.eventSink.invoke(MessageComposerEvent.SetMode(mode))
            state = awaitItem()
            assertThat(state.mode).isEqualTo(mode)
            assertThat(state.textEditorState.messageHtml()).isEqualTo("")
            backToNormalMode(state)
        }
    }

    @Test
    fun `present - cancel reply`() = runTest {
        val presenter = createPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            var state = awaitFirstItem()
            val mode = aReplyMode()
            state.eventSink.invoke(MessageComposerEvent.SetMode(mode))
            state = awaitItem()
            assertThat(state.mode).isEqualTo(mode)
            state.textEditorState.setHtml(A_REPLY)
            state = backToNormalMode(state)

            // The message typed while replying is not cleared
            assertThat(state.textEditorState.messageHtml()).isEqualTo(A_REPLY)
        }
    }

    @Test
    fun `present - send message with rich text enabled`() = runTest {
        val presenter = createPresenter(
            room = FakeJoinedRoom(
                liveTimeline = FakeTimeline().apply {
                    sendMessageLambda = { _, _, _ -> Result.success(Unit) }
                },
                typingNoticeResult = { Result.success(Unit) }
            ),
        )
        moleculeFlow(RecompositionMode.Immediate) {
            val state = presenter.present()
            remember(state, state.textEditorState.messageHtml()) { state }
        }.test {
            val initialState = awaitFirstItem()
            initialState.textEditorState.setHtml(A_MESSAGE)
            val withMessageState = awaitItem()
            assertThat(withMessageState.textEditorState.messageHtml()).isEqualTo(A_MESSAGE)
            withMessageState.eventSink.invoke(MessageComposerEvent.SendMessage)
            val messageSentState = awaitItem()
            assertThat(messageSentState.textEditorState.messageHtml()).isEqualTo("")
            waitForPredicate { analyticsService.capturedEvents.size == 1 }
            assertThat(analyticsService.capturedEvents).containsExactly(
                Composer(
                    inThread = false,
                    isEditing = false,
                    isReply = false,
                    messageType = Composer.MessageType.Text,
                )
            )
        }
    }

    @Test
    fun `present - send message with plain text enabled`() = runTest {
        val permalinkBuilder = FakePermalinkBuilder(permalinkForUserLambda = { Result.success("") })
        val presenter = createPresenter(
            isRichTextEditorEnabled = false,
            room = FakeJoinedRoom(
                liveTimeline = FakeTimeline().apply {
                    sendMessageLambda = { _, _, _ -> Result.success(Unit) }
                },
                typingNoticeResult = { Result.success(Unit) }
            ),
        )
        moleculeFlow(RecompositionMode.Immediate) {
            val state = presenter.present()
            val messageMarkdown = state.textEditorState.messageMarkdown(permalinkBuilder)
            remember(state, messageMarkdown) { state }
        }.test {
            val initialState = awaitFirstItem()
            initialState.textEditorState.setMarkdown(A_MESSAGE)
            val withMessageState = awaitItem()
            assertThat(withMessageState.textEditorState.messageMarkdown(permalinkBuilder)).isEqualTo(A_MESSAGE)
            assertThat(withMessageState.textEditorState.messageHtml()).isNull()
            withMessageState.eventSink.invoke(MessageComposerEvent.SendMessage)
            val messageSentState = awaitItem()
            assertThat(messageSentState.textEditorState.messageMarkdown(permalinkBuilder)).isEqualTo("")
            waitForPredicate { analyticsService.capturedEvents.size == 1 }
            assertThat(analyticsService.capturedEvents).containsExactly(
                Composer(
                    inThread = false,
                    isEditing = false,
                    isReply = false,
                    messageType = Composer.MessageType.Text,
                )
            )
        }
    }

    @Test
    fun `present - edit sent message`() = runTest {
        val editMessageLambda = lambdaRecorder { _: EventOrTransactionId, _: String, _: String?, _: List<IntentionalMention> ->
            Result.success(Unit)
        }
        val timeline = FakeTimeline().apply {
            this.editMessageLambda = editMessageLambda
        }
        val joinedRoom = FakeJoinedRoom(
            liveTimeline = timeline,
            typingNoticeResult = { Result.success(Unit) }
        )
        val presenter = createPresenter(
            joinedRoom,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            val state = presenter.present()
            remember(state, state.textEditorState.messageHtml()) { state }
        }.test {
            val initialState = awaitFirstItem()
            assertThat(initialState.textEditorState.messageHtml()).isEqualTo("")
            val mode = anEditMode()
            initialState.eventSink.invoke(MessageComposerEvent.SetMode(mode))
            val withMessageState = awaitItem()
            assertThat(withMessageState.mode).isEqualTo(mode)
            assertThat(withMessageState.textEditorState.messageHtml()).isEqualTo(A_MESSAGE)
            withMessageState.textEditorState.setHtml(ANOTHER_MESSAGE)
            val withEditedMessageState = awaitItem()
            assertThat(withEditedMessageState.textEditorState.messageHtml()).isEqualTo(ANOTHER_MESSAGE)
            withEditedMessageState.eventSink.invoke(MessageComposerEvent.SendMessage)
            skipItems(1)
            val messageSentState = awaitItem()
            assertThat(messageSentState.textEditorState.messageHtml()).isEqualTo("")

            advanceUntilIdle()

            assert(editMessageLambda)
                .isCalledOnce()
                .with(value(AN_EVENT_ID.toEventOrTransactionId()), value(ANOTHER_MESSAGE), value(ANOTHER_MESSAGE), any())

            assertThat(analyticsService.capturedEvents).containsExactly(
                Composer(
                    inThread = false,
                    isEditing = true,
                    isReply = false,
                    messageType = Composer.MessageType.Text,
                )
            )
        }
    }

    @Test
    fun `present - edit sent message event not found`() = runTest {
        val timelineEditMessageLambda = lambdaRecorder { _: EventOrTransactionId, _: String, _: String?, _: List<IntentionalMention> ->
            Result.failure<Unit>(TimelineException.EventNotFound)
        }
        val timeline = FakeTimeline().apply {
            this.editMessageLambda = timelineEditMessageLambda
        }
        val roomEditMessageLambda = lambdaRecorder { _: EventId?, _: String, _: String?, _: List<IntentionalMention> ->
            Result.success(Unit)
        }
        val joinedRoom = FakeJoinedRoom(
            liveTimeline = timeline,
            typingNoticeResult = { Result.success(Unit) },
            editMessageLambda = roomEditMessageLambda,
        )
        val presenter = createPresenter(
            joinedRoom,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            val state = presenter.present()
            remember(state, state.textEditorState.messageHtml()) { state }
        }.test {
            val initialState = awaitFirstItem()
            assertThat(initialState.textEditorState.messageHtml()).isEqualTo("")
            val mode = anEditMode()
            initialState.eventSink.invoke(MessageComposerEvent.SetMode(mode))
            val withMessageState = awaitItem()
            assertThat(withMessageState.mode).isEqualTo(mode)
            assertThat(withMessageState.textEditorState.messageHtml()).isEqualTo(A_MESSAGE)
            withMessageState.textEditorState.setHtml(ANOTHER_MESSAGE)
            val withEditedMessageState = awaitItem()
            assertThat(withEditedMessageState.textEditorState.messageHtml()).isEqualTo(ANOTHER_MESSAGE)
            withEditedMessageState.eventSink.invoke(MessageComposerEvent.SendMessage)
            skipItems(1)
            val messageSentState = awaitItem()
            assertThat(messageSentState.textEditorState.messageHtml()).isEqualTo("")

            advanceUntilIdle()

            assert(timelineEditMessageLambda)
                .isCalledOnce()
                .with(value(AN_EVENT_ID.toEventOrTransactionId()), value(ANOTHER_MESSAGE), value(ANOTHER_MESSAGE), any())

            assert(roomEditMessageLambda)
                .isCalledOnce()
                .with(value(AN_EVENT_ID), value(ANOTHER_MESSAGE), value(ANOTHER_MESSAGE), any())

            assertThat(analyticsService.capturedEvents).containsExactly(
                Composer(
                    inThread = false,
                    isEditing = true,
                    isReply = false,
                    messageType = Composer.MessageType.Text,
                )
            )
        }
    }

    @Test
    fun `present - edit not sent message`() = runTest {
        val editMessageLambda = lambdaRecorder { _: EventOrTransactionId, _: String, _: String?, _: List<IntentionalMention> ->
            Result.success(Unit)
        }
        val timeline = FakeTimeline().apply {
            this.editMessageLambda = editMessageLambda
        }
        val joinedRoom = FakeJoinedRoom(
            liveTimeline = timeline,
            typingNoticeResult = { Result.success(Unit) },
        )
        val presenter = createPresenter(
            joinedRoom,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            val state = presenter.present()
            remember(state, state.textEditorState.messageHtml()) { state }
        }.test {
            val initialState = awaitFirstItem()
            assertThat(initialState.textEditorState.messageHtml()).isEqualTo("")
            val mode = anEditMode(eventOrTransactionId = A_TRANSACTION_ID.toEventOrTransactionId())
            initialState.eventSink.invoke(MessageComposerEvent.SetMode(mode))
            val withMessageState = awaitItem()
            assertThat(withMessageState.mode).isEqualTo(mode)
            assertThat(withMessageState.textEditorState.messageHtml()).isEqualTo(A_MESSAGE)
            withMessageState.textEditorState.setHtml(ANOTHER_MESSAGE)
            val withEditedMessageState = awaitItem()
            assertThat(withEditedMessageState.textEditorState.messageHtml()).isEqualTo(ANOTHER_MESSAGE)
            withEditedMessageState.eventSink.invoke(MessageComposerEvent.SendMessage)
            skipItems(1)
            val messageSentState = awaitItem()
            assertThat(messageSentState.textEditorState.messageHtml()).isEqualTo("")

            advanceUntilIdle()

            assert(editMessageLambda)
                .isCalledOnce()
                .with(value(A_TRANSACTION_ID.toEventOrTransactionId()), value(ANOTHER_MESSAGE), value(ANOTHER_MESSAGE), any())

            assertThat(analyticsService.capturedEvents).containsExactly(
                Composer(
                    inThread = false,
                    isEditing = true,
                    isReply = false,
                    messageType = Composer.MessageType.Text,
                )
            )
        }
    }

    @Test
    fun `present - reply message`() = runTest {
        val replyMessageLambda = lambdaRecorder { _: EventId?, _: String, _: String?, _: List<IntentionalMention>, _: Boolean ->
            Result.success(Unit)
        }
        val timeline = FakeTimeline().apply {
            this.replyMessageLambda = replyMessageLambda
        }
        val joinedRoom = FakeJoinedRoom(
            liveTimeline = timeline,
            typingNoticeResult = { Result.success(Unit) }
        )
        val presenter = createPresenter(
            joinedRoom,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            assertThat(initialState.textEditorState.messageHtml()).isEqualTo("")
            val mode = aReplyMode()
            initialState.eventSink.invoke(MessageComposerEvent.SetMode(mode))
            val state = awaitItem()
            assertThat(state.mode).isEqualTo(mode)
            assertThat(state.textEditorState.messageHtml()).isEqualTo("")
            state.textEditorState.setHtml(A_REPLY)
            assertThat(state.textEditorState.messageHtml()).isEqualTo(A_REPLY)
            state.eventSink.invoke(MessageComposerEvent.SendMessage)
            val messageSentState = awaitItem()
            assertThat(messageSentState.textEditorState.messageHtml()).isEqualTo("")

            advanceUntilIdle()

            assert(replyMessageLambda)
                .isCalledOnce()
                .with(any(), value(A_REPLY), value(A_REPLY), any(), value(false))

            assertThat(analyticsService.capturedEvents).containsExactly(
                Composer(
                    inThread = false,
                    isEditing = false,
                    isReply = true,
                    messageType = Composer.MessageType.Text,
                )
            )
        }
    }

    @Test
    fun `present - Open attachments menu`() = runTest {
        val presenter = createPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            assertThat(initialState.showAttachmentSourcePicker).isFalse()
            initialState.eventSink(MessageComposerEvent.AddAttachment)
            assertThat(awaitItem().showAttachmentSourcePicker).isTrue()
        }
    }

    @Test
    fun `present - Dismiss attachments menu`() = runTest {
        val presenter = createPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            initialState.eventSink(MessageComposerEvent.AddAttachment)
            skipItems(1)

            initialState.eventSink(MessageComposerEvent.DismissAttachmentMenu)
            assertThat(awaitItem().showAttachmentSourcePicker).isFalse()
        }
    }

    @Test
    fun `present - Pick image from gallery`() = runTest {
        val room = FakeJoinedRoom(
            typingNoticeResult = { Result.success(Unit) }
        )
        val onPreviewAttachmentLambda = lambdaRecorder { _: ImmutableList<Attachment>, _: EventId? -> }
        val navigator = FakeMessagesNavigator(
            onPreviewAttachmentLambda = onPreviewAttachmentLambda
        )
        val presenter = createPresenter(
            room = room,
            navigator = navigator,
        )
        pickerProvider.givenMimeType(MimeTypes.Images)
        mediaPreProcessor.givenResult(
            Result.success(
                MediaUploadInfo.Image(
                    file = File("/some/path"),
                    imageInfo = ImageInfo(
                        width = null,
                        height = null,
                        mimetype = null,
                        size = null,
                        thumbnailInfo = null,
                        thumbnailSource = null,
                        blurhash = null,
                    ),
                    thumbnailFile = File("/some/path")
                )
            )
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            initialState.eventSink(MessageComposerEvent.PickAttachmentSource.FromGallery)
            onPreviewAttachmentLambda.assertions().isCalledOnce()
        }
    }

    @Test
    fun `present - Pick video from gallery`() = runTest {
        val room = FakeJoinedRoom(
            typingNoticeResult = { Result.success(Unit) }
        )
        val onPreviewAttachmentLambda = lambdaRecorder { _: ImmutableList<Attachment>, _: EventId? -> }
        val navigator = FakeMessagesNavigator(
            onPreviewAttachmentLambda = onPreviewAttachmentLambda
        )
        val presenter = createPresenter(
            room = room,
            navigator = navigator,
        )
        pickerProvider.givenMimeType(MimeTypes.Videos)
        mediaPreProcessor.givenResult(
            Result.success(
                MediaUploadInfo.Video(
                    file = File("/some/path"),
                    videoInfo = VideoInfo(
                        width = null,
                        height = null,
                        mimetype = null,
                        duration = null,
                        size = null,
                        thumbnailInfo = null,
                        thumbnailSource = null,
                        blurhash = null,
                    ),
                    thumbnailFile = File("/some/path")
                )
            )
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            initialState.eventSink(MessageComposerEvent.PickAttachmentSource.FromGallery)
            onPreviewAttachmentLambda.assertions().isCalledOnce()
        }
    }

    @Test
    fun `present - Pick media from gallery & cancel does nothing`() = runTest {
        val presenter = createPresenter()
        with(pickerProvider) {
            givenResult(null) // Simulate a user canceling the flow
            givenMimeType(MimeTypes.Images)
        }
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            initialState.eventSink(MessageComposerEvent.PickAttachmentSource.FromGallery)
            // No crashes here, otherwise it fails
        }
    }

    @Test
    fun `present - Pick file from storage will open the preview`() = runTest {
        val room = FakeJoinedRoom(
            typingNoticeResult = { Result.success(Unit) }
        )
        val onPreviewAttachmentLambda = lambdaRecorder { _: ImmutableList<Attachment>, _: EventId? -> }
        val navigator = FakeMessagesNavigator(
            onPreviewAttachmentLambda = onPreviewAttachmentLambda
        )
        val presenter = createPresenter(
            room = room,
            navigator = navigator,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            initialState.eventSink(MessageComposerEvent.PickAttachmentSource.FromFiles)
            onPreviewAttachmentLambda.assertions().isCalledOnce()
        }
    }

    @Test
    fun `present - create poll`() = runTest {
        val room = FakeJoinedRoom(
            typingNoticeResult = { Result.success(Unit) }
        )
        val presenter = createPresenter(room = room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            initialState.eventSink(MessageComposerEvent.AddAttachment)
            val attachmentOpenState = awaitItem()
            assertThat(attachmentOpenState.showAttachmentSourcePicker).isTrue()
            initialState.eventSink(MessageComposerEvent.PickAttachmentSource.Poll)
            val finalState = awaitItem()
            assertThat(finalState.showAttachmentSourcePicker).isFalse()
        }
    }

    @Test
    fun `present - share location`() = runTest {
        val room = FakeJoinedRoom(
            typingNoticeResult = { Result.success(Unit) }
        )
        val presenter = createPresenter(room = room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            initialState.eventSink(MessageComposerEvent.AddAttachment)
            val attachmentOpenState = awaitItem()
            assertThat(attachmentOpenState.showAttachmentSourcePicker).isTrue()
            initialState.eventSink(MessageComposerEvent.PickAttachmentSource.Location)
            val finalState = awaitItem()
            assertThat(finalState.showAttachmentSourcePicker).isFalse()
        }
    }

    @Test
    fun `present - Take photo`() = runTest {
        val room = FakeJoinedRoom(
            typingNoticeResult = { Result.success(Unit) }
        )
        val permissionPresenter = FakePermissionsPresenter().apply { setPermissionGranted() }
        val onPreviewAttachmentLambda = lambdaRecorder { _: ImmutableList<Attachment>, _: EventId? -> }
        val navigator = FakeMessagesNavigator(
            onPreviewAttachmentLambda = onPreviewAttachmentLambda
        )
        val presenter = createPresenter(
            room = room,
            permissionPresenter = permissionPresenter,
            navigator = navigator,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            initialState.eventSink(MessageComposerEvent.PickAttachmentSource.PhotoFromCamera)
            onPreviewAttachmentLambda.assertions().isCalledOnce()
        }
    }

    @Test
    fun `present - Take photo with permission request`() = runTest {
        val room = FakeJoinedRoom(
            typingNoticeResult = { Result.success(Unit) }
        )
        val permissionPresenter = FakePermissionsPresenter()
        val onPreviewAttachmentLambda = lambdaRecorder { _: ImmutableList<Attachment>, _: EventId? -> }
        val navigator = FakeMessagesNavigator(
            onPreviewAttachmentLambda = onPreviewAttachmentLambda
        )
        val presenter = createPresenter(
            room = room,
            permissionPresenter = permissionPresenter,
            navigator = navigator,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            initialState.eventSink(MessageComposerEvent.PickAttachmentSource.PhotoFromCamera)
            permissionPresenter.setPermissionGranted()
            onPreviewAttachmentLambda.assertions().isCalledOnce()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - Record video`() = runTest {
        val room = FakeJoinedRoom(
            typingNoticeResult = { Result.success(Unit) }
        )
        val permissionPresenter = FakePermissionsPresenter().apply { setPermissionGranted() }
        val onPreviewAttachmentLambda = lambdaRecorder { _: ImmutableList<Attachment>, _: EventId? -> }
        val navigator = FakeMessagesNavigator(
            onPreviewAttachmentLambda = onPreviewAttachmentLambda
        )
        val presenter = createPresenter(
            room = room,
            permissionPresenter = permissionPresenter,
            navigator = navigator,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            initialState.eventSink(MessageComposerEvent.PickAttachmentSource.VideoFromCamera)
            onPreviewAttachmentLambda.assertions().isCalledOnce()
        }
    }

    @Test
    fun `present - Record video with permission request`() = runTest {
        val room = FakeJoinedRoom(
            typingNoticeResult = { Result.success(Unit) }
        )
        val permissionPresenter = FakePermissionsPresenter()
        val onPreviewAttachmentLambda = lambdaRecorder { _: ImmutableList<Attachment>, _: EventId? -> }
        val navigator = FakeMessagesNavigator(
            onPreviewAttachmentLambda = onPreviewAttachmentLambda
        )
        val presenter = createPresenter(
            room = room,
            permissionPresenter = permissionPresenter,
            navigator = navigator,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            initialState.eventSink(MessageComposerEvent.PickAttachmentSource.VideoFromCamera)
            val permissionState = awaitItem()
            assertThat(permissionState.showAttachmentSourcePicker).isFalse()
            permissionPresenter.setPermissionGranted()
            skipItems(1)
            onPreviewAttachmentLambda.assertions().isCalledOnce()
        }
    }

    @Test
    fun `present - errors are tracked`() = runTest {
        val testException = Exception("Test error")
        val presenter = createPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            initialState.eventSink(MessageComposerEvent.Error(testException))
            assertThat(analyticsService.trackedErrors).containsExactly(testException)
        }
    }

    @Test
    fun `present - ToggleTextFormatting toggles text formatting`() = runTest {
        val presenter = createPresenter(isRichTextEditorEnabled = false)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            assertThat(initialState.showTextFormatting).isFalse()
            initialState.eventSink(MessageComposerEvent.AddAttachment)
            val composerOptions = awaitItem()
            assertThat(composerOptions.showAttachmentSourcePicker).isTrue()
            composerOptions.eventSink(MessageComposerEvent.ToggleTextFormatting(true))
            skipItems(2) // composer options closed
            val showTextFormatting = awaitItem()
            assertThat(showTextFormatting.showAttachmentSourcePicker).isFalse()
            assertThat(showTextFormatting.showTextFormatting).isTrue()
            assertThat(analyticsService.capturedEvents).containsExactly(
                Interaction(index = null, interactionType = null, name = Interaction.Name.MobileRoomComposerFormattingEnabled)
            )
            analyticsService.capturedEvents.clear()
            showTextFormatting.eventSink(MessageComposerEvent.ToggleTextFormatting(false))
            skipItems(1)
            val finished = awaitItem()
            assertThat(finished.showTextFormatting).isFalse()
            assertThat(analyticsService.capturedEvents).isEmpty()
        }
    }

    @Test
    fun `present - room member mention suggestions`() = runTest {
        val currentUser = aRoomMember(userId = A_USER_ID, membership = RoomMembershipState.JOIN)
        val invitedUser = aRoomMember(userId = A_USER_ID_3, membership = RoomMembershipState.INVITE)
        val bob = aRoomMember(userId = A_USER_ID_2, membership = RoomMembershipState.JOIN)
        val david = aRoomMember(userId = A_USER_ID_4, displayName = "Dave", membership = RoomMembershipState.JOIN)
        var canUserTriggerRoomNotificationResult = true
        val room = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(canUserTriggerRoomNotificationResult = { Result.success(canUserTriggerRoomNotificationResult) }),
            typingNoticeResult = { Result.success(Unit) }
        ).apply {
            givenRoomMembersState(
                RoomMembersState.Ready(
                    persistentListOf(currentUser, invitedUser, bob, david),
                )
            )
            givenRoomInfo(aRoomInfo(isDirect = false))
        }
        val presenter = createPresenter(room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()

            // A null suggestion (no suggestion was received) returns nothing
            initialState.eventSink(MessageComposerEvent.SuggestionReceived(null))
            assertThat(awaitItem().suggestions).isEmpty()

            // An empty suggestion returns the room and joined members that are not the current user
            initialState.eventSink(MessageComposerEvent.SuggestionReceived(Suggestion(0, 0, SuggestionType.Mention, "")))
            assertThat(awaitItem().suggestions)
                .containsExactly(ResolvedSuggestion.AtRoom, ResolvedSuggestion.Member(bob), ResolvedSuggestion.Member(david))

            // A suggestion containing a part of "room" will also return the room mention
            initialState.eventSink(MessageComposerEvent.SuggestionReceived(Suggestion(0, 0, SuggestionType.Mention, "roo")))
            assertThat(awaitItem().suggestions).containsExactly(ResolvedSuggestion.AtRoom)

            // A non-empty suggestion will return those joined members whose user id matches it
            initialState.eventSink(MessageComposerEvent.SuggestionReceived(Suggestion(0, 0, SuggestionType.Mention, "bob")))
            assertThat(awaitItem().suggestions).containsExactly(ResolvedSuggestion.Member(bob))

            // A non-empty suggestion will return those joined members whose display name matches it
            initialState.eventSink(MessageComposerEvent.SuggestionReceived(Suggestion(0, 0, SuggestionType.Mention, "dave")))
            assertThat(awaitItem().suggestions).containsExactly(ResolvedSuggestion.Member(david))

            // If the suggestion isn't a mention, no suggestions are returned
            initialState.eventSink(MessageComposerEvent.SuggestionReceived(Suggestion(0, 0, SuggestionType.Command, "")))
            assertThat(awaitItem().suggestions).isEmpty()

            // If user has no permission to send `@room` mentions, `RoomMemberSuggestion.Room` is not returned
            canUserTriggerRoomNotificationResult = false
            initialState.eventSink(MessageComposerEvent.SuggestionReceived(Suggestion(0, 0, SuggestionType.Mention, "")))
            assertThat(awaitItem().suggestions)
                .containsExactly(ResolvedSuggestion.Member(bob), ResolvedSuggestion.Member(david))
        }
    }

    @Test
    fun `present - room member mention suggestions in a DM`() = runTest {
        val currentUser = aRoomMember(userId = A_USER_ID, membership = RoomMembershipState.JOIN)
        val invitedUser = aRoomMember(userId = A_USER_ID_3, membership = RoomMembershipState.INVITE)
        val bob = aRoomMember(userId = A_USER_ID_2, membership = RoomMembershipState.JOIN)
        val david = aRoomMember(userId = A_USER_ID_4, displayName = "Dave", membership = RoomMembershipState.JOIN)
        val room = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(canUserTriggerRoomNotificationResult = { Result.success(true) }),
            typingNoticeResult = { Result.success(Unit) }
        ).apply {
            givenRoomMembersState(
                RoomMembersState.Ready(
                    persistentListOf(currentUser, invitedUser, bob, david),
                )
            )
            givenRoomInfo(
                aRoomInfo(
                    isDirect = true,
                    activeMembersCount = 2,
                )
            )
        }
        val presenter = createPresenter(room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()

            // An empty suggestion returns the joined members that are not the current user, but not the room
            initialState.eventSink(MessageComposerEvent.SuggestionReceived(Suggestion(0, 0, SuggestionType.Mention, "")))
            skipItems(1)
            assertThat(awaitItem().suggestions)
                .containsExactly(ResolvedSuggestion.Member(bob), ResolvedSuggestion.Member(david))
        }
    }

    fun `present - InsertSuggestion`() = runTest {
        val presenter = createPresenter(
            permalinkBuilder = FakePermalinkBuilder(
                permalinkForUserLambda = {
                    Result.success("https://matrix.to/#/${A_USER_ID_2.value}")
                }
            )
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            initialState.textEditorState.setHtml("Hey @bo")
            initialState.eventSink(MessageComposerEvent.InsertSuggestion(ResolvedSuggestion.Member(aRoomMember(userId = A_USER_ID_2))))

            assertThat(initialState.textEditorState.messageHtml())
                .isEqualTo("Hey <a href='https://matrix.to/#/${A_USER_ID_2.value}'>${A_USER_ID_2.value}</a>")
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `present - send messages with intentional mentions`() = runTest {
        val replyMessageLambda = lambdaRecorder { _: EventId?, _: String, _: String?, _: List<IntentionalMention>, _: Boolean ->
            Result.success(Unit)
        }
        val editMessageLambda = lambdaRecorder { _: EventOrTransactionId, _: String, _: String?, _: List<IntentionalMention> ->
            Result.success(Unit)
        }
        val sendMessageResult = lambdaRecorder { _: String, _: String?, _: List<IntentionalMention> ->
            Result.success(Unit)
        }
        val timeline = FakeTimeline().apply {
            this.replyMessageLambda = replyMessageLambda
            this.editMessageLambda = editMessageLambda
            sendMessageLambda = sendMessageResult
        }
        val room = FakeJoinedRoom(
            liveTimeline = timeline,
            typingNoticeResult = { Result.success(Unit) }
        )
        val presenter = createPresenter(room = room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()

            // Check intentional mentions on message sent
            val mentionUser1 = listOf(A_USER_ID.value)
            (initialState.textEditorState as? TextEditorState.Rich)?.richTextEditorState?.mentionsState = MentionsState(
                userIds = mentionUser1,
                roomIds = emptyList(),
                roomAliases = emptyList(),
                hasAtRoomMention = false
            )
            initialState.textEditorState.setHtml(A_MESSAGE)
            initialState.eventSink(MessageComposerEvent.SendMessage)

            advanceUntilIdle()

            sendMessageResult.assertions().isCalledOnce()
                .with(value(A_MESSAGE), any(), value(listOf(IntentionalMention.User(A_USER_ID))))

            // Check intentional mentions on reply sent
            initialState.eventSink(MessageComposerEvent.SetMode(aReplyMode()))
            val mentionUser2 = listOf(A_USER_ID_2.value)
            (awaitItem().textEditorState as? TextEditorState.Rich)?.richTextEditorState?.mentionsState = MentionsState(
                userIds = mentionUser2,
                roomIds = emptyList(),
                roomAliases = emptyList(),
                hasAtRoomMention = false
            )

            initialState.eventSink(MessageComposerEvent.SendMessage)
            advanceUntilIdle()

            assert(replyMessageLambda)
                .isCalledOnce()
                .with(any(), any(), any(), value(listOf(IntentionalMention.User(A_USER_ID_2))), value(false))

            // Check intentional mentions on edit message
            skipItems(1)
            initialState.eventSink(MessageComposerEvent.SetMode(anEditMode()))
            val mentionUser3 = listOf(A_USER_ID_3.value)
            (awaitItem().textEditorState as? TextEditorState.Rich)?.richTextEditorState?.mentionsState = MentionsState(
                userIds = mentionUser3,
                roomIds = emptyList(),
                roomAliases = emptyList(),
                hasAtRoomMention = false
            )

            initialState.eventSink(MessageComposerEvent.SendMessage)
            advanceUntilIdle()

            assert(editMessageLambda)
                .isCalledOnce()
                .with(any(), any(), any(), value(listOf(IntentionalMention.User(A_USER_ID_3))))

            skipItems(1)
        }
    }

    @Test
    fun `present - send uri`() = runTest {
        val presenter = createPresenter(
            room = FakeJoinedRoom(
                typingNoticeResult = { Result.success(Unit) },
                liveTimeline = FakeTimeline().apply {
                    sendFileLambda = { _, _, _, _, _ ->
                        Result.success(FakeMediaUploadHandler())
                    }
                }
            ),
        )
        moleculeFlow(RecompositionMode.Immediate) {
            val state = presenter.present()
            remember(state, state.textEditorState.messageHtml()) { state }
        }.test {
            val initialState = awaitFirstItem()
            initialState.eventSink.invoke(MessageComposerEvent.SendUri(Uri.parse("content://uri")))
            waitForPredicate { mediaPreProcessor.processCallCount == 1 }
        }
    }

    @Test
    fun `present - handle typing notice event`() = runTest {
        val typingNoticeResult = lambdaRecorder<Boolean, Result<Unit>> { Result.success(Unit) }
        val room = FakeJoinedRoom(
            typingNoticeResult = typingNoticeResult,
        )
        val presenter = createPresenter(room = room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            typingNoticeResult.assertions().isNeverCalled()
            initialState.eventSink.invoke(MessageComposerEvent.TypingNotice(true))
            initialState.eventSink.invoke(MessageComposerEvent.TypingNotice(false))
            advanceUntilIdle()
            typingNoticeResult.assertions().isCalledExactly(2)
                .withSequence(
                    listOf(value(true)),
                    listOf(value(false)),
                )
        }
    }

    @Test
    fun `present - handle typing notice event when sending typing notice is disabled`() = runTest {
        val typingNoticeResult = lambdaRecorder<Boolean, Result<Unit>> { Result.success(Unit) }
        val room = FakeJoinedRoom(
            typingNoticeResult = typingNoticeResult
        )
        val store = InMemorySessionPreferencesStore(
            isSendTypingNotificationsEnabled = false
        )
        val presenter = createPresenter(room = room, sessionPreferencesStore = store)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            typingNoticeResult.assertions().isNeverCalled()
            initialState.eventSink.invoke(MessageComposerEvent.TypingNotice(true))
            initialState.eventSink.invoke(MessageComposerEvent.TypingNotice(false))
            typingNoticeResult.assertions().isNeverCalled()
        }
    }

    @Test
    fun `present - when there is no draft, nothing is restored`() = runTest {
        val loadDraftLambda = lambdaRecorder<RoomId, ThreadId?, Boolean, ComposerDraft?> { _, _, _ -> null }
        val composerDraftService = FakeComposerDraftService().apply {
            this.loadDraftLambda = loadDraftLambda
        }
        val presenter = createPresenter(draftService = composerDraftService)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitFirstItem()
            assert(loadDraftLambda)
                .isCalledOnce()
                .with(value(A_ROOM_ID), value(null), value(false))

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - when there is a draft for new message with plain text, it is restored`() = runTest {
        val loadDraftLambda = lambdaRecorder<RoomId, ThreadId?, Boolean, ComposerDraft?> { _, _, _ ->
            ComposerDraft(plainText = A_MESSAGE, htmlText = null, draftType = ComposerDraftType.NewMessage)
        }
        val composerDraftService = FakeComposerDraftService().apply {
            this.loadDraftLambda = loadDraftLambda
        }
        val permalinkBuilder = FakePermalinkBuilder()
        val presenter = createPresenter(
            draftService = composerDraftService,
            permalinkBuilder = permalinkBuilder,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(2)
            awaitItem().also { state ->
                assertThat(state.textEditorState.messageMarkdown(permalinkBuilder)).isEqualTo(A_MESSAGE)
                assertThat(state.textEditorState.messageHtml()).isNull()
            }
            assert(loadDraftLambda)
                .isCalledOnce()
                .with(value(A_ROOM_ID), value(null), value(false))

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - when there is a draft for new message with rich text, it is restored`() = runTest {
        val loadDraftLambda = lambdaRecorder<RoomId, ThreadId?, Boolean, ComposerDraft?> { _, _, _ ->
            ComposerDraft(
                plainText = A_MESSAGE,
                htmlText = A_MESSAGE,
                draftType = ComposerDraftType.NewMessage
            )
        }
        val composerDraftService = FakeComposerDraftService().apply {
            this.loadDraftLambda = loadDraftLambda
        }
        val permalinkBuilder = FakePermalinkBuilder()
        val presenter = createPresenter(
            draftService = composerDraftService,
            permalinkBuilder = permalinkBuilder,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            awaitItem().also { state ->
                assertThat(state.showTextFormatting).isTrue()
                assertThat(state.textEditorState.messageMarkdown(permalinkBuilder)).isEqualTo(A_MESSAGE)
                assertThat(state.textEditorState.messageHtml()).isEqualTo(A_MESSAGE)
            }
            assert(loadDraftLambda)
                .isCalledOnce()
                .with(value(A_ROOM_ID), value(null), value(false))
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - when there is a draft for edit, it is restored`() = runTest {
        val loadDraftLambda = lambdaRecorder<RoomId, ThreadId?, Boolean, ComposerDraft?> { _, _, _ ->
            ComposerDraft(
                plainText = A_MESSAGE,
                htmlText = null,
                draftType = ComposerDraftType.Edit(AN_EVENT_ID)
            )
        }
        val composerDraftService = FakeComposerDraftService().apply {
            this.loadDraftLambda = loadDraftLambda
        }
        val permalinkBuilder = FakePermalinkBuilder()
        val presenter = createPresenter(
            draftService = composerDraftService,
            permalinkBuilder = permalinkBuilder,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(2)
            awaitItem().also { state ->
                assertThat(state.showTextFormatting).isFalse()
                assertThat(state.mode).isEqualTo(anEditMode())
                assertThat(state.textEditorState.messageMarkdown(permalinkBuilder)).isEqualTo(A_MESSAGE)
                assertThat(state.textEditorState.messageHtml()).isNull()
            }
            assert(loadDraftLambda)
                .isCalledOnce()
                .with(value(A_ROOM_ID), value(null), value(false))

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - when there is a draft for reply, it is restored`() = runTest {
        val loadDraftLambda = lambdaRecorder<RoomId, ThreadId?, Boolean, ComposerDraft?> { _, _, _ ->
            ComposerDraft(
                plainText = A_MESSAGE,
                htmlText = null,
                draftType = ComposerDraftType.Reply(AN_EVENT_ID)
            )
        }
        val composerDraftService = FakeComposerDraftService().apply {
            this.loadDraftLambda = loadDraftLambda
        }
        val loadReplyDetailsLambda = lambdaRecorder<EventId, InReplyTo> { eventId ->
            InReplyTo.Pending(eventId)
        }
        val timeline = FakeTimeline().apply {
            this.loadReplyDetailsLambda = loadReplyDetailsLambda
        }
        val room = FakeJoinedRoom(
            liveTimeline = timeline,
            typingNoticeResult = { Result.success(Unit) },
        )
        val permalinkBuilder = FakePermalinkBuilder()
        val presenter = createPresenter(
            room = room,
            draftService = composerDraftService,
            permalinkBuilder = permalinkBuilder,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(2)
            awaitItem().also { state ->
                assertThat(state.showTextFormatting).isFalse()
                assertThat(state.mode).isEqualTo(aReplyMode())
                assertThat(state.textEditorState.messageMarkdown(permalinkBuilder)).isEqualTo(A_MESSAGE)
                assertThat(state.textEditorState.messageHtml()).isNull()
            }
            assert(loadDraftLambda)
                .isCalledOnce()
                .with(value(A_ROOM_ID), value(null), value(false))

            assert(loadReplyDetailsLambda)
                .isCalledOnce()
                .with(value(AN_EVENT_ID))

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - when save draft event is invoked and composer is empty then service is called with null draft`() = runTest {
        val saveDraftLambda = lambdaRecorder<RoomId, ThreadId?, ComposerDraft?, Boolean, Unit> { _, _, _, _ -> }
        val composerDraftService = FakeComposerDraftService().apply {
            this.saveDraftLambda = saveDraftLambda
        }
        val presenter = createPresenter(draftService = composerDraftService)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            initialState.eventSink.invoke(MessageComposerEvent.SaveDraft)
            advanceUntilIdle()
            assert(saveDraftLambda)
                .isCalledOnce()
                .with(value(A_ROOM_ID), value(null), value(null), value(false))
        }
    }

    @Test
    fun `present - when save draft event is invoked and composer is not empty then service is called`() = runTest {
        val saveDraftLambda = lambdaRecorder<RoomId, ThreadId?, ComposerDraft?, Boolean, Unit> { _, _, _, _ -> }
        val composerDraftService = FakeComposerDraftService().apply {
            this.saveDraftLambda = saveDraftLambda
        }
        val permalinkBuilder = FakePermalinkBuilder()
        val presenter = createPresenter(
            isRichTextEditorEnabled = false,
            draftService = composerDraftService,
            permalinkBuilder = permalinkBuilder,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            val state = presenter.present()
            val messageMarkdown = state.textEditorState.messageMarkdown(permalinkBuilder)
            remember(state, messageMarkdown) { state }
        }.test {
            val initialState = awaitFirstItem()
            initialState.textEditorState.setMarkdown(A_MESSAGE)

            val withMessageState = awaitItem()
            assertThat(withMessageState.textEditorState.messageMarkdown(permalinkBuilder)).isEqualTo(A_MESSAGE)
            withMessageState.eventSink(MessageComposerEvent.SaveDraft)
            advanceUntilIdle()

            withMessageState.eventSink(MessageComposerEvent.ToggleTextFormatting(true))
            skipItems(1)
            val withFormattingState = awaitItem()
            assertThat(withFormattingState.showTextFormatting).isTrue()
            withFormattingState.eventSink(MessageComposerEvent.SaveDraft)
            advanceUntilIdle()

            withFormattingState.eventSink(MessageComposerEvent.SetMode(anEditMode()))
            val withEditModeState = awaitItem()
            assertThat(withEditModeState.mode).isEqualTo(anEditMode())
            withEditModeState.eventSink(MessageComposerEvent.SaveDraft)
            advanceUntilIdle()

            withEditModeState.eventSink(MessageComposerEvent.SetMode(aReplyMode()))
            val withReplyModeState = awaitItem()
            assertThat(withReplyModeState.mode).isEqualTo(aReplyMode())
            withReplyModeState.eventSink(MessageComposerEvent.SaveDraft)
            advanceUntilIdle()

            assert(saveDraftLambda)
                .isCalledExactly(5)
                .withSequence(
                    listOf(
                        value(A_ROOM_ID),
                        value(null),
                        value(ComposerDraft(plainText = A_MESSAGE, htmlText = null, draftType = ComposerDraftType.NewMessage)),
                        value(false)
                    ),
                    listOf(
                        value(A_ROOM_ID),
                        value(null),
                        value(ComposerDraft(plainText = A_MESSAGE, htmlText = A_MESSAGE, draftType = ComposerDraftType.NewMessage)),
                        value(false)
                    ),
                    listOf(
                        value(A_ROOM_ID),
                        value(null),
                        value(ComposerDraft(plainText = A_MESSAGE, htmlText = A_MESSAGE, draftType = ComposerDraftType.NewMessage)),
                        // The volatile draft created when switching to edit mode.
                        value(true)
                    ),
                    listOf(
                        value(A_ROOM_ID),
                        value(null),
                        value(ComposerDraft(plainText = A_MESSAGE, htmlText = A_MESSAGE, draftType = ComposerDraftType.Edit(AN_EVENT_ID))),
                        value(false)
                    ),
                    listOf(
                        value(A_ROOM_ID),
                        value(null),
                        // When moving from edit mode, text composer is cleared, so the draft is null
                        value(null),
                        value(false)
                    )
                )
        }
    }

    private suspend fun ReceiveTurbine<MessageComposerState>.backToNormalMode(state: MessageComposerState, skipCount: Int = 0): MessageComposerState {
        state.eventSink.invoke(MessageComposerEvent.CloseSpecialMode)
        skipItems(skipCount)
        val normalState = awaitItem()
        assertThat(normalState.mode).isEqualTo(MessageComposerMode.Normal)
        return normalState
    }

    private fun TestScope.createPresenter(
        room: JoinedRoom = FakeJoinedRoom(
            typingNoticeResult = { Result.success(Unit) }
        ),
        timeline: Timeline = room.liveTimeline,
        navigator: MessagesNavigator = FakeMessagesNavigator(),
        pickerProvider: PickerProvider = this@MessageComposerPresenterTest.pickerProvider,
        locationService: LocationService = FakeLocationService(true),
        sessionPreferencesStore: SessionPreferencesStore = InMemorySessionPreferencesStore(),
        mediaPreProcessor: MediaPreProcessor = this@MessageComposerPresenterTest.mediaPreProcessor,
        snackbarDispatcher: SnackbarDispatcher = this@MessageComposerPresenterTest.snackbarDispatcher,
        permissionPresenter: PermissionsPresenter = FakePermissionsPresenter(),
        permalinkBuilder: PermalinkBuilder = FakePermalinkBuilder(),
        permalinkParser: PermalinkParser = FakePermalinkParser(),
        mentionSpanProvider: MentionSpanProvider = MentionSpanProvider(
            permalinkParser = permalinkParser,
            mentionSpanFormatter = FakeMentionSpanFormatter(),
            mentionSpanTheme = MentionSpanTheme(A_USER_ID)
        ),
        textPillificationHelper: TextPillificationHelper = FakeTextPillificationHelper(),
        isRichTextEditorEnabled: Boolean = true,
        draftService: ComposerDraftService = FakeComposerDraftService(),
        mediaOptimizationConfigProvider: FakeMediaOptimizationConfigProvider = FakeMediaOptimizationConfigProvider(),
    ) = MessageComposerPresenter(
        navigator = navigator,
        sessionCoroutineScope = this,
        room = room,
        mediaPickerProvider = pickerProvider,
        sessionPreferencesStore = sessionPreferencesStore,
        localMediaFactory = localMediaFactory,
        mediaSenderFactory = MediaSenderFactory { timelineMode ->
            DefaultMediaSender(
                preProcessor = mediaPreProcessor,
                room = room,
                timelineMode = timelineMode,
                mediaOptimizationConfigProvider = {
                    MediaOptimizationConfig(
                        compressImages = true,
                        videoCompressionPreset = VideoCompressionPreset.STANDARD
                    )
                }
            )
        },
        snackbarDispatcher = snackbarDispatcher,
        analyticsService = analyticsService,
        locationService = locationService,
        messageComposerContext = DefaultMessageComposerContext(),
        richTextEditorStateFactory = TestRichTextEditorStateFactory(),
        roomAliasSuggestionsDataSource = FakeRoomAliasSuggestionsDataSource(),
        permissionsPresenterFactory = FakePermissionsPresenterFactory(permissionPresenter),
        permalinkParser = permalinkParser,
        permalinkBuilder = permalinkBuilder,
        timelineController = TimelineController(room, timeline),
        draftService = draftService,
        mentionSpanProvider = mentionSpanProvider,
        pillificationHelper = textPillificationHelper,
        suggestionsProcessor = SuggestionsProcessor(),
        mediaOptimizationConfigProvider = mediaOptimizationConfigProvider,
        notificationConversationService = notificationConversationService,
    ).apply {
        isTesting = true
        showTextFormatting = isRichTextEditorEnabled
    }

    private suspend fun <T> ReceiveTurbine<T>.awaitFirstItem(): T {
        skipItems(1)
        return awaitItem()
    }
}

fun anEditMode(
    eventOrTransactionId: EventOrTransactionId = AN_EVENT_ID.toEventOrTransactionId(),
    message: String = A_MESSAGE,
) = MessageComposerMode.Edit(eventOrTransactionId, message)

fun anEditCaptionMode(
    eventOrTransactionId: EventOrTransactionId = AN_EVENT_ID.toEventOrTransactionId(),
    caption: String = A_CAPTION,
) = MessageComposerMode.EditCaption(
    eventOrTransactionId = eventOrTransactionId,
    content = caption,
)

fun aReplyMode() = MessageComposerMode.Reply(
    replyToDetails = InReplyToDetails.Loading(AN_EVENT_ID),
    hideImage = false,
)

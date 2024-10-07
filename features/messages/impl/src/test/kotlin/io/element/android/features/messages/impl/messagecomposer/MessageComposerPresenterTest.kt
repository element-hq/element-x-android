/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
import io.element.android.features.messages.impl.draft.ComposerDraftService
import io.element.android.features.messages.impl.draft.FakeComposerDraftService
import io.element.android.features.messages.impl.messagecomposer.suggestions.SuggestionsProcessor
import io.element.android.features.messages.impl.timeline.TimelineController
import io.element.android.features.messages.impl.utils.FakeTextPillificationHelper
import io.element.android.features.messages.impl.utils.TextPillificationHelper
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.featureflag.test.FakeFeatureFlagService
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.ProgressCallback
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.TransactionId
import io.element.android.libraries.matrix.api.media.ImageInfo
import io.element.android.libraries.matrix.api.media.VideoInfo
import io.element.android.libraries.matrix.api.permalink.PermalinkBuilder
import io.element.android.libraries.matrix.api.permalink.PermalinkParser
import io.element.android.libraries.matrix.api.room.IntentionalMention
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.MatrixRoomMembersState
import io.element.android.libraries.matrix.api.room.RoomMembershipState
import io.element.android.libraries.matrix.api.room.draft.ComposerDraft
import io.element.android.libraries.matrix.api.room.draft.ComposerDraftType
import io.element.android.libraries.matrix.api.timeline.TimelineException
import io.element.android.libraries.matrix.api.timeline.item.event.InReplyTo
import io.element.android.libraries.matrix.test.ANOTHER_MESSAGE
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_MESSAGE
import io.element.android.libraries.matrix.test.A_REPLY
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_TRANSACTION_ID
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.A_USER_ID_2
import io.element.android.libraries.matrix.test.A_USER_ID_3
import io.element.android.libraries.matrix.test.A_USER_ID_4
import io.element.android.libraries.matrix.test.core.aBuildMeta
import io.element.android.libraries.matrix.test.media.FakeMediaUploadHandler
import io.element.android.libraries.matrix.test.permalink.FakePermalinkBuilder
import io.element.android.libraries.matrix.test.permalink.FakePermalinkParser
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.libraries.matrix.test.room.aRoomMember
import io.element.android.libraries.matrix.test.timeline.FakeTimeline
import io.element.android.libraries.matrix.ui.messages.RoomMemberProfilesCache
import io.element.android.libraries.matrix.ui.messages.reply.InReplyToDetails
import io.element.android.libraries.mediapickers.api.PickerProvider
import io.element.android.libraries.mediapickers.test.FakePickerProvider
import io.element.android.libraries.mediaupload.api.MediaPreProcessor
import io.element.android.libraries.mediaupload.api.MediaSender
import io.element.android.libraries.mediaupload.api.MediaUploadInfo
import io.element.android.libraries.mediaupload.test.FakeMediaPreProcessor
import io.element.android.libraries.mediaviewer.test.FakeLocalMediaFactory
import io.element.android.libraries.permissions.api.PermissionsPresenter
import io.element.android.libraries.permissions.test.FakePermissionsPresenter
import io.element.android.libraries.permissions.test.FakePermissionsPresenterFactory
import io.element.android.libraries.preferences.api.store.SessionPreferencesStore
import io.element.android.libraries.preferences.test.InMemorySessionPreferencesStore
import io.element.android.libraries.textcomposer.mentions.MentionSpanProvider
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
import io.element.android.tests.testutils.waitForPredicate
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
    private val featureFlagService = FakeFeatureFlagService(
        mapOf(FeatureFlags.LocationSharing.key to true)
    )
    private val mediaPreProcessor = FakeMediaPreProcessor()
    private val snackbarDispatcher = SnackbarDispatcher()
    private val mockMediaUrl: Uri = mockk("localMediaUri")
    private val localMediaFactory = FakeLocalMediaFactory(mockMediaUrl)
    private val analyticsService = FakeAnalyticsService()

    @Test
    fun `present - initial state`() = runTest {
        val presenter = createPresenter(this)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            assertThat(initialState.isFullScreen).isFalse()
            assertThat(initialState.textEditorState.messageHtml()).isEqualTo("")
            assertThat(initialState.mode).isEqualTo(MessageComposerMode.Normal)
            assertThat(initialState.showAttachmentSourcePicker).isFalse()
            assertThat(initialState.canShareLocation).isTrue()
            assertThat(initialState.attachmentsState).isEqualTo(AttachmentsState.None)
        }
    }

    @Test
    fun `present - toggle fullscreen`() = runTest {
        val presenter = createPresenter(this)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            initialState.eventSink.invoke(MessageComposerEvents.ToggleFullScreenState)
            val fullscreenState = awaitItem()
            assertThat(fullscreenState.isFullScreen).isTrue()
            fullscreenState.eventSink.invoke(MessageComposerEvents.ToggleFullScreenState)
            val notFullscreenState = awaitItem()
            assertThat(notFullscreenState.isFullScreen).isFalse()
        }
    }

    @Test
    fun `present - change message`() = runTest {
        val presenter = createPresenter(this)
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
        val loadDraftLambda = lambdaRecorder { _: RoomId, _: Boolean ->
            ComposerDraft(A_MESSAGE, A_MESSAGE, ComposerDraftType.NewMessage)
        }
        val updateDraftLambda = lambdaRecorder { _: RoomId, _: ComposerDraft?, _: Boolean -> }
        val draftService = FakeComposerDraftService().apply {
            this.loadDraftLambda = loadDraftLambda
            this.saveDraftLambda = updateDraftLambda
        }
        val presenter = createPresenter(
            coroutineScope = this,
            draftService = draftService,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            val state = presenter.present()
            remember(state, state.textEditorState.messageHtml()) { state }
        }.test {
            var state = awaitFirstItem()
            val mode = anEditMode(message = ANOTHER_MESSAGE)
            state.eventSink.invoke(MessageComposerEvents.SetMode(mode))
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
                    listOf(value(A_ROOM_ID), value(false)),
                    // Load of volatile draft when closing edit mode
                    listOf(value(A_ROOM_ID), value(true))
                )

            assert(updateDraftLambda)
                .isCalledOnce()
                .with(value(A_ROOM_ID), any(), value(true))
        }
    }

    @Test
    fun `present - change mode to reply after edit`() = runTest {
        val loadDraftLambda = lambdaRecorder { _: RoomId, _: Boolean ->
            ComposerDraft(A_MESSAGE, A_MESSAGE, ComposerDraftType.NewMessage)
        }
        val updateDraftLambda = lambdaRecorder { _: RoomId, _: ComposerDraft?, _: Boolean -> }
        val draftService = FakeComposerDraftService().apply {
            this.loadDraftLambda = loadDraftLambda
            this.saveDraftLambda = updateDraftLambda
        }
        val presenter = createPresenter(
            coroutineScope = this,
            draftService = draftService,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            val state = presenter.present()
            remember(state, state.textEditorState.messageHtml()) { state }
        }.test {
            var state = awaitFirstItem()
            val editMode = anEditMode(message = ANOTHER_MESSAGE)
            state.eventSink.invoke(MessageComposerEvents.SetMode(editMode))
            state = awaitItem()
            assertThat(state.mode).isEqualTo(editMode)
            assertThat(state.textEditorState.messageHtml()).isEqualTo(ANOTHER_MESSAGE)

            val replyMode = aReplyMode()
            state.eventSink.invoke(MessageComposerEvents.SetMode(replyMode))
            state = awaitItem()
            assertThat(state.mode).isEqualTo(replyMode)
            assertThat(state.textEditorState.messageHtml()).isEmpty()

            assert(loadDraftLambda)
                .isCalledOnce()
                .with(value(A_ROOM_ID), value(false))

            assert(updateDraftLambda)
                .isCalledOnce()
                .with(value(A_ROOM_ID), any(), value(true))
        }
    }

    @Test
    fun `present - change mode to reply`() = runTest {
        val presenter = createPresenter(this)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            var state = awaitFirstItem()
            val mode = aReplyMode()
            state.eventSink.invoke(MessageComposerEvents.SetMode(mode))
            state = awaitItem()
            assertThat(state.mode).isEqualTo(mode)
            assertThat(state.textEditorState.messageHtml()).isEqualTo("")
            backToNormalMode(state)
        }
    }

    @Test
    fun `present - cancel reply`() = runTest {
        val presenter = createPresenter(this)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            var state = awaitFirstItem()
            val mode = aReplyMode()
            state.eventSink.invoke(MessageComposerEvents.SetMode(mode))
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
            coroutineScope = this,
            room = FakeMatrixRoom(
                sendMessageResult = { _, _, _ -> Result.success(Unit) },
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
            withMessageState.eventSink.invoke(MessageComposerEvents.SendMessage)
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
            coroutineScope = this,
            isRichTextEditorEnabled = false,
            room = FakeMatrixRoom(
                sendMessageResult = { _, _, _ -> Result.success(Unit) },
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
            withMessageState.eventSink.invoke(MessageComposerEvents.SendMessage)
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
        val editMessageLambda = lambdaRecorder { _: EventId?, _: TransactionId?, _: String, _: String?, _: List<IntentionalMention> ->
            Result.success(Unit)
        }
        val timeline = FakeTimeline().apply {
            this.editMessageLambda = editMessageLambda
        }
        val fakeMatrixRoom = FakeMatrixRoom(
            liveTimeline = timeline,
            typingNoticeResult = { Result.success(Unit) }
        )
        val presenter = createPresenter(
            this,
            fakeMatrixRoom,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            val state = presenter.present()
            remember(state, state.textEditorState.messageHtml()) { state }
        }.test {
            val initialState = awaitFirstItem()
            assertThat(initialState.textEditorState.messageHtml()).isEqualTo("")
            val mode = anEditMode()
            initialState.eventSink.invoke(MessageComposerEvents.SetMode(mode))
            val withMessageState = awaitItem()
            assertThat(withMessageState.mode).isEqualTo(mode)
            assertThat(withMessageState.textEditorState.messageHtml()).isEqualTo(A_MESSAGE)
            withMessageState.textEditorState.setHtml(ANOTHER_MESSAGE)
            val withEditedMessageState = awaitItem()
            assertThat(withEditedMessageState.textEditorState.messageHtml()).isEqualTo(ANOTHER_MESSAGE)
            withEditedMessageState.eventSink.invoke(MessageComposerEvents.SendMessage)
            skipItems(1)
            val messageSentState = awaitItem()
            assertThat(messageSentState.textEditorState.messageHtml()).isEqualTo("")

            advanceUntilIdle()

            assert(editMessageLambda)
                .isCalledOnce()
                .with(value(AN_EVENT_ID), value(null), value(ANOTHER_MESSAGE), value(ANOTHER_MESSAGE), any())

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
        val timelineEditMessageLambda = lambdaRecorder { _: EventId?, _: TransactionId?, _: String, _: String?, _: List<IntentionalMention> ->
            Result.failure<Unit>(TimelineException.EventNotFound)
        }
        val timeline = FakeTimeline().apply {
            this.editMessageLambda = timelineEditMessageLambda
        }
        val roomEditMessageLambda = lambdaRecorder { _: EventId?, _: String, _: String?, _: List<IntentionalMention> ->
            Result.success(Unit)
        }
        val fakeMatrixRoom = FakeMatrixRoom(
            liveTimeline = timeline,
            typingNoticeResult = { Result.success(Unit) },
            editMessageLambda = roomEditMessageLambda,
        )
        val presenter = createPresenter(
            this,
            fakeMatrixRoom,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            val state = presenter.present()
            remember(state, state.textEditorState.messageHtml()) { state }
        }.test {
            val initialState = awaitFirstItem()
            assertThat(initialState.textEditorState.messageHtml()).isEqualTo("")
            val mode = anEditMode()
            initialState.eventSink.invoke(MessageComposerEvents.SetMode(mode))
            val withMessageState = awaitItem()
            assertThat(withMessageState.mode).isEqualTo(mode)
            assertThat(withMessageState.textEditorState.messageHtml()).isEqualTo(A_MESSAGE)
            withMessageState.textEditorState.setHtml(ANOTHER_MESSAGE)
            val withEditedMessageState = awaitItem()
            assertThat(withEditedMessageState.textEditorState.messageHtml()).isEqualTo(ANOTHER_MESSAGE)
            withEditedMessageState.eventSink.invoke(MessageComposerEvents.SendMessage)
            skipItems(1)
            val messageSentState = awaitItem()
            assertThat(messageSentState.textEditorState.messageHtml()).isEqualTo("")

            advanceUntilIdle()

            assert(timelineEditMessageLambda)
                .isCalledOnce()
                .with(value(AN_EVENT_ID), value(null), value(ANOTHER_MESSAGE), value(ANOTHER_MESSAGE), any())

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
        val editMessageLambda = lambdaRecorder { _: EventId?, _: TransactionId?, _: String, _: String?, _: List<IntentionalMention> ->
            Result.success(Unit)
        }
        val timeline = FakeTimeline().apply {
            this.editMessageLambda = editMessageLambda
        }
        val fakeMatrixRoom = FakeMatrixRoom(
            liveTimeline = timeline,
            typingNoticeResult = { Result.success(Unit) },
        )
        val presenter = createPresenter(
            this,
            fakeMatrixRoom,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            val state = presenter.present()
            remember(state, state.textEditorState.messageHtml()) { state }
        }.test {
            val initialState = awaitFirstItem()
            assertThat(initialState.textEditorState.messageHtml()).isEqualTo("")
            val mode = anEditMode(eventId = null, transactionId = A_TRANSACTION_ID)
            initialState.eventSink.invoke(MessageComposerEvents.SetMode(mode))
            val withMessageState = awaitItem()
            assertThat(withMessageState.mode).isEqualTo(mode)
            assertThat(withMessageState.textEditorState.messageHtml()).isEqualTo(A_MESSAGE)
            withMessageState.textEditorState.setHtml(ANOTHER_MESSAGE)
            val withEditedMessageState = awaitItem()
            assertThat(withEditedMessageState.textEditorState.messageHtml()).isEqualTo(ANOTHER_MESSAGE)
            withEditedMessageState.eventSink.invoke(MessageComposerEvents.SendMessage)
            skipItems(1)
            val messageSentState = awaitItem()
            assertThat(messageSentState.textEditorState.messageHtml()).isEqualTo("")

            advanceUntilIdle()

            assert(editMessageLambda)
                .isCalledOnce()
                .with(value(null), value(A_TRANSACTION_ID), value(ANOTHER_MESSAGE), value(ANOTHER_MESSAGE), any())

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
        val replyMessageLambda = lambdaRecorder { _: EventId, _: String, _: String?, _: List<IntentionalMention>, _: Boolean ->
            Result.success(Unit)
        }
        val timeline = FakeTimeline().apply {
            this.replyMessageLambda = replyMessageLambda
        }
        val fakeMatrixRoom = FakeMatrixRoom(
            liveTimeline = timeline,
            typingNoticeResult = { Result.success(Unit) }
        )
        val presenter = createPresenter(
            this,
            fakeMatrixRoom,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            assertThat(initialState.textEditorState.messageHtml()).isEqualTo("")
            val mode = aReplyMode()
            initialState.eventSink.invoke(MessageComposerEvents.SetMode(mode))
            val state = awaitItem()
            assertThat(state.mode).isEqualTo(mode)
            assertThat(state.textEditorState.messageHtml()).isEqualTo("")
            state.textEditorState.setHtml(A_REPLY)
            assertThat(state.textEditorState.messageHtml()).isEqualTo(A_REPLY)
            state.eventSink.invoke(MessageComposerEvents.SendMessage)
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
        val presenter = createPresenter(this)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            assertThat(initialState.showAttachmentSourcePicker).isFalse()
            initialState.eventSink(MessageComposerEvents.AddAttachment)
            assertThat(awaitItem().showAttachmentSourcePicker).isTrue()
        }
    }

    @Test
    fun `present - Dismiss attachments menu`() = runTest {
        val presenter = createPresenter(this)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            initialState.eventSink(MessageComposerEvents.AddAttachment)
            skipItems(1)

            initialState.eventSink(MessageComposerEvents.DismissAttachmentMenu)
            assertThat(awaitItem().showAttachmentSourcePicker).isFalse()
        }
    }

    @Test
    fun `present - Pick image from gallery`() = runTest {
        val room = FakeMatrixRoom(
            typingNoticeResult = { Result.success(Unit) }
        )
        val presenter = createPresenter(this, room = room)
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
            initialState.eventSink(MessageComposerEvents.PickAttachmentSource.FromGallery)
            val previewingState = awaitItem()
            assertThat(previewingState.showAttachmentSourcePicker).isFalse()
            assertThat(previewingState.attachmentsState).isInstanceOf(AttachmentsState.Previewing::class.java)
        }
    }

    @Test
    fun `present - Pick video from gallery`() = runTest {
        val room = FakeMatrixRoom(
            typingNoticeResult = { Result.success(Unit) }
        )
        val presenter = createPresenter(this, room = room)
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
            initialState.eventSink(MessageComposerEvents.PickAttachmentSource.FromGallery)
            val previewingState = awaitItem()
            assertThat(previewingState.showAttachmentSourcePicker).isFalse()
            assertThat(previewingState.attachmentsState).isInstanceOf(AttachmentsState.Previewing::class.java)
        }
    }

    @Test
    fun `present - Pick media from gallery & cancel does nothing`() = runTest {
        val presenter = createPresenter(this)
        with(pickerProvider) {
            givenResult(null) // Simulate a user canceling the flow
            givenMimeType(MimeTypes.Images)
        }
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            initialState.eventSink(MessageComposerEvents.PickAttachmentSource.FromGallery)
            // No crashes here, otherwise it fails
        }
    }

    @Test
    fun `present - Pick file from storage`() = runTest {
        val sendMediaResult = lambdaRecorder { _: ProgressCallback? ->
            Result.success(FakeMediaUploadHandler())
        }
        val room = FakeMatrixRoom(
            progressCallbackValues = listOf(
                Pair(0, 10),
                Pair(5, 10),
                Pair(10, 10)
            ),
            sendMediaResult = sendMediaResult,
            typingNoticeResult = { Result.success(Unit) }
        )
        val presenter = createPresenter(this, room = room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            initialState.eventSink(MessageComposerEvents.PickAttachmentSource.FromFiles)
            val sendingState = awaitItem()
            assertThat(sendingState.showAttachmentSourcePicker).isFalse()
            assertThat(sendingState.attachmentsState).isInstanceOf(AttachmentsState.Sending.Processing::class.java)
            assertThat(awaitItem().attachmentsState).isEqualTo(AttachmentsState.Sending.Uploading(0f))
            assertThat(awaitItem().attachmentsState).isEqualTo(AttachmentsState.Sending.Uploading(0.5f))
            assertThat(awaitItem().attachmentsState).isEqualTo(AttachmentsState.Sending.Uploading(1f))
            val sentState = awaitItem()
            assertThat(sentState.attachmentsState).isEqualTo(AttachmentsState.None)
            sendMediaResult.assertions().isCalledOnce()
        }
    }

    @Test
    fun `present - create poll`() = runTest {
        val room = FakeMatrixRoom(
            typingNoticeResult = { Result.success(Unit) }
        )
        val presenter = createPresenter(this, room = room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            initialState.eventSink(MessageComposerEvents.AddAttachment)
            val attachmentOpenState = awaitItem()
            assertThat(attachmentOpenState.showAttachmentSourcePicker).isTrue()
            initialState.eventSink(MessageComposerEvents.PickAttachmentSource.Poll)
            val finalState = awaitItem()
            assertThat(finalState.showAttachmentSourcePicker).isFalse()
        }
    }

    @Test
    fun `present - share location`() = runTest {
        val room = FakeMatrixRoom(
            typingNoticeResult = { Result.success(Unit) }
        )
        val presenter = createPresenter(this, room = room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            initialState.eventSink(MessageComposerEvents.AddAttachment)
            val attachmentOpenState = awaitItem()
            assertThat(attachmentOpenState.showAttachmentSourcePicker).isTrue()
            initialState.eventSink(MessageComposerEvents.PickAttachmentSource.Location)
            val finalState = awaitItem()
            assertThat(finalState.showAttachmentSourcePicker).isFalse()
        }
    }

    @Test
    fun `present - Take photo`() = runTest {
        val room = FakeMatrixRoom(
            typingNoticeResult = { Result.success(Unit) }
        )
        val permissionPresenter = FakePermissionsPresenter().apply { setPermissionGranted() }
        val presenter = createPresenter(
            this,
            room = room,
            permissionPresenter = permissionPresenter,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            initialState.eventSink(MessageComposerEvents.PickAttachmentSource.PhotoFromCamera)
            val finalState = awaitItem()
            assertThat(finalState.attachmentsState).isInstanceOf(AttachmentsState.Previewing::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - Take photo with permission request`() = runTest {
        val room = FakeMatrixRoom(
            typingNoticeResult = { Result.success(Unit) }
        )
        val permissionPresenter = FakePermissionsPresenter()
        val presenter = createPresenter(
            this,
            room = room,
            permissionPresenter = permissionPresenter,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            initialState.eventSink(MessageComposerEvents.PickAttachmentSource.PhotoFromCamera)
            val permissionState = awaitItem()
            assertThat(permissionState.showAttachmentSourcePicker).isFalse()
            assertThat(permissionState.attachmentsState).isInstanceOf(AttachmentsState.None::class.java)
            permissionPresenter.setPermissionGranted()
            skipItems(1)
            val finalState = awaitItem()
            assertThat(finalState.attachmentsState).isInstanceOf(AttachmentsState.Previewing::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - Record video`() = runTest {
        val room = FakeMatrixRoom(
            typingNoticeResult = { Result.success(Unit) }
        )
        val permissionPresenter = FakePermissionsPresenter().apply { setPermissionGranted() }
        val presenter = createPresenter(
            this,
            room = room,
            permissionPresenter = permissionPresenter,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            initialState.eventSink(MessageComposerEvents.PickAttachmentSource.VideoFromCamera)
            val finalState = awaitItem()
            assertThat(finalState.attachmentsState).isInstanceOf(AttachmentsState.Previewing::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - Record video with permission request`() = runTest {
        val room = FakeMatrixRoom(
            typingNoticeResult = { Result.success(Unit) }
        )
        val permissionPresenter = FakePermissionsPresenter()
        val presenter = createPresenter(
            this,
            room = room,
            permissionPresenter = permissionPresenter,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            initialState.eventSink(MessageComposerEvents.PickAttachmentSource.VideoFromCamera)
            val permissionState = awaitItem()
            assertThat(permissionState.showAttachmentSourcePicker).isFalse()
            assertThat(permissionState.attachmentsState).isInstanceOf(AttachmentsState.None::class.java)
            permissionPresenter.setPermissionGranted()
            skipItems(1)
            val finalState = awaitItem()
            assertThat(finalState.attachmentsState).isInstanceOf(AttachmentsState.Previewing::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - Uploading media failure can be recovered from`() = runTest {
        val room = FakeMatrixRoom(
            sendMediaResult = { Result.failure(Exception()) },
            typingNoticeResult = { Result.success(Unit) }
        )
        val presenter = createPresenter(this, room = room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            initialState.eventSink(MessageComposerEvents.PickAttachmentSource.FromFiles)
            val sendingState = awaitItem()
            assertThat(sendingState.attachmentsState).isInstanceOf(AttachmentsState.Sending::class.java)
            val finalState = awaitItem()
            assertThat(finalState.attachmentsState).isInstanceOf(AttachmentsState.None::class.java)
            snackbarDispatcher.snackbarMessage.test {
                // Assert error message received
                assertThat(awaitItem()).isNotNull()
            }
        }
    }

    @Test
    fun `present - CancelSendAttachment stops media upload`() = runTest {
        val presenter = createPresenter(this)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            initialState.eventSink(MessageComposerEvents.PickAttachmentSource.FromFiles)
            val sendingState = awaitItem()
            assertThat(sendingState.showAttachmentSourcePicker).isFalse()
            assertThat(sendingState.attachmentsState).isInstanceOf(AttachmentsState.Sending.Processing::class.java)
            sendingState.eventSink(MessageComposerEvents.CancelSendAttachment)
            assertThat(awaitItem().attachmentsState).isEqualTo(AttachmentsState.None)
        }
    }

    @Test
    fun `present - errors are tracked`() = runTest {
        val testException = Exception("Test error")
        val presenter = createPresenter(this)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            initialState.eventSink(MessageComposerEvents.Error(testException))
            assertThat(analyticsService.trackedErrors).containsExactly(testException)
        }
    }

    @Test
    fun `present - ToggleTextFormatting toggles text formatting`() = runTest {
        val presenter = createPresenter(this, isRichTextEditorEnabled = false)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            assertThat(initialState.showTextFormatting).isFalse()
            initialState.eventSink(MessageComposerEvents.AddAttachment)
            val composerOptions = awaitItem()
            assertThat(composerOptions.showAttachmentSourcePicker).isTrue()
            composerOptions.eventSink(MessageComposerEvents.ToggleTextFormatting(true))
            skipItems(2) // composer options closed
            val showTextFormatting = awaitItem()
            assertThat(showTextFormatting.showAttachmentSourcePicker).isFalse()
            assertThat(showTextFormatting.showTextFormatting).isTrue()
            assertThat(analyticsService.capturedEvents).containsExactly(
                Interaction(index = null, interactionType = null, name = Interaction.Name.MobileRoomComposerFormattingEnabled)
            )
            analyticsService.capturedEvents.clear()
            showTextFormatting.eventSink(MessageComposerEvents.ToggleTextFormatting(false))
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
        val room = FakeMatrixRoom(
            isDirect = false,
            canUserTriggerRoomNotificationResult = { Result.success(canUserTriggerRoomNotificationResult) },
            typingNoticeResult = { Result.success(Unit) }
        ).apply {
            givenRoomMembersState(
                MatrixRoomMembersState.Ready(
                    persistentListOf(currentUser, invitedUser, bob, david),
                )
            )
        }
        val flagsService = FakeFeatureFlagService(
            mapOf(
                FeatureFlags.Mentions.key to true,
            )
        )
        val presenter = createPresenter(this, room, featureFlagService = flagsService)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()

            // A null suggestion (no suggestion was received) returns nothing
            initialState.eventSink(MessageComposerEvents.SuggestionReceived(null))
            assertThat(awaitItem().suggestions).isEmpty()

            // An empty suggestion returns the room and joined members that are not the current user
            initialState.eventSink(MessageComposerEvents.SuggestionReceived(Suggestion(0, 0, SuggestionType.Mention, "")))
            assertThat(awaitItem().suggestions)
                .containsExactly(ResolvedSuggestion.AtRoom, ResolvedSuggestion.Member(bob), ResolvedSuggestion.Member(david))

            // A suggestion containing a part of "room" will also return the room mention
            initialState.eventSink(MessageComposerEvents.SuggestionReceived(Suggestion(0, 0, SuggestionType.Mention, "roo")))
            assertThat(awaitItem().suggestions).containsExactly(ResolvedSuggestion.AtRoom)

            // A non-empty suggestion will return those joined members whose user id matches it
            initialState.eventSink(MessageComposerEvents.SuggestionReceived(Suggestion(0, 0, SuggestionType.Mention, "bob")))
            assertThat(awaitItem().suggestions).containsExactly(ResolvedSuggestion.Member(bob))

            // A non-empty suggestion will return those joined members whose display name matches it
            initialState.eventSink(MessageComposerEvents.SuggestionReceived(Suggestion(0, 0, SuggestionType.Mention, "dave")))
            assertThat(awaitItem().suggestions).containsExactly(ResolvedSuggestion.Member(david))

            // If the suggestion isn't a mention, no suggestions are returned
            initialState.eventSink(MessageComposerEvents.SuggestionReceived(Suggestion(0, 0, SuggestionType.Command, "")))
            assertThat(awaitItem().suggestions).isEmpty()

            // If user has no permission to send `@room` mentions, `RoomMemberSuggestion.Room` is not returned
            canUserTriggerRoomNotificationResult = false
            initialState.eventSink(MessageComposerEvents.SuggestionReceived(Suggestion(0, 0, SuggestionType.Mention, "")))
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
        val room = FakeMatrixRoom(
            isDirect = true,
            activeMemberCount = 2,
            isEncrypted = true,
            canUserTriggerRoomNotificationResult = { Result.success(true) },
            typingNoticeResult = { Result.success(Unit) }
        ).apply {
            givenRoomMembersState(
                MatrixRoomMembersState.Ready(
                    persistentListOf(currentUser, invitedUser, bob, david),
                )
            )
        }
        val flagsService = FakeFeatureFlagService(
            mapOf(
                FeatureFlags.Mentions.key to true,
            )
        )
        val presenter = createPresenter(this, room, featureFlagService = flagsService)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()

            // An empty suggestion returns the joined members that are not the current user, but not the room
            initialState.eventSink(MessageComposerEvents.SuggestionReceived(Suggestion(0, 0, SuggestionType.Mention, "")))
            skipItems(1)
            assertThat(awaitItem().suggestions)
                .containsExactly(ResolvedSuggestion.Member(bob), ResolvedSuggestion.Member(david))
        }
    }

    fun `present - InsertSuggestion`() = runTest {
        val presenter = createPresenter(
            coroutineScope = this,
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
            initialState.eventSink(MessageComposerEvents.InsertSuggestion(ResolvedSuggestion.Member(aRoomMember(userId = A_USER_ID_2))))

            assertThat(initialState.textEditorState.messageHtml())
                .isEqualTo("Hey <a href='https://matrix.to/#/${A_USER_ID_2.value}'>${A_USER_ID_2.value}</a>")
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `present - send messages with intentional mentions`() = runTest {
        val replyMessageLambda = lambdaRecorder { _: EventId, _: String, _: String?, _: List<IntentionalMention>, _: Boolean ->
            Result.success(Unit)
        }
        val editMessageLambda = lambdaRecorder { _: EventId?, _: TransactionId?, _: String, _: String?, _: List<IntentionalMention> ->
            Result.success(Unit)
        }
        val timeline = FakeTimeline().apply {
            this.replyMessageLambda = replyMessageLambda
            this.editMessageLambda = editMessageLambda
        }
        val sendMessageResult = lambdaRecorder { _: String, _: String?, _: List<IntentionalMention> ->
            Result.success(Unit)
        }
        val room = FakeMatrixRoom(
            liveTimeline = timeline,
            sendMessageResult = sendMessageResult,
            typingNoticeResult = { Result.success(Unit) }
        )
        val presenter = createPresenter(room = room, coroutineScope = this)
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
            initialState.eventSink(MessageComposerEvents.SendMessage)

            advanceUntilIdle()

            sendMessageResult.assertions().isCalledOnce()
                .with(value(A_MESSAGE), any(), value(listOf(IntentionalMention.User(A_USER_ID))))

            // Check intentional mentions on reply sent
            initialState.eventSink(MessageComposerEvents.SetMode(aReplyMode()))
            val mentionUser2 = listOf(A_USER_ID_2.value)
            (awaitItem().textEditorState as? TextEditorState.Rich)?.richTextEditorState?.mentionsState = MentionsState(
                userIds = mentionUser2,
                roomIds = emptyList(),
                roomAliases = emptyList(),
                hasAtRoomMention = false
            )

            initialState.eventSink(MessageComposerEvents.SendMessage)
            advanceUntilIdle()

            assert(replyMessageLambda)
                .isCalledOnce()
                .with(any(), any(), any(), value(listOf(IntentionalMention.User(A_USER_ID_2))), value(false))

            // Check intentional mentions on edit message
            skipItems(1)
            initialState.eventSink(MessageComposerEvents.SetMode(anEditMode()))
            val mentionUser3 = listOf(A_USER_ID_3.value)
            (awaitItem().textEditorState as? TextEditorState.Rich)?.richTextEditorState?.mentionsState = MentionsState(
                userIds = mentionUser3,
                roomIds = emptyList(),
                roomAliases = emptyList(),
                hasAtRoomMention = false
            )

            initialState.eventSink(MessageComposerEvents.SendMessage)
            advanceUntilIdle()

            assert(editMessageLambda)
                .isCalledOnce()
                .with(any(), any(), any(), any(), value(listOf(IntentionalMention.User(A_USER_ID_3))))

            skipItems(1)
        }
    }

    @Test
    fun `present - send uri`() = runTest {
        val presenter = createPresenter(this)
        moleculeFlow(RecompositionMode.Immediate) {
            val state = presenter.present()
            remember(state, state.textEditorState.messageHtml()) { state }
        }.test {
            val initialState = awaitFirstItem()
            initialState.eventSink.invoke(MessageComposerEvents.SendUri(Uri.parse("content://uri")))
            waitForPredicate { mediaPreProcessor.processCallCount == 1 }
        }
    }

    @Test
    fun `present - handle typing notice event`() = runTest {
        val typingNoticeResult = lambdaRecorder<Boolean, Result<Unit>> { Result.success(Unit) }
        val room = FakeMatrixRoom(
            typingNoticeResult = typingNoticeResult
        )
        val presenter = createPresenter(room = room, coroutineScope = this)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            typingNoticeResult.assertions().isNeverCalled()
            initialState.eventSink.invoke(MessageComposerEvents.TypingNotice(true))
            initialState.eventSink.invoke(MessageComposerEvents.TypingNotice(false))
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
        val room = FakeMatrixRoom(
            typingNoticeResult = typingNoticeResult
        )
        val store = InMemorySessionPreferencesStore(
            isSendTypingNotificationsEnabled = false
        )
        val presenter = createPresenter(room = room, sessionPreferencesStore = store, coroutineScope = this)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            typingNoticeResult.assertions().isNeverCalled()
            initialState.eventSink.invoke(MessageComposerEvents.TypingNotice(true))
            initialState.eventSink.invoke(MessageComposerEvents.TypingNotice(false))
            typingNoticeResult.assertions().isNeverCalled()
        }
    }

    @Test
    fun `present - when there is no draft, nothing is restored`() = runTest {
        val loadDraftLambda = lambdaRecorder<RoomId, Boolean, ComposerDraft?> { _, _ -> null }
        val composerDraftService = FakeComposerDraftService().apply {
            this.loadDraftLambda = loadDraftLambda
        }
        val presenter = createPresenter(draftService = composerDraftService, coroutineScope = this)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitFirstItem()
            assert(loadDraftLambda)
                .isCalledOnce()
                .with(value(A_ROOM_ID), value(false))

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - when there is a draft for new message with plain text, it is restored`() = runTest {
        val loadDraftLambda = lambdaRecorder<RoomId, Boolean, ComposerDraft?> { _, _ ->
            ComposerDraft(plainText = A_MESSAGE, htmlText = null, draftType = ComposerDraftType.NewMessage)
        }
        val composerDraftService = FakeComposerDraftService().apply {
            this.loadDraftLambda = loadDraftLambda
        }
        val permalinkBuilder = FakePermalinkBuilder()
        val presenter = createPresenter(
            draftService = composerDraftService,
            permalinkBuilder = permalinkBuilder,
            coroutineScope = this
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitFirstItem().also { state ->
                assertThat(state.textEditorState.messageMarkdown(permalinkBuilder)).isEqualTo(A_MESSAGE)
                assertThat(state.textEditorState.messageHtml()).isNull()
            }

            assert(loadDraftLambda)
                .isCalledOnce()
                .with(value(A_ROOM_ID), value(false))

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - when there is a draft for new message with rich text, it is restored`() = runTest {
        val loadDraftLambda = lambdaRecorder<RoomId, Boolean, ComposerDraft?> { _, _ ->
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
            coroutineScope = this
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitFirstItem().also { state ->
                assertThat(state.showTextFormatting).isTrue()
                assertThat(state.textEditorState.messageMarkdown(permalinkBuilder)).isEqualTo(A_MESSAGE)
                assertThat(state.textEditorState.messageHtml()).isEqualTo(A_MESSAGE)
            }
            assert(loadDraftLambda)
                .isCalledOnce()
                .with(value(A_ROOM_ID), value(false))
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - when there is a draft for edit, it is restored`() = runTest {
        val loadDraftLambda = lambdaRecorder<RoomId, Boolean, ComposerDraft?> { _, _ ->
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
            coroutineScope = this
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitFirstItem().also { state ->
                assertThat(state.showTextFormatting).isFalse()
                assertThat(state.mode).isEqualTo(anEditMode())
                assertThat(state.textEditorState.messageMarkdown(permalinkBuilder)).isEqualTo(A_MESSAGE)
                assertThat(state.textEditorState.messageHtml()).isNull()
            }
            assert(loadDraftLambda)
                .isCalledOnce()
                .with(value(A_ROOM_ID), value(false))

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - when there is a draft for reply, it is restored`() = runTest {
        val loadDraftLambda = lambdaRecorder<RoomId, Boolean, ComposerDraft?> { _, _ ->
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
        val room = FakeMatrixRoom(
            liveTimeline = timeline,
            typingNoticeResult = { Result.success(Unit) },
        )
        val permalinkBuilder = FakePermalinkBuilder()
        val presenter = createPresenter(
            room = room,
            draftService = composerDraftService,
            permalinkBuilder = permalinkBuilder,
            coroutineScope = this
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitFirstItem().also { state ->
                assertThat(state.showTextFormatting).isFalse()
                assertThat(state.mode).isEqualTo(aReplyMode())
                assertThat(state.textEditorState.messageMarkdown(permalinkBuilder)).isEqualTo(A_MESSAGE)
                assertThat(state.textEditorState.messageHtml()).isNull()
            }
            assert(loadDraftLambda)
                .isCalledOnce()
                .with(value(A_ROOM_ID), value(false))

            assert(loadReplyDetailsLambda)
                .isCalledOnce()
                .with(value(AN_EVENT_ID))

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - when save draft event is invoked and composer is empty then service is called with null draft`() = runTest {
        val saveDraftLambda = lambdaRecorder<RoomId, ComposerDraft?, Boolean, Unit> { _, _, _ -> }
        val composerDraftService = FakeComposerDraftService().apply {
            this.saveDraftLambda = saveDraftLambda
        }
        val presenter = createPresenter(draftService = composerDraftService, coroutineScope = this)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            initialState.eventSink.invoke(MessageComposerEvents.SaveDraft)
            advanceUntilIdle()
            assert(saveDraftLambda)
                .isCalledOnce()
                .with(value(A_ROOM_ID), value(null), value(false))
        }
    }

    @Test
    fun `present - when save draft event is invoked and composer is not empty then service is called`() = runTest {
        val saveDraftLambda = lambdaRecorder<RoomId, ComposerDraft?, Boolean, Unit> { _, _, _ -> }
        val composerDraftService = FakeComposerDraftService().apply {
            this.saveDraftLambda = saveDraftLambda
        }
        val permalinkBuilder = FakePermalinkBuilder()
        val presenter = createPresenter(
            isRichTextEditorEnabled = false,
            draftService = composerDraftService,
            permalinkBuilder = permalinkBuilder,
            coroutineScope = this
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
            withMessageState.eventSink(MessageComposerEvents.SaveDraft)
            advanceUntilIdle()

            withMessageState.eventSink(MessageComposerEvents.ToggleTextFormatting(true))
            skipItems(1)
            val withFormattingState = awaitItem()
            assertThat(withFormattingState.showTextFormatting).isTrue()
            withFormattingState.eventSink(MessageComposerEvents.SaveDraft)
            advanceUntilIdle()

            withFormattingState.eventSink(MessageComposerEvents.SetMode(anEditMode()))
            val withEditModeState = awaitItem()
            assertThat(withEditModeState.mode).isEqualTo(anEditMode())
            withEditModeState.eventSink(MessageComposerEvents.SaveDraft)
            advanceUntilIdle()

            withEditModeState.eventSink(MessageComposerEvents.SetMode(aReplyMode()))
            val withReplyModeState = awaitItem()
            assertThat(withReplyModeState.mode).isEqualTo(aReplyMode())
            withReplyModeState.eventSink(MessageComposerEvents.SaveDraft)
            advanceUntilIdle()

            assert(saveDraftLambda)
                .isCalledExactly(5)
                .withSequence(
                    listOf(
                        value(A_ROOM_ID),
                        value(ComposerDraft(plainText = A_MESSAGE, htmlText = null, draftType = ComposerDraftType.NewMessage)),
                        value(false)
                    ),
                    listOf(
                        value(A_ROOM_ID),
                        value(ComposerDraft(plainText = A_MESSAGE, htmlText = A_MESSAGE, draftType = ComposerDraftType.NewMessage)),
                        value(false)
                    ),
                    listOf(
                        value(A_ROOM_ID),
                        value(ComposerDraft(plainText = A_MESSAGE, htmlText = A_MESSAGE, draftType = ComposerDraftType.NewMessage)),
                        // The volatile draft created when switching to edit mode.
                        value(true)
                    ),
                    listOf(
                        value(A_ROOM_ID),
                        value(ComposerDraft(plainText = A_MESSAGE, htmlText = A_MESSAGE, draftType = ComposerDraftType.Edit(AN_EVENT_ID))),
                        value(false)
                    ),
                    listOf(
                        value(A_ROOM_ID),
                        // When moving from edit mode, text composer is cleared, so the draft is null
                        value(null),
                        value(false)
                    )
                )
        }
    }

    private suspend fun ReceiveTurbine<MessageComposerState>.backToNormalMode(state: MessageComposerState, skipCount: Int = 0): MessageComposerState {
        state.eventSink.invoke(MessageComposerEvents.CloseSpecialMode)
        skipItems(skipCount)
        val normalState = awaitItem()
        assertThat(normalState.mode).isEqualTo(MessageComposerMode.Normal)
        return normalState
    }

    private fun createPresenter(
        coroutineScope: CoroutineScope,
        room: MatrixRoom = FakeMatrixRoom(
            typingNoticeResult = { Result.success(Unit) }
        ),
        pickerProvider: PickerProvider = this.pickerProvider,
        featureFlagService: FeatureFlagService = this.featureFlagService,
        sessionPreferencesStore: SessionPreferencesStore = InMemorySessionPreferencesStore(),
        mediaPreProcessor: MediaPreProcessor = this.mediaPreProcessor,
        snackbarDispatcher: SnackbarDispatcher = this.snackbarDispatcher,
        permissionPresenter: PermissionsPresenter = FakePermissionsPresenter(),
        permalinkBuilder: PermalinkBuilder = FakePermalinkBuilder(),
        permalinkParser: PermalinkParser = FakePermalinkParser(),
        mentionSpanProvider: MentionSpanProvider = MentionSpanProvider(permalinkParser),
        roomMemberProfilesCache: RoomMemberProfilesCache = RoomMemberProfilesCache(),
        textPillificationHelper: TextPillificationHelper = FakeTextPillificationHelper(),
        isRichTextEditorEnabled: Boolean = true,
        draftService: ComposerDraftService = FakeComposerDraftService(),
    ) = MessageComposerPresenter(
        coroutineScope,
        room,
        pickerProvider,
        featureFlagService,
        sessionPreferencesStore,
        localMediaFactory,
        MediaSender(mediaPreProcessor, room),
        snackbarDispatcher,
        analyticsService,
        DefaultMessageComposerContext(),
        TestRichTextEditorStateFactory(),
        roomAliasSuggestionsDataSource = FakeRoomAliasSuggestionsDataSource(),
        permissionsPresenterFactory = FakePermissionsPresenterFactory(permissionPresenter),
        permalinkParser = permalinkParser,
        permalinkBuilder = permalinkBuilder,
        timelineController = TimelineController(room),
        draftService = draftService,
        mentionSpanProvider = mentionSpanProvider,
        pillificationHelper = textPillificationHelper,
        roomMemberProfilesCache = roomMemberProfilesCache,
        suggestionsProcessor = SuggestionsProcessor(),
    ).apply {
        isTesting = true
        showTextFormatting = isRichTextEditorEnabled
    }

    private suspend fun <T> ReceiveTurbine<T>.awaitFirstItem(): T {
        // Skip 2 item if Mentions feature is enabled, else 1
        skipItems(if (FeatureFlags.Mentions.defaultValue(aBuildMeta())) 2 else 1)
        return awaitItem()
    }
}

fun anEditMode(
    eventId: EventId? = AN_EVENT_ID,
    message: String = A_MESSAGE,
    transactionId: TransactionId? = null,
) = MessageComposerMode.Edit(eventId, transactionId, message)

fun aReplyMode() = MessageComposerMode.Reply(
    replyToDetails = InReplyToDetails.Loading(AN_EVENT_ID),
    hideImage = false,
)

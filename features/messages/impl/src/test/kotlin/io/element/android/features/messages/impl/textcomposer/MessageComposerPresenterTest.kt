/*
 * Copyright (c) 2023 New Vector Ltd
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

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.features.messages.impl.textcomposer

import android.net.Uri
import androidx.compose.runtime.remember
import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import im.vector.app.features.analytics.plan.Composer
import im.vector.app.features.analytics.plan.Interaction
import io.element.android.features.messages.impl.messagecomposer.AttachmentsState
import io.element.android.features.messages.impl.messagecomposer.DefaultMessageComposerContext
import io.element.android.features.messages.impl.messagecomposer.MessageComposerEvents
import io.element.android.features.messages.impl.messagecomposer.MessageComposerPresenter
import io.element.android.features.messages.impl.messagecomposer.MessageComposerState
import io.element.android.features.messages.impl.timeline.TimelineController
import io.element.android.features.preferences.api.store.SessionPreferencesStore
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.featureflag.test.FakeFeatureFlagService
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.TransactionId
import io.element.android.libraries.matrix.api.media.ImageInfo
import io.element.android.libraries.matrix.api.media.VideoInfo
import io.element.android.libraries.matrix.api.permalink.PermalinkBuilder
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.MatrixRoomMembersState
import io.element.android.libraries.matrix.api.room.Mention
import io.element.android.libraries.matrix.api.room.RoomMembershipState
import io.element.android.libraries.matrix.api.user.CurrentSessionIdHolder
import io.element.android.libraries.matrix.test.ANOTHER_MESSAGE
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_MESSAGE
import io.element.android.libraries.matrix.test.A_REPLY
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_TRANSACTION_ID
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.A_USER_ID_2
import io.element.android.libraries.matrix.test.A_USER_ID_3
import io.element.android.libraries.matrix.test.A_USER_ID_4
import io.element.android.libraries.matrix.test.A_USER_NAME
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.permalink.FakePermalinkBuilder
import io.element.android.libraries.matrix.test.permalink.FakePermalinkParser
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.libraries.matrix.test.room.aRoomMember
import io.element.android.libraries.matrix.test.timeline.FakeTimeline
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
import io.element.android.libraries.preferences.test.InMemorySessionPreferencesStore
import io.element.android.libraries.textcomposer.mentions.ResolvedMentionSuggestion
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
        val presenter = createPresenter(this)
        moleculeFlow(RecompositionMode.Immediate) {
            val state = presenter.present()
            remember(state, state.textEditorState.messageHtml()) { state }
        }.test {
            var state = awaitFirstItem()
            val mode = anEditMode()
            state.eventSink.invoke(MessageComposerEvents.SetMode(mode))
            state = awaitItem()
            assertThat(state.mode).isEqualTo(mode)
            state = awaitItem()
            assertThat(state.textEditorState.messageHtml()).isEqualTo(A_MESSAGE)
            state = backToNormalMode(state, skipCount = 1)

            // The message that was being edited is cleared
            assertThat(state.textEditorState.messageHtml()).isEqualTo("")
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
    fun `present - change mode to quote`() = runTest {
        val presenter = createPresenter(this)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            var state = awaitFirstItem()
            val mode = aQuoteMode()
            state.eventSink.invoke(MessageComposerEvents.SetMode(mode))
            state = awaitItem()
            assertThat(state.mode).isEqualTo(mode)
            assertThat(state.textEditorState.messageHtml()).isEqualTo("")
            backToNormalMode(state)
        }
    }

    @Test
    fun `present - send message with rich text enabled`() = runTest {
        val presenter = createPresenter(this)
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
        val permalinkBuilder = FakePermalinkBuilder(result = { Result.success("") })
        val presenter = createPresenter(this, isRichTextEditorEnabled = false)
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
        val editMessageLambda = lambdaRecorder { _: EventId?, _: TransactionId?, _: String, _: String?, _: List<Mention> ->
            Result.success(Unit)
        }
        val timeline = FakeTimeline().apply {
            this.editMessageLambda = editMessageLambda
        }
        val fakeMatrixRoom = FakeMatrixRoom(liveTimeline = timeline)
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
            skipItems(1)
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
                .with(any(), any(), value(ANOTHER_MESSAGE), value(ANOTHER_MESSAGE), any())

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
        val editMessageLambda = lambdaRecorder { _: EventId?, _: TransactionId?, _: String, _: String?, _: List<Mention> ->
            Result.success(Unit)
        }
        val timeline = FakeTimeline().apply {
            this.editMessageLambda = editMessageLambda
        }
        val fakeMatrixRoom = FakeMatrixRoom(liveTimeline = timeline)
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
            skipItems(1)
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
                .with(any(), any(), value(ANOTHER_MESSAGE), value(ANOTHER_MESSAGE), any())

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
        val replyMessageLambda = lambdaRecorder { _: EventId, _: String, _: String?, _: List<Mention>, _: Boolean ->
            Result.success(Unit)
        }
        val timeline = FakeTimeline().apply {
            this.replyMessageLambda = replyMessageLambda
        }
        val fakeMatrixRoom = FakeMatrixRoom(liveTimeline = timeline)
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
        val room = FakeMatrixRoom()
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
        val room = FakeMatrixRoom()
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
        val room = FakeMatrixRoom()
        room.givenProgressCallbackValues(
            listOf(
                Pair(0, 10),
                Pair(5, 10),
                Pair(10, 10)
            )
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
            assertThat(room.sendMediaCount).isEqualTo(1)
        }
    }

    @Test
    fun `present - create poll`() = runTest {
        val room = FakeMatrixRoom()
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
        val room = FakeMatrixRoom()
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
        val room = FakeMatrixRoom()
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
        val room = FakeMatrixRoom()
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
        val room = FakeMatrixRoom()
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
        val room = FakeMatrixRoom()
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
        val room = FakeMatrixRoom().apply {
            givenSendMediaResult(Result.failure(Exception()))
        }
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
        val room = FakeMatrixRoom(
            isDirect = false,
            isOneToOne = false,
        ).apply {
            givenRoomMembersState(
                MatrixRoomMembersState.Ready(
                    persistentListOf(currentUser, invitedUser, bob, david),
                )
            )
            givenCanTriggerRoomNotification(Result.success(true))
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
            assertThat(awaitItem().memberSuggestions).isEmpty()

            // An empty suggestion returns the room and joined members that are not the current user
            initialState.eventSink(MessageComposerEvents.SuggestionReceived(Suggestion(0, 0, SuggestionType.Mention, "")))
            assertThat(awaitItem().memberSuggestions)
                .containsExactly(ResolvedMentionSuggestion.AtRoom, ResolvedMentionSuggestion.Member(bob), ResolvedMentionSuggestion.Member(david))

            // A suggestion containing a part of "room" will also return the room mention
            initialState.eventSink(MessageComposerEvents.SuggestionReceived(Suggestion(0, 0, SuggestionType.Mention, "roo")))
            assertThat(awaitItem().memberSuggestions).containsExactly(ResolvedMentionSuggestion.AtRoom)

            // A non-empty suggestion will return those joined members whose user id matches it
            initialState.eventSink(MessageComposerEvents.SuggestionReceived(Suggestion(0, 0, SuggestionType.Mention, "bob")))
            assertThat(awaitItem().memberSuggestions).containsExactly(ResolvedMentionSuggestion.Member(bob))

            // A non-empty suggestion will return those joined members whose display name matches it
            initialState.eventSink(MessageComposerEvents.SuggestionReceived(Suggestion(0, 0, SuggestionType.Mention, "dave")))
            assertThat(awaitItem().memberSuggestions).containsExactly(ResolvedMentionSuggestion.Member(david))

            // If the suggestion isn't a mention, no suggestions are returned
            initialState.eventSink(MessageComposerEvents.SuggestionReceived(Suggestion(0, 0, SuggestionType.Command, "")))
            assertThat(awaitItem().memberSuggestions).isEmpty()

            // If user has no permission to send `@room` mentions, `RoomMemberSuggestion.Room` is not returned
            room.givenCanTriggerRoomNotification(Result.success(false))
            initialState.eventSink(MessageComposerEvents.SuggestionReceived(Suggestion(0, 0, SuggestionType.Mention, "")))
            assertThat(awaitItem().memberSuggestions)
                .containsExactly(ResolvedMentionSuggestion.Member(bob), ResolvedMentionSuggestion.Member(david))

            // If room is a DM, `RoomMemberSuggestion.Room` is not returned
            room.givenCanTriggerRoomNotification(Result.success(true))
            room.isDirect
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
            isOneToOne = true,
        ).apply {
            givenRoomMembersState(
                MatrixRoomMembersState.Ready(
                    persistentListOf(currentUser, invitedUser, bob, david),
                )
            )
            givenCanTriggerRoomNotification(Result.success(true))
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
            assertThat(awaitItem().memberSuggestions)
                .containsExactly(ResolvedMentionSuggestion.Member(bob), ResolvedMentionSuggestion.Member(david))
        }
    }

    @Test
    fun `present - insertMention`() = runTest {
        val presenter = createPresenter(
            coroutineScope = this,
            permalinkBuilder = FakePermalinkBuilder(
                result = {
                    Result.success("https://matrix.to/#/${A_USER_ID_2.value}")
                }
            )
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            initialState.textEditorState.setHtml("Hey @bo")
            initialState.eventSink(MessageComposerEvents.InsertMention(ResolvedMentionSuggestion.Member(aRoomMember(userId = A_USER_ID_2))))

            assertThat(initialState.textEditorState.messageHtml())
                .isEqualTo("Hey <a href='https://matrix.to/#/${A_USER_ID_2.value}'>${A_USER_ID_2.value}</a>")
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `present - send messages with intentional mentions`() = runTest {
        val replyMessageLambda = lambdaRecorder { _: EventId, _: String, _: String?, _: List<Mention>, _: Boolean ->
            Result.success(Unit)
        }
        val editMessageLambda = lambdaRecorder { _: EventId?, _: TransactionId?, _: String, _: String?, _: List<Mention> ->
            Result.success(Unit)
        }
        val timeline = FakeTimeline().apply {
            this.replyMessageLambda = replyMessageLambda
            this.editMessageLambda = editMessageLambda
        }
        val room = FakeMatrixRoom(liveTimeline = timeline)
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

            assertThat(room.sendMessageMentions).isEqualTo(listOf(Mention.User(A_USER_ID)))

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
                .with(any(), any(), any(), value(listOf(Mention.User(A_USER_ID_2))), value(false))

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
                .with(any(), any(), any(), any(), value(listOf(Mention.User(A_USER_ID_3))))

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
        val room = FakeMatrixRoom()
        val presenter = createPresenter(room = room, coroutineScope = this)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            assertThat(room.typingRecord).isEmpty()
            initialState.eventSink.invoke(MessageComposerEvents.TypingNotice(true))
            initialState.eventSink.invoke(MessageComposerEvents.TypingNotice(false))
            assertThat(room.typingRecord).isEqualTo(listOf(true, false))
        }
    }

    @Test
    fun `present - handle typing notice event when sending typing notice is disabled`() = runTest {
        val room = FakeMatrixRoom()
        val store = InMemorySessionPreferencesStore(
            isSendTypingNotificationsEnabled = false
        )
        val presenter = createPresenter(room = room, sessionPreferencesStore = store, coroutineScope = this)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            assertThat(room.typingRecord).isEmpty()
            initialState.eventSink.invoke(MessageComposerEvents.TypingNotice(true))
            initialState.eventSink.invoke(MessageComposerEvents.TypingNotice(false))
            assertThat(room.typingRecord).isEmpty()
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
        room: MatrixRoom = FakeMatrixRoom(),
        pickerProvider: PickerProvider = this.pickerProvider,
        featureFlagService: FeatureFlagService = this.featureFlagService,
        sessionPreferencesStore: SessionPreferencesStore = InMemorySessionPreferencesStore(),
        mediaPreProcessor: MediaPreProcessor = this.mediaPreProcessor,
        snackbarDispatcher: SnackbarDispatcher = this.snackbarDispatcher,
        permissionPresenter: PermissionsPresenter = FakePermissionsPresenter(),
        permalinkBuilder: PermalinkBuilder = FakePermalinkBuilder(),
        isRichTextEditorEnabled: Boolean = true,
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
        currentSessionIdHolder = CurrentSessionIdHolder(FakeMatrixClient(A_SESSION_ID)),
        permissionsPresenterFactory = FakePermissionsPresenterFactory(permissionPresenter),
        permalinkParser = FakePermalinkParser(),
        permalinkBuilder = permalinkBuilder,
        timelineController = TimelineController(room),
    ).apply {
        isTesting = true
        showTextFormatting = isRichTextEditorEnabled
    }

    private suspend fun <T> ReceiveTurbine<T>.awaitFirstItem(): T {
        // Skip 2 item if Mentions feature is enabled, else 1
        skipItems(if (FeatureFlags.Mentions.defaultValue) 2 else 1)
        return awaitItem()
    }
}

fun anEditMode(
    eventId: EventId? = AN_EVENT_ID,
    message: String = A_MESSAGE,
    transactionId: TransactionId? = null,
) = MessageComposerMode.Edit(eventId, message, transactionId)

fun aReplyMode() = MessageComposerMode.Reply(A_USER_NAME, null, false, AN_EVENT_ID, A_MESSAGE)
fun aQuoteMode() = MessageComposerMode.Quote(AN_EVENT_ID, A_MESSAGE)

private suspend fun TextEditorState.setHtml(html: String) {
    (this as? TextEditorState.Rich)?.richTextEditorState?.setHtml(html) ?: error("TextEditorState is not Rich")
}

private fun TextEditorState.setMarkdown(markdown: String) {
    (this as? TextEditorState.Markdown)?.state?.text?.update(markdown, needsDisplaying = false) ?: error("TextEditorState is not Markdown")
}

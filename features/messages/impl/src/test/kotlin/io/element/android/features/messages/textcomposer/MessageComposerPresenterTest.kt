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

package io.element.android.features.messages.textcomposer

import android.net.Uri
import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.messages.impl.messagecomposer.AttachmentsState
import io.element.android.features.messages.impl.messagecomposer.MessageComposerEvents
import io.element.android.features.messages.impl.messagecomposer.MessageComposerPresenter
import io.element.android.features.messages.impl.messagecomposer.MessageComposerState
import io.element.android.features.messages.media.FakeLocalMediaFactory
import io.element.android.libraries.core.data.StableCharSequence
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.designsystem.utils.SnackbarDispatcher
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.featureflag.test.FakeFeatureFlagService
import io.element.android.libraries.matrix.api.media.ImageInfo
import io.element.android.libraries.matrix.api.media.ThumbnailInfo
import io.element.android.libraries.matrix.api.media.VideoInfo
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.test.ANOTHER_MESSAGE
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_MESSAGE
import io.element.android.libraries.matrix.test.A_REPLY
import io.element.android.libraries.matrix.test.A_USER_NAME
import io.element.android.libraries.matrix.test.room.aFakeMatrixRoom
import io.element.android.libraries.mediapickers.api.PickerProvider
import io.element.android.libraries.mediapickers.test.FakePickerProvider
import io.element.android.libraries.mediaupload.api.MediaPreProcessor
import io.element.android.libraries.mediaupload.api.MediaSender
import io.element.android.libraries.mediaupload.api.MediaUploadInfo
import io.element.android.libraries.mediaupload.api.ThumbnailProcessingInfo
import io.element.android.libraries.mediaupload.test.FakeMediaPreProcessor
import io.element.android.libraries.textcomposer.MessageComposerMode
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.File

class MessageComposerPresenterTest {

    private val pickerProvider = FakePickerProvider().apply {
        givenResult(mockk()) // Uri is not available in JVM, so the only way to have a non-null Uri is using Mockk
    }
    private val featureFlagService = FakeFeatureFlagService(
        mapOf(FeatureFlags.ShowMediaUploadingFlow.key to true)
    )
    private val mediaPreProcessor = FakeMediaPreProcessor()
    private val snackbarDispatcher = SnackbarDispatcher()
    private val mockMediaUrl: Uri = mockk("localMediaUri")
    private val localMediaFactory = FakeLocalMediaFactory(mockMediaUrl)

    @Test
    fun `present - initial state`() = runTest {
        val presenter = createPresenter()
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.isFullScreen).isFalse()
            assertThat(initialState.text).isEqualTo(StableCharSequence(""))
            assertThat(initialState.mode).isEqualTo(MessageComposerMode.Normal(""))
            assertThat(initialState.showAttachmentSourcePicker).isFalse()
            assertThat(initialState.attachmentsState).isEqualTo(AttachmentsState.None)
            assertThat(initialState.isSendButtonVisible).isFalse()
        }
    }

    @Test
    fun `present - toggle fullscreen`() = runTest {
        val presenter = createPresenter()
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
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
        val presenter = createPresenter()
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(MessageComposerEvents.UpdateText(A_MESSAGE))
            val withMessageState = awaitItem()
            assertThat(withMessageState.text).isEqualTo(StableCharSequence(A_MESSAGE))
            assertThat(withMessageState.isSendButtonVisible).isTrue()
            withMessageState.eventSink.invoke(MessageComposerEvents.UpdateText(""))
            val withEmptyMessageState = awaitItem()
            assertThat(withEmptyMessageState.text).isEqualTo(StableCharSequence(""))
            assertThat(withEmptyMessageState.isSendButtonVisible).isFalse()
        }
    }

    @Test
    fun `present - change mode to edit`() = runTest {
        val presenter = createPresenter()
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            var state = awaitItem()
            val mode = anEditMode()
            state.eventSink.invoke(MessageComposerEvents.SetMode(mode))
            state = awaitItem()
            assertThat(state.mode).isEqualTo(mode)
            state = awaitItem()
            assertThat(state.text).isEqualTo(StableCharSequence(A_MESSAGE))
            assertThat(state.isSendButtonVisible).isTrue()
            backToNormalMode(state, skipCount = 1)
        }
    }

    @Test
    fun `present - change mode to reply`() = runTest {
        val presenter = createPresenter()
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            var state = awaitItem()
            val mode = aReplyMode()
            state.eventSink.invoke(MessageComposerEvents.SetMode(mode))
            state = awaitItem()
            assertThat(state.mode).isEqualTo(mode)
            assertThat(state.text).isEqualTo(StableCharSequence(""))
            assertThat(state.isSendButtonVisible).isFalse()
            backToNormalMode(state)
        }
    }

    @Test
    fun `present - change mode to quote`() = runTest {
        val presenter = createPresenter()
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            var state = awaitItem()
            val mode = aQuoteMode()
            state.eventSink.invoke(MessageComposerEvents.SetMode(mode))
            state = awaitItem()
            assertThat(state.mode).isEqualTo(mode)
            assertThat(state.text).isEqualTo(StableCharSequence(""))
            assertThat(state.isSendButtonVisible).isFalse()
            backToNormalMode(state)
        }
    }

    @Test
    fun `present - send message`() = runTest {
        val presenter = createPresenter()
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink.invoke(MessageComposerEvents.UpdateText(A_MESSAGE))
            val withMessageState = awaitItem()
            assertThat(withMessageState.text).isEqualTo(StableCharSequence(A_MESSAGE))
            assertThat(withMessageState.isSendButtonVisible).isTrue()
            withMessageState.eventSink.invoke(MessageComposerEvents.SendMessage(A_MESSAGE))
            val messageSentState = awaitItem()
            assertThat(messageSentState.text).isEqualTo(StableCharSequence(""))
            assertThat(messageSentState.isSendButtonVisible).isFalse()
        }
    }

    @Test
    fun `present - edit message`() = runTest {
        val fakeMatrixRoom = aFakeMatrixRoom()
        val presenter = createPresenter(
            fakeMatrixRoom,
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.text).isEqualTo(StableCharSequence(""))
            val mode = anEditMode()
            initialState.eventSink.invoke(MessageComposerEvents.SetMode(mode))
            skipItems(1)
            val withMessageState = awaitItem()
            assertThat(withMessageState.mode).isEqualTo(mode)
            assertThat(withMessageState.text).isEqualTo(StableCharSequence(A_MESSAGE))
            assertThat(withMessageState.isSendButtonVisible).isTrue()
            withMessageState.eventSink.invoke(MessageComposerEvents.UpdateText(ANOTHER_MESSAGE))
            val withEditedMessageState = awaitItem()
            assertThat(withEditedMessageState.text).isEqualTo(StableCharSequence(ANOTHER_MESSAGE))
            withEditedMessageState.eventSink.invoke(MessageComposerEvents.SendMessage(ANOTHER_MESSAGE))
            skipItems(1)
            val messageSentState = awaitItem()
            assertThat(messageSentState.text).isEqualTo(StableCharSequence(""))
            assertThat(messageSentState.isSendButtonVisible).isFalse()
            assertThat(fakeMatrixRoom.editMessageParameter).isEqualTo(ANOTHER_MESSAGE)
        }
    }

    @Test
    fun `present - reply message`() = runTest {
        val fakeMatrixRoom = aFakeMatrixRoom()
        val presenter = createPresenter(
            fakeMatrixRoom,
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.text).isEqualTo(StableCharSequence(""))
            val mode = aReplyMode()
            initialState.eventSink.invoke(MessageComposerEvents.SetMode(mode))
            val state = awaitItem()
            assertThat(state.mode).isEqualTo(mode)
            assertThat(state.text).isEqualTo(StableCharSequence(""))
            assertThat(state.isSendButtonVisible).isFalse()
            initialState.eventSink.invoke(MessageComposerEvents.UpdateText(A_REPLY))
            val withMessageState = awaitItem()
            assertThat(withMessageState.text).isEqualTo(StableCharSequence(A_REPLY))
            assertThat(withMessageState.isSendButtonVisible).isTrue()
            withMessageState.eventSink.invoke(MessageComposerEvents.SendMessage(A_REPLY))
            skipItems(1)
            val messageSentState = awaitItem()
            assertThat(messageSentState.text).isEqualTo(StableCharSequence(""))
            assertThat(messageSentState.isSendButtonVisible).isFalse()
            assertThat(fakeMatrixRoom.replyMessageParameter).isEqualTo(A_REPLY)
        }
    }

    @Test
    fun `present - Open attachments menu`() = runTest {
        val presenter = createPresenter()
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.showAttachmentSourcePicker).isEqualTo(false)
            initialState.eventSink(MessageComposerEvents.AddAttachment)
            assertThat(awaitItem().showAttachmentSourcePicker).isEqualTo(true)
        }
    }

    @Test
    fun `present - Dismiss attachments menu`() = runTest {
        val presenter = createPresenter()
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(MessageComposerEvents.AddAttachment)
            skipItems(1)

            initialState.eventSink(MessageComposerEvents.DismissAttachmentMenu)
            assertThat(awaitItem().showAttachmentSourcePicker).isFalse()
        }
    }

    @Test
    fun `present - Pick image from gallery`() = runTest {
        val room = aFakeMatrixRoom()
        val presenter = createPresenter(room = room)
        pickerProvider.givenMimeType(MimeTypes.Images)
        mediaPreProcessor.givenResult(
            Result.success(
                MediaUploadInfo.Image(
                    file = File("/some/path"),
                    info = ImageInfo(
                        width = null,
                        height = null,
                        mimetype = null,
                        size = null,
                        thumbnailInfo = null,
                        thumbnailSource = null,
                        blurhash = null,
                    ),
                    thumbnailInfo = ThumbnailProcessingInfo(
                        file = File("/some/path"),
                        info = ThumbnailInfo(
                            width = null,
                            height = null,
                            mimetype = null,
                            size = null,
                        ),
                        blurhash = "",
                    )
                )
            )
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(MessageComposerEvents.PickAttachmentSource.FromGallery)
            val previewingState = awaitItem()
            assertThat(previewingState.showAttachmentSourcePicker).isFalse()
            assertThat(previewingState.attachmentsState).isInstanceOf(AttachmentsState.Previewing::class.java)
        }
    }

    @Test
    fun `present - Pick video from gallery`() = runTest {
        val room = aFakeMatrixRoom()
        val presenter = createPresenter(room = room)
        pickerProvider.givenMimeType(MimeTypes.Videos)
        mediaPreProcessor.givenResult(
            Result.success(
                MediaUploadInfo.Video(
                    file = File("/some/path"),
                    info = VideoInfo(
                        width = null,
                        height = null,
                        mimetype = null,
                        duration = null,
                        size = null,
                        thumbnailInfo = null,
                        thumbnailSource = null,
                        blurhash = null,
                    ),
                    thumbnailInfo = ThumbnailProcessingInfo(
                        file = File("/some/path"),
                        info = ThumbnailInfo(
                            width = null,
                            height = null,
                            mimetype = null,
                            size = null,
                        ),
                        blurhash = "",
                    )
                )
            )
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(MessageComposerEvents.PickAttachmentSource.FromGallery)
            val previewingState = awaitItem()
            assertThat(previewingState.showAttachmentSourcePicker).isFalse()
            assertThat(previewingState.attachmentsState).isInstanceOf(AttachmentsState.Previewing::class.java)
        }
    }

    @Test
    fun `present - Pick media from gallery & cancel does nothing`() = runTest {
        val presenter = createPresenter()
        with(pickerProvider) {
            givenResult(null) // Simulate a user canceling the flow
            givenMimeType(MimeTypes.Images)
        }
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(MessageComposerEvents.PickAttachmentSource.FromGallery)
            // No crashes here, otherwise it fails
        }
    }

    @Test
    fun `present - Pick file from storage`() = runTest {
        val room = aFakeMatrixRoom()
        val presenter = createPresenter(room = room)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(MessageComposerEvents.PickAttachmentSource.FromFiles)
            val sendingState = awaitItem()
            assertThat(sendingState.showAttachmentSourcePicker).isFalse()
            assertThat(sendingState.attachmentsState).isInstanceOf(AttachmentsState.Sending::class.java)
            val sentState = awaitItem()
            assertThat(sentState.attachmentsState).isEqualTo(AttachmentsState.None)
            assertThat(room.sendMediaCount).isEqualTo(1)
        }
    }

    @Test
    fun `present - Take photo`() = runTest {
        val room = aFakeMatrixRoom()
        val presenter = createPresenter(room = room)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(MessageComposerEvents.PickAttachmentSource.PhotoFromCamera)
            val previewingState = awaitItem()
            assertThat(previewingState.showAttachmentSourcePicker).isFalse()
            assertThat(previewingState.attachmentsState).isInstanceOf(AttachmentsState.Previewing::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - Record video`() = runTest {
        val room = aFakeMatrixRoom()
        val presenter = createPresenter(room = room)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(MessageComposerEvents.PickAttachmentSource.VideoFromCamera)
            val previewingState = awaitItem()
            assertThat(previewingState.showAttachmentSourcePicker).isFalse()
            assertThat(previewingState.attachmentsState).isInstanceOf(AttachmentsState.Previewing::class.java)
        }
    }

    @Test
    fun `present - Uploading media failure can be recovered from`() = runTest {
        val room = aFakeMatrixRoom().apply {
            givenSendMediaResult(Result.failure(Exception()))
        }
        val presenter = createPresenter(room = room)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
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

    private suspend fun ReceiveTurbine<MessageComposerState>.backToNormalMode(state: MessageComposerState, skipCount: Int = 0) {
        state.eventSink.invoke(MessageComposerEvents.CloseSpecialMode)
        skipItems(skipCount)
        val normalState = awaitItem()
        assertThat(normalState.mode).isEqualTo(MessageComposerMode.Normal(""))
        assertThat(normalState.text).isEqualTo(StableCharSequence(""))
        assertThat(normalState.isSendButtonVisible).isFalse()
    }

    private fun TestScope.createPresenter(
        room: MatrixRoom = aFakeMatrixRoom(),
        pickerProvider: PickerProvider = this@MessageComposerPresenterTest.pickerProvider,
        featureFlagService: FeatureFlagService = this@MessageComposerPresenterTest.featureFlagService,
        mediaPreProcessor: MediaPreProcessor = this@MessageComposerPresenterTest.mediaPreProcessor,
        snackbarDispatcher: SnackbarDispatcher = this@MessageComposerPresenterTest.snackbarDispatcher,
    ) = MessageComposerPresenter(
        this,
        room,
        pickerProvider,
        featureFlagService,
        localMediaFactory,
        MediaSender(mediaPreProcessor, room),
        snackbarDispatcher
    )
}

fun anEditMode() = MessageComposerMode.Edit(AN_EVENT_ID, A_MESSAGE)
fun aReplyMode() = MessageComposerMode.Reply(A_USER_NAME, null, AN_EVENT_ID, A_MESSAGE)
fun aQuoteMode() = MessageComposerMode.Quote(AN_EVENT_ID, A_MESSAGE)

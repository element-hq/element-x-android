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

import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertThat
import io.element.android.features.messages.impl.textcomposer.AttachmentSourcePicker
import io.element.android.features.messages.impl.textcomposer.MessageComposerEvents
import io.element.android.features.messages.impl.textcomposer.MessageComposerPresenter
import io.element.android.features.messages.impl.textcomposer.MessageComposerState
import io.element.android.libraries.core.data.StableCharSequence
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.featureflag.test.FakeFeatureFlagService
import io.element.android.libraries.matrix.test.ANOTHER_MESSAGE
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_MESSAGE
import io.element.android.libraries.matrix.test.A_REPLY
import io.element.android.libraries.matrix.test.A_USER_NAME
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.libraries.mediapickers.PickerProvider
import io.element.android.libraries.textcomposer.MessageComposerMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Test

class MessageComposerPresenterTest {

    private val pickerProvider = PickerProvider(isInTest = true)
    private val featureFlagService = FakeFeatureFlagService().apply {
        runBlocking {
            setFeatureEnabled(FeatureFlags.ShowMediaUploadingFlow, true)
        }
    }

    @Test
    fun `present - initial state`() = runTest {
        val presenter = MessageComposerPresenter(
            this,
            FakeMatrixRoom(),
            pickerProvider,
            featureFlagService,
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.isFullScreen).isFalse()
            assertThat(initialState.text).isEqualTo(StableCharSequence(""))
            assertThat(initialState.mode).isEqualTo(MessageComposerMode.Normal(""))
            assertThat(initialState.isSendButtonVisible).isFalse()
        }
    }

    @Test
    fun `present - toggle fullscreen`() = runTest {
        val presenter = MessageComposerPresenter(
            this,
            FakeMatrixRoom(),
            pickerProvider,
            featureFlagService,
        )
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
        val presenter = MessageComposerPresenter(
            this,
            FakeMatrixRoom(),
            pickerProvider,
            featureFlagService,
        )
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
        val presenter = MessageComposerPresenter(
            this,
            FakeMatrixRoom(),
            pickerProvider,
            featureFlagService,
        )
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

    private suspend fun ReceiveTurbine<MessageComposerState>.backToNormalMode(state: MessageComposerState, skipCount: Int = 0) {
        state.eventSink.invoke(MessageComposerEvents.CloseSpecialMode)
        skipItems(skipCount)
        val normalState = awaitItem()
        assertThat(normalState.mode).isEqualTo(MessageComposerMode.Normal(""))
        assertThat(normalState.text).isEqualTo(StableCharSequence(""))
        assertThat(normalState.isSendButtonVisible).isFalse()
    }

    @Test
    fun `present - change mode to reply`() = runTest {
        val presenter = MessageComposerPresenter(
            this,
            FakeMatrixRoom(),
            pickerProvider,
            featureFlagService,
        )
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
        val presenter = MessageComposerPresenter(
            this,
            FakeMatrixRoom(),
            pickerProvider,
            featureFlagService,
        )
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
        val presenter = MessageComposerPresenter(
            this,
            FakeMatrixRoom(),
            pickerProvider,
            featureFlagService,
        )
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
        val fakeMatrixRoom = FakeMatrixRoom()
        val presenter = MessageComposerPresenter(
            this,
            fakeMatrixRoom,
            pickerProvider,
            featureFlagService,
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
        val fakeMatrixRoom = FakeMatrixRoom()
        val presenter = MessageComposerPresenter(
            this,
            fakeMatrixRoom,
            pickerProvider,
            featureFlagService,
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
        val fakeMatrixRoom = FakeMatrixRoom()
        val presenter = MessageComposerPresenter(
            this,
            fakeMatrixRoom,
            pickerProvider,
            featureFlagService,
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(MessageComposerEvents.AddAttachment)

            assertThat(awaitItem().attachmentSourcePicker).isEqualTo(AttachmentSourcePicker.AllMedia)
        }
    }

    @Test
    fun `present - Open camera attachments menu`() = runTest {
        val fakeMatrixRoom = FakeMatrixRoom()
        val presenter = MessageComposerPresenter(
            this,
            fakeMatrixRoom,
            pickerProvider,
            featureFlagService,
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(MessageComposerEvents.PickAttachmentSource.FromCamera)

            assertThat(awaitItem().attachmentSourcePicker).isEqualTo(AttachmentSourcePicker.Camera)
        }
    }

    @Test
    fun `present - Dismiss attachments menu`() = runTest {
        val fakeMatrixRoom = FakeMatrixRoom()
        val presenter = MessageComposerPresenter(
            this,
            fakeMatrixRoom,
            pickerProvider,
            featureFlagService,
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(MessageComposerEvents.AddAttachment)
            skipItems(1)

            initialState.eventSink(MessageComposerEvents.DismissAttachmentMenu)
            assertThat(awaitItem().attachmentSourcePicker).isNull()
        }
    }

    @Test
    fun `present - Pick media from gallery`() = runTest {
        val fakeMatrixRoom = FakeMatrixRoom()
        val presenter = MessageComposerPresenter(
            this,
            fakeMatrixRoom,
            pickerProvider,
            featureFlagService,
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(MessageComposerEvents.PickAttachmentSource.FromGallery)

            // TODO verify some post processing of the selected media is done
        }
    }

    @Test
    fun `present - Pick file from storage`() = runTest {
        val fakeMatrixRoom = FakeMatrixRoom()
        val presenter = MessageComposerPresenter(
            this,
            fakeMatrixRoom,
            pickerProvider,
            featureFlagService,
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(MessageComposerEvents.PickAttachmentSource.FromFiles)

            // TODO verify some post processing of the selected media is done
        }
    }

    @Test
    fun `present - Take photo`() = runTest {
        val fakeMatrixRoom = FakeMatrixRoom()
        val presenter = MessageComposerPresenter(
            this,
            fakeMatrixRoom,
            pickerProvider,
            featureFlagService,
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(MessageComposerEvents.PickCameraAttachmentSource.Photo)

            // TODO verify some post processing of the captured image is done
        }
    }

    @Test
    fun `present - Record video`() = runTest {
        val fakeMatrixRoom = FakeMatrixRoom()
        val presenter = MessageComposerPresenter(
            this,
            fakeMatrixRoom,
            pickerProvider,
            featureFlagService,
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(MessageComposerEvents.PickCameraAttachmentSource.Video)

            // TODO verify some post processing of the captured video is done
        }
    }
}

fun anEditMode() = MessageComposerMode.Edit(AN_EVENT_ID, A_MESSAGE)
fun aReplyMode() = MessageComposerMode.Reply(A_USER_NAME, AN_EVENT_ID, A_MESSAGE)
fun aQuoteMode() = MessageComposerMode.Quote(AN_EVENT_ID, A_MESSAGE)

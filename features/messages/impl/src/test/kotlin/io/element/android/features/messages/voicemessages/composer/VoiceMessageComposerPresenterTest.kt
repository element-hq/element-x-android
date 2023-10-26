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

package io.element.android.features.messages.voicemessages.composer

import android.Manifest
import androidx.lifecycle.Lifecycle
import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.TurbineTestContext
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.messages.impl.voicemessages.composer.VoiceMessageComposerEvents
import io.element.android.features.messages.impl.voicemessages.composer.VoiceMessageComposerPresenter
import io.element.android.features.messages.impl.voicemessages.composer.VoiceMessageComposerState
import io.element.android.features.messages.impl.voicemessages.VoiceMessageException
import io.element.android.features.messages.impl.voicemessages.composer.VoiceMessageComposerPlayer
import io.element.android.features.messages.mediaplayer.FakeMediaPlayer
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.libraries.mediaupload.api.MediaSender
import io.element.android.libraries.mediaupload.test.FakeMediaPreProcessor
import io.element.android.libraries.permissions.api.PermissionsPresenter
import io.element.android.libraries.permissions.api.aPermissionsState
import io.element.android.libraries.permissions.test.FakePermissionsPresenter
import io.element.android.libraries.permissions.test.FakePermissionsPresenterFactory
import io.element.android.libraries.textcomposer.model.PressEvent
import io.element.android.libraries.textcomposer.model.VoiceMessagePlayerEvent
import io.element.android.libraries.textcomposer.model.VoiceMessageState
import io.element.android.libraries.voicerecorder.test.FakeVoiceRecorder
import io.element.android.services.analytics.test.FakeAnalyticsService
import io.element.android.tests.testutils.WarmUpRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

class VoiceMessageComposerPresenterTest {

    @get:Rule
    val warmUpRule = WarmUpRule()

    private val voiceRecorder = FakeVoiceRecorder(
        recordingDuration = RECORDING_DURATION
    )
    private val analyticsService = FakeAnalyticsService()
    private val matrixRoom = FakeMatrixRoom()
    private val mediaPreProcessor = FakeMediaPreProcessor().apply { givenAudioResult() }
    private val mediaSender = MediaSender(mediaPreProcessor, matrixRoom)

    companion object {
        private val RECORDING_DURATION = 1.seconds
        private val RECORDING_STATE = VoiceMessageState.Recording(RECORDING_DURATION, 0.2f)
    }

    @Test
    fun `present - initial state`() = runTest {
        val presenter = createVoiceMessageComposerPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.voiceMessageState).isEqualTo(VoiceMessageState.Idle)
            voiceRecorder.assertCalls(started = 0)

            testPauseAndDestroy(initialState)
        }
    }

    @Test
    fun `present - recording state`() = runTest {
        val presenter = createVoiceMessageComposerPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem().eventSink(VoiceMessageComposerEvents.RecordButtonEvent(PressEvent.PressStart))

            val finalState = awaitItem()
            assertThat(finalState.voiceMessageState).isEqualTo(RECORDING_STATE)
            voiceRecorder.assertCalls(started = 1)

            testPauseAndDestroy(finalState)
        }
    }

    @Test
    fun `present - abort recording`() = runTest {
        val presenter = createVoiceMessageComposerPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem().eventSink(VoiceMessageComposerEvents.RecordButtonEvent(PressEvent.PressStart))
            awaitItem().eventSink(VoiceMessageComposerEvents.RecordButtonEvent(PressEvent.Tapped))

            val finalState = awaitItem()
            assertThat(finalState.voiceMessageState).isEqualTo(VoiceMessageState.Idle)
            voiceRecorder.assertCalls(started = 1, stopped = 1, deleted = 1)

            testPauseAndDestroy(finalState)
        }
    }

    @Test
    fun `present - finish recording`() = runTest {
        val presenter = createVoiceMessageComposerPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem().eventSink(VoiceMessageComposerEvents.RecordButtonEvent(PressEvent.PressStart))
            awaitItem().eventSink(VoiceMessageComposerEvents.RecordButtonEvent(PressEvent.LongPressEnd))

            val finalState = awaitItem()
            assertThat(finalState.voiceMessageState).isEqualTo(aPreviewState())
            voiceRecorder.assertCalls(started = 1, stopped = 1, deleted = 0)

            testPauseAndDestroy(finalState)
        }
    }

    @Test
    fun `present - play recording before it is ready`() = runTest {
        val presenter = createVoiceMessageComposerPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem().eventSink(VoiceMessageComposerEvents.RecordButtonEvent(PressEvent.PressStart))
            val finalState = awaitItem().apply {
                this.eventSink(VoiceMessageComposerEvents.PlayerEvent(VoiceMessagePlayerEvent.Play))
            }

            // Nothing should happen
            assertThat(finalState.voiceMessageState).isEqualTo(VoiceMessageState.Recording(RECORDING_DURATION, 0.2f))
            voiceRecorder.assertCalls(started = 1, stopped = 0, deleted = 0)

            testPauseAndDestroy(finalState)
        }
    }

    @Test
    fun `present - play recording`() = runTest {
        val presenter = createVoiceMessageComposerPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem().eventSink(VoiceMessageComposerEvents.RecordButtonEvent(PressEvent.PressStart))
            awaitItem().eventSink(VoiceMessageComposerEvents.RecordButtonEvent(PressEvent.LongPressEnd))
            awaitItem().eventSink(VoiceMessageComposerEvents.PlayerEvent(VoiceMessagePlayerEvent.Play))
            val finalState = awaitItem().also {
                assertThat(it.voiceMessageState).isEqualTo(aPreviewState(isPlaying = true))
            }
            voiceRecorder.assertCalls(started = 1, stopped = 1, deleted = 0)

            testPauseAndDestroy(finalState)
        }
    }

    @Test
    fun `present - pause recording`() = runTest {
        val presenter = createVoiceMessageComposerPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem().eventSink(VoiceMessageComposerEvents.RecordButtonEvent(PressEvent.PressStart))
            awaitItem().eventSink(VoiceMessageComposerEvents.RecordButtonEvent(PressEvent.LongPressEnd))
            awaitItem().eventSink(VoiceMessageComposerEvents.PlayerEvent(VoiceMessagePlayerEvent.Play))
            awaitItem().eventSink(VoiceMessageComposerEvents.PlayerEvent(VoiceMessagePlayerEvent.Pause))
            val finalState = awaitItem().also {
                assertThat(it.voiceMessageState).isEqualTo(aPreviewState(isPlaying = false))
            }
            voiceRecorder.assertCalls(started = 1, stopped = 1, deleted = 0)

            testPauseAndDestroy(finalState)
        }
    }

    @Test
    fun `present - delete recording`() = runTest {
        val presenter = createVoiceMessageComposerPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem().eventSink(VoiceMessageComposerEvents.RecordButtonEvent(PressEvent.PressStart))
            awaitItem().eventSink(VoiceMessageComposerEvents.RecordButtonEvent(PressEvent.LongPressEnd))
            awaitItem().eventSink(VoiceMessageComposerEvents.DeleteVoiceMessage)

            val finalState = awaitItem()
            assertThat(finalState.voiceMessageState).isEqualTo(VoiceMessageState.Idle)
            voiceRecorder.assertCalls(started = 1, stopped = 1, deleted = 1)

            testPauseAndDestroy(finalState)
        }
    }

    @Test
    fun `present - send recording`() = runTest {
        val presenter = createVoiceMessageComposerPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem().eventSink(VoiceMessageComposerEvents.RecordButtonEvent(PressEvent.PressStart))
            awaitItem().eventSink(VoiceMessageComposerEvents.RecordButtonEvent(PressEvent.LongPressEnd))
            awaitItem().eventSink(VoiceMessageComposerEvents.SendVoiceMessage)
            assertThat(awaitItem().voiceMessageState).isEqualTo(VoiceMessageState.Sending)

            val finalState = awaitItem()
            assertThat(finalState.voiceMessageState).isEqualTo(VoiceMessageState.Idle)
            assertThat(matrixRoom.sendMediaCount).isEqualTo(1)
            voiceRecorder.assertCalls(started = 1, stopped = 1, deleted = 1)

            testPauseAndDestroy(finalState)
        }
    }

    @Test
    fun `present - send recording before previous completed, waits`() = runTest {
        val presenter = createVoiceMessageComposerPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem().eventSink(VoiceMessageComposerEvents.RecordButtonEvent(PressEvent.PressStart))
            awaitItem().eventSink(VoiceMessageComposerEvents.RecordButtonEvent(PressEvent.LongPressEnd))
            awaitItem().run {
                eventSink(VoiceMessageComposerEvents.SendVoiceMessage)
                eventSink(VoiceMessageComposerEvents.SendVoiceMessage)
            }
            assertThat(awaitItem().voiceMessageState).isEqualTo(VoiceMessageState.Sending)

            val finalState = awaitItem()
            assertThat(finalState.voiceMessageState).isEqualTo(VoiceMessageState.Idle)
            assertThat(matrixRoom.sendMediaCount).isEqualTo(1)
            voiceRecorder.assertCalls(started = 1, stopped = 1, deleted = 1)

            testPauseAndDestroy(finalState)
        }
    }

    @Test
    fun `present - send failures aren't tracked`() = runTest {
        // Let sending fail due to media preprocessing error
        mediaPreProcessor.givenResult(Result.failure(Exception()))
        val presenter = createVoiceMessageComposerPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem().eventSink(VoiceMessageComposerEvents.RecordButtonEvent(PressEvent.PressStart))
            awaitItem().eventSink(VoiceMessageComposerEvents.RecordButtonEvent(PressEvent.LongPressEnd))
            awaitItem().apply {
                assertThat(voiceMessageState).isEqualTo(aPreviewState())
                eventSink(VoiceMessageComposerEvents.SendVoiceMessage)
            }

            val finalState = awaitItem()
            assertThat(finalState.voiceMessageState).isEqualTo(VoiceMessageState.Sending)
            assertThat(matrixRoom.sendMediaCount).isEqualTo(0)
            assertThat(analyticsService.trackedErrors).hasSize(0)
            voiceRecorder.assertCalls(started = 1, stopped = 1, deleted = 0)

            testPauseAndDestroy(finalState)
        }
    }

    @Test
    fun `present - send failures can be retried`() = runTest {
        // Let sending fail due to media preprocessing error
        val presenter = createVoiceMessageComposerPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            mediaPreProcessor.givenResult(Result.failure(Exception()))
            awaitItem().eventSink(VoiceMessageComposerEvents.RecordButtonEvent(PressEvent.PressStart))
            awaitItem().eventSink(VoiceMessageComposerEvents.RecordButtonEvent(PressEvent.LongPressEnd))
            val previewState = awaitItem()

            previewState.eventSink(VoiceMessageComposerEvents.SendVoiceMessage)
            assertThat(awaitItem().voiceMessageState).isEqualTo(VoiceMessageState.Sending)

            ensureAllEventsConsumed()
            assertThat(previewState.voiceMessageState).isEqualTo(aPreviewState())
            assertThat(matrixRoom.sendMediaCount).isEqualTo(0)

            mediaPreProcessor.givenAudioResult()
            previewState.eventSink(VoiceMessageComposerEvents.SendVoiceMessage)
            val finalState = awaitItem()
            assertThat(finalState.voiceMessageState).isEqualTo(VoiceMessageState.Idle)
            assertThat(matrixRoom.sendMediaCount).isEqualTo(1)
            voiceRecorder.assertCalls(started = 1, stopped = 1, deleted = 1)

            testPauseAndDestroy(finalState)
        }
    }

    @Test
    fun `present - send error - missing recording is tracked`() = runTest {
        val presenter = createVoiceMessageComposerPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            // Send the message before recording anything
            initialState.eventSink(VoiceMessageComposerEvents.SendVoiceMessage)

            assertThat(initialState.voiceMessageState).isEqualTo(VoiceMessageState.Idle)
            assertThat(matrixRoom.sendMediaCount).isEqualTo(0)
            assertThat(analyticsService.trackedErrors).hasSize(1)
            voiceRecorder.assertCalls(started = 0)

            testPauseAndDestroy(initialState)
        }
    }

    @Test
    fun `present - record error - security exceptions are tracked`() = runTest {
        val exception = SecurityException("")
        voiceRecorder.givenThrowsSecurityException(exception)
        val presenter = createVoiceMessageComposerPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(VoiceMessageComposerEvents.RecordButtonEvent(PressEvent.PressStart))

            assertThat(matrixRoom.sendMediaCount).isEqualTo(0)
            assertThat(analyticsService.trackedErrors).containsExactly(
                VoiceMessageException.PermissionMissing(message = "Expected permission to record but none", cause = exception)
            )
            voiceRecorder.assertCalls(started = 1)

            testPauseAndDestroy(initialState)
        }
    }

    @Test
    fun `present - permission accepted first time`() = runTest {
        val permissionsPresenter = createFakePermissionsPresenter(
            recordPermissionGranted = false,
        )
        val presenter = createVoiceMessageComposerPresenter(
            permissionsPresenter = permissionsPresenter,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(VoiceMessageComposerEvents.RecordButtonEvent(PressEvent.PressStart))
            assertThat(awaitItem().voiceMessageState).isEqualTo(VoiceMessageState.Idle)

            initialState.eventSink(VoiceMessageComposerEvents.RecordButtonEvent(PressEvent.LongPressEnd))
            voiceRecorder.assertCalls(stopped = 1)

            permissionsPresenter.setPermissionGranted()

            awaitItem().eventSink(VoiceMessageComposerEvents.RecordButtonEvent(PressEvent.PressStart))
            val finalState = awaitItem()
            assertThat(finalState.voiceMessageState).isEqualTo(RECORDING_STATE)
            voiceRecorder.assertCalls(stopped = 1, started = 1)

            testPauseAndDestroy(finalState)
        }
    }

    @Test
    fun `present - permission denied previously`() = runTest {
        val permissionsPresenter = createFakePermissionsPresenter(
            recordPermissionGranted = false,
        )
        val presenter = createVoiceMessageComposerPresenter(
            permissionsPresenter = permissionsPresenter,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem().eventSink(VoiceMessageComposerEvents.RecordButtonEvent(PressEvent.PressStart))

            // See the dialog and accept it
            awaitItem().also {
                assertThat(it.voiceMessageState).isEqualTo(VoiceMessageState.Idle)
                assertThat(it.showPermissionRationaleDialog).isTrue()
                it.eventSink(VoiceMessageComposerEvents.AcceptPermissionRationale)
            }

            // Dialog is hidden, user accepts permissions
            assertThat(awaitItem().showPermissionRationaleDialog).isFalse()

            permissionsPresenter.setPermissionGranted()

            awaitItem().eventSink(VoiceMessageComposerEvents.RecordButtonEvent(PressEvent.PressStart))
            val finalState = awaitItem()
            assertThat(finalState.voiceMessageState).isEqualTo(RECORDING_STATE)
            voiceRecorder.assertCalls(started = 1)

            testPauseAndDestroy(finalState)
        }
    }

    @Test
    fun `present - permission rationale dismissed`() = runTest {
        val permissionsPresenter = createFakePermissionsPresenter(
            recordPermissionGranted = false,
        )
        val presenter = createVoiceMessageComposerPresenter(
            permissionsPresenter = permissionsPresenter,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem().eventSink(VoiceMessageComposerEvents.RecordButtonEvent(PressEvent.PressStart))

            // See the dialog and accept it
            awaitItem().also {
                assertThat(it.voiceMessageState).isEqualTo(VoiceMessageState.Idle)
                assertThat(it.showPermissionRationaleDialog).isTrue()
                it.eventSink(VoiceMessageComposerEvents.DismissPermissionsRationale)
            }

            // Dialog is hidden, user tries to record again
            awaitItem().also {
                assertThat(it.showPermissionRationaleDialog).isFalse()
                it.eventSink(VoiceMessageComposerEvents.RecordButtonEvent(PressEvent.PressStart))
            }

            // Dialog is shown once again
            val finalState = awaitItem().also {
                assertThat(it.voiceMessageState).isEqualTo(VoiceMessageState.Idle)
                assertThat(it.showPermissionRationaleDialog).isTrue()
            }
            voiceRecorder.assertCalls(started = 0)

            testPauseAndDestroy(finalState)
        }
    }

    private suspend fun TurbineTestContext<VoiceMessageComposerState>.testPauseAndDestroy(
        mostRecentState: VoiceMessageComposerState,
    ) {
        mostRecentState.eventSink(
            VoiceMessageComposerEvents.LifecycleEvent(event = Lifecycle.Event.ON_PAUSE)
        )

        val onPauseState = when (val vmState = mostRecentState.voiceMessageState) {
            VoiceMessageState.Idle,
            VoiceMessageState.Sending -> {
                mostRecentState
            }
            is VoiceMessageState.Recording -> {
                // If recorder was active, it stops
                awaitItem().apply {
                    assertThat(voiceMessageState).isEqualTo(aPreviewState())
                }
            }
            is VoiceMessageState.Preview -> when(vmState.isPlaying) {
                // If the preview was playing, it pauses
                true -> awaitItem().apply {
                    assertThat(voiceMessageState).isEqualTo(aPreviewState())
                }
                false -> mostRecentState
            }
        }

        onPauseState.eventSink(
            VoiceMessageComposerEvents.LifecycleEvent(event = Lifecycle.Event.ON_DESTROY)
        )

        when (onPauseState.voiceMessageState) {
            VoiceMessageState.Idle,
            VoiceMessageState.Sending ->
                ensureAllEventsConsumed()
            is VoiceMessageState.Recording,
            is VoiceMessageState.Preview ->
                assertThat(awaitItem().voiceMessageState).isEqualTo(VoiceMessageState.Idle)
        }
    }

    private fun TestScope.createVoiceMessageComposerPresenter(
        permissionsPresenter: PermissionsPresenter = createFakePermissionsPresenter(),
    ): VoiceMessageComposerPresenter {
        return VoiceMessageComposerPresenter(
            this,
            voiceRecorder,
            analyticsService,
            mediaSender,
            player = VoiceMessageComposerPlayer(FakeMediaPlayer()),
            FakePermissionsPresenterFactory(permissionsPresenter),
        )
    }

    private fun createFakePermissionsPresenter(
        recordPermissionGranted: Boolean = true,
        recordPermissionShowDialog: Boolean = false,
    ): FakePermissionsPresenter {
        val initialPermissionState = aPermissionsState(
            showDialog = recordPermissionShowDialog,
            permission = Manifest.permission.RECORD_AUDIO,
            permissionGranted = recordPermissionGranted,
        )
        return FakePermissionsPresenter(
            initialState = initialPermissionState
        )
    }

    private fun aPreviewState(
        isPlaying: Boolean = false
    ) = VoiceMessageState.Preview(
        isPlaying = isPlaying
    )

}

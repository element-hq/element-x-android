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

package io.element.android.features.messages.voicemessages

import android.Manifest
import androidx.lifecycle.Lifecycle
import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.TurbineTestContext
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.messages.impl.voicemessages.VoiceMessageComposerEvents
import io.element.android.features.messages.impl.voicemessages.VoiceMessageComposerPresenter
import io.element.android.features.messages.impl.voicemessages.VoiceMessageComposerState
import io.element.android.features.messages.impl.voicemessages.VoiceMessageException
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.libraries.mediaupload.api.MediaSender
import io.element.android.libraries.mediaupload.test.FakeMediaPreProcessor
import io.element.android.libraries.permissions.api.PermissionsPresenter
import io.element.android.libraries.permissions.api.aPermissionsState
import io.element.android.libraries.permissions.test.FakePermissionsPresenter
import io.element.android.libraries.permissions.test.FakePermissionsPresenterFactory
import io.element.android.libraries.textcomposer.model.PressEvent
import io.element.android.libraries.textcomposer.model.VoiceMessageState
import io.element.android.libraries.voicerecorder.test.FakeVoiceRecorder
import io.element.android.services.analytics.test.FakeAnalyticsService
import io.element.android.tests.testutils.WarmUpRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class VoiceMessageComposerPresenterTest {

    @get:Rule
    val warmUpRule = WarmUpRule()

    private val voiceRecorder = FakeVoiceRecorder()
    private val analyticsService = FakeAnalyticsService()
    private val matrixRoom = FakeMatrixRoom()
    private val mediaPreProcessor = FakeMediaPreProcessor().apply { givenAudioResult() }
    private val mediaSender = MediaSender(mediaPreProcessor, matrixRoom)

    @Test
    fun `present - initial state`() = runTest {
        val presenter = createVoiceMessageComposerPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.voiceMessageState).isEqualTo(VoiceMessageState.Idle)

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
            assertThat(finalState.voiceMessageState).isEqualTo(VoiceMessageState.Recording(0.2))

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
            assertThat(finalState.voiceMessageState).isEqualTo(VoiceMessageState.Preview)

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

            val finalState = awaitItem()
            assertThat(finalState.voiceMessageState).isEqualTo(VoiceMessageState.Idle)
            assertThat(matrixRoom.sendMediaCount).isEqualTo(1)

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

            val finalState = awaitItem()
            assertThat(finalState.voiceMessageState).isEqualTo(VoiceMessageState.Idle)
            assertThat(matrixRoom.sendMediaCount).isEqualTo(1)

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

            val finalState = awaitItem().apply {
                assertThat(voiceMessageState).isEqualTo(VoiceMessageState.Preview)
                eventSink(VoiceMessageComposerEvents.SendVoiceMessage)
            }
            assertThat(matrixRoom.sendMediaCount).isEqualTo(0)
            assertThat(analyticsService.trackedErrors).hasSize(0)

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

            ensureAllEventsConsumed()
            assertThat(previewState.voiceMessageState).isEqualTo(VoiceMessageState.Preview)
            assertThat(matrixRoom.sendMediaCount).isEqualTo(0)

            mediaPreProcessor.givenAudioResult()
            previewState.eventSink(VoiceMessageComposerEvents.SendVoiceMessage)
            val finalState = awaitItem()
            assertThat(finalState.voiceMessageState).isEqualTo(VoiceMessageState.Idle)
            assertThat(matrixRoom.sendMediaCount).isEqualTo(1)

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
            permissionsPresenter.setPermissionGranted()

            awaitItem().eventSink(VoiceMessageComposerEvents.RecordButtonEvent(PressEvent.PressStart))
            val finalState = awaitItem()
            assertThat(finalState.voiceMessageState).isEqualTo(VoiceMessageState.Recording(0.2))

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
            assertThat(finalState.voiceMessageState).isEqualTo(VoiceMessageState.Recording(0.2))

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

            testPauseAndDestroy(finalState)
        }
    }

    private suspend fun TurbineTestContext<VoiceMessageComposerState>.testPauseAndDestroy(
        mostRecentState: VoiceMessageComposerState,
    ) {
        mostRecentState.eventSink(
            VoiceMessageComposerEvents.LifecycleEvent(event = Lifecycle.Event.ON_PAUSE)
        )

        val onPauseState = when (mostRecentState.voiceMessageState) {
            VoiceMessageState.Idle,
            VoiceMessageState.Preview -> {
                mostRecentState
            }
            is VoiceMessageState.Recording -> {
                awaitItem().also {
                    assertThat(it.voiceMessageState).isEqualTo(VoiceMessageState.Preview)
                }
            }
        }

        onPauseState.eventSink(
            VoiceMessageComposerEvents.LifecycleEvent(event = Lifecycle.Event.ON_DESTROY)
        )

        when (onPauseState.voiceMessageState) {
            VoiceMessageState.Idle ->
                ensureAllEventsConsumed()
            is VoiceMessageState.Recording,
            VoiceMessageState.Preview ->
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
            matrixRoom,
            mediaSender,
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
}

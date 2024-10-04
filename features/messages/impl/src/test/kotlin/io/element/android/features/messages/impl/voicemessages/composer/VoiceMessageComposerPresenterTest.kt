/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.features.messages.impl.voicemessages.composer

import android.Manifest
import androidx.lifecycle.Lifecycle
import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.TurbineTestContext
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import im.vector.app.features.analytics.plan.Composer
import io.element.android.features.messages.impl.messagecomposer.aReplyMode
import io.element.android.features.messages.impl.voicemessages.VoiceMessageException
import io.element.android.features.messages.test.FakeMessageComposerContext
import io.element.android.libraries.matrix.api.core.ProgressCallback
import io.element.android.libraries.matrix.test.media.FakeMediaUploadHandler
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.libraries.mediaplayer.test.FakeMediaPlayer
import io.element.android.libraries.mediaupload.api.MediaSender
import io.element.android.libraries.mediaupload.test.FakeMediaPreProcessor
import io.element.android.libraries.permissions.api.PermissionsPresenter
import io.element.android.libraries.permissions.api.aPermissionsState
import io.element.android.libraries.permissions.test.FakePermissionsPresenter
import io.element.android.libraries.permissions.test.FakePermissionsPresenterFactory
import io.element.android.libraries.textcomposer.model.MessageComposerMode
import io.element.android.libraries.textcomposer.model.VoiceMessagePlayerEvent
import io.element.android.libraries.textcomposer.model.VoiceMessageRecorderEvent
import io.element.android.libraries.textcomposer.model.VoiceMessageState
import io.element.android.libraries.voicerecorder.test.FakeVoiceRecorder
import io.element.android.services.analytics.test.FakeAnalyticsService
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.lambda.lambdaRecorder
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class VoiceMessageComposerPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    private val voiceRecorder = FakeVoiceRecorder(
        recordingDuration = RECORDING_DURATION
    )
    private val analyticsService = FakeAnalyticsService()
    private val sendMediaResult = lambdaRecorder<ProgressCallback?, Result<FakeMediaUploadHandler>> { Result.success(FakeMediaUploadHandler()) }
    private val matrixRoom = FakeMatrixRoom(
        sendMediaResult = sendMediaResult
    )
    private val mediaPreProcessor = FakeMediaPreProcessor().apply { givenAudioResult() }
    private val mediaSender = MediaSender(mediaPreProcessor, matrixRoom)
    private val messageComposerContext = FakeMessageComposerContext()

    companion object {
        private val RECORDING_DURATION = 1.seconds
        private val RECORDING_STATE = VoiceMessageState.Recording(RECORDING_DURATION, listOf(0.1f, 0.2f).toPersistentList())
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
            awaitItem().eventSink(VoiceMessageComposerEvents.RecorderEvent(VoiceMessageRecorderEvent.Start))

            val finalState = awaitItem()
            assertThat(finalState.voiceMessageState).isEqualTo(RECORDING_STATE)
            voiceRecorder.assertCalls(started = 1)

            testPauseAndDestroy(finalState)
        }
    }

    @Test
    fun `present - recording keeps screen on`() = runTest {
        val presenter = createVoiceMessageComposerPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem().apply {
                eventSink(VoiceMessageComposerEvents.RecorderEvent(VoiceMessageRecorderEvent.Start))
                assertThat(keepScreenOn).isFalse()
            }

            awaitItem().apply {
                assertThat(keepScreenOn).isTrue()
                eventSink(VoiceMessageComposerEvents.RecorderEvent(VoiceMessageRecorderEvent.Stop))
            }

            val finalState = awaitItem().apply {
                assertThat(keepScreenOn).isFalse()
            }

            testPauseAndDestroy(finalState)
        }
    }

    @Test
    fun `present - abort recording`() = runTest {
        val presenter = createVoiceMessageComposerPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem().eventSink(VoiceMessageComposerEvents.RecorderEvent(VoiceMessageRecorderEvent.Start))
            awaitItem().eventSink(VoiceMessageComposerEvents.RecorderEvent(VoiceMessageRecorderEvent.Cancel))
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
            awaitItem().eventSink(VoiceMessageComposerEvents.RecorderEvent(VoiceMessageRecorderEvent.Start))
            awaitItem().eventSink(VoiceMessageComposerEvents.RecorderEvent(VoiceMessageRecorderEvent.Stop))

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
            awaitItem().eventSink(VoiceMessageComposerEvents.RecorderEvent(VoiceMessageRecorderEvent.Start))
            val finalState = awaitItem().apply {
                this.eventSink(VoiceMessageComposerEvents.PlayerEvent(VoiceMessagePlayerEvent.Play))
            }

            // Nothing should happen
            assertThat(finalState.voiceMessageState).isEqualTo(RECORDING_STATE)
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
            awaitItem().eventSink(VoiceMessageComposerEvents.RecorderEvent(VoiceMessageRecorderEvent.Start))
            awaitItem().eventSink(VoiceMessageComposerEvents.RecorderEvent(VoiceMessageRecorderEvent.Stop))
            awaitItem().eventSink(VoiceMessageComposerEvents.PlayerEvent(VoiceMessagePlayerEvent.Play))
            val finalState = awaitItem().also {
                assertThat(it.voiceMessageState).isEqualTo(aPlayingState())
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
            awaitItem().eventSink(VoiceMessageComposerEvents.RecorderEvent(VoiceMessageRecorderEvent.Start))
            awaitItem().eventSink(VoiceMessageComposerEvents.RecorderEvent(VoiceMessageRecorderEvent.Stop))
            awaitItem().eventSink(VoiceMessageComposerEvents.PlayerEvent(VoiceMessagePlayerEvent.Play))
            awaitItem().eventSink(VoiceMessageComposerEvents.PlayerEvent(VoiceMessagePlayerEvent.Pause))
            val finalState = awaitItem().also {
                assertThat(it.voiceMessageState).isEqualTo(aPausedState())
            }
            voiceRecorder.assertCalls(started = 1, stopped = 1, deleted = 0)

            testPauseAndDestroy(finalState)
        }
    }

    @Test
    fun `present - seek recording`() = runTest {
        val presenter = createVoiceMessageComposerPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem().eventSink(VoiceMessageComposerEvents.RecorderEvent(VoiceMessageRecorderEvent.Start))
            awaitItem().eventSink(VoiceMessageComposerEvents.RecorderEvent(VoiceMessageRecorderEvent.Stop))
            awaitItem().eventSink(VoiceMessageComposerEvents.PlayerEvent(VoiceMessagePlayerEvent.Seek(0.5f)))
            awaitItem().apply {
                assertThat(voiceMessageState).isEqualTo(aPreviewState(playbackProgress = 0.5f, time = 0.seconds, showCursor = true))
            }
            awaitItem().apply {
                assertThat(voiceMessageState).isEqualTo(aPreviewState(playbackProgress = 0.5f, time = 5.seconds, showCursor = true))
                eventSink(VoiceMessageComposerEvents.PlayerEvent(VoiceMessagePlayerEvent.Seek(0.2f)))
            }
            awaitItem().apply {
                assertThat(voiceMessageState).isEqualTo(aPreviewState(playbackProgress = 0.2f, time = 5.seconds, showCursor = true))
            }
            val finalState = awaitItem().apply {
                assertThat(voiceMessageState).isEqualTo(aPreviewState(playbackProgress = 0.2f, time = 2.seconds, showCursor = true))
            }

            testPauseAndDestroy(finalState)
        }
    }

    @Test
    fun `present - delete recording`() = runTest {
        val presenter = createVoiceMessageComposerPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem().eventSink(VoiceMessageComposerEvents.RecorderEvent(VoiceMessageRecorderEvent.Start))
            awaitItem().eventSink(VoiceMessageComposerEvents.RecorderEvent(VoiceMessageRecorderEvent.Stop))
            awaitItem().eventSink(VoiceMessageComposerEvents.DeleteVoiceMessage)

            val finalState = awaitItem()
            assertThat(finalState.voiceMessageState).isEqualTo(VoiceMessageState.Idle)
            voiceRecorder.assertCalls(started = 1, stopped = 1, deleted = 1)

            testPauseAndDestroy(finalState)
        }
    }

    @Test
    fun `present - delete while playing`() = runTest {
        val presenter = createVoiceMessageComposerPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem().eventSink(VoiceMessageComposerEvents.RecorderEvent(VoiceMessageRecorderEvent.Start))
            awaitItem().eventSink(VoiceMessageComposerEvents.RecorderEvent(VoiceMessageRecorderEvent.Stop))
            awaitItem().eventSink(VoiceMessageComposerEvents.PlayerEvent(VoiceMessagePlayerEvent.Play))
            awaitItem().eventSink(VoiceMessageComposerEvents.DeleteVoiceMessage)
            awaitItem().apply {
                assertThat(voiceMessageState).isEqualTo(aPausedState())
            }

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
            awaitItem().eventSink(VoiceMessageComposerEvents.RecorderEvent(VoiceMessageRecorderEvent.Start))
            awaitItem().eventSink(VoiceMessageComposerEvents.RecorderEvent(VoiceMessageRecorderEvent.Stop))
            awaitItem().eventSink(VoiceMessageComposerEvents.SendVoiceMessage)
            assertThat(awaitItem().voiceMessageState).isEqualTo(aPreviewState().toSendingState())

            val finalState = awaitItem()
            assertThat(finalState.voiceMessageState).isEqualTo(VoiceMessageState.Idle)
            sendMediaResult.assertions().isCalledOnce()
            voiceRecorder.assertCalls(started = 1, stopped = 1, deleted = 1)

            testPauseAndDestroy(finalState)
        }
    }

    @Test
    fun `present - sending is tracked`() = runTest {
        val presenter = createVoiceMessageComposerPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            // Send a normal voice message
            messageComposerContext.composerMode = MessageComposerMode.Normal
            awaitItem().eventSink(VoiceMessageComposerEvents.RecorderEvent(VoiceMessageRecorderEvent.Start))
            awaitItem().eventSink(VoiceMessageComposerEvents.RecorderEvent(VoiceMessageRecorderEvent.Stop))
            awaitItem().eventSink(VoiceMessageComposerEvents.SendVoiceMessage)
            skipItems(1) // Sending state

            // Now reply with a voice message
            messageComposerContext.composerMode = aReplyMode()
            awaitItem().eventSink(VoiceMessageComposerEvents.RecorderEvent(VoiceMessageRecorderEvent.Start))
            awaitItem().eventSink(VoiceMessageComposerEvents.RecorderEvent(VoiceMessageRecorderEvent.Stop))
            awaitItem().eventSink(VoiceMessageComposerEvents.SendVoiceMessage)
            val finalState = awaitItem() // Sending state

            assertThat(analyticsService.capturedEvents).containsExactly(
                aVoiceMessageComposerEvent(isReply = false),
                aVoiceMessageComposerEvent(isReply = true)
            )

            testPauseAndDestroy(finalState)
        }
    }

    @Test
    fun `present - send while playing`() = runTest {
        val presenter = createVoiceMessageComposerPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem().eventSink(VoiceMessageComposerEvents.RecorderEvent(VoiceMessageRecorderEvent.Start))
            awaitItem().eventSink(VoiceMessageComposerEvents.RecorderEvent(VoiceMessageRecorderEvent.Stop))
            awaitItem().eventSink(VoiceMessageComposerEvents.PlayerEvent(VoiceMessagePlayerEvent.Play))
            awaitItem().eventSink(VoiceMessageComposerEvents.SendVoiceMessage)
            assertThat(awaitItem().voiceMessageState).isEqualTo(aPlayingState().toSendingState())
            skipItems(1) // Duplicate sending state

            val finalState = awaitItem()
            assertThat(finalState.voiceMessageState).isEqualTo(VoiceMessageState.Idle)
            sendMediaResult.assertions().isCalledOnce()
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
            awaitItem().eventSink(VoiceMessageComposerEvents.RecorderEvent(VoiceMessageRecorderEvent.Start))
            awaitItem().eventSink(VoiceMessageComposerEvents.RecorderEvent(VoiceMessageRecorderEvent.Stop))
            awaitItem().run {
                eventSink(VoiceMessageComposerEvents.SendVoiceMessage)
                eventSink(VoiceMessageComposerEvents.SendVoiceMessage)
            }
            assertThat(awaitItem().voiceMessageState).isEqualTo(aPreviewState().toSendingState())

            val finalState = awaitItem()
            assertThat(finalState.voiceMessageState).isEqualTo(VoiceMessageState.Idle)
            sendMediaResult.assertions().isCalledOnce()
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
            awaitItem().eventSink(VoiceMessageComposerEvents.RecorderEvent(VoiceMessageRecorderEvent.Start))
            awaitItem().eventSink(VoiceMessageComposerEvents.RecorderEvent(VoiceMessageRecorderEvent.Stop))
            awaitItem().apply {
                assertThat(voiceMessageState).isEqualTo(aPreviewState())
                eventSink(VoiceMessageComposerEvents.SendVoiceMessage)
            }

            val finalState = awaitItem()
            assertThat(finalState.voiceMessageState).isEqualTo(aPreviewState(isSending = true))
            sendMediaResult.assertions().isNeverCalled()
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
            awaitItem().eventSink(VoiceMessageComposerEvents.RecorderEvent(VoiceMessageRecorderEvent.Start))
            awaitItem().eventSink(VoiceMessageComposerEvents.RecorderEvent(VoiceMessageRecorderEvent.Stop))
            val previewState = awaitItem()

            previewState.eventSink(VoiceMessageComposerEvents.SendVoiceMessage)
            assertThat(awaitItem().voiceMessageState).isEqualTo(aPreviewState().toSendingState())

            ensureAllEventsConsumed()
            assertThat(previewState.voiceMessageState).isEqualTo(aPreviewState())
            sendMediaResult.assertions().isNeverCalled()

            mediaPreProcessor.givenAudioResult()
            previewState.eventSink(VoiceMessageComposerEvents.SendVoiceMessage)
            val finalState = awaitItem()
            assertThat(finalState.voiceMessageState).isEqualTo(VoiceMessageState.Idle)
            sendMediaResult.assertions().isCalledOnce()
            voiceRecorder.assertCalls(started = 1, stopped = 1, deleted = 1)

            testPauseAndDestroy(finalState)
        }
    }

    @Test
    fun `present - send failures are displayed as an error dialog`() = runTest {
        val presenter = createVoiceMessageComposerPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            // Let sending fail due to media preprocessing error
            mediaPreProcessor.givenResult(Result.failure(Exception()))
            awaitItem().eventSink(VoiceMessageComposerEvents.RecorderEvent(VoiceMessageRecorderEvent.Start))
            awaitItem().eventSink(VoiceMessageComposerEvents.RecorderEvent(VoiceMessageRecorderEvent.Stop))
            awaitItem().eventSink(VoiceMessageComposerEvents.SendVoiceMessage)

            assertThat(awaitItem().voiceMessageState).isEqualTo(aPreviewState().toSendingState())

            awaitItem().apply {
                assertThat(voiceMessageState).isEqualTo(aPreviewState().toSendingState())
                assertThat(showSendFailureDialog).isTrue()
            }

            awaitItem().apply {
                assertThat(voiceMessageState).isEqualTo(aPreviewState())
                assertThat(showSendFailureDialog).isTrue()
                eventSink(VoiceMessageComposerEvents.DismissSendFailureDialog)
            }

            val finalState = awaitItem().apply {
                assertThat(voiceMessageState).isEqualTo(aPreviewState())
                assertThat(showSendFailureDialog).isFalse()
            }

            sendMediaResult.assertions().isNeverCalled()
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
            sendMediaResult.assertions().isNeverCalled()
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
            initialState.eventSink(VoiceMessageComposerEvents.RecorderEvent(VoiceMessageRecorderEvent.Start))

            sendMediaResult.assertions().isNeverCalled()
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
            initialState.eventSink(VoiceMessageComposerEvents.RecorderEvent(VoiceMessageRecorderEvent.Start))
            assertThat(awaitItem().voiceMessageState).isEqualTo(VoiceMessageState.Idle)

            initialState.eventSink(VoiceMessageComposerEvents.RecorderEvent(VoiceMessageRecorderEvent.Stop))
            voiceRecorder.assertCalls(stopped = 1)

            permissionsPresenter.setPermissionGranted()

            awaitItem().eventSink(VoiceMessageComposerEvents.RecorderEvent(VoiceMessageRecorderEvent.Start))
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
            awaitItem().eventSink(VoiceMessageComposerEvents.RecorderEvent(VoiceMessageRecorderEvent.Start))

            // See the dialog and accept it
            awaitItem().also {
                assertThat(it.voiceMessageState).isEqualTo(VoiceMessageState.Idle)
                assertThat(it.showPermissionRationaleDialog).isTrue()
                it.eventSink(VoiceMessageComposerEvents.AcceptPermissionRationale)
            }

            // Dialog is hidden, user accepts permissions
            assertThat(awaitItem().showPermissionRationaleDialog).isFalse()

            permissionsPresenter.setPermissionGranted()

            awaitItem().eventSink(VoiceMessageComposerEvents.RecorderEvent(VoiceMessageRecorderEvent.Start))
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
            awaitItem().eventSink(VoiceMessageComposerEvents.RecorderEvent(VoiceMessageRecorderEvent.Start))

            // See the dialog and accept it
            awaitItem().also {
                assertThat(it.voiceMessageState).isEqualTo(VoiceMessageState.Idle)
                assertThat(it.showPermissionRationaleDialog).isTrue()
                it.eventSink(VoiceMessageComposerEvents.DismissPermissionsRationale)
            }

            // Dialog is hidden, user tries to record again
            awaitItem().also {
                assertThat(it.showPermissionRationaleDialog).isFalse()
                it.eventSink(VoiceMessageComposerEvents.RecorderEvent(VoiceMessageRecorderEvent.Start))
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

        val onPauseState = when (val state = mostRecentState.voiceMessageState) {
            VoiceMessageState.Idle -> mostRecentState
            is VoiceMessageState.Recording -> {
                // If recorder was active, it stops
                awaitItem().apply {
                    assertThat(voiceMessageState).isEqualTo(aPreviewState())
                }
            }
            is VoiceMessageState.Preview -> when (state.isPlaying) {
                // If the preview was playing, it pauses
                true -> awaitItem().apply {
                    assertThat(voiceMessageState).isEqualTo(aPausedState())
                }
                false -> mostRecentState
            }
        }

        onPauseState.eventSink(
            VoiceMessageComposerEvents.LifecycleEvent(event = Lifecycle.Event.ON_DESTROY)
        )

        when (val state = onPauseState.voiceMessageState) {
            VoiceMessageState.Idle ->
                ensureAllEventsConsumed()
            is VoiceMessageState.Recording ->
                assertThat(awaitItem().voiceMessageState).isEqualTo(VoiceMessageState.Idle)
            is VoiceMessageState.Preview -> when (state.isSending) {
                true -> ensureAllEventsConsumed()
                false -> assertThat(awaitItem().voiceMessageState).isEqualTo(VoiceMessageState.Idle)
            }
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
            player = VoiceMessageComposerPlayer(FakeMediaPlayer(), this),
            messageComposerContext = messageComposerContext,
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
        isPlaying: Boolean = false,
        playbackProgress: Float = 0f,
        isSending: Boolean = false,
        time: Duration = RECORDING_DURATION,
        showCursor: Boolean = false,
        waveform: List<Float> = voiceRecorder.waveform,
    ) = VoiceMessageState.Preview(
        isPlaying = isPlaying,
        playbackProgress = playbackProgress,
        isSending = isSending,
        time = time,
        showCursor = showCursor,
        waveform = waveform.toImmutableList(),
    )

    private fun aPlayingState() =
        aPreviewState(
            isPlaying = true,
            playbackProgress = 0.1f,
            showCursor = true,
            time = RECORDING_DURATION,
        )

    private fun aPausedState() =
        aPlayingState()
            .copy(isPlaying = false)

    private fun VoiceMessageState.Preview.toSendingState() =
        copy(
            isPlaying = false,
            isSending = true,
            showCursor = false,
            time = RECORDING_DURATION,
        )
}

private fun aVoiceMessageComposerEvent(
    isReply: Boolean = false
) = Composer(
    inThread = false,
    isEditing = false,
    isReply = isReply,
    messageType = Composer.MessageType.VoiceMessage,
    startsThread = null
)

/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.features.messages.impl.voicemessages.composer

import android.Manifest
import androidx.lifecycle.Lifecycle
import app.cash.turbine.TurbineTestContext
import com.google.common.truth.Truth.assertThat
import im.vector.app.features.analytics.plan.Composer
import io.element.android.features.messages.api.timeline.voicemessages.composer.VoiceMessageComposerEvent
import io.element.android.features.messages.api.timeline.voicemessages.composer.VoiceMessageComposerState
import io.element.android.features.messages.impl.messagecomposer.aReplyMode
import io.element.android.features.messages.test.FakeMessageComposerContext
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.media.AudioInfo
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.test.media.FakeMediaUploadHandler
import io.element.android.libraries.matrix.test.room.FakeJoinedRoom
import io.element.android.libraries.matrix.test.timeline.FakeTimeline
import io.element.android.libraries.mediaplayer.test.FakeMediaPlayer
import io.element.android.libraries.mediaupload.api.MediaOptimizationConfig
import io.element.android.libraries.mediaupload.impl.DefaultMediaSender
import io.element.android.libraries.mediaupload.test.FakeMediaPreProcessor
import io.element.android.libraries.permissions.api.PermissionsPresenter
import io.element.android.libraries.permissions.api.aPermissionsState
import io.element.android.libraries.permissions.test.FakePermissionsPresenter
import io.element.android.libraries.permissions.test.FakePermissionsPresenterFactory
import io.element.android.libraries.preferences.api.store.VideoCompressionPreset
import io.element.android.libraries.textcomposer.model.MessageComposerMode
import io.element.android.libraries.textcomposer.model.VoiceMessagePlayerEvent
import io.element.android.libraries.textcomposer.model.VoiceMessageRecorderEvent
import io.element.android.libraries.textcomposer.model.VoiceMessageState
import io.element.android.libraries.voiceplayer.api.VoiceMessageException
import io.element.android.libraries.voicerecorder.api.VoiceRecorder
import io.element.android.libraries.voicerecorder.test.FakeVoiceRecorder
import io.element.android.services.analytics.test.FakeAnalyticsService
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.test
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.io.File
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class DefaultVoiceMessageComposerPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    private val voiceRecorder = FakeVoiceRecorder(
        recordingDuration = RECORDING_DURATION
    )
    private val analyticsService = FakeAnalyticsService()
    private val sendVoiceMessageResult =
        lambdaRecorder<File, AudioInfo, List<Float>, EventId?, Result<FakeMediaUploadHandler>> { _, _, _, _ ->
            Result.success(FakeMediaUploadHandler())
        }
    private val joinedRoom = FakeJoinedRoom(
        liveTimeline = FakeTimeline().apply {
            sendVoiceMessageLambda = sendVoiceMessageResult
        },
    )
    private val mediaPreProcessor = FakeMediaPreProcessor().apply { givenAudioResult() }
    private val mediaSender = DefaultMediaSender(
        preProcessor = mediaPreProcessor,
        room = joinedRoom,
        timelineMode = Timeline.Mode.Live,
        mediaOptimizationConfigProvider = { MediaOptimizationConfig(compressImages = true, videoCompressionPreset = VideoCompressionPreset.STANDARD) },
    )
    private val messageComposerContext = FakeMessageComposerContext()

    companion object {
        private val RECORDING_DURATION = 1.seconds
        private val RECORDING_STATE = VoiceMessageState.Recording(RECORDING_DURATION, listOf(0.1f, 0.2f).toImmutableList())
    }

    @Test
    fun `present - initial state`() = runTest {
        val presenter = createDefaultVoiceMessageComposerPresenter()
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.voiceMessageState).isEqualTo(VoiceMessageState.Idle)
            voiceRecorder.assertCalls(started = 0)

            testPauseAndDestroy(initialState)
        }
    }

    @Test
    fun `present - recording state`() = runTest {
        val presenter = createDefaultVoiceMessageComposerPresenter()
        presenter.test {
            awaitItem().eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.Start))

            val finalState = awaitItem()
            assertThat(finalState.voiceMessageState).isEqualTo(RECORDING_STATE)
            voiceRecorder.assertCalls(started = 1)

            testPauseAndDestroy(finalState)
        }
    }

    @Test
    fun `present - recording state - number of levels is limited`() = runTest {
        val numberOfLevels = 200
        val levels = List(numberOfLevels) { it / numberOfLevels.toFloat() }
        val voiceRecorder = FakeVoiceRecorder(
            levels = levels,
            recordingDuration = RECORDING_DURATION,
        )
        val presenter = createDefaultVoiceMessageComposerPresenter(
            voiceRecorder = voiceRecorder,
        )
        presenter.test {
            awaitItem().eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.Start))
            skipItems(numberOfLevels / 2 - 1)
            val finalState = awaitItem()
            assertThat(finalState.voiceMessageState).isInstanceOf(VoiceMessageState.Recording::class.java)
            val recordingState = finalState.voiceMessageState as VoiceMessageState.Recording
            // The number of levels should be limited to 128 items
            assertThat(recordingState.levels.size).isEqualTo(128)
            assertThat(recordingState.levels).isEqualTo(levels.takeLast(128))
            testPauseAndDestroy(finalState)
        }
    }

    @Test
    fun `present - recording keeps screen on`() = runTest {
        val presenter = createDefaultVoiceMessageComposerPresenter()
        presenter.test {
            awaitItem().apply {
                eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.Start))
                assertThat(keepScreenOn).isFalse()
            }

            awaitItem().apply {
                assertThat(keepScreenOn).isTrue()
                eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.Stop))
            }

            val finalState = awaitItem().apply {
                assertThat(keepScreenOn).isFalse()
            }

            testPauseAndDestroy(finalState)
        }
    }

    @Test
    fun `present - abort recording`() = runTest {
        val presenter = createDefaultVoiceMessageComposerPresenter()
        presenter.test {
            awaitItem().eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.Start))
            awaitItem().eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.Cancel))
            val finalState = awaitItem()
            assertThat(finalState.voiceMessageState).isEqualTo(VoiceMessageState.Idle)
            voiceRecorder.assertCalls(started = 1, stopped = 1, deleted = 1)
            testPauseAndDestroy(finalState)
        }
    }

    @Test
    fun `present - finish recording`() = runTest {
        val presenter = createDefaultVoiceMessageComposerPresenter()
        presenter.test {
            awaitItem().eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.Start))
            awaitItem().eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.Stop))

            val finalState = awaitItem()
            assertThat(finalState.voiceMessageState).isEqualTo(aPreviewState())
            voiceRecorder.assertCalls(started = 1, stopped = 1, deleted = 0)

            testPauseAndDestroy(finalState)
        }
    }

    @Test
    fun `present - play recording before it is ready`() = runTest {
        val presenter = createDefaultVoiceMessageComposerPresenter()
        presenter.test {
            awaitItem().eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.Start))
            val finalState = awaitItem().apply {
                this.eventSink(VoiceMessageComposerEvent.PlayerEvent(VoiceMessagePlayerEvent.Play))
            }

            // Nothing should happen
            assertThat(finalState.voiceMessageState).isEqualTo(RECORDING_STATE)
            voiceRecorder.assertCalls(started = 1, stopped = 0, deleted = 0)

            testPauseAndDestroy(finalState)
        }
    }

    @Test
    fun `present - play recording`() = runTest {
        val presenter = createDefaultVoiceMessageComposerPresenter()
        presenter.test {
            awaitItem().eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.Start))
            awaitItem().eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.Stop))
            awaitItem().eventSink(VoiceMessageComposerEvent.PlayerEvent(VoiceMessagePlayerEvent.Play))
            val finalState = awaitItem().also {
                assertThat(it.voiceMessageState).isEqualTo(aPlayingState())
            }
            voiceRecorder.assertCalls(started = 1, stopped = 1, deleted = 0)

            testPauseAndDestroy(finalState)
        }
    }

    @Test
    fun `present - pause recording`() = runTest {
        val presenter = createDefaultVoiceMessageComposerPresenter()
        presenter.test {
            awaitItem().eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.Start))
            awaitItem().eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.Stop))
            awaitItem().eventSink(VoiceMessageComposerEvent.PlayerEvent(VoiceMessagePlayerEvent.Play))
            awaitItem().eventSink(VoiceMessageComposerEvent.PlayerEvent(VoiceMessagePlayerEvent.Pause))
            val finalState = awaitItem().also {
                assertThat(it.voiceMessageState).isEqualTo(aPausedState())
            }
            voiceRecorder.assertCalls(started = 1, stopped = 1, deleted = 0)

            testPauseAndDestroy(finalState)
        }
    }

    @Test
    fun `present - seek recording`() = runTest {
        val presenter = createDefaultVoiceMessageComposerPresenter()
        presenter.test {
            awaitItem().eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.Start))
            awaitItem().eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.Stop))
            awaitItem().eventSink(VoiceMessageComposerEvent.PlayerEvent(VoiceMessagePlayerEvent.Seek(0.5f)))
            awaitItem().apply {
                assertThat(voiceMessageState).isEqualTo(aPreviewState(playbackProgress = 0.5f, time = 0.seconds, showCursor = true))
            }
            awaitItem().apply {
                assertThat(voiceMessageState).isEqualTo(aPreviewState(playbackProgress = 0.5f, time = 5.seconds, showCursor = true))
                eventSink(VoiceMessageComposerEvent.PlayerEvent(VoiceMessagePlayerEvent.Seek(0.2f)))
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
        val presenter = createDefaultVoiceMessageComposerPresenter()
        presenter.test {
            awaitItem().eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.Start))
            awaitItem().eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.Stop))
            awaitItem().eventSink(VoiceMessageComposerEvent.DeleteVoiceMessage)

            val finalState = awaitItem()
            assertThat(finalState.voiceMessageState).isEqualTo(VoiceMessageState.Idle)
            voiceRecorder.assertCalls(started = 1, stopped = 1, deleted = 1)

            testPauseAndDestroy(finalState)
        }
    }

    @Test
    fun `present - delete while playing`() = runTest {
        val presenter = createDefaultVoiceMessageComposerPresenter()
        presenter.test {
            awaitItem().eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.Start))
            awaitItem().eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.Stop))
            awaitItem().eventSink(VoiceMessageComposerEvent.PlayerEvent(VoiceMessagePlayerEvent.Play))
            awaitItem().eventSink(VoiceMessageComposerEvent.DeleteVoiceMessage)
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
        val presenter = createDefaultVoiceMessageComposerPresenter()
        presenter.test {
            awaitItem().eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.Start))
            awaitItem().eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.Stop))
            awaitItem().eventSink(VoiceMessageComposerEvent.SendVoiceMessage)
            assertThat(awaitItem().voiceMessageState).isEqualTo(aPreviewState().toSendingState())
            val finalState = awaitItem()
            assertThat(finalState.voiceMessageState).isEqualTo(VoiceMessageState.Idle)
            sendVoiceMessageResult.assertions().isCalledOnce()
            voiceRecorder.assertCalls(started = 1, stopped = 1, deleted = 1)

            testPauseAndDestroy(finalState)
        }
    }

    @Test
    fun `present - sending is tracked`() = runTest {
        val presenter = createDefaultVoiceMessageComposerPresenter()
        presenter.test {
            // Send a normal voice message
            messageComposerContext.composerMode = MessageComposerMode.Normal
            awaitItem().eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.Start))
            awaitItem().eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.Stop))
            awaitItem().eventSink(VoiceMessageComposerEvent.SendVoiceMessage)
            skipItems(1) // Sending state
            advanceUntilIdle()
            // Now reply with a voice message
            messageComposerContext.composerMode = aReplyMode()
            awaitItem().eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.Start))
            awaitItem().eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.Stop))
            awaitItem().eventSink(VoiceMessageComposerEvent.SendVoiceMessage)
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
        val presenter = createDefaultVoiceMessageComposerPresenter()
        presenter.test {
            awaitItem().eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.Start))
            awaitItem().eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.Stop))
            awaitItem().eventSink(VoiceMessageComposerEvent.PlayerEvent(VoiceMessagePlayerEvent.Play))
            awaitItem().eventSink(VoiceMessageComposerEvent.SendVoiceMessage)
            assertThat(awaitItem().voiceMessageState).isEqualTo(aPlayingState().toSendingState())
            skipItems(1) // Duplicate sending state

            val finalState = awaitItem()
            assertThat(finalState.voiceMessageState).isEqualTo(VoiceMessageState.Idle)
            sendVoiceMessageResult.assertions().isCalledOnce()
            voiceRecorder.assertCalls(started = 1, stopped = 1, deleted = 1)

            testPauseAndDestroy(finalState)
        }
    }

    @Test
    fun `present - send recording before previous completed, waits`() = runTest {
        val presenter = createDefaultVoiceMessageComposerPresenter()
        presenter.test {
            awaitItem().eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.Start))
            awaitItem().eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.Stop))
            awaitItem().run {
                eventSink(VoiceMessageComposerEvent.SendVoiceMessage)
                eventSink(VoiceMessageComposerEvent.SendVoiceMessage)
            }
            assertThat(awaitItem().voiceMessageState).isEqualTo(aPreviewState().toSendingState())

            val finalState = awaitItem()
            assertThat(finalState.voiceMessageState).isEqualTo(VoiceMessageState.Idle)
            sendVoiceMessageResult.assertions().isCalledOnce()
            voiceRecorder.assertCalls(started = 1, stopped = 1, deleted = 1)

            testPauseAndDestroy(finalState)
        }
    }

    @Test
    fun `present - send failures aren't tracked`() = runTest {
        // Let sending fail due to media preprocessing error
        mediaPreProcessor.givenResult(Result.failure(Exception()))
        val presenter = createDefaultVoiceMessageComposerPresenter()
        presenter.test {
            awaitItem().eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.Start))
            awaitItem().eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.Stop))
            awaitItem().apply {
                assertThat(voiceMessageState).isEqualTo(aPreviewState())
                eventSink(VoiceMessageComposerEvent.SendVoiceMessage)
            }

            val finalState = awaitItem()
            assertThat(finalState.voiceMessageState).isEqualTo(aPreviewState(isSending = true))
            sendVoiceMessageResult.assertions().isNeverCalled()
            assertThat(analyticsService.trackedErrors).isEmpty()
            voiceRecorder.assertCalls(started = 1, stopped = 1, deleted = 0)

            testPauseAndDestroy(finalState)
        }
    }

    @Test
    fun `present - send failures can be retried`() = runTest {
        // Let sending fail due to media preprocessing error
        val presenter = createDefaultVoiceMessageComposerPresenter()
        presenter.test {
            mediaPreProcessor.givenResult(Result.failure(Exception()))
            awaitItem().eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.Start))
            awaitItem().eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.Stop))
            val previewState = awaitItem()

            previewState.eventSink(VoiceMessageComposerEvent.SendVoiceMessage)
            assertThat(awaitItem().voiceMessageState).isEqualTo(aPreviewState().toSendingState())

            ensureAllEventsConsumed()
            assertThat(previewState.voiceMessageState).isEqualTo(aPreviewState())
            sendVoiceMessageResult.assertions().isNeverCalled()

            mediaPreProcessor.givenAudioResult()
            previewState.eventSink(VoiceMessageComposerEvent.SendVoiceMessage)
            val finalState = awaitItem()
            assertThat(finalState.voiceMessageState).isEqualTo(VoiceMessageState.Idle)
            sendVoiceMessageResult.assertions().isCalledOnce()
            voiceRecorder.assertCalls(started = 1, stopped = 1, deleted = 1)

            testPauseAndDestroy(finalState)
        }
    }

    @Test
    fun `present - send failures are displayed as an error dialog`() = runTest {
        val presenter = createDefaultVoiceMessageComposerPresenter()
        presenter.test {
            // Let sending fail due to media preprocessing error
            mediaPreProcessor.givenResult(Result.failure(Exception()))
            awaitItem().eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.Start))
            awaitItem().eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.Stop))
            awaitItem().eventSink(VoiceMessageComposerEvent.SendVoiceMessage)

            assertThat(awaitItem().voiceMessageState).isEqualTo(aPreviewState().toSendingState())

            awaitItem().apply {
                assertThat(voiceMessageState).isEqualTo(aPreviewState().toSendingState())
                assertThat(showSendFailureDialog).isTrue()
            }

            awaitItem().apply {
                assertThat(voiceMessageState).isEqualTo(aPreviewState())
                assertThat(showSendFailureDialog).isTrue()
                eventSink(VoiceMessageComposerEvent.DismissSendFailureDialog)
            }

            val finalState = awaitItem().apply {
                assertThat(voiceMessageState).isEqualTo(aPreviewState())
                assertThat(showSendFailureDialog).isFalse()
            }

            sendVoiceMessageResult.assertions().isNeverCalled()
            testPauseAndDestroy(finalState)
        }
    }

    @Test
    fun `present - send error - missing recording is tracked`() = runTest {
        val presenter = createDefaultVoiceMessageComposerPresenter()
        presenter.test {
            val initialState = awaitItem()
            // Send the message before recording anything
            initialState.eventSink(VoiceMessageComposerEvent.SendVoiceMessage)

            assertThat(initialState.voiceMessageState).isEqualTo(VoiceMessageState.Idle)
            sendVoiceMessageResult.assertions().isNeverCalled()
            assertThat(analyticsService.trackedErrors).hasSize(1)
            voiceRecorder.assertCalls(started = 0)

            testPauseAndDestroy(initialState)
        }
    }

    @Test
    fun `present - record error - security exceptions are tracked`() = runTest {
        val exception = SecurityException("")
        voiceRecorder.givenThrowsSecurityException(exception)
        val presenter = createDefaultVoiceMessageComposerPresenter()
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.Start))

            sendVoiceMessageResult.assertions().isNeverCalled()
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
        val presenter = createDefaultVoiceMessageComposerPresenter(
            permissionsPresenter = permissionsPresenter,
        )
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.Start))
            assertThat(awaitItem().voiceMessageState).isEqualTo(VoiceMessageState.Idle)

            initialState.eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.Stop))
            voiceRecorder.assertCalls(stopped = 1)

            permissionsPresenter.setPermissionGranted()

            awaitItem().eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.Start))
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
        val presenter = createDefaultVoiceMessageComposerPresenter(
            permissionsPresenter = permissionsPresenter,
        )
        presenter.test {
            awaitItem().eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.Start))

            // See the dialog and accept it
            awaitItem().also {
                assertThat(it.voiceMessageState).isEqualTo(VoiceMessageState.Idle)
                assertThat(it.showPermissionRationaleDialog).isTrue()
                it.eventSink(VoiceMessageComposerEvent.AcceptPermissionRationale)
            }

            // Dialog is hidden, user accepts permissions
            assertThat(awaitItem().showPermissionRationaleDialog).isFalse()

            permissionsPresenter.setPermissionGranted()

            awaitItem().eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.Start))
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
        val presenter = createDefaultVoiceMessageComposerPresenter(
            permissionsPresenter = permissionsPresenter,
        )
        presenter.test {
            awaitItem().eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.Start))

            // See the dialog and accept it
            awaitItem().also {
                assertThat(it.voiceMessageState).isEqualTo(VoiceMessageState.Idle)
                assertThat(it.showPermissionRationaleDialog).isTrue()
                it.eventSink(VoiceMessageComposerEvent.DismissPermissionsRationale)
            }

            // Dialog is hidden, user tries to record again
            awaitItem().also {
                assertThat(it.showPermissionRationaleDialog).isFalse()
                it.eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.Start))
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
            VoiceMessageComposerEvent.LifecycleEvent(event = Lifecycle.Event.ON_PAUSE)
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
            VoiceMessageComposerEvent.LifecycleEvent(event = Lifecycle.Event.ON_DESTROY)
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

    private fun TestScope.createDefaultVoiceMessageComposerPresenter(
        permissionsPresenter: PermissionsPresenter = createFakePermissionsPresenter(),
        voiceRecorder: VoiceRecorder = this@DefaultVoiceMessageComposerPresenterTest.voiceRecorder,
    ): DefaultVoiceMessageComposerPresenter {
        return DefaultVoiceMessageComposerPresenter(
            sessionCoroutineScope = backgroundScope,
            timelineMode = Timeline.Mode.Live,
            voiceRecorder = voiceRecorder,
            analyticsService = analyticsService,
            mediaSenderFactory = { mediaSender },
            player = VoiceMessageComposerPlayer(FakeMediaPlayer(), this),
            messageComposerContext = messageComposerContext,
            permissionsPresenterFactory = FakePermissionsPresenterFactory(permissionsPresenter),
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

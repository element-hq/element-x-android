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
import io.element.android.libraries.audio.api.AudioFocus
import io.element.android.libraries.audio.api.AudioFocusRequester
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.media.AudioInfo
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.media.FakeMediaUploadHandler
import io.element.android.libraries.matrix.test.room.FakeJoinedRoom
import io.element.android.libraries.matrix.test.timeline.FakeTimeline
import io.element.android.libraries.mediaplayer.test.FakeAudioFocus
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
import io.element.android.libraries.textcomposer.model.RecordingMode
import io.element.android.libraries.textcomposer.model.VoiceMessagePlayerEvent
import io.element.android.libraries.textcomposer.model.VoiceMessageRecorderEvent
import io.element.android.libraries.textcomposer.model.VoiceMessageState
import io.element.android.libraries.voiceplayer.api.VoiceMessageException
import io.element.android.libraries.voicerecorder.api.VoiceRecorder
import io.element.android.libraries.voicerecorder.test.FakeVoiceRecorder
import io.element.android.services.analytics.test.FakeAnalyticsService
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.consumeItemsUntilTimeout
import io.element.android.tests.testutils.lambda.any
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import io.element.android.tests.testutils.test
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.io.File
import kotlin.time.Duration.Companion.seconds

@Suppress("LargeClass")
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
    private val requestAudioFocusResult = lambdaRecorder<AudioFocusRequester, () -> Unit, Unit> { _, _ -> }
    private val releaseAudioFocusResult = lambdaRecorder<Unit> { }
    private val audioFocus: AudioFocus = FakeAudioFocus(
        requestAudioFocusResult = requestAudioFocusResult,
        releaseAudioFocusResult = releaseAudioFocusResult,
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
                eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.StopAndSend))
            }

            advanceUntilIdle()

            val finalState = consumeItemsUntilTimeout().last().apply {
                assertThat(keepScreenOn).isFalse()
            }

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - recording requests audio focus and releases on stop`() = runTest {
        val presenter = createDefaultVoiceMessageComposerPresenter()
        presenter.test {
            awaitItem().eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.Start))
            val recordingState = awaitItem()
            requestAudioFocusResult.assertions().isCalledOnce()
            releaseAudioFocusResult.assertions().isNeverCalled()

            recordingState.eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.StopAndSend))
            advanceUntilIdle()
            consumeItemsUntilTimeout().last()
            releaseAudioFocusResult.assertions().isCalledOnce()

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - cancelling recording releases audio focus`() = runTest {
        val presenter = createDefaultVoiceMessageComposerPresenter()
        presenter.test {
            awaitItem().eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.Start))
            awaitItem().eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.Cancel))
            awaitItem()
            requestAudioFocusResult.assertions().isCalledOnce()
            releaseAudioFocusResult.assertions().isCalledOnce()

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - audio focus loss during recording finishes gracefully`() = runTest {
        var onFocusLost: (() -> Unit)? = null
        val testAudioFocus = FakeAudioFocus(
            requestAudioFocusResult = { _, callback -> onFocusLost = callback },
            releaseAudioFocusResult = { },
        )
        val presenter = createDefaultVoiceMessageComposerPresenter(audioFocus = testAudioFocus)
        presenter.test {
            awaitItem().eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.Start))
            awaitItem()

            // simulate focus loss (phone call, etc)
            onFocusLost?.invoke()
            advanceUntilIdle()

            val finalState = consumeItemsUntilTimeout().last()
            val voiceState = finalState.voiceMessageState
            assertThat(voiceState).isInstanceOf(VoiceMessageState.Recording::class.java)
            val recordingState = voiceState as VoiceMessageState.Recording
            assertThat(recordingState.isPlayingBack).isTrue()
            assertThat(recordingState.mode).isEqualTo(RecordingMode.Locked)
            voiceRecorder.assertCalls(started = 1, stopped = 1)

            cancelAndIgnoreRemainingEvents()
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
    fun `present - stop and preview recording`() = runTest {
        val presenter = createDefaultVoiceMessageComposerPresenter()
        presenter.test {
            awaitItem().eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.Start))
            awaitItem().eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.StopAndPreview))

            advanceUntilIdle()

            val finalState = consumeItemsUntilTimeout().last()
            val voiceState = finalState.voiceMessageState
            assertThat(voiceState).isInstanceOf(VoiceMessageState.Recording::class.java)
            val recordingState = voiceState as VoiceMessageState.Recording
            assertThat(recordingState.isPlayingBack).isTrue()
            assertThat(recordingState.mode).isEqualTo(RecordingMode.Locked)
            // StopAndPreview auto-plays
            assertThat(recordingState.isPlaying).isTrue()
            voiceRecorder.assertCalls(started = 1, stopped = 1, deleted = 0)

            cancelAndIgnoreRemainingEvents()
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
            awaitItem().eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.StopAndPreview))

            advanceUntilIdle()

            // StopAndPreview auto-plays, so the state should already be playing
            val finalState = consumeItemsUntilTimeout().last()
            val voiceState = finalState.voiceMessageState as VoiceMessageState.Recording
            assertThat(voiceState.isPlayingBack).isTrue()
            assertThat(voiceState.isPlaying).isTrue()
            voiceRecorder.assertCalls(started = 1, stopped = 1, deleted = 0)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - pause playback`() = runTest {
        val presenter = createDefaultVoiceMessageComposerPresenter()
        presenter.test {
            awaitItem().eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.Start))
            awaitItem().eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.StopAndPreview))

            advanceUntilIdle()

            val playingState = consumeItemsUntilTimeout().last()
            assertThat((playingState.voiceMessageState as VoiceMessageState.Recording).isPlaying).isTrue()

            playingState.eventSink(VoiceMessageComposerEvent.PlayerEvent(VoiceMessagePlayerEvent.Pause))

            val finalState = awaitItem()
            val voiceState = finalState.voiceMessageState as VoiceMessageState.Recording
            assertThat(voiceState.isPlayingBack).isTrue()
            assertThat(voiceState.isPlaying).isFalse()
            voiceRecorder.assertCalls(started = 1, stopped = 1, deleted = 0)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - seek recording`() = runTest {
        val presenter = createDefaultVoiceMessageComposerPresenter()
        presenter.test {
            awaitItem().eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.Start))
            awaitItem().eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.StopAndPreview))

            advanceUntilIdle()

            val playbackState = consumeItemsUntilTimeout().last()
            assertThat((playbackState.voiceMessageState as VoiceMessageState.Recording).isPlayingBack).isTrue()

            playbackState.eventSink(VoiceMessageComposerEvent.PlayerEvent(VoiceMessagePlayerEvent.Seek(0.5f)))

            advanceUntilIdle()

            val seekedState = consumeItemsUntilTimeout().last()
            val voiceState = seekedState.voiceMessageState as VoiceMessageState.Recording
            assertThat(voiceState.isPlayingBack).isTrue()
            assertThat(voiceState.playbackProgress).isEqualTo(0.5f)
            assertThat(voiceState.playbackTime).isEqualTo(5.seconds)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - delete recording`() = runTest {
        val presenter = createDefaultVoiceMessageComposerPresenter()
        presenter.test {
            awaitItem().eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.Start))
            awaitItem().eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.StopAndPreview))

            advanceUntilIdle()

            val playbackState = consumeItemsUntilTimeout().last()
            playbackState.eventSink(VoiceMessageComposerEvent.DeleteVoiceMessage)

            advanceUntilIdle()

            val finalState = consumeItemsUntilTimeout().last()
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
            awaitItem().eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.StopAndPreview))

            advanceUntilIdle()

            // StopAndPreview auto-plays
            val playingState = consumeItemsUntilTimeout().last()
            assertThat((playingState.voiceMessageState as VoiceMessageState.Recording).isPlaying).isTrue()

            playingState.eventSink(VoiceMessageComposerEvent.DeleteVoiceMessage)

            advanceUntilIdle()

            val finalState = consumeItemsUntilTimeout().last()
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
            awaitItem().eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.StopAndPreview))

            advanceUntilIdle()

            val playbackState = consumeItemsUntilTimeout().last()
            playbackState.eventSink(VoiceMessageComposerEvent.SendVoiceMessage)

            advanceUntilIdle()

            val finalState = consumeItemsUntilTimeout().last()
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
            awaitItem().eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.StopAndPreview))
            advanceUntilIdle()
            consumeItemsUntilTimeout().last().eventSink(VoiceMessageComposerEvent.SendVoiceMessage)
            advanceUntilIdle()

            // Now reply with a voice message
            messageComposerContext.composerMode = aReplyMode()
            consumeItemsUntilTimeout().last().eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.Start))
            advanceUntilIdle()
            consumeItemsUntilTimeout().last().eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.StopAndPreview))
            advanceUntilIdle()
            consumeItemsUntilTimeout().last().eventSink(VoiceMessageComposerEvent.SendVoiceMessage)
            advanceUntilIdle()

            consumeItemsUntilTimeout().last()

            assertThat(analyticsService.capturedEvents).containsExactly(
                aVoiceMessageComposerEvent(isReply = false),
                aVoiceMessageComposerEvent(isReply = true)
            )

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - send voice message passes reply event ID only when in reply mode`() = runTest {
        val presenter = createDefaultVoiceMessageComposerPresenter()
        presenter.test {
            // Send without reply - should pass null
            messageComposerContext.composerMode = MessageComposerMode.Normal
            awaitItem().eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.Start))
            awaitItem().eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.StopAndSend))

            advanceUntilIdle()

            val afterFirstSend = consumeItemsUntilTimeout().last()
            sendVoiceMessageResult.assertions().isCalledOnce()
                .with(any(), any(), any(), value(null))

            // Send as reply - should pass event ID
            messageComposerContext.composerMode = aReplyMode()
            afterFirstSend.eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.Start))

            advanceUntilIdle()

            consumeItemsUntilTimeout().last().eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.StopAndSend))

            advanceUntilIdle()

            consumeItemsUntilTimeout().last()

            sendVoiceMessageResult.assertions().isCalledExactly(2)
                .withSequence(
                    listOf(any(), any(), any(), value(null)),
                    listOf(any(), any(), any(), value(AN_EVENT_ID)),
                )

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - send while playing`() = runTest {
        val presenter = createDefaultVoiceMessageComposerPresenter()
        presenter.test {
            awaitItem().eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.Start))
            awaitItem().eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.StopAndPreview))

            advanceUntilIdle()

            // StopAndPreview auto-plays
            val playingState = consumeItemsUntilTimeout().last()
            assertThat((playingState.voiceMessageState as VoiceMessageState.Recording).isPlaying).isTrue()

            playingState.eventSink(VoiceMessageComposerEvent.SendVoiceMessage)

            advanceUntilIdle()

            val finalState = consumeItemsUntilTimeout().last()
            assertThat(finalState.voiceMessageState).isEqualTo(VoiceMessageState.Idle)
            sendVoiceMessageResult.assertions().isCalledOnce()
            voiceRecorder.assertCalls(started = 1, stopped = 1, deleted = 1)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - send recording before previous completed, waits`() = runTest {
        val presenter = createDefaultVoiceMessageComposerPresenter()
        presenter.test {
            awaitItem().eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.Start))
            awaitItem().eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.StopAndPreview))

            advanceUntilIdle()

            val playbackState = consumeItemsUntilTimeout().last()
            playbackState.run {
                eventSink(VoiceMessageComposerEvent.SendVoiceMessage)
                eventSink(VoiceMessageComposerEvent.SendVoiceMessage)
            }

            advanceUntilIdle()

            val finalState = consumeItemsUntilTimeout().last()
            assertThat(finalState.voiceMessageState).isEqualTo(VoiceMessageState.Idle)
            sendVoiceMessageResult.assertions().isCalledOnce()
            voiceRecorder.assertCalls(started = 1, stopped = 1, deleted = 1)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - send failures aren't tracked`() = runTest {
        // Let sending fail due to media preprocessing error
        mediaPreProcessor.givenResult(Result.failure(Exception()))
        val presenter = createDefaultVoiceMessageComposerPresenter()
        presenter.test {
            awaitItem().eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.Start))
            awaitItem().eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.StopAndPreview))

            advanceUntilIdle()

            val playbackState = consumeItemsUntilTimeout().last()
            assertThat((playbackState.voiceMessageState as VoiceMessageState.Recording).isPlayingBack).isTrue()

            playbackState.eventSink(VoiceMessageComposerEvent.SendVoiceMessage)

            advanceUntilIdle()

            val finalState = consumeItemsUntilTimeout().last()
            sendVoiceMessageResult.assertions().isNeverCalled()
            assertThat(analyticsService.trackedErrors).isEmpty()
            voiceRecorder.assertCalls(started = 1, stopped = 1, deleted = 0)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - send failures can be retried`() = runTest {
        // Let sending fail due to media preprocessing error
        val presenter = createDefaultVoiceMessageComposerPresenter()
        presenter.test {
            mediaPreProcessor.givenResult(Result.failure(Exception()))
            awaitItem().eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.Start))
            awaitItem().eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.StopAndPreview))

            advanceUntilIdle()

            val playbackState = consumeItemsUntilTimeout().last()
            playbackState.eventSink(VoiceMessageComposerEvent.SendVoiceMessage)

            advanceUntilIdle()

            // Sending failed, but state is still in playback review
            val failedState = consumeItemsUntilTimeout().last()
            sendVoiceMessageResult.assertions().isNeverCalled()

            // Retry with working preprocessor
            mediaPreProcessor.givenAudioResult()
            failedState.eventSink(VoiceMessageComposerEvent.SendVoiceMessage)

            advanceUntilIdle()

            val finalState = consumeItemsUntilTimeout().last()
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
            awaitItem().eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.StopAndPreview))

            advanceUntilIdle()

            val playbackState = consumeItemsUntilTimeout().last()
            playbackState.eventSink(VoiceMessageComposerEvent.SendVoiceMessage)

            advanceUntilIdle()

            // Find the state with the failure dialog shown
            val dialogState = consumeItemsUntilTimeout().last()
            assertThat(dialogState.showSendFailureDialog).isTrue()

            dialogState.eventSink(VoiceMessageComposerEvent.DismissSendFailureDialog)

            val finalState = awaitItem()
            assertThat(finalState.showSendFailureDialog).isFalse()

            sendVoiceMessageResult.assertions().isNeverCalled()
            cancelAndIgnoreRemainingEvents()
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

            // Clear pendingEvent so permission grant doesn't auto-start recording
            initialState.eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.Cancel))
            voiceRecorder.assertCalls(stopped = 1, deleted = 1)

            permissionsPresenter.setPermissionGranted()

            consumeItemsUntilTimeout().last().eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.Start))
            advanceUntilIdle()

            val finalState = consumeItemsUntilTimeout().last()
            assertThat(finalState.voiceMessageState).isInstanceOf(VoiceMessageState.Recording::class.java)
            voiceRecorder.assertCalls(stopped = 1, started = 1, deleted = 1)

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
            skipItems(1)

            // Dialog is hidden, user accepts permissions
            assertThat(awaitItem().showPermissionRationaleDialog).isFalse()

            // Permission is granted, recording starts automatically
            permissionsPresenter.setPermissionGranted()
            advanceUntilIdle()

            val finalState = consumeItemsUntilTimeout().last()
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
            skipItems(1)

            // Dialog is hidden, user tries to record again
            awaitItem().also {
                assertThat(it.showPermissionRationaleDialog).isFalse()
                it.eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.Start))
            }
            skipItems(1)

            // Dialog is shown once again
            val finalState = awaitItem().also {
                assertThat(it.voiceMessageState).isEqualTo(VoiceMessageState.Idle)
                assertThat(it.showPermissionRationaleDialog).isTrue()
            }
            voiceRecorder.assertCalls(started = 0)

            cancelAndIgnoreRemainingEvents()
            testPauseAndDestroy(finalState)
        }
    }

    @Test
    fun `present - lock recording transitions to locked mode`() = runTest {
        val presenter = createDefaultVoiceMessageComposerPresenter()
        presenter.test {
            awaitItem().eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.Start))
            val recordingState = awaitItem()
            assertThat(recordingState.voiceMessageState).isInstanceOf(VoiceMessageState.Recording::class.java)
            assertThat((recordingState.voiceMessageState as VoiceMessageState.Recording).mode).isEqualTo(RecordingMode.Hold)

            recordingState.eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.Lock))
            val lockedState = awaitItem()
            assertThat(lockedState.voiceMessageState).isInstanceOf(VoiceMessageState.Recording::class.java)
            assertThat((lockedState.voiceMessageState as VoiceMessageState.Recording).mode).isEqualTo(RecordingMode.Locked)

            voiceRecorder.assertCalls(started = 1)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - pause and resume recording`() = runTest {
        val presenter = createDefaultVoiceMessageComposerPresenter()
        presenter.test {
            awaitItem().eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.Start))
            val recordingState = awaitItem()

            // Pause
            recordingState.eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.Pause))
            val pausedState = awaitItem()
            assertThat(pausedState.voiceMessageState).isInstanceOf(VoiceMessageState.Recording::class.java)
            assertThat((pausedState.voiceMessageState as VoiceMessageState.Recording).isPaused).isTrue()

            // Resume
            pausedState.eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.Resume))
            val resumedState = awaitItem()
            assertThat(resumedState.voiceMessageState).isInstanceOf(VoiceMessageState.Recording::class.java)
            assertThat((resumedState.voiceMessageState as VoiceMessageState.Recording).isPaused).isFalse()

            voiceRecorder.assertCalls(started = 1, paused = 1, resumed = 1)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - stop and send in hold mode`() = runTest {
        val presenter = createDefaultVoiceMessageComposerPresenter()
        presenter.test {
            awaitItem().eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.Start))
            awaitItem().eventSink(VoiceMessageComposerEvent.RecorderEvent(VoiceMessageRecorderEvent.StopAndSend))

            advanceUntilIdle()

            // Recording should stop and send — ends up back at Idle after send
            val finalState = consumeItemsUntilTimeout().last()
            assertThat(finalState.voiceMessageState).isEqualTo(VoiceMessageState.Idle)
            voiceRecorder.assertCalls(started = 1, stopped = 1, deleted = 1)

            cancelAndIgnoreRemainingEvents()
        }
    }

    private suspend fun TurbineTestContext<VoiceMessageComposerState>.testPauseAndDestroy(
        mostRecentState: VoiceMessageComposerState,
    ) {
        mostRecentState.eventSink(
            VoiceMessageComposerEvent.LifecycleEvent(event = Lifecycle.Event.ON_PAUSE)
        )
        mostRecentState.eventSink(
            VoiceMessageComposerEvent.LifecycleEvent(event = Lifecycle.Event.ON_DESTROY)
        )
        cancelAndIgnoreRemainingEvents()
    }

    private fun TestScope.createDefaultVoiceMessageComposerPresenter(
        permissionsPresenter: PermissionsPresenter = createFakePermissionsPresenter(),
        voiceRecorder: VoiceRecorder = this@DefaultVoiceMessageComposerPresenterTest.voiceRecorder,
        audioFocus: AudioFocus = this@DefaultVoiceMessageComposerPresenterTest.audioFocus,
    ): DefaultVoiceMessageComposerPresenter {
        return DefaultVoiceMessageComposerPresenter(
            sessionCoroutineScope = backgroundScope,
            timelineMode = Timeline.Mode.Live,
            voiceRecorder = voiceRecorder,
            analyticsService = analyticsService,
            audioFocus = audioFocus,
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

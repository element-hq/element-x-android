/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.voicemessages.composer

import android.Manifest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesBinding
import im.vector.app.features.analytics.plan.Composer
import io.element.android.features.messages.api.MessageComposerContext
import io.element.android.features.messages.api.timeline.voicemessages.composer.VoiceMessageComposerEvent
import io.element.android.features.messages.api.timeline.voicemessages.composer.VoiceMessageComposerPresenter
import io.element.android.features.messages.api.timeline.voicemessages.composer.VoiceMessageComposerState
import io.element.android.libraries.audio.api.AudioFocus
import io.element.android.libraries.audio.api.AudioFocusRequester
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.di.annotations.SessionCoroutineScope
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.mediaupload.api.MediaSenderFactory
import io.element.android.libraries.permissions.api.PermissionsEvent
import io.element.android.libraries.permissions.api.PermissionsPresenter
import io.element.android.libraries.textcomposer.model.RecordingMode
import io.element.android.libraries.textcomposer.model.VoiceMessagePlayerEvent
import io.element.android.libraries.textcomposer.model.VoiceMessageRecorderEvent
import io.element.android.libraries.textcomposer.model.VoiceMessageState
import io.element.android.libraries.voiceplayer.api.VoiceMessageException
import io.element.android.libraries.voicerecorder.api.VoiceRecorder
import io.element.android.libraries.voicerecorder.api.VoiceRecorderState
import io.element.android.services.analytics.api.AnalyticsService
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import kotlin.time.Duration.Companion.milliseconds

@AssistedInject
class DefaultVoiceMessageComposerPresenter(
    @SessionCoroutineScope private val sessionCoroutineScope: CoroutineScope,
    @Assisted private val timelineMode: Timeline.Mode,
    private val voiceRecorder: VoiceRecorder,
    private val analyticsService: AnalyticsService,
    private val audioFocus: AudioFocus,
    mediaSenderFactory: MediaSenderFactory,
    private val player: VoiceMessageComposerPlayer,
    private val messageComposerContext: MessageComposerContext,
    permissionsPresenterFactory: PermissionsPresenter.Factory
) : VoiceMessageComposerPresenter {
    @ContributesBinding(RoomScope::class)
    @AssistedFactory
    interface Factory : VoiceMessageComposerPresenter.Factory {
        override fun create(timelineMode: Timeline.Mode): DefaultVoiceMessageComposerPresenter
    }

    private val permissionsPresenter = permissionsPresenterFactory.create(Manifest.permission.RECORD_AUDIO)
    private var pendingEvent: VoiceMessageRecorderEvent.Start? = null
    private val mediaSender = mediaSenderFactory.create(timelineMode)

    @Composable
    override fun present(): VoiceMessageComposerState {
        val localCoroutineScope = rememberCoroutineScope()
        val recorderState by voiceRecorder.state.collectAsState(initial = VoiceRecorderState.Idle)
        val playerState by player.state.collectAsState(initial = VoiceMessageComposerPlayer.State.Initial)
        val keepScreenOn by remember { derivedStateOf { recorderState is VoiceRecorderState.Recording } }
        val permissionState by rememberUpdatedState(permissionsPresenter.present())
        var isSending by remember { mutableStateOf(false) }
        var showSendFailureDialog by remember { mutableStateOf(false) }
        var recordingMode by remember { mutableStateOf(RecordingMode.Hold) }
        var skipPreview by remember { mutableStateOf(false) }
        var isPlayingBack by remember { mutableStateOf(false) }

        LaunchedEffect(recorderState) {
            val recording = recorderState as? VoiceRecorderState.Finished
                ?: return@LaunchedEffect
            if (skipPreview || isPlayingBack) {
                return@LaunchedEffect
            }
            // Recording finished unexpectedly (e.g., audio focus stolen by phone call)
            // Route to locked playback review
            isPlayingBack = true
            recordingMode = RecordingMode.Locked
            player.setMedia(recording.file.path)
        }

        LaunchedEffect(permissionState.permissionGranted) {
            if (permissionState.permissionGranted) {
                pendingEvent?.let {
                    localCoroutineScope.startRecording()
                    pendingEvent = null
                }
            }
        }

        fun handleLifecycleEvent(event: Lifecycle.Event) {
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    // Route to locked playback review instead of old Preview
                    if (recorderState is VoiceRecorderState.Recording) {
                        isPlayingBack = true
                        recordingMode = RecordingMode.Locked
                    }
                    sessionCoroutineScope.finishRecording()
                    player.pause()
                }
                Lifecycle.Event.ON_DESTROY -> {
                    sessionCoroutineScope.cancelRecording()
                }
                else -> {}
            }
        }

        fun sendVoiceMessage() {
            val finishedState = recorderState as? VoiceRecorderState.Finished
            if (finishedState == null) {
                val exception = VoiceMessageException.FileException("No file to send")
                analyticsService.trackError(exception)
                Timber.e(exception)
                return
            }
            if (isSending) {
                return
            }
            isSending = true
            player.pause()
            analyticsService.captureComposerEvent()
            sessionCoroutineScope.launch {
                val result = sendMessage(
                    file = finishedState.file,
                    mimeType = finishedState.mimeType,
                    waveform = finishedState.waveform,
                )
                if (result.isFailure) {
                    showSendFailureDialog = true
                }
            }.invokeOnCompletion {
                isSending = false
            }
        }

        fun handleVoiceMessageRecorderEvent(event: VoiceMessageRecorderEvent) {
            pendingEvent = null
            when (event) {
                VoiceMessageRecorderEvent.Start -> {
                    Timber.v("Voice message record started (hold mode)")
                    recordingMode = RecordingMode.Hold
                    when {
                        permissionState.permissionGranted -> {
                            localCoroutineScope.startRecording()
                        }
                        else -> {
                            Timber.i("Voice message permission needed")
                            pendingEvent = VoiceMessageRecorderEvent.Start
                            permissionState.eventSink(PermissionsEvent.RequestPermissions)
                        }
                    }
                }
                VoiceMessageRecorderEvent.Lock -> {
                    Timber.v("Voice message recording locked")
                    recordingMode = RecordingMode.Locked
                }
                VoiceMessageRecorderEvent.StopAndSend -> {
                    Timber.v("Voice message released — stop and send")
                    skipPreview = true
                    isPlayingBack = false
                    player.pause()
                    sessionCoroutineScope.launch {
                        try {
                            voiceRecorder.stopRecord()
                            audioFocus.releaseAudioFocus()
                            // Wait for the recorder to actually transition to Finished state
                            val finishedState = voiceRecorder.state.first { it is VoiceRecorderState.Finished } as VoiceRecorderState.Finished
                            isSending = true
                            analyticsService.captureComposerEvent()
                            val result = sendMessage(
                                file = finishedState.file,
                                mimeType = finishedState.mimeType,
                                waveform = finishedState.waveform,
                            )
                            isSending = false
                            if (result.isFailure) {
                                showSendFailureDialog = true
                            }
                        } finally {
                            skipPreview = false
                        }
                    }
                }
                VoiceMessageRecorderEvent.Cancel -> {
                    Timber.v("Voice message cancel")
                    isPlayingBack = false
                    player.pause()
                    localCoroutineScope.cancelRecording()
                }
                VoiceMessageRecorderEvent.Pause -> {
                    Timber.v("Voice message recording paused")
                    localCoroutineScope.launch { voiceRecorder.pauseRecord() }
                }
                VoiceMessageRecorderEvent.Resume -> {
                    Timber.v("Voice message recording resumed")
                    localCoroutineScope.launch { voiceRecorder.resumeRecord() }
                }
                VoiceMessageRecorderEvent.StopAndPreview -> {
                    Timber.v("Voice message stop for in-place playback review")
                    isPlayingBack = true
                    sessionCoroutineScope.launch {
                        voiceRecorder.stopRecord()
                        audioFocus.releaseAudioFocus()
                        // Wait for the recorder to actually transition to Finished state
                        val finishedState = voiceRecorder.state.first { it is VoiceRecorderState.Finished } as VoiceRecorderState.Finished
                        player.setMedia(finishedState.file.path)
                        player.play()
                    }
                }
            }
        }

        fun handleVoiceMessagePlayerEvent(event: VoiceMessagePlayerEvent) {
            localCoroutineScope.launch {
                when (event) {
                    VoiceMessagePlayerEvent.Play -> player.play()
                    VoiceMessagePlayerEvent.Pause -> player.pause()
                    is VoiceMessagePlayerEvent.Seek -> player.seek(event.position)
                }
            }
        }

        fun handleEvent(event: VoiceMessageComposerEvent) {
            when (event) {
                is VoiceMessageComposerEvent.RecorderEvent -> handleVoiceMessageRecorderEvent(event.recorderEvent)
                is VoiceMessageComposerEvent.PlayerEvent -> handleVoiceMessagePlayerEvent(event.playerEvent)
                is VoiceMessageComposerEvent.SendVoiceMessage -> localCoroutineScope.launch {
                    sendVoiceMessage()
                }
                VoiceMessageComposerEvent.DeleteVoiceMessage -> {
                    player.pause()
                    localCoroutineScope.deleteRecording()
                }
                VoiceMessageComposerEvent.DismissPermissionsRationale -> {
                    permissionState.eventSink(PermissionsEvent.CloseDialog)
                }
                VoiceMessageComposerEvent.AcceptPermissionRationale -> {
                    permissionState.eventSink(PermissionsEvent.OpenSystemSettingAndCloseDialog)
                }
                is VoiceMessageComposerEvent.LifecycleEvent -> handleLifecycleEvent(event.event)
                VoiceMessageComposerEvent.DismissSendFailureDialog -> {
                    showSendFailureDialog = false
                }
            }
        }

        return VoiceMessageComposerState(
            voiceMessageState = when (val state = recorderState) {
                is VoiceRecorderState.Recording -> VoiceMessageState.Recording(
                    duration = state.elapsedTime,
                    levels = state.levels
                        .takeLast(128)
                        .toImmutableList(),
                    mode = recordingMode,
                    isPaused = state.isPaused,
                )
                is VoiceRecorderState.Finished -> when {
                    skipPreview -> VoiceMessageState.Idle
                    isPlayingBack -> {
                        VoiceMessageState.Recording(
                            duration = state.duration,
                            levels = state.waveform.toImmutableList(),
                            mode = RecordingMode.Locked,
                            isPaused = true,
                            isPlayingBack = true,
                            isPlaying = playerState.isPlaying,
                            playbackProgress = playerState.progress,
                            playbackTime = playerState.currentPosition.milliseconds,
                            waveform = state.waveform.toImmutableList(),
                        )
                    }
                    else -> VoiceMessageState.Idle
                }
                else -> VoiceMessageState.Idle
            },
            showPermissionRationaleDialog = permissionState.showDialog,
            showSendFailureDialog = showSendFailureDialog,
            keepScreenOn = keepScreenOn,
            eventSink = ::handleEvent,
        )
    }

    private fun CoroutineScope.startRecording() = launch {
        try {
            audioFocus.requestAudioFocus(AudioFocusRequester.RecordVoiceMessage) {
                // something else grabbed focus (phone call, etc) - finish gracefully
                // so the user keeps their partial recording
                sessionCoroutineScope.finishRecording()
            }
            voiceRecorder.startRecord()
        } catch (e: SecurityException) {
            audioFocus.releaseAudioFocus()
            Timber.e(e, "Voice message error")
            analyticsService.trackError(VoiceMessageException.PermissionMissing("Expected permission to record but none", e))
        }
    }

    private fun CoroutineScope.finishRecording() = launch {
        voiceRecorder.stopRecord()
        audioFocus.releaseAudioFocus()
    }

    private fun CoroutineScope.cancelRecording() = launch {
        voiceRecorder.stopRecord(cancelled = true)
        audioFocus.releaseAudioFocus()
    }

    private fun CoroutineScope.deleteRecording() = launch {
        voiceRecorder.deleteRecording()
    }

    private suspend fun sendMessage(
        file: File,
        mimeType: String,
        waveform: List<Float>,
    ): Result<Unit> {
        val result = mediaSender.sendVoiceMessage(
            uri = file.toUri(),
            mimeType = mimeType,
            waveForm = waveform,
        )

        if (result.isFailure) {
            Timber.e(result.exceptionOrNull(), "Voice message error")
            return result
        }

        voiceRecorder.deleteRecording()

        return result
    }

    private fun AnalyticsService.captureComposerEvent() =
        capture(
            Composer(
                inThread = messageComposerContext.composerMode.inThread,
                isEditing = messageComposerContext.composerMode.isEditing,
                isReply = messageComposerContext.composerMode.isReply,
                messageType = Composer.MessageType.VoiceMessage,
            )
        )
}

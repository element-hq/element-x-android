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

package io.element.android.features.messages.impl.voicemessages.composer

import android.Manifest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import im.vector.app.features.analytics.plan.Composer
import io.element.android.features.messages.api.MessageComposerContext
import io.element.android.features.messages.impl.voicemessages.VoiceMessageException
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.mediaupload.api.MediaSender
import io.element.android.libraries.permissions.api.PermissionsEvents
import io.element.android.libraries.permissions.api.PermissionsPresenter
import io.element.android.libraries.textcomposer.model.PressEvent
import io.element.android.libraries.textcomposer.model.VoiceMessagePlayerEvent
import io.element.android.libraries.textcomposer.model.VoiceMessageState
import io.element.android.libraries.voicerecorder.api.VoiceRecorder
import io.element.android.libraries.voicerecorder.api.VoiceRecorderState
import io.element.android.services.analytics.api.AnalyticsService
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@SingleIn(RoomScope::class)
class VoiceMessageComposerPresenter @Inject constructor(
    private val appCoroutineScope: CoroutineScope,
    private val voiceRecorder: VoiceRecorder,
    private val analyticsService: AnalyticsService,
    private val mediaSender: MediaSender,
    private val player: VoiceMessageComposerPlayer,
    private val messageComposerContext: MessageComposerContext,
    permissionsPresenterFactory: PermissionsPresenter.Factory
) : Presenter<VoiceMessageComposerState> {
    private val permissionsPresenter = permissionsPresenterFactory.create(Manifest.permission.RECORD_AUDIO)

    @Composable
    override fun present(): VoiceMessageComposerState {
        val localCoroutineScope = rememberCoroutineScope()
        val recorderState by voiceRecorder.state.collectAsState(initial = VoiceRecorderState.Idle)
        val keepScreenOn by remember { derivedStateOf { recorderState is VoiceRecorderState.Recording } }

        val permissionState = permissionsPresenter.present()
        var isSending by remember { mutableStateOf(false) }
        val playerState by player.state.collectAsState(initial = VoiceMessageComposerPlayer.State.NotLoaded)
        val playerTime by remember(playerState, recorderState) { derivedStateOf { displayTime(playerState, recorderState) } }
        val waveform by remember(recorderState) { derivedStateOf { recorderState.finishedWaveform() } }

        val onLifecycleEvent = { event: Lifecycle.Event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    appCoroutineScope.finishRecording()
                    player.pause()
                }
                Lifecycle.Event.ON_DESTROY -> {
                    appCoroutineScope.cancelRecording()
                }
                else -> {}
            }
        }

        val onRecordButtonPress = { event: VoiceMessageComposerEvents.RecordButtonEvent ->
            val permissionGranted = permissionState.permissionGranted
            when (event.pressEvent) {
                PressEvent.PressStart -> {
                    Timber.v("Voice message record button pressed")
                    when {
                        permissionGranted -> {
                            localCoroutineScope.startRecording()
                        }
                        else -> {
                            Timber.i("Voice message permission needed")
                            permissionState.eventSink(PermissionsEvents.RequestPermissions)
                        }
                    }
                }
                PressEvent.LongPressEnd -> {
                    Timber.v("Voice message record button released")
                    localCoroutineScope.finishRecording()
                }
                PressEvent.Tapped -> {
                    Timber.v("Voice message record button tapped")
                    localCoroutineScope.cancelRecording()
                }
            }
        }
        val onPlayerEvent = { event: VoiceMessagePlayerEvent ->
            when (event) {
                VoiceMessagePlayerEvent.Play ->
                    when (val recording = recorderState) {
                        is VoiceRecorderState.Finished ->
                            localCoroutineScope.launch {
                                player.play(
                                    mediaPath = recording.file.path,
                                    mimeType = recording.mimeType,
                                )
                            }
                        else -> Timber.e("Voice message player event received but no file to play")
                    }
                VoiceMessagePlayerEvent.Pause -> {
                    player.pause()
                }
                is VoiceMessagePlayerEvent.Seek -> {
                    // TODO implement seeking
                }
            }
        }

        val onAcceptPermissionsRationale = {
            permissionState.eventSink(PermissionsEvents.OpenSystemSettingAndCloseDialog)
        }

        val onDismissPermissionsRationale = {
            permissionState.eventSink(PermissionsEvents.CloseDialog)
        }

        val onSendButtonPress = lambda@{
            val finishedState = recorderState as? VoiceRecorderState.Finished
            if (finishedState == null) {
                val exception = VoiceMessageException.FileException("No file to send")
                analyticsService.trackError(exception)
                Timber.e(exception)
                return@lambda
            }
            if (isSending) {
                return@lambda
            }
            isSending = true
            player.pause()
            analyticsService.captureComposerEvent()
            appCoroutineScope.sendMessage(
                file = finishedState.file,
                mimeType = finishedState.mimeType,
                waveform = finishedState.waveform,
            ).invokeOnCompletion {
                isSending = false
            }
        }

        val handleEvents: (VoiceMessageComposerEvents) -> Unit = { event ->
            when (event) {
                is VoiceMessageComposerEvents.RecordButtonEvent -> onRecordButtonPress(event)
                is VoiceMessageComposerEvents.PlayerEvent -> onPlayerEvent(event.playerEvent)
                is VoiceMessageComposerEvents.SendVoiceMessage -> localCoroutineScope.launch {
                    onSendButtonPress()
                }
                VoiceMessageComposerEvents.DeleteVoiceMessage -> {
                    player.pause()
                    localCoroutineScope.deleteRecording()
                }
                VoiceMessageComposerEvents.DismissPermissionsRationale -> onDismissPermissionsRationale()
                VoiceMessageComposerEvents.AcceptPermissionRationale -> onAcceptPermissionsRationale()
                is VoiceMessageComposerEvents.LifecycleEvent -> onLifecycleEvent(event.event)
            }
        }

        return VoiceMessageComposerState(
            voiceMessageState = when (val state = recorderState) {
                is VoiceRecorderState.Recording -> VoiceMessageState.Recording(
                    duration = state.elapsedTime,
                    levels = state.levels.toPersistentList()
                )
                is VoiceRecorderState.Finished -> VoiceMessageState.Preview(
                    isSending = isSending,
                    isPlaying = playerState.isPlaying,
                    showCursor = playerState.isLoaded && !isSending,
                    playbackProgress = playerState.progress,
                    time = playerTime,
                    waveform = waveform,
                )
                else -> VoiceMessageState.Idle
            },
            showPermissionRationaleDialog = permissionState.showDialog,
            keepScreenOn = keepScreenOn,
            eventSink = handleEvents,
        )
    }

    private fun CoroutineScope.startRecording() = launch {
        try {
            voiceRecorder.startRecord()
        } catch (e: SecurityException) {
            Timber.e(e, "Voice message error")
            analyticsService.trackError(VoiceMessageException.PermissionMissing("Expected permission to record but none", e))
        }
    }

    private fun CoroutineScope.finishRecording() = launch {
        voiceRecorder.stopRecord()
    }

    private fun CoroutineScope.cancelRecording() = launch {
        voiceRecorder.stopRecord(cancelled = true)
    }

    private fun CoroutineScope.deleteRecording() = launch {
        voiceRecorder.deleteRecording()
    }

    private fun CoroutineScope.sendMessage(
        file: File,
        mimeType: String,
        waveform: List<Float>
    ) = launch {
        val result = mediaSender.sendVoiceMessage(
            uri = file.toUri(),
            mimeType = mimeType,
            waveForm = waveform,
        )

        if (result.isFailure) {
            Timber.e(result.exceptionOrNull(), "Voice message error")
            return@launch
        }

        voiceRecorder.deleteRecording()
    }

    private fun AnalyticsService.captureComposerEvent() =
        analyticsService.capture(
            Composer(
                inThread = messageComposerContext.composerMode.inThread,
                isEditing = messageComposerContext.composerMode.isEditing,
                isReply = messageComposerContext.composerMode.isReply,
                messageType = Composer.MessageType.VoiceMessage,
            )
        )
}

private fun VoiceRecorderState.finishedWaveform(): ImmutableList<Float> =
    (this as? VoiceRecorderState.Finished)
        ?.waveform
        .orEmpty()
        .toImmutableList()

/**
 * The time to display depending on the player state.
 *
 * Either the current position or total duration.
 */
private fun displayTime(
    playerState: VoiceMessageComposerPlayer.State,
    recording: VoiceRecorderState
): Duration = when {
    playerState.isLoaded ->
        playerState.currentPosition.milliseconds
    recording is VoiceRecorderState.Finished ->
        recording.duration
    else ->
        0.milliseconds
}

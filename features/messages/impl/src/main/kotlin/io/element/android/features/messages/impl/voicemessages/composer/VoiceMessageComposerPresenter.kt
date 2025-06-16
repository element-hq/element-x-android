/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
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
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import im.vector.app.features.analytics.plan.Composer
import io.element.android.features.messages.api.MessageComposerContext
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.di.annotations.SessionCoroutineScope
import io.element.android.libraries.mediaupload.api.MediaSender
import io.element.android.libraries.permissions.api.PermissionsEvents
import io.element.android.libraries.permissions.api.PermissionsPresenter
import io.element.android.libraries.textcomposer.model.VoiceMessagePlayerEvent
import io.element.android.libraries.textcomposer.model.VoiceMessageRecorderEvent
import io.element.android.libraries.textcomposer.model.VoiceMessageState
import io.element.android.libraries.voiceplayer.api.VoiceMessageException
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

class VoiceMessageComposerPresenter @Inject constructor(
    @SessionCoroutineScope
    private val sessionCoroutineScope: CoroutineScope,
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
        val playerState by player.state.collectAsState(initial = VoiceMessageComposerPlayer.State.Initial)
        val keepScreenOn by remember { derivedStateOf { recorderState is VoiceRecorderState.Recording } }

        val permissionState = permissionsPresenter.present()
        var isSending by remember { mutableStateOf(false) }
        var showSendFailureDialog by remember { mutableStateOf(false) }

        LaunchedEffect(recorderState) {
            val recording = recorderState as? VoiceRecorderState.Finished
                ?: return@LaunchedEffect
            player.setMedia(recording.file.path)
        }

        val onLifecycleEvent = { event: Lifecycle.Event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    sessionCoroutineScope.finishRecording()
                    player.pause()
                }
                Lifecycle.Event.ON_DESTROY -> {
                    sessionCoroutineScope.cancelRecording()
                }
                else -> {}
            }
        }

        val onVoiceMessageRecorderEvent = { event: VoiceMessageComposerEvents.RecorderEvent ->
            val permissionGranted = permissionState.permissionGranted
            when (event.recorderEvent) {
                VoiceMessageRecorderEvent.Start -> {
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
                VoiceMessageRecorderEvent.Stop -> {
                    Timber.v("Voice message stop button pressed")
                    localCoroutineScope.finishRecording()
                }
                VoiceMessageRecorderEvent.Cancel -> {
                    Timber.v("Voice message cancel button tapped")
                    localCoroutineScope.cancelRecording()
                }
            }
        }
        val onPlayerEvent = { event: VoiceMessagePlayerEvent ->
            localCoroutineScope.launch {
                when (event) {
                    VoiceMessagePlayerEvent.Play -> player.play()
                    VoiceMessagePlayerEvent.Pause -> player.pause()
                    is VoiceMessagePlayerEvent.Seek -> player.seek(event.position)
                }
            }
        }

        val onAcceptPermissionsRationale = {
            permissionState.eventSink(PermissionsEvents.OpenSystemSettingAndCloseDialog)
        }

        val onDismissPermissionsRationale = {
            permissionState.eventSink(PermissionsEvents.CloseDialog)
        }

        val onDismissSendFailureDialog = {
            showSendFailureDialog = false
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

        val handleEvents: (VoiceMessageComposerEvents) -> Unit = { event ->
            when (event) {
                is VoiceMessageComposerEvents.RecorderEvent -> onVoiceMessageRecorderEvent(event)
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
                VoiceMessageComposerEvents.DismissSendFailureDialog -> onDismissSendFailureDialog()
            }
        }

        return VoiceMessageComposerState(
            voiceMessageState = when (val state = recorderState) {
                is VoiceRecorderState.Recording -> VoiceMessageState.Recording(
                    duration = state.elapsedTime,
                    levels = state.levels.toPersistentList(),
                )
                is VoiceRecorderState.Finished ->
                    previewState(
                        playerState = playerState,
                        recorderState = recorderState,
                        isSending = isSending
                    )
                else -> VoiceMessageState.Idle
            },
            showPermissionRationaleDialog = permissionState.showDialog,
            showSendFailureDialog = showSendFailureDialog,
            keepScreenOn = keepScreenOn,
            eventSink = handleEvents,
        )
    }

    @Composable
    private fun previewState(
        playerState: VoiceMessageComposerPlayer.State,
        recorderState: VoiceRecorderState,
        isSending: Boolean,
    ): VoiceMessageState {
        val showCursor by remember(playerState.isStopped, isSending) { derivedStateOf { !playerState.isStopped && !isSending } }
        val playerTime by remember(playerState, recorderState) { derivedStateOf { displayTime(playerState, recorderState) } }
        val waveform by remember(recorderState) { derivedStateOf { recorderState.finishedWaveform() } }

        return VoiceMessageState.Preview(
            isSending = isSending,
            isPlaying = playerState.isPlaying,
            showCursor = showCursor,
            playbackProgress = playerState.progress,
            time = playerTime,
            waveform = waveform,
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
    !playerState.isStopped ->
        playerState.currentPosition.milliseconds
    recording is VoiceRecorderState.Finished ->
        recording.duration
    else ->
        0.milliseconds
}

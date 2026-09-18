/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.circlemessages.composer

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
import io.element.android.features.messages.api.MessageComposerContext
import io.element.android.features.messages.api.timeline.circlemessages.composer.CircleMessageComposerEvent
import io.element.android.features.messages.api.timeline.circlemessages.composer.CircleMessageComposerPresenter
import io.element.android.features.messages.api.timeline.circlemessages.composer.CircleMessageComposerState
import io.element.android.libraries.audio.api.AudioFocus
import io.element.android.libraries.audio.api.AudioFocusRequester
import io.element.android.libraries.circlerecorder.api.CircleRecorder
import io.element.android.libraries.circlerecorder.api.CircleRecorderState
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.di.annotations.SessionCoroutineScope
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.mediaupload.api.MediaSenderFactory
import io.element.android.libraries.permissions.api.PermissionsEvent
import io.element.android.libraries.permissions.api.PermissionsPresenter
import io.element.android.libraries.textcomposer.model.MessageComposerMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@AssistedInject
class DefaultCircleMessageComposerPresenter(
    @SessionCoroutineScope private val sessionCoroutineScope: CoroutineScope,
    @Assisted private val timelineMode: Timeline.Mode,
    private val circleRecorder: CircleRecorder,
    private val audioFocus: AudioFocus,
    mediaSenderFactory: MediaSenderFactory,
    private val messageComposerContext: MessageComposerContext,
    permissionsPresenterFactory: PermissionsPresenter.Factory,
) : CircleMessageComposerPresenter {
    @ContributesBinding(RoomScope::class)
    @AssistedFactory
    interface Factory : CircleMessageComposerPresenter.Factory {
        override fun create(timelineMode: Timeline.Mode): DefaultCircleMessageComposerPresenter
    }

    private val cameraPermissionsPresenter = permissionsPresenterFactory.create(Manifest.permission.CAMERA)
    private val audioPermissionsPresenter = permissionsPresenterFactory.create(Manifest.permission.RECORD_AUDIO)
    private var pendingStart: Boolean = false
    private var isUploadInProgress: Boolean = false
    private var lastAttemptedFile: File? = null
    private var lastFinishedRecording: CircleRecorderState.Finished? = null
    private val mediaSender = mediaSenderFactory.create(timelineMode)

    @Composable
    override fun present(): CircleMessageComposerState {
        val localCoroutineScope = rememberCoroutineScope()
        val recorderState by circleRecorder.state.collectAsState()
        val keepScreenOn by remember { derivedStateOf { recorderState is CircleRecorderState.Recording } }
        val cameraPermissionState by rememberUpdatedState(cameraPermissionsPresenter.present())
        val audioPermissionState by rememberUpdatedState(audioPermissionsPresenter.present())
        var isSending by remember { mutableStateOf(false) }
        var showSendFailureDialog by remember { mutableStateOf(false) }
        var useFrontCamera by remember { mutableStateOf(true) }

        LaunchedEffect(cameraPermissionState.permissionGranted, audioPermissionState.permissionGranted) {
            if (pendingStart && cameraPermissionState.permissionGranted && audioPermissionState.permissionGranted) {
                pendingStart = false
                localCoroutineScope.startRecording()
            }
        }

        fun sendFinishedRecording(finished: CircleRecorderState.Finished, forceRetry: Boolean = false) {
            lastFinishedRecording = finished
            if (isUploadInProgress || (!forceRetry && finished.file == lastAttemptedFile)) {
                return
            }
            lastAttemptedFile = finished.file
            val inReplyToEventId = (messageComposerContext.composerMode as? MessageComposerMode.Reply)?.eventId
            isUploadInProgress = true
            sessionCoroutineScope.launch(start = CoroutineStart.UNDISPATCHED) {
                val result = try {
                    sendMessage(
                        file = finished.file,
                        mimeType = finished.mimeType,
                        inReplyToEventId = inReplyToEventId,
                    )
                } finally {
                    isUploadInProgress = false
                    isSending = false
                }
                if (result.isFailure) {
                    showSendFailureDialog = true
                }
            }
            isSending = true
        }

        LaunchedEffect(recorderState, isSending) {
            val finished = recorderState as? CircleRecorderState.Finished ?: return@LaunchedEffect
            if (isSending || finished.file == lastAttemptedFile) return@LaunchedEffect
            if (finished.duration < MIN_USABLE_DURATION) {
                circleRecorder.deleteRecording()
                return@LaunchedEffect
            }
            sendFinishedRecording(finished)
        }

        fun handleLifecycleEvent(event: Lifecycle.Event) {
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    sessionCoroutineScope.cancelRecording()
                }
                Lifecycle.Event.ON_DESTROY -> {
                    sessionCoroutineScope.cancelRecording()
                }
                else -> {}
            }
        }

        fun handleEvent(event: CircleMessageComposerEvent) {
            when (event) {
                CircleMessageComposerEvent.Start -> {
                    if (isUploadInProgress) return
                    pendingStart = false
                    when {
                        !cameraPermissionState.permissionGranted -> {
                            pendingStart = true
                            cameraPermissionState.eventSink(PermissionsEvent.RequestPermissions)
                        }
                        !audioPermissionState.permissionGranted -> {
                            pendingStart = true
                            audioPermissionState.eventSink(PermissionsEvent.RequestPermissions)
                        }
                        else -> localCoroutineScope.startRecording()
                    }
                }
                CircleMessageComposerEvent.Stop -> {
                    localCoroutineScope.finishRecording()
                }
                CircleMessageComposerEvent.Cancel -> {
                    localCoroutineScope.cancelRecording()
                }
                CircleMessageComposerEvent.FlipCamera -> {
                    useFrontCamera = !useFrontCamera
                    circleRecorder.setUseFrontCamera(useFrontCamera)
                }
                is CircleMessageComposerEvent.SetLinearZoom -> {
                    circleRecorder.setLinearZoom(event.linearZoom)
                }
                CircleMessageComposerEvent.DismissCameraPermissionRationale -> {
                    cameraPermissionState.eventSink(PermissionsEvent.CloseDialog)
                }
                CircleMessageComposerEvent.AcceptCameraPermissionRationale -> {
                    cameraPermissionState.eventSink(PermissionsEvent.OpenSystemSettingAndCloseDialog)
                }
                CircleMessageComposerEvent.DismissAudioPermissionRationale -> {
                    audioPermissionState.eventSink(PermissionsEvent.CloseDialog)
                }
                CircleMessageComposerEvent.AcceptAudioPermissionRationale -> {
                    audioPermissionState.eventSink(PermissionsEvent.OpenSystemSettingAndCloseDialog)
                }
                is CircleMessageComposerEvent.LifecycleEvent -> handleLifecycleEvent(event.event)
                CircleMessageComposerEvent.DismissSendFailureDialog -> {
                    showSendFailureDialog = false
                    isUploadInProgress = false
                    val finished = lastFinishedRecording ?: recorderState as? CircleRecorderState.Finished
                    finished?.let { sendFinishedRecording(it, forceRetry = true) }
                }
                is CircleMessageComposerEvent.BindPreview -> {
                    circleRecorder.bindPreview(event.previewView, event.lifecycleOwner)
                }
            }
        }

        return CircleMessageComposerState(
            isRecording = recorderState is CircleRecorderState.Recording,
            useFrontCamera = useFrontCamera,
            isSwitchingCamera = (recorderState as? CircleRecorderState.Recording)?.isSwitchingCamera == true,
            showCameraPermissionRationaleDialog = cameraPermissionState.showDialog,
            showAudioPermissionRationaleDialog = audioPermissionState.showDialog,
            showSendFailureDialog = showSendFailureDialog,
            keepScreenOn = keepScreenOn,
            eventSink = ::handleEvent,
        )
    }

    private fun CoroutineScope.startRecording() = launch {
        try {
            audioFocus.requestAudioFocus(AudioFocusRequester.RecordCircleMessage) {
                sessionCoroutineScope.finishRecording()
            }
            circleRecorder.startRecord()
        } catch (e: SecurityException) {
            audioFocus.releaseAudioFocus()
            Timber.e(e, "Circle message permission error")
        }
    }

    private fun CoroutineScope.finishRecording() = launch {
        circleRecorder.stopRecord()
        audioFocus.releaseAudioFocus()
    }

    private fun CoroutineScope.cancelRecording() = launch {
        circleRecorder.stopRecord(cancelled = true)
        audioFocus.releaseAudioFocus()
    }

    private suspend fun sendMessage(
        file: File,
        mimeType: String,
        inReplyToEventId: EventId?,
    ): Result<Unit> {
        val result = mediaSender.sendCircleMessage(
            uri = file.toUri(),
            mimeType = mimeType,
            inReplyToEventId = inReplyToEventId,
        )
        if (result.isFailure) {
            Timber.e(result.exceptionOrNull(), "Circle message send error")
            return result
        }
        circleRecorder.deleteRecording()
        return result
    }
}

private val MIN_USABLE_DURATION: Duration = 1.seconds

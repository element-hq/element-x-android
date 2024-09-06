/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.mediaupload.api

import android.net.Uri
import io.element.android.libraries.core.extensions.flatMapCatching
import io.element.android.libraries.matrix.api.core.ProgressCallback
import io.element.android.libraries.matrix.api.media.MediaUploadHandler
import io.element.android.libraries.matrix.api.room.MatrixRoom
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

class MediaSender @Inject constructor(
    private val preProcessor: MediaPreProcessor,
    private val room: MatrixRoom,
) {
    private val ongoingUploadJobs = ConcurrentHashMap<Job.Key, MediaUploadHandler>()
    val hasOngoingMediaUploads get() = ongoingUploadJobs.isNotEmpty()

    suspend fun sendMedia(
        uri: Uri,
        mimeType: String,
        compressIfPossible: Boolean,
        caption: String? = null,
        formattedCaption: String? = null,
        progressCallback: ProgressCallback? = null
    ): Result<Unit> {
        return preProcessor
            .process(
                uri = uri,
                mimeType = mimeType,
                deleteOriginal = true,
                compressIfPossible = compressIfPossible,
            )
            .flatMapCatching { info ->
                room.sendMedia(
                    uploadInfo = info,
                    progressCallback = progressCallback,
                    caption = caption,
                    formattedCaption = formattedCaption
                )
            }
            .handleSendResult()
    }
    suspend fun sendVoiceMessage(
        uri: Uri,
        mimeType: String,
        waveForm: List<Float>,
        progressCallback: ProgressCallback? = null
    ): Result<Unit> {
        return preProcessor
            .process(
                uri = uri,
                mimeType = mimeType,
                deleteOriginal = true,
                compressIfPossible = false
            )
            .flatMapCatching { info ->
                val audioInfo = (info as MediaUploadInfo.Audio).audioInfo
                val newInfo = MediaUploadInfo.VoiceMessage(
                    file = info.file,
                    audioInfo = audioInfo,
                    waveform = waveForm,
                )
                room.sendMedia(
                    uploadInfo = newInfo,
                    progressCallback = progressCallback,
                    caption = null,
                    formattedCaption = null
                )
            }
            .handleSendResult()
    }

    private fun Result<Unit>.handleSendResult() = this
        .onFailure { error ->
            val job = ongoingUploadJobs.remove(Job)
            if (error !is CancellationException) {
                job?.cancel()
            }
        }
        .onSuccess {
            ongoingUploadJobs.remove(Job)
        }

    private suspend fun MatrixRoom.sendMedia(
        uploadInfo: MediaUploadInfo,
        progressCallback: ProgressCallback?,
        caption: String?,
        formattedCaption: String?,
    ): Result<Unit> {
        val handler = when (uploadInfo) {
            is MediaUploadInfo.Image -> {
                sendImage(
                    file = uploadInfo.file,
                    thumbnailFile = uploadInfo.thumbnailFile,
                    imageInfo = uploadInfo.imageInfo,
                    body = caption,
                    formattedBody = formattedCaption,
                    progressCallback = progressCallback
                )
            }
            is MediaUploadInfo.Video -> {
                sendVideo(
                    file = uploadInfo.file,
                    thumbnailFile = uploadInfo.thumbnailFile,
                    videoInfo = uploadInfo.videoInfo,
                    body = caption,
                    formattedBody = formattedCaption,
                    progressCallback = progressCallback
                )
            }
            is MediaUploadInfo.Audio -> {
                sendAudio(
                    file = uploadInfo.file,
                    audioInfo = uploadInfo.audioInfo,
                    progressCallback = progressCallback
                )
            }
            is MediaUploadInfo.VoiceMessage -> {
                sendVoiceMessage(
                    file = uploadInfo.file,
                    audioInfo = uploadInfo.audioInfo,
                    waveform = uploadInfo.waveform,
                    progressCallback = progressCallback
                )
            }
            is MediaUploadInfo.AnyFile -> {
                sendFile(
                    file = uploadInfo.file,
                    fileInfo = uploadInfo.fileInfo,
                    progressCallback = progressCallback
                )
            }
        }

        return handler
            .flatMapCatching { uploadHandler ->
                ongoingUploadJobs[Job] = uploadHandler
                uploadHandler.await()
            }
    }
}

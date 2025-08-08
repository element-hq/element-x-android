/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaupload.api

import android.net.Uri
import io.element.android.libraries.core.extensions.flatMapCatching
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.ProgressCallback
import io.element.android.libraries.matrix.api.media.MediaUploadHandler
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.timeline.Timeline
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

class MediaSender @Inject constructor(
    private val preProcessor: MediaPreProcessor,
    private val room: JoinedRoom,
    private val mediaOptimizationConfigProvider: MediaOptimizationConfigProvider,
) {
    private val ongoingUploadJobs = ConcurrentHashMap<Job.Key, MediaUploadHandler>()
    val hasOngoingMediaUploads get() = ongoingUploadJobs.isNotEmpty()

    suspend fun preProcessMedia(
        uri: Uri,
        mimeType: String,
        mediaOptimizationConfig: MediaOptimizationConfig,
    ): Result<MediaUploadInfo> {
        return preProcessor
            .process(
                uri = uri,
                mimeType = mimeType,
                deleteOriginal = false,
                mediaOptimizationConfig = mediaOptimizationConfig,
            )
    }

    suspend fun sendPreProcessedMedia(
        mediaUploadInfo: MediaUploadInfo,
        caption: String?,
        formattedCaption: String?,
        progressCallback: ProgressCallback?,
        inReplyToEventId: EventId?,
    ): Result<Unit> {
        return room.liveTimeline.sendMedia(
            uploadInfo = mediaUploadInfo,
            progressCallback = progressCallback,
            caption = caption,
            formattedCaption = formattedCaption,
            inReplyToEventId = inReplyToEventId,
        )
            .handleSendResult()
    }

    suspend fun sendMedia(
        uri: Uri,
        mimeType: String,
        caption: String? = null,
        formattedCaption: String? = null,
        progressCallback: ProgressCallback? = null,
        inReplyToEventId: EventId? = null,
        mediaOptimizationConfig: MediaOptimizationConfig,
    ): Result<Unit> {
        return preProcessor
            .process(
                uri = uri,
                mimeType = mimeType,
                deleteOriginal = false,
                mediaOptimizationConfig = mediaOptimizationConfig,
            )
            .flatMapCatching { info ->
                room.liveTimeline.sendMedia(
                    uploadInfo = info,
                    progressCallback = progressCallback,
                    caption = caption,
                    formattedCaption = formattedCaption,
                    inReplyToEventId = inReplyToEventId,
                )
            }
            .handleSendResult()
    }

    suspend fun sendVoiceMessage(
        uri: Uri,
        mimeType: String,
        waveForm: List<Float>,
        progressCallback: ProgressCallback? = null,
        inReplyToEventId: EventId? = null,
    ): Result<Unit> {
        return preProcessor
            .process(
                uri = uri,
                mimeType = mimeType,
                deleteOriginal = true,
                mediaOptimizationConfig = mediaOptimizationConfigProvider.get(),
            )
            .flatMapCatching { info ->
                val audioInfo = (info as MediaUploadInfo.Audio).audioInfo
                val newInfo = MediaUploadInfo.VoiceMessage(
                    file = info.file,
                    audioInfo = audioInfo,
                    waveform = waveForm,
                )
                room.liveTimeline.sendMedia(
                    uploadInfo = newInfo,
                    progressCallback = progressCallback,
                    caption = null,
                    formattedCaption = null,
                    inReplyToEventId = inReplyToEventId,
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

    private suspend fun Timeline.sendMedia(
        uploadInfo: MediaUploadInfo,
        progressCallback: ProgressCallback?,
        caption: String?,
        formattedCaption: String?,
        inReplyToEventId: EventId?,
    ): Result<Unit> {
        val handler = when (uploadInfo) {
            is MediaUploadInfo.Image -> {
                sendImage(
                    file = uploadInfo.file,
                    thumbnailFile = uploadInfo.thumbnailFile,
                    imageInfo = uploadInfo.imageInfo,
                    caption = caption,
                    formattedCaption = formattedCaption,
                    progressCallback = progressCallback,
                    inReplyToEventId = inReplyToEventId,
                )
            }
            is MediaUploadInfo.Video -> {
                sendVideo(
                    file = uploadInfo.file,
                    thumbnailFile = uploadInfo.thumbnailFile,
                    videoInfo = uploadInfo.videoInfo,
                    caption = caption,
                    formattedCaption = formattedCaption,
                    progressCallback = progressCallback,
                    inReplyToEventId = inReplyToEventId,
                )
            }
            is MediaUploadInfo.Audio -> {
                sendAudio(
                    file = uploadInfo.file,
                    audioInfo = uploadInfo.audioInfo,
                    caption = caption,
                    formattedCaption = formattedCaption,
                    progressCallback = progressCallback,
                    inReplyToEventId = inReplyToEventId,
                )
            }
            is MediaUploadInfo.VoiceMessage -> {
                sendVoiceMessage(
                    file = uploadInfo.file,
                    audioInfo = uploadInfo.audioInfo,
                    waveform = uploadInfo.waveform,
                    progressCallback = progressCallback,
                    inReplyToEventId = inReplyToEventId,
                )
            }
            is MediaUploadInfo.AnyFile -> {
                sendFile(
                    file = uploadInfo.file,
                    fileInfo = uploadInfo.fileInfo,
                    caption = caption,
                    formattedCaption = formattedCaption,
                    progressCallback = progressCallback,
                    inReplyToEventId = inReplyToEventId,
                )
            }
        }

        // We handle the cancellations here manually, so we suppress the warning
        @Suppress("RunCatchingNotAllowed")
        return handler
            .mapCatching { uploadHandler ->
                ongoingUploadJobs[Job] = uploadHandler
                uploadHandler.await()
            }
    }

    /**
     * Clean up any temporary files or resources used during the media processing.
     */
    fun cleanUp() = preProcessor.cleanUp()
}

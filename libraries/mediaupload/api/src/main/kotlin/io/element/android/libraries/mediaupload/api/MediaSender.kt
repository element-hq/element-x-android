/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaupload.api

import android.net.Uri
import io.element.android.libraries.core.extensions.flatMapCatching
import io.element.android.libraries.matrix.api.core.ProgressCallback
import io.element.android.libraries.matrix.api.media.MediaUploadHandler
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.room.message.ReplyParameters
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.preferences.api.store.SessionPreferencesStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

class MediaSender @Inject constructor(
    private val preProcessor: MediaPreProcessor,
    private val room: JoinedRoom,
    private val sessionPreferencesStore: SessionPreferencesStore,
) {
    private val ongoingUploadJobs = ConcurrentHashMap<Job.Key, MediaUploadHandler>()
    val hasOngoingMediaUploads get() = ongoingUploadJobs.isNotEmpty()

    suspend fun preProcessMedia(
        uri: Uri,
        mimeType: String,
    ): Result<MediaUploadInfo> {
        val compressIfPossible = sessionPreferencesStore.doesCompressMedia().first()
        return preProcessor
            .process(
                uri = uri,
                mimeType = mimeType,
                deleteOriginal = false,
                compressIfPossible = compressIfPossible,
            )
    }

    suspend fun sendPreProcessedMedia(
        mediaUploadInfo: MediaUploadInfo,
        caption: String?,
        formattedCaption: String?,
        progressCallback: ProgressCallback?,
        replyParameters: ReplyParameters?,
    ): Result<Unit> {
        return room.liveTimeline.sendMedia(
            uploadInfo = mediaUploadInfo,
            progressCallback = progressCallback,
            caption = caption,
            formattedCaption = formattedCaption,
            replyParameters = replyParameters,
        )
            .handleSendResult()
    }

    suspend fun sendMedia(
        uri: Uri,
        mimeType: String,
        caption: String? = null,
        formattedCaption: String? = null,
        progressCallback: ProgressCallback? = null,
        replyParameters: ReplyParameters? = null,
    ): Result<Unit> {
        val compressIfPossible = sessionPreferencesStore.doesCompressMedia().first()
        return preProcessor
            .process(
                uri = uri,
                mimeType = mimeType,
                deleteOriginal = false,
                compressIfPossible = compressIfPossible,
            )
            .flatMapCatching { info ->
                room.liveTimeline.sendMedia(
                    uploadInfo = info,
                    progressCallback = progressCallback,
                    caption = caption,
                    formattedCaption = formattedCaption,
                    replyParameters = replyParameters,
                )
            }
            .handleSendResult()
    }

    suspend fun sendVoiceMessage(
        uri: Uri,
        mimeType: String,
        waveForm: List<Float>,
        progressCallback: ProgressCallback? = null,
        replyParameters: ReplyParameters? = null,
    ): Result<Unit> {
        return preProcessor
            .process(
                uri = uri,
                mimeType = mimeType,
                deleteOriginal = true,
                compressIfPossible = false,
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
                    replyParameters = replyParameters,
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
        replyParameters: ReplyParameters?,
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
                    replyParameters = replyParameters,
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
                    replyParameters = replyParameters,
                )
            }
            is MediaUploadInfo.Audio -> {
                sendAudio(
                    file = uploadInfo.file,
                    audioInfo = uploadInfo.audioInfo,
                    caption = caption,
                    formattedCaption = formattedCaption,
                    progressCallback = progressCallback,
                    replyParameters = replyParameters,
                )
            }
            is MediaUploadInfo.VoiceMessage -> {
                sendVoiceMessage(
                    file = uploadInfo.file,
                    audioInfo = uploadInfo.audioInfo,
                    waveform = uploadInfo.waveform,
                    progressCallback = progressCallback,
                    replyParameters = replyParameters,
                )
            }
            is MediaUploadInfo.AnyFile -> {
                sendFile(
                    file = uploadInfo.file,
                    fileInfo = uploadInfo.fileInfo,
                    caption = caption,
                    formattedCaption = formattedCaption,
                    progressCallback = progressCallback,
                    replyParameters = replyParameters,
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
}

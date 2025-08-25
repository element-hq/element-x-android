/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaupload.api

import android.net.Uri
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.element.android.libraries.androidutils.hash.hash
import io.element.android.libraries.core.extensions.flatMap
import io.element.android.libraries.core.extensions.flatMapCatching
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.media.MediaUploadHandler
import io.element.android.libraries.matrix.api.room.CreateTimelineParams
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.timeline.Timeline
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap

class MediaSender @AssistedInject constructor(
    private val preProcessor: MediaPreProcessor,
    private val room: JoinedRoom,
    @Assisted private val timelineMode: Timeline.Mode,
    private val mediaOptimizationConfigProvider: MediaOptimizationConfigProvider,
) {
    @AssistedFactory
    interface Factory {
        fun create(
            timelineMode: Timeline.Mode,
        ): MediaSender
    }

    private val ongoingUploadJobs = ConcurrentHashMap<Job.Key, MediaUploadHandler>()
    val hasOngoingMediaUploads get() = ongoingUploadJobs.isNotEmpty()

    suspend fun preProcessMedia(
        uri: Uri,
        mimeType: String,
        mediaOptimizationConfig: MediaOptimizationConfig,
    ): Result<MediaUploadInfo> {
        Timber.d("Pre-processing media | uri: ${uri.path.orEmpty().hash()} | mimeType: $mimeType")
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
        inReplyToEventId: EventId?,
    ): Result<Unit> {
        return getTimeline().flatMap {
            Timber.d("Started sending media using timeline: ${it.mode}")
            it.sendMedia(
                uploadInfo = mediaUploadInfo,
                caption = caption,
                formattedCaption = formattedCaption,
                inReplyToEventId = inReplyToEventId,
            )
        }
            .handleSendResult()
    }

    suspend fun sendMedia(
        uri: Uri,
        mimeType: String,
        caption: String? = null,
        formattedCaption: String? = null,
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
                getTimeline().getOrThrow().sendMedia(
                    uploadInfo = info,
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
                getTimeline().getOrThrow().sendMedia(
                    uploadInfo = newInfo,
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
            Timber.e(error, "Sending media failed. Removing ongoing upload job. Total: ${ongoingUploadJobs.size}")
            if (error !is CancellationException) {
                job?.cancel()
            }
        }
        .onSuccess {
            Timber.d("Sent media successfully. Removing ongoing upload job. Total: ${ongoingUploadJobs.size}")
            ongoingUploadJobs.remove(Job)
        }

    private suspend fun Timeline.sendMedia(
        uploadInfo: MediaUploadInfo,
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
                    inReplyToEventId = inReplyToEventId,
                )
            }
            is MediaUploadInfo.Audio -> {
                sendAudio(
                    file = uploadInfo.file,
                    audioInfo = uploadInfo.audioInfo,
                    caption = caption,
                    formattedCaption = formattedCaption,
                    inReplyToEventId = inReplyToEventId,
                )
            }
            is MediaUploadInfo.VoiceMessage -> {
                sendVoiceMessage(
                    file = uploadInfo.file,
                    audioInfo = uploadInfo.audioInfo,
                    waveform = uploadInfo.waveform,
                    inReplyToEventId = inReplyToEventId,
                )
            }
            is MediaUploadInfo.AnyFile -> {
                sendFile(
                    file = uploadInfo.file,
                    fileInfo = uploadInfo.fileInfo,
                    caption = caption,
                    formattedCaption = formattedCaption,
                    inReplyToEventId = inReplyToEventId,
                )
            }
        }

        // We handle the cancellations here manually, so we suppress the warning
        @Suppress("RunCatchingNotAllowed")
        return handler
            .mapCatching { uploadHandler ->
                Timber.d("Added ongoing upload job, total: ${ongoingUploadJobs.size + 1}")
                ongoingUploadJobs[Job] = uploadHandler
                uploadHandler.await()
            }
    }

    private suspend fun getTimeline(): Result<Timeline> {
        return when (timelineMode) {
            is Timeline.Mode.Thread -> {
                room.createTimeline(CreateTimelineParams.Threaded(threadRootEventId = timelineMode.threadRootId))
            }
            else -> Result.success(room.liveTimeline)
        }
    }

    /**
     * Clean up any temporary files or resources used during the media processing.
     */
    fun cleanUp() = preProcessor.cleanUp()
}

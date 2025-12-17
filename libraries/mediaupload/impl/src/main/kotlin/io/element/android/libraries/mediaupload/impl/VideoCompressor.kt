/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaupload.impl

import android.content.Context
import android.media.MediaCodecInfo
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Size
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.Presentation
import androidx.media3.transformer.Composition
import androidx.media3.transformer.DefaultEncoderFactory
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.ProgressHolder
import androidx.media3.transformer.TransformationRequest
import androidx.media3.transformer.Transformer
import androidx.media3.transformer.VideoEncoderSettings
import dev.zacsweers.metro.Inject
import io.element.android.libraries.androidutils.file.createTmpFile
import io.element.android.libraries.androidutils.file.safeDelete
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.preferences.api.store.VideoCompressionPreset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

@Inject
class VideoCompressor(
    @ApplicationContext private val context: Context,
) {
    @OptIn(UnstableApi::class)
    fun compress(uri: Uri, videoCompressionPreset: VideoCompressionPreset): Flow<VideoTranscodingEvent> = callbackFlow {
        val metadata = getVideoMetadata(uri)

        val videoCompressorConfig = VideoCompressorConfigFactory.create(
            metadata = metadata,
            preset = videoCompressionPreset,
        )

        val tmpFile = context.createTmpFile(extension = "mp4")

        val width = metadata?.width ?: Int.MAX_VALUE
        val height = metadata?.height ?: Int.MAX_VALUE

        val videoResizeEffect = run {
            val outputSize = videoCompressorConfig.videoCompressorHelper.getOutputSize(Size(width, height))
            if (metadata?.rotation == 90 || metadata?.rotation == 270) {
                // If the video is rotated, we need to swap width and height
                Presentation.createForWidthAndHeight(
                    outputSize.height,
                    outputSize.width,
                    Presentation.LAYOUT_SCALE_TO_FIT,
                )
            } else {
                // Otherwise, we can use the original width and height
                Presentation.createForWidthAndHeight(
                    outputSize.width,
                    outputSize.height,
                    Presentation.LAYOUT_SCALE_TO_FIT,
                )
            }
        }

        // If we are resizing, we also want to reduce set frame rate to the default value (30fps)
        val newFrameRate = videoCompressorConfig.newFrameRate

        // If we need to resize the video, we also want to recalculate the bitrate
        val newBitrate = videoCompressorConfig.newBitRate

        val inputMediaItem = MediaItem.fromUri(uri)
        val outputMediaItem = EditedMediaItem.Builder(inputMediaItem)
            .setFrameRate(newFrameRate)
            .setEffects(Effects(emptyList(), listOf(videoResizeEffect)))
            .build()

        val encoderFactory = DefaultEncoderFactory.Builder(context)
            .setRequestedVideoEncoderSettings(
                VideoEncoderSettings.Builder()
                    // Use VBR which is generally better for quality and compatibility, although slightly worse for file size
                    .setBitrateMode(MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR)
                    .setBitrate(newBitrate)
                    .build()
            )
            .build()

        val videoTransformer = Transformer.Builder(context)
            .setVideoMimeType(MimeTypes.VIDEO_H264)
            .setAudioMimeType(MimeTypes.AUDIO_AAC)
            .setPortraitEncodingEnabled(false)
            .setEncoderFactory(encoderFactory)
            .addListener(object : Transformer.Listener {
                override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                    trySend(VideoTranscodingEvent.Completed(tmpFile))
                    close()
                }

                override fun onError(composition: Composition, exportResult: ExportResult, exportException: ExportException) {
                    Timber.e(exportException, "Video transcoding failed")
                    tmpFile.safeDelete()
                    close(exportException)
                }

                override fun onFallbackApplied(
                    composition: Composition,
                    originalTransformationRequest: TransformationRequest,
                    fallbackTransformationRequest: TransformationRequest
                ) = Unit
            })
            .build()

        val progressJob = launch(Dispatchers.Main) {
            val progressHolder = ProgressHolder()
            while (isActive) {
                val state = videoTransformer.getProgress(progressHolder)
                if (state != Transformer.PROGRESS_STATE_NOT_STARTED) {
                    channel.send(VideoTranscodingEvent.Progress(progressHolder.progress.toFloat()))
                }
                delay(500)
            }
        }

        withContext(Dispatchers.Main) {
            videoTransformer.start(outputMediaItem, tmpFile.path)
        }

        awaitClose {
            progressJob.cancel()
        }
    }

    private fun getVideoMetadata(uri: Uri): VideoFileMetadata? {
        return runCatchingExceptions {
            MediaMetadataRetriever().use {
                it.setDataSource(context, uri)

                val width = it.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: -1
                val height = it.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: -1
                val bitrate = it.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toLongOrNull() ?: -1
                val frameRate = it.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE)?.toIntOrNull() ?: -1
                val rotation = it.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)?.toIntOrNull() ?: 0

                val (actualWidth, actualHeight) = if (width == -1 || height == -1) {
                    // Try getting the first frame instead
                    val bitmap = it.getFrameAtTime(0) ?: return null
                    bitmap.width to bitmap.height
                } else {
                    width to height
                }

                VideoFileMetadata(
                    width = actualWidth,
                    height = actualHeight,
                    bitrate = bitrate,
                    frameRate = frameRate,
                    rotation = rotation,
                )
            }
        }.onFailure {
            Timber.e(it, "Failed to get video dimensions")
        }.getOrNull()
    }
}

internal data class VideoFileMetadata(
    val width: Int,
    val height: Int,
    val bitrate: Long,
    val frameRate: Int,
    val rotation: Int,
)

sealed interface VideoTranscodingEvent {
    data class Progress(val value: Float) : VideoTranscodingEvent
    data class Completed(val file: File) : VideoTranscodingEvent
}

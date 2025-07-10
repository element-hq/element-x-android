/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaupload.impl

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.webkit.MimeTypeMap
import com.otaliastudios.transcoder.Transcoder
import com.otaliastudios.transcoder.TranscoderListener
import com.otaliastudios.transcoder.common.Size
import com.otaliastudios.transcoder.internal.media.MediaFormatConstants
import com.otaliastudios.transcoder.resize.AtMostResizer
import com.otaliastudios.transcoder.strategy.DefaultVideoStrategy
import com.otaliastudios.transcoder.strategy.PassThroughTrackStrategy
import com.otaliastudios.transcoder.strategy.TrackStrategy
import com.otaliastudios.transcoder.validator.WriteAlwaysValidator
import io.element.android.libraries.androidutils.file.createTmpFile
import io.element.android.libraries.androidutils.file.getMimeType
import io.element.android.libraries.androidutils.file.safeDelete
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.di.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import timber.log.Timber
import java.io.File
import javax.inject.Inject

private const val MP4_EXTENSION = "mp4"

class VideoCompressor @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun compress(uri: Uri, shouldBeCompressed: Boolean) = callbackFlow {
        val metadata = getVideoMetadata(uri)

        val expectedExtension = MimeTypeMap.getSingleton().getExtensionFromMimeType(context.getMimeType(uri))

        val videoStrategy = VideoStrategyFactory.create(
            expectedExtension = expectedExtension,
            metadata = metadata,
            shouldBeCompressed = shouldBeCompressed
        )

        val tmpFile = context.createTmpFile(extension = MP4_EXTENSION)
        val future = Transcoder.into(tmpFile.path)
            .setVideoTrackStrategy(videoStrategy)
            .addDataSource(context, uri)
            // Force the output to be written, even if no transcoding was actually needed
            .setValidator(WriteAlwaysValidator())
            .setListener(object : TranscoderListener {
                override fun onTranscodeProgress(progress: Double) {
                    trySend(VideoTranscodingEvent.Progress(progress.toFloat()))
                }

                override fun onTranscodeCompleted(successCode: Int) {
                    trySend(VideoTranscodingEvent.Completed(tmpFile))
                    close()
                }

                override fun onTranscodeCanceled() {
                    tmpFile.safeDelete()
                    close()
                }

                override fun onTranscodeFailed(exception: Throwable) {
                    tmpFile.safeDelete()
                    close(exception)
                }
            })
            .transcode()

        awaitClose {
            if (!future.isDone) {
                future.cancel(true)
            }
        }
    }

    private fun getVideoMetadata(uri: Uri): VideoFileMetadata? {
        return runCatchingExceptions {
            MediaMetadataRetriever().use {
                it.setDataSource(context, uri)

                val width = it.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: -1
                val height = it.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: -1
                val bitrate = it.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toLongOrNull() ?: -1
                val framerate = it.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE)?.toIntOrNull() ?: -1
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
                    frameRate = framerate,
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

internal object VideoStrategyFactory {
    // 720p
    private const val MAX_COMPRESSED_PIXEL_SIZE = 1280

    // 1080p
    private const val MAX_PIXEL_SIZE = 1920

    fun create(
        expectedExtension: String?,
        metadata: VideoFileMetadata?,
        shouldBeCompressed: Boolean,
    ): TrackStrategy {
        val width = metadata?.width?.takeIf { it >= 0 } ?: Int.MAX_VALUE
        val height = metadata?.height?.takeIf { it >= 0 } ?: Int.MAX_VALUE
        val originalBitrate = metadata?.bitrate?.takeIf { it >= 0 }
        val originalFrameRate = metadata?.frameRate?.takeIf { it >= 0 }
        val rotation = metadata?.rotation?.takeIf { it >= 0 }

        // We only create a resizer if needed
        val resizer = when {
            shouldBeCompressed && (width > MAX_COMPRESSED_PIXEL_SIZE || height > MAX_COMPRESSED_PIXEL_SIZE) -> AtMostResizer(MAX_COMPRESSED_PIXEL_SIZE)
            width > MAX_PIXEL_SIZE || height > MAX_PIXEL_SIZE -> AtMostResizer(MAX_PIXEL_SIZE)
            else -> null
        }

        val newFrameRate = if (resizer is AtMostResizer) {
            // If we are resizing, we also want to reduce the frame rate to 30fps
            DefaultVideoStrategy.DEFAULT_FRAME_RATE
        } else {
            originalFrameRate
        }

        val newBitrate = if (resizer is AtMostResizer) {
            val maxSize = resizer.getOutputSize(Size(width, height))
            val pixelsPerFrame = maxSize.major * maxSize.minor
            val frameRate = newFrameRate ?: DefaultVideoStrategy.DEFAULT_FRAME_RATE
            // Apparently, 0.1 bits per pixel is a sweet spot for video compression
            val bitsPerPixel = 0.1f

            (pixelsPerFrame * bitsPerPixel * frameRate / 1000).toLong()
        } else {
            originalBitrate
        }

        return if (resizer == null && rotation == 0 && expectedExtension == MP4_EXTENSION) {
            // If there's no transcoding or resizing needed for the video file, just create a new file with the same contents but no metadata
            // Rotation is not kept by the PassThroughTrackStrategy, so we need to ensure the video is not rotated
            PassThroughTrackStrategy()
        } else {
            DefaultVideoStrategy.Builder()
                .apply {
                    resizer?.let { addResizer(it) }
                    newBitrate?.let { bitRate(it) }
                    newFrameRate?.let { frameRate(it) }
                }
                .mimeType(MediaFormatConstants.MIMETYPE_VIDEO_AVC)
                .build()
        }
    }
}

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
import com.otaliastudios.transcoder.internal.media.MediaFormatConstants
import com.otaliastudios.transcoder.resize.AtMostResizer
import com.otaliastudios.transcoder.strategy.DefaultVideoStrategy
import io.element.android.libraries.androidutils.file.createTmpFile
import io.element.android.libraries.androidutils.file.getMimeType
import io.element.android.libraries.androidutils.file.safeDelete
import io.element.android.libraries.di.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class VideoCompressor @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        private const val MP4_EXTENSION = "mp4"

        // 720p
        private const val MAX_COMPRESSED_PIXEL_SIZE = 1280

        // 1080p
        private const val MAX_PIXEL_SIZE = 1920
    }

    fun compress(uri: Uri, shouldBeCompressed: Boolean) = callbackFlow {
        val (width, height) = getVideoDimensions(uri) ?: (Int.MAX_VALUE to Int.MAX_VALUE)

        // We only create a resizer if needed
        val resizer = when {
            shouldBeCompressed && (width > MAX_COMPRESSED_PIXEL_SIZE || height > MAX_COMPRESSED_PIXEL_SIZE) -> AtMostResizer(MAX_COMPRESSED_PIXEL_SIZE)
            width > MAX_PIXEL_SIZE || height > MAX_PIXEL_SIZE -> AtMostResizer(MAX_PIXEL_SIZE)
            else -> null
        }

        val expectedExtension = MimeTypeMap.getSingleton().getExtensionFromMimeType(context.getMimeType(uri))

        val tmpFile = context.createTmpFile(extension = MP4_EXTENSION)

        // If there's no transcoding needed for the video file, just copy it to the tmp file and return it
        val future = if (expectedExtension == MP4_EXTENSION && resizer == null) {
            context.contentResolver.openInputStream(uri)?.use { input ->
                tmpFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            trySend(VideoTranscodingEvent.Completed(tmpFile))
            close()
            null
        } else {
            Transcoder.into(tmpFile.path)
                .setVideoTrackStrategy(
                    DefaultVideoStrategy.Builder()
                        .apply {
                            resizer?.let { addResizer(it) }
                        }
                        .mimeType(MediaFormatConstants.MIMETYPE_VIDEO_AVC)
                        .build()
                )
                .addDataSource(context, uri)
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
        }

        awaitClose {
            if (future?.isDone == false) {
                future.cancel(true)
            }
        }
    }

    private fun getVideoDimensions(uri: Uri): Pair<Int, Int>? {
        return runCatching {
            MediaMetadataRetriever().use {
                it.setDataSource(context, uri)

                val width = it.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: -1
                val height = it.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: -1

                if (width == -1 || height == -1) {
                    // Try getting the first frame instead
                    val bitmap = it.getFrameAtTime(0) ?: return null
                    bitmap.width to bitmap.height
                } else {
                    width to height
                }
            }
        }.onFailure {
            Timber.e(it, "Failed to get video dimensions")
        }.getOrNull()
    }
}

sealed interface VideoTranscodingEvent {
    data class Progress(val value: Float) : VideoTranscodingEvent
    data class Completed(val file: File) : VideoTranscodingEvent
}

/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaupload.impl

import android.content.Context
import android.net.Uri
import com.otaliastudios.transcoder.Transcoder
import com.otaliastudios.transcoder.TranscoderListener
import com.otaliastudios.transcoder.common.TrackType
import com.otaliastudios.transcoder.internal.media.MediaFormatConstants
import com.otaliastudios.transcoder.internal.utils.mutableTrackMapOf
import com.otaliastudios.transcoder.resize.AtMostResizer
import com.otaliastudios.transcoder.strategy.DefaultVideoStrategy
import com.otaliastudios.transcoder.time.TimeInterpolator
import io.element.android.libraries.androidutils.file.createTmpFile
import io.element.android.libraries.androidutils.file.safeDelete
import io.element.android.libraries.di.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import java.io.File
import javax.inject.Inject

class VideoCompressor @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun compress(uri: Uri, shouldBeCompressed: Boolean) = callbackFlow {
        val tmpFile = context.createTmpFile(extension = "mp4")
        val future = Transcoder.into(tmpFile.path)
            .setTimeInterpolator(MonotonicTimelineInterpolator())
            .setVideoTrackStrategy(
                DefaultVideoStrategy.Builder()
                    .addResizer(
                        AtMostResizer(
                            if (shouldBeCompressed) {
                                720
                            } else {
                                1080
                            }
                        )
                    )
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

        awaitClose {
            if (!future.isDone) {
                future.cancel(true)
            }
        }
    }
}

sealed interface VideoTranscodingEvent {
    data class Progress(val value: Float) : VideoTranscodingEvent
    data class Completed(val file: File) : VideoTranscodingEvent
}

/**
 * A TimeInterpolator that ensures timestamps are monotonically increasing.
 * Timestamps can go back and forth for many reasons, like miscalculations in
 * MediaCodec output or manually generated timestamps, or at the boundary
 * between one data source and another.
 *
 * Since `MediaMuxer.writeSampleData` can throw in case of invalid timestamps,
 * this interpolator ensures that the next timestamp is at least equal to
 * the previous timestamp plus 1. It does no effort to preserve the input deltas,
 * so the input stream must be as consistent as possible.
 *
 * For example, `20 30 40 50 10 20 30` would become `20 30 40 50 51 52 53`.
 *
 * Copied from the private [com.otaliastudios.transcoder.time.MonotonicTimelineInterpolator].
 */
private class MonotonicTimelineInterpolator : TimeInterpolator {
    private val last = mutableTrackMapOf(Long.MIN_VALUE, Long.MIN_VALUE)

    override fun interpolate(type: TrackType, time: Long): Long {
        return interpolate(last[type], time).also { last[type] = it }
    }

    private fun interpolate(prev: Long, next: Long): Long {
        if (prev == Long.MIN_VALUE) return next
        return next.coerceAtLeast(prev + 1)
    }
}

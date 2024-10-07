/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.mediaupload.impl

import android.content.Context
import android.net.Uri
import com.otaliastudios.transcoder.Transcoder
import com.otaliastudios.transcoder.TranscoderListener
import com.otaliastudios.transcoder.resize.AtMostResizer
import com.otaliastudios.transcoder.strategy.DefaultVideoStrategy
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
    fun compress(uri: Uri) = callbackFlow {
        val tmpFile = context.createTmpFile(extension = "mp4")
        val future = Transcoder.into(tmpFile.path)
            .setVideoTrackStrategy(
                DefaultVideoStrategy.Builder()
                    .addResizer(AtMostResizer(1920, 1080))
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

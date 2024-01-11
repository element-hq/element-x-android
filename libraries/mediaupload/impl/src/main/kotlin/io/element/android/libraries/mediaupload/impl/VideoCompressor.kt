/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

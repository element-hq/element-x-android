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

package io.element.android.libraries.mediaupload.api

import android.net.Uri
import io.element.android.libraries.core.extensions.flatMap
import io.element.android.libraries.matrix.api.core.ProgressCallback
import io.element.android.libraries.matrix.api.room.MatrixRoom
import javax.inject.Inject

class MediaSender @Inject constructor(
    private val preProcessor: MediaPreProcessor,
    private val room: MatrixRoom,
) {

    suspend fun sendMedia(
        uri: Uri,
        mimeType: String,
        compressIfPossible: Boolean,
        progressCallback: ProgressCallback? = null
    ): Result<Unit> {
        return preProcessor
            .process(
                uri = uri,
                mimeType = mimeType,
                deleteOriginal = true,
                compressIfPossible = compressIfPossible
            )
            .flatMap { info ->
                room.sendMedia(info, progressCallback)
            }
    }

    private suspend fun MatrixRoom.sendMedia(
        uploadInfo: MediaUploadInfo,
        progressCallback: ProgressCallback?
    ): Result<Unit> {
        return when (uploadInfo) {
            is MediaUploadInfo.Image -> {
                sendImage(
                    file = uploadInfo.file,
                    thumbnailFile = uploadInfo.thumbnailFile,
                    imageInfo = uploadInfo.imageInfo,
                    progressCallback = progressCallback
                )
            }

            is MediaUploadInfo.Video -> {
                sendVideo(
                    file = uploadInfo.file,
                    thumbnailFile = uploadInfo.thumbnailFile,
                    videoInfo = uploadInfo.videoInfo,
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

            is MediaUploadInfo.AnyFile -> {
                sendFile(
                    file = uploadInfo.file,
                    fileInfo = uploadInfo.fileInfo,
                    progressCallback = progressCallback
                )
            }
        }
    }
}

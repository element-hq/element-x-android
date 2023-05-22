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
import io.element.android.libraries.matrix.api.room.MatrixRoom
import javax.inject.Inject

class MediaSender @Inject constructor(
    private val preProcessor: MediaPreProcessor,
    private val room: MatrixRoom,
) {

    suspend fun sendMedia(uri: Uri, mimeType: String, compressIfPossible: Boolean): Result<Unit> {
        return preProcessor
            .process(
                uri = uri,
                mimeType = mimeType,
                deleteOriginal = true,
                compressIfPossible = compressIfPossible
            )
            .flatMap { info ->
                room.sendMedia(info)
            }
    }

    private suspend fun MatrixRoom.sendMedia(
        info: MediaUploadInfo,
    ): Result<Unit> {
        return when (info) {
            is MediaUploadInfo.Image -> {
                sendImage(info.file, info.thumbnailInfo.file, info.info)
            }

            is MediaUploadInfo.Video -> {
                sendVideo(info.file, info.thumbnailInfo.file, info.info)
            }

            is MediaUploadInfo.AnyFile -> {
                sendFile(info.file, info.info)
            }
            else -> error("Unexpected MediaUploadInfo format: $info")
        }
    }
}

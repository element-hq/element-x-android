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

package io.element.android.libraries.matrix.api.media

interface MatrixMediaLoader {
    /**
     * @param source to fetch the content for.
     * @return a [Result] of ByteArray. It contains the binary data for the media.
     */
    suspend fun loadMediaContent(source: MediaSource): Result<ByteArray>

    /**
     * @param source to fetch the data for.
     * @param width: the desired width for rescaling the media as thumbnail
     * @param height: the desired height for rescaling the media as thumbnail
     * @return a [Result] of ByteArray. It contains the binary data for the media.
     */
    suspend fun loadMediaThumbnail(source: MediaSource, width: Long, height: Long): Result<ByteArray>

    /**
     * @param source to fetch the data for.
     * @param mimeType: optional mime type.
     * @param body: optional body which will be used to name the file.
     * @return a [Result] of [MediaFile]
     */
    suspend fun downloadMediaFile(source: MediaSource, mimeType: String?, body: String?): Result<MediaFile>
}

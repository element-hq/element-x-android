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

import java.nio.file.Path

interface MatrixMediaLoader {
    /**
     * @param url to fetch the content for.
     * @return a [Result] of ByteArray. It contains the binary data for the media.
     */
    suspend fun loadMediaContent(url: String): Result<ByteArray>

    /**
     * @param url to fetch the data for.
     * @param width: the desired width for rescaling the media as thumbnail
     * @param height: the desired height for rescaling the media as thumbnail
     * @return a [Result] of ByteArray. It contains the binary data for the media.
     */
    suspend fun loadMediaThumbnail(url: String, width: Long, height: Long): Result<ByteArray>

    /**
     * @param url to fetch the data for.
     * @param mimeType: optional mime type
     * @return a [Result] of [Path]. It's the path to the downloaded file.
     */
    suspend fun loadMediaFile(url: String, mimeType: String?): Result<Path>
}

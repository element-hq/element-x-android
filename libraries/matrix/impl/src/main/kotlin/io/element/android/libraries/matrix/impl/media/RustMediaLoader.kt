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

package io.element.android.libraries.matrix.impl.media

import android.net.Uri
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.api.media.MatrixMediaLoader
import io.element.android.libraries.matrix.api.media.MatrixMediaSource
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.Client
import org.matrix.rustcomponents.sdk.mediaSourceFromUrl
import org.matrix.rustcomponents.sdk.use
import java.io.File

class RustMediaLoader(
    private val dispatchers: CoroutineDispatchers,
    private val innerClient: Client
) : MatrixMediaLoader {

    @OptIn(ExperimentalUnsignedTypes::class)
    override suspend fun loadMediaContent(source: MatrixMediaSource): Result<ByteArray> =
        withContext(dispatchers.io) {
            runCatching {
                mediaSourceFromUrl(source.url).use { source ->
                    innerClient.getMediaContent(source).toUByteArray().toByteArray()
                }
            }
        }

    @OptIn(ExperimentalUnsignedTypes::class)
    override suspend fun loadMediaThumbnail(
        source: MatrixMediaSource,
        width: Long,
        height: Long
    ): Result<ByteArray> =
        withContext(dispatchers.io) {
            runCatching {
                mediaSourceFromUrl(source.url).use { mediaSource ->
                    innerClient.getMediaThumbnail(
                        mediaSource = mediaSource,
                        width = width.toULong(),
                        height = height.toULong()
                    ).toUByteArray().toByteArray()
                }
            }
        }

    override suspend fun loadMediaFile(source: MatrixMediaSource, mimeType: String?): Result<Uri> =
        withContext(dispatchers.io) {
            runCatching {
                mediaSourceFromUrl(source.url).use { mediaSource ->
                    innerClient.getMediaFile(
                        mediaSource = mediaSource,
                        mimeType = mimeType ?: "application/octet-stream"
                    ).use {
                        val file = File(it.path())
                        Uri.fromFile(file)
                    }
                }
            }

        }
}

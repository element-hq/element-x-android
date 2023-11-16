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

import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.matrix.api.media.MatrixMediaLoader
import io.element.android.libraries.matrix.api.media.MediaFile
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.impl.util.useAny
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.ClientInterface
import org.matrix.rustcomponents.sdk.MediaSourceInterface
import org.matrix.rustcomponents.sdk.mediaSourceFromUrl
import java.io.File
import org.matrix.rustcomponents.sdk.MediaSource as RustMediaSource

class RustMediaLoader(
    private val baseCacheDirectory: File,
    dispatchers: CoroutineDispatchers,
    private val innerClient: ClientInterface,
) : MatrixMediaLoader {

    @OptIn(ExperimentalCoroutinesApi::class)
    private val mediaDispatcher = dispatchers.io.limitedParallelism(32)
    private val cacheDirectory
        get() = File(baseCacheDirectory, "temp/media").apply {
            if (!exists()) mkdirs() // Must always ensure that this directory exists because "Clear cache" does not restart an app's process.
        }

    @OptIn(ExperimentalUnsignedTypes::class)
    override suspend fun loadMediaContent(source: MediaSource): Result<ByteArray> =
        withContext(mediaDispatcher) {
            runCatching {
                source.toRustMediaSource().useAny { source ->
                    innerClient.getMediaContent(source as RustMediaSource /* TODO Fix in Rust SDK */).toUByteArray().toByteArray()
                }
            }
        }

    @OptIn(ExperimentalUnsignedTypes::class)
    override suspend fun loadMediaThumbnail(
        source: MediaSource,
        width: Long,
        height: Long
    ): Result<ByteArray> =
        withContext(mediaDispatcher) {
            runCatching {
                source.toRustMediaSource().useAny { mediaSource ->
                    innerClient.getMediaThumbnail(
                        mediaSource = mediaSource as RustMediaSource /* TODO Fix in Rust SDK */,
                        width = width.toULong(),
                        height = height.toULong()
                    ).toUByteArray().toByteArray()
                }
            }
        }

    override suspend fun downloadMediaFile(
        source: MediaSource,
        mimeType: String?,
        body: String?,
        useCache: Boolean,
    ): Result<MediaFile> =
        withContext(mediaDispatcher) {
            runCatching {
                source.toRustMediaSource().useAny { mediaSource ->
                    val mediaFile = innerClient.getMediaFile(
                        mediaSource = mediaSource as RustMediaSource, /* SDK CHANGE NEEDED */
                        body = body,
                        mimeType = mimeType ?: MimeTypes.OctetStream,
                        useCache = useCache,
                        tempDir = cacheDirectory.path,
                    )
                    RustMediaFile(mediaFile)
                }
            }
        }

    private fun MediaSource.toRustMediaSource(): MediaSourceInterface {
        val json = this.json
        return if (json != null) {
            RustMediaSource.fromJson(json)
        } else {
            mediaSourceFromUrl(url)
        }
    }
}

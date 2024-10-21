/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.media

import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.matrix.api.media.MatrixMediaLoader
import io.element.android.libraries.matrix.api.media.MediaFile
import io.element.android.libraries.matrix.api.media.MediaSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.Client
import org.matrix.rustcomponents.sdk.mediaSourceFromUrl
import org.matrix.rustcomponents.sdk.use
import java.io.File
import org.matrix.rustcomponents.sdk.MediaSource as RustMediaSource

class RustMediaLoader(
    private val baseCacheDirectory: File,
    dispatchers: CoroutineDispatchers,
    private val innerClient: Client,
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
                source.toRustMediaSource().use { source ->
                    innerClient.getMediaContent(source).toUByteArray().toByteArray()
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
                source.toRustMediaSource().use { mediaSource ->
                    innerClient.getMediaThumbnail(
                        mediaSource = mediaSource,
                        width = width.toULong(),
                        height = height.toULong()
                    ).toUByteArray().toByteArray()
                }
            }
        }

    override suspend fun downloadMediaFile(
        source: MediaSource,
        mimeType: String?,
        filename: String?,
        useCache: Boolean,
    ): Result<MediaFile> =
        withContext(mediaDispatcher) {
            runCatching {
                source.toRustMediaSource().use { mediaSource ->
                    val mediaFile = innerClient.getMediaFile(
                        mediaSource = mediaSource,
                        filename = filename,
                        mimeType = mimeType?.takeIf { MimeTypes.hasSubtype(it) } ?: MimeTypes.OctetStream,
                        useCache = useCache,
                        tempDir = cacheDirectory.path,
                    )
                    RustMediaFile(mediaFile)
                }
            }
        }

    private fun MediaSource.toRustMediaSource(): RustMediaSource {
        val json = this.json
        return if (json != null) {
            RustMediaSource.fromJson(json)
        } else {
            mediaSourceFromUrl(url)
        }
    }
}

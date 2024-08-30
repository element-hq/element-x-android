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

package io.element.android.libraries.matrix.ui.media

import coil.decode.DataSource
import coil.decode.ImageSource
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.fetch.SourceResult
import coil.request.Options
import io.element.android.libraries.matrix.api.media.MatrixMediaLoader
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.api.media.toFile
import okio.Buffer
import okio.Path.Companion.toOkioPath
import timber.log.Timber
import java.nio.ByteBuffer
import kotlin.math.roundToLong

internal class CoilMediaFetcher(
    private val scalingFunction: (Float) -> Float,
    private val mediaLoader: MatrixMediaLoader,
    private val mediaData: MediaRequestData?,
    private val options: Options
) : Fetcher {
    override suspend fun fetch(): FetchResult? {
        if (mediaData?.source == null) return null.also {
            Timber.e("MediaData source is null")
        }
        return when (mediaData.kind) {
            is MediaRequestData.Kind.Content -> fetchContent(mediaData.source, options)
            is MediaRequestData.Kind.Thumbnail -> fetchThumbnail(mediaData.source, mediaData.kind, options)
            is MediaRequestData.Kind.File -> fetchFile(mediaData.source, mediaData.kind)
        }
    }

    /**
     * This method is here to avoid using [MatrixMediaLoader.loadMediaContent] as too many ByteArray allocations will flood the memory and cause lots of GC.
     * The MediaFile will be closed (and so destroyed from disk) when the image source is closed.
     *
     */
    private suspend fun fetchFile(mediaSource: MediaSource, kind: MediaRequestData.Kind.File): FetchResult? {
        return mediaLoader.downloadMediaFile(mediaSource, kind.mimeType, kind.body)
            .map { mediaFile ->
                val file = mediaFile.toFile()
                SourceResult(
                    source = ImageSource(file = file.toOkioPath(), closeable = mediaFile),
                    mimeType = null,
                    dataSource = DataSource.DISK
                )
            }
            .onFailure {
                Timber.e(it)
            }
            .getOrNull()
    }

    private suspend fun fetchContent(mediaSource: MediaSource, options: Options): FetchResult? {
        return mediaLoader.loadMediaContent(
            source = mediaSource,
        ).map { byteArray ->
            byteArray.asSourceResult(options)
        }.onFailure {
            Timber.e(it)
        }.getOrNull()
    }

    private suspend fun fetchThumbnail(mediaSource: MediaSource, kind: MediaRequestData.Kind.Thumbnail, options: Options): FetchResult? {
        return mediaLoader.loadMediaThumbnail(
            source = mediaSource,
            width = scalingFunction(kind.width.toFloat()).roundToLong(),
            height = scalingFunction(kind.height.toFloat()).roundToLong(),
        ).map { byteArray ->
            byteArray.asSourceResult(options)
        }.onFailure {
            Timber.e(it)
        }.getOrNull()
    }

    private fun ByteArray.asSourceResult(options: Options): SourceResult {
        val byteBuffer = ByteBuffer.wrap(this)
        val bufferedSource = try {
            Buffer().apply { write(byteBuffer) }
        } finally {
            byteBuffer.position(0)
        }
        return SourceResult(
            source = ImageSource(bufferedSource, options.context),
            mimeType = null,
            dataSource = DataSource.MEMORY
        )
    }
}

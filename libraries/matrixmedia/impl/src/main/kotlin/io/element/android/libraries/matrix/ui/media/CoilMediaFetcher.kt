/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.media

import coil3.decode.DataSource
import coil3.decode.ImageSource
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.SourceFetchResult
import io.element.android.libraries.matrix.api.media.MatrixMediaLoader
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.api.media.toFile
import okio.Buffer
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import timber.log.Timber
import java.nio.ByteBuffer

internal class CoilMediaFetcher(
    private val mediaLoader: MatrixMediaLoader,
    private val mediaData: MediaRequestData,
) : Fetcher {
    override suspend fun fetch(): FetchResult? {
        val source = mediaData.source
        if (source == null) {
            Timber.e("MediaData source is null")
            return null
        }
        return when (val kind = mediaData.kind) {
            is MediaRequestData.Kind.Content -> fetchContent(source)
            is MediaRequestData.Kind.Thumbnail -> fetchThumbnail(source, kind)
            is MediaRequestData.Kind.File -> fetchFile(source, kind)
        }
    }

    /**
     * This method is here to avoid using [MatrixMediaLoader.loadMediaContent] as too many ByteArray allocations will flood the memory and cause lots of GC.
     * The MediaFile will be closed (and so destroyed from disk) when the image source is closed.
     *
     */
    private suspend fun fetchFile(mediaSource: MediaSource, kind: MediaRequestData.Kind.File): FetchResult? {
        return mediaLoader.downloadMediaFile(mediaSource, kind.mimeType, kind.fileName)
            .map { mediaFile ->
                val file = mediaFile.toFile()
                SourceFetchResult(
                    source = ImageSource(
                        file = file.toOkioPath(),
                        fileSystem = FileSystem.SYSTEM,
                        closeable = mediaFile,
                    ),
                    mimeType = null,
                    dataSource = DataSource.DISK
                )
            }
            .onFailure {
                Timber.e(it)
            }
            .getOrNull()
    }

    private suspend fun fetchContent(mediaSource: MediaSource): FetchResult? {
        return mediaLoader.loadMediaContent(
            source = mediaSource,
        ).map { byteArray ->
            byteArray.asSourceResult()
        }.onFailure {
            Timber.e(it)
        }.getOrNull()
    }

    private suspend fun fetchThumbnail(mediaSource: MediaSource, kind: MediaRequestData.Kind.Thumbnail): FetchResult? {
        return mediaLoader.loadMediaThumbnail(
            source = mediaSource,
            width = kind.width,
            height = kind.height,
        ).map { byteArray ->
            byteArray.asSourceResult()
        }.onFailure {
            Timber.e(it)
        }.getOrNull()
    }

    private fun ByteArray.asSourceResult(): SourceFetchResult {
        val byteBuffer = ByteBuffer.wrap(this)
        val bufferedSource = try {
            Buffer().apply { write(byteBuffer) }
        } finally {
            byteBuffer.position(0)
        }
        return SourceFetchResult(
            source = ImageSource(
                source = bufferedSource,
                fileSystem = FileSystem.SYSTEM,
            ),
            mimeType = null,
            dataSource = DataSource.MEMORY
        )
    }
}

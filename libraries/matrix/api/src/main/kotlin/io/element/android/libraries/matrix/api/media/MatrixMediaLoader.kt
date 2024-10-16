/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
     * @param filename: optional String which will be used to name the file.
     * @param useCache: if true, the rust sdk will cache the media in its store.
     * @return a [Result] of [MediaFile]
     */
    suspend fun downloadMediaFile(
        source: MediaSource,
        mimeType: String?,
        filename: String?,
        useCache: Boolean = true,
    ): Result<MediaFile>
}

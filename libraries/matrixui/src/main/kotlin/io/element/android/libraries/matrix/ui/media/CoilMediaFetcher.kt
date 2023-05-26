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

import coil.ImageLoader
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.request.Options
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.media.MatrixMediaLoader
import java.nio.ByteBuffer

internal class CoilMediaFetcher(
    private val mediaLoader: MatrixMediaLoader,
    private val mediaData: MediaRequestData?,
    private val options: Options,
    private val imageLoader: ImageLoader
) : Fetcher {

    override suspend fun fetch(): FetchResult? {
        return loadMedia()
            .map { data ->
                val byteBuffer = ByteBuffer.wrap(data)
                imageLoader.components.newFetcher(byteBuffer, options, imageLoader)?.first?.fetch()
            }.getOrThrow()
    }

    private suspend fun loadMedia(): Result<ByteArray> {
        if (mediaData?.source == null) return Result.failure(IllegalStateException("No media data to fetch."))
        return when (mediaData.kind) {
            is MediaRequestData.Kind.Content -> mediaLoader.loadMediaContent(source = mediaData.source)
            is MediaRequestData.Kind.Thumbnail -> mediaLoader.loadMediaThumbnail(
                source = mediaData.source,
                width = mediaData.kind.width,
                height = mediaData.kind.height
            )
        }
    }

    class MediaRequestDataFactory(private val client: MatrixClient) :
        Fetcher.Factory<MediaRequestData> {
        override fun create(
            data: MediaRequestData,
            options: Options,
            imageLoader: ImageLoader
        ): Fetcher {
            return CoilMediaFetcher(
                mediaLoader = client.mediaLoader,
                mediaData = data,
                options = options,
                imageLoader = imageLoader
            )
        }
    }

    class AvatarFactory(private val client: MatrixClient) :
        Fetcher.Factory<AvatarData> {

        override fun create(
            data: AvatarData,
            options: Options,
            imageLoader: ImageLoader
        ): Fetcher {
            return CoilMediaFetcher(
                mediaLoader = client.mediaLoader,
                mediaData = data.toMediaRequestData(),
                options = options,
                imageLoader = imageLoader
            )
        }
    }
}

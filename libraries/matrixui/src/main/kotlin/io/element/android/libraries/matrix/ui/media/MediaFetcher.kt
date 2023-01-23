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
import io.element.android.libraries.matrix.MatrixClient
import io.element.android.libraries.matrix.media.MediaResolver
import java.nio.ByteBuffer

internal class MediaFetcher(
    private val mediaResolver: MediaResolver?,
    private val meta: MediaResolver.Meta,
    private val options: Options,
    private val imageLoader: ImageLoader
) : Fetcher {

    override suspend fun fetch(): FetchResult? {
        val byteArray = mediaResolver?.resolve(meta) ?: return null
        val byteBuffer = ByteBuffer.wrap(byteArray)
        return imageLoader.components.newFetcher(byteBuffer, options, imageLoader)?.first?.fetch()
    }

    class Factory(private val client: MatrixClient) :
        Fetcher.Factory<MediaResolver.Meta> {
        override fun create(
            data: MediaResolver.Meta,
            options: Options,
            imageLoader: ImageLoader
        ): Fetcher {
            return MediaFetcher(
                mediaResolver = client.mediaResolver(),
                meta = data,
                options = options,
                imageLoader = imageLoader
            )
        }
    }
}

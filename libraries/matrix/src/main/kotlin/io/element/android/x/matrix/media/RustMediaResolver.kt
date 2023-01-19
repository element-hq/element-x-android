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

package io.element.android.x.matrix.media

import io.element.android.x.matrix.MatrixClient
import org.matrix.rustcomponents.sdk.mediaSourceFromUrl

internal class RustMediaResolver(private val client: MatrixClient) : MediaResolver {

    override suspend fun resolve(url: String?, kind: MediaResolver.Kind): ByteArray? {
        if (url.isNullOrEmpty()) return null
        val mediaSource = mediaSourceFromUrl(url)
        return resolve(MediaResolver.Meta(mediaSource, kind))
    }

    override suspend fun resolve(meta: MediaResolver.Meta): ByteArray? {
        return when (meta.kind) {
            is MediaResolver.Kind.Content -> client.loadMediaContentForSource(meta.source)
            is MediaResolver.Kind.Thumbnail -> client.loadMediaThumbnailForSource(
                meta.source,
                meta.kind.width.toLong(),
                meta.kind.height.toLong()
            )
        }.getOrNull()
    }
}

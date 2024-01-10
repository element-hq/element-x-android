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

import io.element.android.libraries.matrix.api.media.MediaSource

/**
 * Can be use with [coil.compose.AsyncImage] to load a [MediaSource].
 * This will go internally through our [CoilMediaFetcher].
 *
 * Example of usage:
 *  AsyncImage(
 *      model = MediaRequestData(mediaSource, MediaRequestData.Kind.Content),
 *      contentScale = ContentScale.Fit,
 *  )
 *
 */
data class MediaRequestData(
    val source: MediaSource?,
    val kind: Kind
) {
    sealed interface Kind {
        data object Content : Kind
        data class File(val body: String?, val mimeType: String) : Kind
        data class Thumbnail(val width: Long, val height: Long) : Kind {
            constructor(size: Long) : this(size, size)
        }
    }
}

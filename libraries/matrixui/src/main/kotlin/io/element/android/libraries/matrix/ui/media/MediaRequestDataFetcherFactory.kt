/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.libraries.matrix.ui.media

import android.content.Context
import coil.ImageLoader
import coil.fetch.Fetcher
import coil.request.Options
import io.element.android.libraries.matrix.api.MatrixClient

internal class MediaRequestDataFetcherFactory(
    private val context: Context,
    private val client: MatrixClient
) : Fetcher.Factory<MediaRequestData> {
    override fun create(
        data: MediaRequestData,
        options: Options,
        imageLoader: ImageLoader
    ): Fetcher {
        return CoilMediaFetcher(
            scalingFunction = { context.resources.displayMetrics.density * it },
            mediaLoader = client.mediaLoader,
            mediaData = data,
            options = options
        )
    }
}

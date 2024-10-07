/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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

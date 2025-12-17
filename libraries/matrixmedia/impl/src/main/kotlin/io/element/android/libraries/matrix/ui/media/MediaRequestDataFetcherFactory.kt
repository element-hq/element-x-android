/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.media

import coil3.ImageLoader
import coil3.fetch.Fetcher
import coil3.request.Options
import io.element.android.libraries.matrix.api.media.MatrixMediaLoader

internal class MediaRequestDataFetcherFactory(
    private val matrixMediaLoader: MatrixMediaLoader,
) : Fetcher.Factory<MediaRequestData> {
    override fun create(
        data: MediaRequestData,
        options: Options,
        imageLoader: ImageLoader
    ): Fetcher {
        return CoilMediaFetcher(
            mediaLoader = matrixMediaLoader,
            mediaData = data,
        )
    }
}

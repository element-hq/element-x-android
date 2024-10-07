/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.media

import coil.ImageLoader
import io.element.android.libraries.matrix.api.MatrixClient

class FakeLoggedInImageLoaderFactory(
    private val newImageLoaderLambda: (MatrixClient) -> ImageLoader
) : LoggedInImageLoaderFactory {
    override fun newImageLoader(matrixClient: MatrixClient): ImageLoader {
        return newImageLoaderLambda(matrixClient)
    }
}

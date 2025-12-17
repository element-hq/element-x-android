/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.media.test

import coil3.ImageLoader
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.ui.media.ImageLoaderHolder

class FakeImageLoaderHolder(
    val fakeImageLoader: ImageLoader = FakeImageLoader(),
) : ImageLoaderHolder {
    override fun get(): ImageLoader {
        return fakeImageLoader
    }

    override fun get(client: MatrixClient): ImageLoader {
        return fakeImageLoader
    }

    override fun remove(sessionId: SessionId) {
        // No-op
    }
}

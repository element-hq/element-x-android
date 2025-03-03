/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.test.notifications

import coil3.ImageLoader
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.ui.media.ImageLoaderHolder

class FakeImageLoaderHolder : ImageLoaderHolder {
    private val fakeImageLoader = FakeImageLoader()
    override fun get(client: MatrixClient): ImageLoader {
        return fakeImageLoader.getImageLoader()
    }

    override fun remove(sessionId: SessionId) {
        // No-op
    }
}

/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.media

import io.element.android.libraries.matrix.api.media.MediaFile
import org.matrix.rustcomponents.sdk.MediaFileHandle

class RustMediaFile(private val inner: MediaFileHandle) : MediaFile {
    override fun path(): String {
        return inner.path()
    }

    override fun persist(path: String): Boolean {
        return inner.persist(path)
    }

    override fun close() {
        inner.close()
    }
}

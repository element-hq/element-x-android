/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.test.media

import io.element.android.libraries.matrix.api.media.MediaFile
import java.io.File

class FakeMediaFile(private val path: String) : MediaFile {
    override fun path(): String {
        return path
    }

    override fun persist(path: String): Boolean {
        return File(path()).renameTo(File(path))
    }

    override fun close() = Unit
}

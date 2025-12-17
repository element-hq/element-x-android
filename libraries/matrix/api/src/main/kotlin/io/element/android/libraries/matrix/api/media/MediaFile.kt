/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.media

import java.io.Closeable
import java.io.File

/**
 * A wrapper around a media file on the disk.
 * When closed the file will be removed from the disk unless [persist] has been used.
 */
interface MediaFile : Closeable {
    fun path(): String

    /**
     * Persists the temp file to the given path. The file will be moved to
     * the given path and won't be deleted anymore when closing the handle.
     */
    fun persist(path: String): Boolean
}

fun MediaFile.toFile(): File {
    return File(path())
}

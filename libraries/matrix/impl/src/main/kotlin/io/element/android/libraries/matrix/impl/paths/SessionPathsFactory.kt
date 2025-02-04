/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.paths

import io.element.android.libraries.di.CacheDirectory
import java.io.File
import java.util.UUID
import javax.inject.Inject

class SessionPathsFactory @Inject constructor(
    private val baseDirectory: File,
    @CacheDirectory private val cacheDirectory: File,
) {
    fun create(): SessionPaths {
        val subPath = UUID.randomUUID().toString()
        return SessionPaths(
            fileDirectory = File(baseDirectory, subPath),
            cacheDirectory = File(cacheDirectory, subPath),
        )
    }
}

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
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Named
import io.element.android.libraries.di.BaseDirectory

@Inject
class SessionPathsFactory(
    @Named("baseDirectory") private val baseDirectory: File,
    @Named("cacheDirectory") private val cacheDirectory: File,
) {
    fun create(): SessionPaths {
        val subPath = UUID.randomUUID().toString()
        return SessionPaths(
            fileDirectory = File(baseDirectory, subPath),
            cacheDirectory = File(cacheDirectory, subPath),
        )
    }
}

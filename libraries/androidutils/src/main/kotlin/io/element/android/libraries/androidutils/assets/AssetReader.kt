/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.androidutils.assets

import android.content.Context
import dev.zacsweers.metro.Inject
import io.element.android.libraries.di.annotations.ApplicationContext
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap

/**
 * Read asset files.
 */
@Inject
class AssetReader(
    @ApplicationContext private val context: Context,
) {
    private val cache = ConcurrentHashMap<String, String?>()

    /**
     * Read an asset from resource and return a String or null in case of error.
     *
     * @param assetFilename Asset filename
     * @return the content of the asset file, or null in case of error
     */
    fun readAssetFile(assetFilename: String): String? {
        return cache.getOrPut(assetFilename, {
            return try {
                context.assets.open(assetFilename).use { it.bufferedReader().readText() }
            } catch (e: Exception) {
                Timber.e(e, "## readAssetFile() failed")
                null
            }
        })
    }

    fun clearCache() {
        cache.clear()
    }
}

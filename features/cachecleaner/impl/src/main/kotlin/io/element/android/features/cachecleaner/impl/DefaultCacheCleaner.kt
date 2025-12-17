/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.cachecleaner.impl

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.cachecleaner.api.CacheCleaner
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.di.CacheDirectory
import io.element.android.libraries.di.annotations.AppCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

/**
 * Default implementation of [CacheCleaner].
 */
@ContributesBinding(AppScope::class)
class DefaultCacheCleaner(
    @AppCoroutineScope
    private val coroutineScope: CoroutineScope,
    private val dispatchers: CoroutineDispatchers,
    @CacheDirectory private val cacheDir: File,
) : CacheCleaner {
    companion object {
        val SUBDIRS_TO_CLEANUP = listOf("temp/media", "temp/voice")
    }

    override fun clearCache() {
        coroutineScope.launch(dispatchers.io) {
            runCatchingExceptions {
                SUBDIRS_TO_CLEANUP.forEach {
                    File(cacheDir.path, it).apply {
                        if (exists()) {
                            if (!deleteRecursively()) error("Failed to delete recursively cache directory $this")
                        }
                        if (!mkdirs()) error("Failed to create cache directory $this")
                    }
                }
            }.onFailure {
                Timber.e(it, "Failed to clear cache")
            }
        }
    }
}

/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.cachecleaner.impl

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.cachecleaner.api.CacheCleaner
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.CacheDirectory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import javax.inject.Inject

/**
 * Default implementation of [CacheCleaner].
 */
@ContributesBinding(AppScope::class)
class DefaultCacheCleaner @Inject constructor(
    private val scope: CoroutineScope,
    private val dispatchers: CoroutineDispatchers,
    @CacheDirectory private val cacheDir: File,
) : CacheCleaner {
    companion object {
        val SUBDIRS_TO_CLEANUP = listOf("temp/media", "temp/voice")
    }

    override fun clearCache() {
        scope.launch(dispatchers.io) {
            runCatching {
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

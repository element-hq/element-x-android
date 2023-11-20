/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

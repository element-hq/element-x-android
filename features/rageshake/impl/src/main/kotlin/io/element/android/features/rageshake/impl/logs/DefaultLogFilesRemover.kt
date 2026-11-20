/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rageshake.impl.logs

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.rageshake.api.logs.LogFilesRemover
import io.element.android.libraries.androidutils.file.safeDelete
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.di.CacheDirectory
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Name of the legacy directory, in the cache directory, containing the log files of all the accounts.
 * The log files are now written in another directory, see DefaultBugReporter.
 */
internal const val LEGACY_LOG_DIRECTORY_NAME = "logs"

@ContributesBinding(AppScope::class)
class DefaultLogFilesRemover(
    @CacheDirectory private val cacheDirectory: File,
    private val coroutineDispatchers: CoroutineDispatchers,
) : LogFilesRemover {
    override suspend fun perform(predicate: (File) -> Boolean) {
        withContext(coroutineDispatchers.io) {
            deleteAllFilesRecursive(File(cacheDirectory, LEGACY_LOG_DIRECTORY_NAME), predicate)
        }
    }

    private fun deleteAllFilesRecursive(
        directory: File,
        predicate: (File) -> Boolean,
    ) {
        directory.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                deleteAllFilesRecursive(file, predicate)
            } else {
                if (predicate(file)) {
                    file.safeDelete()
                }
            }
        }
    }
}

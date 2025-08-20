/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.migration.impl.migrations

import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import io.element.android.features.rageshake.api.logs.LogFilesRemover
import dev.zacsweers.metro.AppScope

/**
 * Delete the previous log files.
 */
@ContributesIntoSet(AppScope::class)
@Inject
class AppMigration07(
    private val logFilesRemover: LogFilesRemover,
) : AppMigration {
    override val order: Int = 7

    override suspend fun migrate() {
        logFilesRemover.perform { file ->
            file.name.startsWith("logs-")
        }
    }
}

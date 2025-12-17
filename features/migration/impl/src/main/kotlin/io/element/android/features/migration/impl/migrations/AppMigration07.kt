/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.migration.impl.migrations

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import io.element.android.features.rageshake.api.logs.LogFilesRemover

/**
 * Delete the previous log files.
 */
@ContributesIntoSet(AppScope::class)
@Inject
class AppMigration07(
    private val logFilesRemover: LogFilesRemover,
) : AppMigration {
    override val order: Int = 7

    override suspend fun migrate(isFreshInstall: Boolean) {
        logFilesRemover.perform { file ->
            file.name.startsWith("logs-")
        }
    }
}

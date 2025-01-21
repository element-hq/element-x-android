/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.migration.impl.migrations

import com.squareup.anvil.annotations.ContributesMultibinding
import io.element.android.features.rageshake.api.logs.LogFilesRemover
import io.element.android.libraries.di.AppScope
import javax.inject.Inject

/**
 * Remove existing logs from the device to remove any leaks of sensitive data.
 */
@ContributesMultibinding(AppScope::class)
class AppMigration01 @Inject constructor(
    private val logFilesRemover: LogFilesRemover,
) : AppMigration {
    override val order: Int = 1

    override suspend fun migrate() {
        logFilesRemover.perform()
    }
}

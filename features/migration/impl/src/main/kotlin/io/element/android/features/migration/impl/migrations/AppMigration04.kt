/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.migration.impl.migrations

import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.di.annotations.ApplicationContext

/**
 * Remove notifications.bin file, used to store notification data locally.
 */
@ContributesIntoSet(AppScope::class)
@Inject
class AppMigration04(
    @ApplicationContext private val context: Context,
) : AppMigration {
    companion object {
        internal const val NOTIFICATION_FILE_NAME = "notifications.bin"
    }
    override val order: Int = 4

    override suspend fun migrate(isFreshInstall: Boolean) {
        runCatchingExceptions { context.getDatabasePath(NOTIFICATION_FILE_NAME).delete() }
    }
}

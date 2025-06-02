/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.migration.impl.migrations

import android.content.Context
import com.squareup.anvil.annotations.ContributesMultibinding
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import javax.inject.Inject

/**
 * Remove notifications.bin file, used to store notification data locally.
 */
@ContributesMultibinding(AppScope::class)
class AppMigration04 @Inject constructor(
    @ApplicationContext private val context: Context,
) : AppMigration {
    companion object {
        internal const val NOTIFICATION_FILE_NAME = "notifications.bin"
    }
    override val order: Int = 4

    override suspend fun migrate() {
        runCatchingExceptions { context.getDatabasePath(NOTIFICATION_FILE_NAME).delete() }
    }
}

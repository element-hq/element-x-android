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

/**
 * This performs the same operation as [AppMigration01], since we need to clear the local logs again.
 */
@ContributesIntoSet(AppScope::class)
@Inject
class AppMigration03(
    private val migration01: AppMigration01,
) : AppMigration {
    override val order: Int = 3

    override suspend fun migrate(isFreshInstall: Boolean) {
        migration01.migrate(isFreshInstall)
    }
}

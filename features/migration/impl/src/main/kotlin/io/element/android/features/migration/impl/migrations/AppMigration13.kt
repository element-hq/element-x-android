/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.migration.impl.migrations

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import io.element.android.libraries.matrix.api.paths.UnusedSessionPathsCleaner

/**
 * Delete the session directories which are not used anymore, they may have been left over by previous login flows.
 */
@ContributesIntoSet(AppScope::class)
class AppMigration13(
    private val unusedSessionPathsCleaner: UnusedSessionPathsCleaner,
) : AppMigration {
    override val order: Int = 13

    override suspend fun migrate(isFreshInstall: Boolean) {
        if (isFreshInstall) return
        unusedSessionPathsCleaner.clean()
    }
}

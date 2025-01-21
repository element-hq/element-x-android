/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.migration.impl.migrations

import com.squareup.anvil.annotations.ContributesMultibinding
import io.element.android.libraries.di.AppScope
import javax.inject.Inject

/**
 * This performs the same operation as [AppMigration01], since we need to clear the local logs again.
 */
@ContributesMultibinding(AppScope::class)
class AppMigration03 @Inject constructor(
    private val migration01: AppMigration01,
) : AppMigration {
    override val order: Int = 3

    override suspend fun migrate() {
        migration01.migrate()
    }
}

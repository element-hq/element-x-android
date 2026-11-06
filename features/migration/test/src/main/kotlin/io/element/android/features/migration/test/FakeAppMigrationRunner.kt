/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.migration.test

import io.element.android.features.migration.api.AppMigrationRunner

class FakeAppMigrationRunner(
    private val hasPendingMigrationsResult: () -> Boolean = { false },
    private val runPendingMigrationsResult: suspend () -> Unit = {},
) : AppMigrationRunner {
    override suspend fun hasPendingMigrations(): Boolean = hasPendingMigrationsResult()

    override suspend fun runPendingMigrations() = runPendingMigrationsResult()
}

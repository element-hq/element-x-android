/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.migration.api

/**
 * Run the application migrations.
 * The migrations must be applied before any Matrix client is restored, since some of them can for instance move the session files.
 * Note that the process can be started without any Activity, for instance to handle a push or to run a worker.
 */
interface AppMigrationRunner {
    /**
     * Return true if some migrations have not been applied yet.
     */
    suspend fun hasPendingMigrations(): Boolean

    /**
     * Apply the pending migrations, if any.
     * It is safe to call this method several times and concurrently: it suspends until all the migrations are applied.
     */
    suspend fun runPendingMigrations()
}

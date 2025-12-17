/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.migration.impl

import kotlinx.coroutines.flow.Flow

interface MigrationStore {
    /**
     * Return of flow of the current value for application migration version.
     * If the value is not set, it will emit 0.
     * If the emitted value is lower than the current application migration version, it means
     * that a migration should occur, and at the end [setApplicationMigrationVersion] should be called.
     */
    fun applicationMigrationVersion(): Flow<Int>

    /**
     * Set the application migration version, typically after a migration has been done.
     */
    suspend fun setApplicationMigrationVersion(version: Int)
}

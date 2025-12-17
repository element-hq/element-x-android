/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.migration.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class InMemoryMigrationStore(
    initialApplicationMigrationVersion: Int = 0
) : MigrationStore {
    private val applicationMigrationVersion = MutableStateFlow(initialApplicationMigrationVersion)

    override suspend fun setApplicationMigrationVersion(version: Int) {
        applicationMigrationVersion.value = version
    }

    override fun applicationMigrationVersion(): Flow<Int> {
        return applicationMigrationVersion
    }
}

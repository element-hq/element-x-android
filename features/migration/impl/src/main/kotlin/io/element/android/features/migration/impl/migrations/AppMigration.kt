/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.migration.impl.migrations

/**
 * A migration of the application data, applied once by the [io.element.android.features.migration.api.AppMigrationRunner].
 *
 * Note: a migration must not use the MatrixClientProvider: the migrations are applied before any client is restored by it,
 * so this would cause a deadlock. If a client is needed, restore it with the MatrixAuthenticationService, and destroy it
 * after use (see [AppMigration09]).
 */
interface AppMigration {
    val order: Int
    suspend fun migrate(isFreshInstall: Boolean)
}

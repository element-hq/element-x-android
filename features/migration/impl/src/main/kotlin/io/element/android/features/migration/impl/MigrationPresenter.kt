/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.migration.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Inject
import io.element.android.features.migration.api.AppMigrationRunner
import io.element.android.features.migration.api.MigrationState
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter

@Inject
class MigrationPresenter(
    private val appMigrationRunner: AppMigrationRunner,
) : Presenter<MigrationState> {
    @Composable
    override fun present(): MigrationState {
        var migrationAction: AsyncData<Unit> by remember { mutableStateOf(AsyncData.Uninitialized) }

        LaunchedEffect(Unit) {
            if (appMigrationRunner.hasPendingMigrations()) {
                migrationAction = AsyncData.Loading(Unit)
            }
            appMigrationRunner.runPendingMigrations()
            migrationAction = AsyncData.Success(Unit)
        }

        return MigrationState(
            migrationAction = migrationAction,
        )
    }
}

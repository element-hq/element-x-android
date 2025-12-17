/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.migration.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.api.MigrationEntryPoint
import io.element.android.features.api.MigrationState

@ContributesBinding(AppScope::class)
class DefaultMigrationEntryPoint(
    private val migrationPresenter: MigrationPresenter,
) : MigrationEntryPoint {
    @Composable
    override fun present(): MigrationState = migrationPresenter.present()

    @Composable
    override fun Render(
        state: MigrationState,
        modifier: Modifier,
    ) = MigrationView(
        migrationState = state,
        modifier = modifier,
    )
}

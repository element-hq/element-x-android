/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.migration.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.api.MigrationEntryPoint
import io.element.android.features.api.MigrationState
import io.element.android.libraries.di.AppScope
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultMigrationEntryPoint @Inject constructor(
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

/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.migration.impl

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.api.MigrationState
import io.element.android.libraries.architecture.AsyncData

internal class MigrationStateProvider : PreviewParameterProvider<MigrationState> {
    override val values: Sequence<MigrationState>
        get() = sequenceOf(
            aMigrationState(),
            aMigrationState(migrationAction = AsyncData.Loading(Unit)),
        )
}

internal fun aMigrationState(
    migrationAction: AsyncData<Unit> = AsyncData.Uninitialized,
) = MigrationState(
    migrationAction = migrationAction,
)

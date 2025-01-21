/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.api

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

interface MigrationEntryPoint {
    @Composable
    fun present(): MigrationState

    @Composable
    fun Render(
        state: MigrationState,
        modifier: Modifier,
    )
}

/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.verifysession.impl.ui

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.atomic.molecules.ButtonColumnMolecule

@Composable
internal fun VerificationBottomMenu(
    modifier: Modifier = Modifier,
    buttons: @Composable ColumnScope.() -> Unit,
) {
    ButtonColumnMolecule(
        modifier = modifier.padding(bottom = 16.dp)
    ) {
        buttons()
    }
}

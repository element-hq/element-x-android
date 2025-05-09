/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CatchingPokemon
import androidx.compose.ui.tooling.preview.PreviewParameterProvider

internal class BigIconStyleProvider : PreviewParameterProvider<BigIcon.Style> {
    override val values: Sequence<BigIcon.Style>
        get() = sequenceOf(
            BigIcon.Style.Default(Icons.Filled.CatchingPokemon),
            BigIcon.Style.Alert,
            BigIcon.Style.AlertSolid,
            BigIcon.Style.Default(Icons.Filled.CatchingPokemon, useCriticalTint = true),
            BigIcon.Style.Success,
            BigIcon.Style.SuccessSolid,
            BigIcon.Style.Loading,
        )
}

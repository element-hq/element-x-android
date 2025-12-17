/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.compound.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import io.element.android.compound.previews.ColorsSchemePreview
import io.element.android.compound.tokens.generated.SemanticColors
import io.element.android.compound.tokens.generated.compoundColorsHcDark
import io.element.android.compound.tokens.generated.compoundColorsHcLight

fun SemanticColors.toMaterialColorScheme(): ColorScheme {
    return if (isLight) {
        toMaterialColorSchemeLight()
    } else {
        toMaterialColorSchemeDark()
    }
}

@Preview(heightDp = 1200)
@Composable
internal fun ColorsSchemeLightPreview() = ElementTheme {
    ColorsSchemePreview(
        Color.Black,
        Color.White,
        ElementTheme.materialColors,
    )
}

@Preview(heightDp = 1200)
@Composable
internal fun ColorsSchemeLightHcPreview() = ElementTheme(
    compoundLight = compoundColorsHcLight,
) {
    ColorsSchemePreview(
        Color.Black,
        Color.White,
        ElementTheme.materialColors,
    )
}

@Preview(heightDp = 1200)
@Composable
internal fun ColorsSchemeDarkPreview() = ElementTheme(
    darkTheme = true,
) {
    ColorsSchemePreview(
        Color.White,
        Color.Black,
        ElementTheme.materialColors,
    )
}

@Preview(heightDp = 1200)
@Composable
internal fun ColorsSchemeDarkHcPreview() = ElementTheme(
    darkTheme = true,
    compoundDark = compoundColorsHcDark,
) {
    ColorsSchemePreview(
        Color.White,
        Color.Black,
        ElementTheme.materialColors,
    )
}

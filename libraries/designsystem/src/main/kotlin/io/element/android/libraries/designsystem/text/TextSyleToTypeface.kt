/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.text

import android.graphics.Typeface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontWeight

@Composable
fun TextStyle.rememberTypeface(): State<Typeface> {
    val resolver: FontFamily.Resolver = LocalFontFamilyResolver.current
    @Suppress("UNCHECKED_CAST")
    return remember(resolver, this) {
        resolver.resolve(
            fontFamily = fontFamily,
            fontWeight = fontWeight ?: FontWeight.Normal,
            fontStyle = fontStyle ?: FontStyle.Normal,
            fontSynthesis = fontSynthesis ?: FontSynthesis.All,
        )
    } as State<Typeface>
}

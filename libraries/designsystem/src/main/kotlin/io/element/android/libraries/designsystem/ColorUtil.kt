/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import io.element.android.compound.theme.ElementTheme

@Composable
fun Boolean.toEnabledColor(): Color {
    return if (this) {
        ElementTheme.colors.textPrimary
    } else {
        ElementTheme.colors.textDisabled
    }
}

@Composable
fun Boolean.toSecondaryEnabledColor(): Color {
    return if (this) {
        ElementTheme.colors.textSecondary
    } else {
        ElementTheme.colors.textDisabled
    }
}

@Composable
fun Boolean.toIconEnabledColor(): Color {
    return if (this) {
        ElementTheme.colors.iconPrimary
    } else {
        ElementTheme.colors.iconDisabled
    }
}

@Composable
fun Boolean.toIconSecondaryEnabledColor(): Color {
    return if (this) {
        ElementTheme.colors.iconSecondary
    } else {
        ElementTheme.colors.iconDisabled
    }
}

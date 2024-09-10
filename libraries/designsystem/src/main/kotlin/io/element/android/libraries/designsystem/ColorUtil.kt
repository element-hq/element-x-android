/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun Boolean.toEnabledColor(): Color {
    return if (this) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.40f)
    }
}

@Composable
fun Boolean.toSecondaryEnabledColor(): Color {
    return if (this) {
        MaterialTheme.colorScheme.secondary
    } else {
        MaterialTheme.colorScheme.secondary.copy(alpha = 0.40f)
    }
}

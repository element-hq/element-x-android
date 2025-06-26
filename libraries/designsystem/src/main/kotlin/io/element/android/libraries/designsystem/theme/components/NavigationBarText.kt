/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.theme.components

import androidx.compose.runtime.Composable
import io.element.android.compound.theme.ElementTheme

@Composable
fun NavigationBarText(
    text: String,
) {
    Text(
        text = text,
        style = ElementTheme.typography.fontBodySmMedium,
    )
}

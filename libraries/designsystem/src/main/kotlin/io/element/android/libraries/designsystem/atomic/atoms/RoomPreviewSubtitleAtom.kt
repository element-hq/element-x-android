/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.atomic.atoms

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.theme.components.Text

@Composable
fun RoomPreviewSubtitleAtom(subtitle: String, modifier: Modifier = Modifier) {
    Text(
        modifier = modifier,
        text = subtitle,
        style = ElementTheme.typography.fontBodyLgRegular,
        textAlign = TextAlign.Center,
        color = ElementTheme.colors.textSecondary,
    )
}

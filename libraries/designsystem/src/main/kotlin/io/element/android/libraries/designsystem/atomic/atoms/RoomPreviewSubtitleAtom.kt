/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
        style = ElementTheme.typography.fontBodyMdRegular,
        textAlign = TextAlign.Center,
        color = ElementTheme.colors.textSecondary,
    )
}

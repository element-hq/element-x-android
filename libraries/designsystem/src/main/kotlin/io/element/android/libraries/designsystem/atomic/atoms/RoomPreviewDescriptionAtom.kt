/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.atomic.atoms

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.theme.components.Text

@Composable
fun RoomPreviewDescriptionAtom(description: String, modifier: Modifier = Modifier) {
    Text(
        modifier = modifier,
        text = description,
        style = ElementTheme.typography.fontBodySmRegular,
        textAlign = TextAlign.Center,
        color = ElementTheme.colors.textSecondary,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis,
    )
}

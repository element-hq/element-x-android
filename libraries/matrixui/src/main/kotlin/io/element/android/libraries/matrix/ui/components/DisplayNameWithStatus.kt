/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.api.user.DisplayedStatus
import io.element.android.libraries.matrix.ui.model.toEmojiText

/**
 * Component to display side by side a display name and an optional status.
 */
@Composable
fun DisplayNameWithStatus(
    name: String,
    status: DisplayedStatus?,
    style: TextStyle,
    modifier: Modifier = Modifier,
    nameColor: Color = Color.Unspecified,
    nameFontStyle: FontStyle? = null,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = name,
            modifier = Modifier
                .weight(1f, fill = false)
                .clipToBounds(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = style,
            color = nameColor,
            fontStyle = nameFontStyle,
        )
        if (status != null) {
            Spacer(Modifier.width(4.dp))
            Text(
                text = status.toEmojiText(),
                style = style,
                color = ElementTheme.colors.textSecondary,
            )
        }
    }
}

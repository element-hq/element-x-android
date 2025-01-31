/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.theme.components.Text

@Composable
internal fun UserRow(
    avatarData: AvatarData,
    name: String,
    subtext: String?,
    modifier: Modifier = Modifier,
    trailingContent: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .padding(start = 16.dp, top = 4.dp, end = 16.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(avatarData)
        Column(
            modifier = Modifier
                .padding(start = 12.dp)
                .weight(1f),
        ) {
            // Name
            Text(
                modifier = Modifier.clipToBounds(),
                text = name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = ElementTheme.colors.textPrimary,
                style = ElementTheme.typography.fontBodyLgRegular,
            )
            // Id
            subtext?.let {
                Text(
                    text = subtext,
                    color = ElementTheme.colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = ElementTheme.typography.fontBodySmRegular,
                )
            }
        }
        trailingContent?.invoke()
    }
}

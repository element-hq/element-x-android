/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.atomic.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text

@Composable
fun RoomPreviewMembersCountMolecule(
    memberCount: Long,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .background(color = ElementTheme.colors.bgSubtleSecondary, shape = CircleShape)
            .padding(start = 2.dp, end = 8.dp, top = 2.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = CompoundIcons.UserProfile(),
            contentDescription = null,
            tint = ElementTheme.colors.iconSecondary,
        )
        Text(
            text = "$memberCount",
            style = ElementTheme.typography.fontBodySmMedium,
            color = ElementTheme.colors.textSecondary,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun RoomPreviewMembersCountMoleculePreview() = ElementPreview {
    Column(
        modifier = Modifier.padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        RoomPreviewMembersCountMolecule(memberCount = 1)
        RoomPreviewMembersCountMolecule(memberCount = 888)
        RoomPreviewMembersCountMolecule(memberCount = 123_456)
    }
}

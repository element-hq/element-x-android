/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.atomic.atoms.PlaceholderAtom
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.placeholderBackground

/**
 * https://www.figma.com/file/0MMNu7cTOzLOlWb7ctTkv3/Element-X?node-id=6547%3A147623
 */
@Composable
internal fun RoomSummaryPlaceholderRow(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(minHeight)
            .padding(horizontal = 16.dp),
    ) {
        Box(
            modifier = Modifier
                .size(AvatarSize.RoomListItem.dp)
                .align(Alignment.CenterVertically)
                .background(color = ElementTheme.colors.placeholderBackground, shape = CircleShape)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, top = 19.dp, end = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(22.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PlaceholderAtom(width = 40.dp, height = 7.dp)
                Spacer(modifier = Modifier.width(7.dp))
                PlaceholderAtom(width = 45.dp, height = 7.dp)
                Spacer(modifier = Modifier.weight(1f))
                PlaceholderAtom(width = 22.dp, height = 4.dp)
            }
            Row(
                modifier = Modifier
                    .height(25.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PlaceholderAtom(width = 70.dp, height = 6.dp)
                Spacer(modifier = Modifier.width(6.dp))
                PlaceholderAtom(width = 70.dp, height = 6.dp)
            }
        }
    }
}

@PreviewsDayNight
@Composable
internal fun RoomSummaryPlaceholderRowPreview() = ElementPreview {
    RoomSummaryPlaceholderRow()
}

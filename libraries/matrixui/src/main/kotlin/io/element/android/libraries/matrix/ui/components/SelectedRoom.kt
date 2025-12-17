/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.LayoutDirection
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.avatar.AvatarType
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.matrix.ui.model.SelectRoomInfo
import io.element.android.libraries.matrix.ui.model.getAvatarData
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.toImmutableList

@Composable
fun SelectedRoom(
    roomInfo: SelectRoomInfo,
    onRemoveRoom: (SelectRoomInfo) -> Unit,
    modifier: Modifier = Modifier,
) {
    SelectedItem(
        avatarData = roomInfo.getAvatarData(AvatarSize.SelectedRoom),
        avatarType = AvatarType.Room(
            heroes = roomInfo.heroes.map { it.getAvatarData(AvatarSize.SelectedRoom) }.toImmutableList(),
            isTombstoned = roomInfo.isTombstoned,
        ),
        // If name is null, we do not have space to render "No room name", so just use `#` here.
        text = roomInfo.name ?: "#",
        maxLines = 1,
        a11yContentDescription = roomInfo.name
            ?: roomInfo.canonicalAlias?.value
            ?: stringResource(id = CommonStrings.common_room_name),
        canRemove = true,
        onRemoveClick = { onRemoveRoom(roomInfo) },
        modifier = modifier,
    )
}

@PreviewsDayNight
@Composable
internal fun SelectedRoomPreview(
    @PreviewParameter(SelectRoomInfoProvider::class) roomInfo: SelectRoomInfo
) = ElementPreview {
    SelectedRoom(
        roomInfo = roomInfo,
        onRemoveRoom = {},
    )
}

@PreviewsDayNight
@Composable
internal fun SelectedRoomRtlPreview(
    @PreviewParameter(SelectRoomInfoProvider::class) roomInfo: SelectRoomInfo
) = CompositionLocalProvider(
    LocalLayoutDirection provides LayoutDirection.Rtl,
) {
    ElementPreview {
        SelectedRoom(
            roomInfo = roomInfo,
            onRemoveRoom = {},
        )
    }
}

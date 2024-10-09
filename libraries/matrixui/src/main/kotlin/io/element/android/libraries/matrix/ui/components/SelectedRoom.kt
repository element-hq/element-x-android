/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.avatar.CompositeAvatar
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Surface
import io.element.android.libraries.designsystem.theme.components.Text
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
    Box(
        modifier = modifier
            .width(56.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CompositeAvatar(
                avatarData = roomInfo.getAvatarData(AvatarSize.SelectedRoom),
                heroes = roomInfo.heroes.map { it.getAvatarData(AvatarSize.SelectedRoom) }.toImmutableList(),
            )
            Text(
                // If name is null, we do not have space to render "No room name", so just use `#` here.
                text = roomInfo.name ?: "#",
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        Surface(
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                    .clip(CircleShape)
                    .size(20.dp)
                    .align(Alignment.TopEnd)
                    .clickable(
                            indication = ripple(),
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = { onRemoveRoom(roomInfo) }
                    ),
        ) {
            Icon(
                imageVector = CompoundIcons.Close(),
                contentDescription = stringResource(id = CommonStrings.action_remove),
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(2.dp)
            )
        }
    }
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

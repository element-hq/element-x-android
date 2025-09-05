/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.components

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.atomic.atoms.UnreadIndicatorAtom
import io.element.android.libraries.designsystem.atomic.molecules.InviteButtonsRowMolecule
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.avatar.AvatarType
import io.element.android.libraries.designsystem.modifiers.onKeyboardContextMenuAction
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.unreadIndicator
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.room.join.JoinRule
import io.element.android.libraries.matrix.api.spaces.SpaceRoom
import io.element.android.libraries.matrix.ui.model.getAvatarData
import io.element.android.libraries.ui.strings.CommonPlurals
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun SpaceRoomItemView(
    spaceRoom: SpaceRoom,
    showUnreadIndicator: Boolean,
    hideAvatars: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SpaceRoomItemScaffold(
        modifier = modifier,
        avatarData = spaceRoom.getAvatarData(AvatarSize.SpaceListItem),
        isSpace = spaceRoom.isSpace,
        hideAvatars = hideAvatars,
        onClick = onClick,
        onLongClick = onLongClick,
    ) {
        NameAndIndicatorRow(
            name = spaceRoom.name,
            showIndicator = showUnreadIndicator
        )
        Spacer(modifier = Modifier.height(1.dp))
        SubtitleRow(
            visibilityIcon = spaceRoom.visibilityIcon(),
            subtitle = spaceRoom.subtitle()
        )
        Spacer(modifier = Modifier.height(1.dp))
        Text(
            modifier = Modifier.weight(1f),
            style = ElementTheme.typography.fontBodyMdRegular,
            text = spaceRoom.info(),
            fontStyle = FontStyle.Italic.takeIf { spaceRoom.name == null },
            color = ElementTheme.colors.textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (spaceRoom.state == CurrentUserMembership.INVITED) {
            Spacer(modifier = Modifier.height(12.dp))
            InviteButtonsRowMolecule(
                onAcceptClick = {},
                onDeclineClick = {},
            )
        }
    }
}

@Composable
private fun SubtitleRow(
    visibilityIcon: ImageVector?,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (visibilityIcon != null) {
            Icon(
                modifier = Modifier
                    .size(16.dp)
                    .padding(end = 4.dp),
                imageVector = visibilityIcon,
                contentDescription = null,
                tint = ElementTheme.colors.iconTertiary,
            )
        }
        Text(
            modifier = Modifier.weight(1f),
            style = ElementTheme.typography.fontBodyMdRegular,
            text = subtitle,
            color = ElementTheme.colors.textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun NameAndIndicatorRow(
    name: String?,
    showIndicator: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.weight(1f),
            style = ElementTheme.typography.fontBodyLgMedium,
            text = name ?: stringResource(id = CommonStrings.common_no_room_name),
            fontStyle = FontStyle.Italic.takeIf { name == null },
            color = ElementTheme.colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (showIndicator) {
            UnreadIndicatorAtom(
                color = ElementTheme.colors.unreadIndicator
            )
        }
    }
}

@Composable
private fun SpaceRoomItemScaffold(
    avatarData: AvatarData,
    isSpace: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    hideAvatars: Boolean,
    content: @Composable ColumnScope.() -> Unit
) {
    val clickModifier = Modifier
        .combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick,
            onLongClickLabel = stringResource(CommonStrings.action_open_context_menu),
            indication = ripple(),
            interactionSource = remember { MutableInteractionSource() }
        )
        .onKeyboardContextMenuAction { onLongClick }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(clickModifier)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(IntrinsicSize.Min),
    ) {
        Avatar(
            avatarData = avatarData,
            avatarType = if (isSpace) AvatarType.Space() else AvatarType.Room(),
            hideImage = hideAvatars,
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f),
            content = content,
        )
    }
}

@Composable
@ReadOnlyComposable
private fun SpaceRoom.subtitle(): String {
    return if (isSpace) {
        if (joinRule == JoinRule.Public) {
            stringResource(CommonStrings.common_public_space)
        } else {
            stringResource(CommonStrings.common_private_space)
        }
    } else {
        pluralStringResource(CommonPlurals.common_member_count, numJoinedMembers, numJoinedMembers)
    }
}

@Composable
@ReadOnlyComposable
private fun SpaceRoom.info(): String {
    return if (isSpace) {
        stringResource(
            CommonStrings.screen_space_list_details,
            pluralStringResource(CommonPlurals.common_rooms, childrenCount, childrenCount),
            pluralStringResource(CommonPlurals.common_member_count, numJoinedMembers, numJoinedMembers),
        )
    } else {
        topic.orEmpty()
    }
}

@Composable
private fun SpaceRoom.visibilityIcon(): ImageVector? {
    return if (joinRule == JoinRule.Public) {
        CompoundIcons.Public()
    } else {
        CompoundIcons.LockSolid()
    }
}

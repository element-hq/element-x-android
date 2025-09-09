/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.spaces

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.atomic.atoms.UnreadIndicatorAtom
import io.element.android.libraries.designsystem.atomic.molecules.InviteButtonsRowMolecule
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.avatar.AvatarType
import io.element.android.libraries.designsystem.modifiers.onKeyboardContextMenuAction
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.unreadIndicator
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.spaces.SpaceRoom
import io.element.android.libraries.matrix.ui.model.getAvatarData
import io.element.android.libraries.ui.strings.CommonPlurals
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
internal fun HomeSpaceItemView(
    spaceRoom: SpaceRoom,
    showUnreadIndicator: Boolean,
    hideAvatars: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SpaceScaffoldRow(
        modifier = modifier,
        spaceRoom = spaceRoom,
        onClick = onClick,
        hideAvatars = hideAvatars,
        onLongClick = { },
    ) {
        NameAndIndicatorRow(
            name = spaceRoom.name,
            showIndicator = showUnreadIndicator,
        )
        Spacer(modifier = Modifier.height(1.dp))
        if (!spaceRoom.worldReadable) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    modifier = Modifier
                        .size(16.dp)
                        .padding(end = 4.dp),
                    imageVector = CompoundIcons.LockSolid(),
                    contentDescription = null,
                    tint = ElementTheme.colors.iconTertiary,
                )
                Text(
                    modifier = Modifier.weight(1f),
                    style = ElementTheme.typography.fontBodyMdRegular,
                    text = stringResource(CommonStrings.common_private_space),
                    fontStyle = FontStyle.Italic.takeIf { spaceRoom.name == null },
                    color = ElementTheme.colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(1.dp))
        }
        val spaceSummary = stringResource(
            CommonStrings.screen_space_list_details,
            pluralStringResource(CommonPlurals.common_rooms, spaceRoom.childrenCount, spaceRoom.childrenCount),
            pluralStringResource(CommonPlurals.common_member_count, spaceRoom.numJoinedMembers, spaceRoom.numJoinedMembers),
        )
        Text(
            modifier = Modifier.weight(1f),
            style = ElementTheme.typography.fontBodyMdRegular,
            text = spaceSummary,
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
private fun SpaceScaffoldRow(
    spaceRoom: SpaceRoom,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    hideAvatars: Boolean,
    modifier: Modifier = Modifier,
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
            avatarData = spaceRoom.getAvatarData(AvatarSize.SpaceListItem),
            avatarType = AvatarType.Space(),
            hideImage = hideAvatars,
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f),
            content = content,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun HomeSpaceItemViewPreview(@PreviewParameter(SpaceRoomProvider::class) spaceRoom: SpaceRoom) = ElementPreview {
    HomeSpaceItemView(
        spaceRoom = spaceRoom,
        showUnreadIndicator = false,
        hideAvatars = true,
        onClick = {},
    )
}

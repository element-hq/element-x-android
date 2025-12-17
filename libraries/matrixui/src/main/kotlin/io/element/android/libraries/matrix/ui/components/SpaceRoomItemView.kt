/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.components

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.atomic.atoms.UnreadIndicatorAtom
import io.element.android.libraries.designsystem.atomic.molecules.InviteButtonsRowMolecule
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarData
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
import io.element.android.libraries.matrix.api.spaces.SpaceRoomVisibility
import io.element.android.libraries.matrix.ui.model.getAvatarData
import io.element.android.libraries.matrix.ui.model.icon
import io.element.android.libraries.matrix.ui.model.label
import io.element.android.libraries.ui.strings.CommonPlurals
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

/**
 * Figma reference: https://www.figma.com/design/G1xy0HDZKJf5TCRFmKb5d5/Compound-Android-Components?node-id=3643-2079&m=dev
 */
@Composable
fun SpaceRoomItemView(
    spaceRoom: SpaceRoom,
    showUnreadIndicator: Boolean,
    hideAvatars: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingAction: @Composable (() -> Unit)? = null,
    bottomAction: @Composable (() -> Unit)? = null,
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
    Column(
        modifier = modifier
            .then(clickModifier)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        SpaceRoomItemScaffold(
            avatarData = spaceRoom.getAvatarData(AvatarSize.SpaceListItem),
            isSpace = spaceRoom.isSpace,
            hideAvatars = hideAvatars,
            heroes = spaceRoom.heroes
                .map { hero -> hero.getAvatarData(AvatarSize.SpaceListItem) }
                .toImmutableList(),
            trailingAction = trailingAction,
        ) {
            NameAndIndicatorRow(
                name = spaceRoom.displayName,
                showIndicator = showUnreadIndicator
            )
            Spacer(modifier = Modifier.height(1.dp))
            SubtitleRow(
                visibilityIcon = spaceRoom.visibilityIcon(),
                subtitle = spaceRoom.subtitle()
            )
            Spacer(modifier = Modifier.height(1.dp))
            val info = spaceRoom.info()
            if (info.isNotBlank()) {
                Text(
                    modifier = Modifier.weight(1f),
                    style = ElementTheme.typography.fontBodyMdRegular,
                    text = info,
                    color = ElementTheme.colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (bottomAction != null) {
            Spacer(modifier = Modifier.height(12.dp))
            // Match the padding of the text content (avatar + spacer)
            Box(modifier = Modifier.padding(start = AvatarSize.SpaceListItem.dp + 16.dp)) {
                bottomAction()
            }
            Spacer(modifier = Modifier.height(4.dp))
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
    name: String,
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
            text = name,
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
    heroes: ImmutableList<AvatarData>,
    hideAvatars: Boolean,
    modifier: Modifier = Modifier,
    trailingAction: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(
            avatarData = avatarData,
            avatarType = if (isSpace) AvatarType.Space() else AvatarType.Room(heroes = heroes),
            hideImage = hideAvatars,
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f),
            content = content,
        )
        if (trailingAction != null) {
            Spacer(modifier = Modifier.width(16.dp))
            trailingAction()
        }
    }
}

@Composable
@ReadOnlyComposable
private fun SpaceRoom.subtitle(): String {
    return if (isSpace) {
        visibility.label
    } else {
        pluralStringResource(CommonPlurals.common_member_count, numJoinedMembers, numJoinedMembers)
    }
}

@Composable
@ReadOnlyComposable
private fun SpaceRoom.info(): String {
    return if (isSpace) {
        pluralStringResource(CommonPlurals.common_member_count, numJoinedMembers, numJoinedMembers)
    } else {
        topic.orEmpty()
    }
}

@Composable
private fun SpaceRoom.visibilityIcon(): ImageVector? {
    // Don't show any icon for restricted rooms as it's the default and would add noise
    return if (visibility == SpaceRoomVisibility.Restricted) {
        null
    } else {
        visibility.icon
    }
}

@Composable
@PreviewsDayNight
internal fun SpaceRoomItemViewPreview(@PreviewParameter(SpaceRoomProvider::class) spaceRoom: SpaceRoom) = ElementPreview {
    SpaceRoomItemView(
        spaceRoom = spaceRoom,
        showUnreadIndicator = spaceRoom.state == CurrentUserMembership.INVITED,
        hideAvatars = false,
        onClick = {},
        onLongClick = {},
        bottomAction = if (spaceRoom.state == CurrentUserMembership.INVITED) {
            { InviteButtonsRowMolecule({}, {}) }
        } else {
            null
        },
        trailingAction = when (spaceRoom.state) {
            null, CurrentUserMembership.LEFT -> {
                {
                    JoinButton(
                        showProgress = spaceRoom.state == CurrentUserMembership.LEFT,
                        onClick = { },
                    )
                }
            }
            else -> null
        }
    )
}

/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.topbars

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.messages.impl.SharedHistoryIcon
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.avatar.anAvatarData
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.api.encryption.identity.IdentityState
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MessagesViewTopBar(
    roomName: String?,
    roomAvatar: AvatarData,
    isTombstoned: Boolean,
    heroes: ImmutableList<AvatarData>,
    dmUserIdentityState: IdentityState?,
    sharedHistoryIcon: SharedHistoryIcon,
    onRoomDetailsClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    menuActions: @Composable RowScope.() -> Unit = {},
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        navigationIcon = {
            BackButton(onClick = onBackClick)
        },
        title = {
            Row(
                modifier = Modifier.clickable { onRoomDetailsClick() },
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .semantics {
                            heading()
                        },
                    text = roomName ?: stringResource(CommonStrings.common_no_room_name),
                    style = ElementTheme.typography.fontBodyLgMedium,
                    fontStyle = FontStyle.Italic.takeIf { roomName == null },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                when (dmUserIdentityState) {
                    IdentityState.Verified -> {
                        Icon(
                            imageVector = CompoundIcons.Verified(),
                            tint = ElementTheme.colors.iconSuccessPrimary,
                            contentDescription = null,
                        )
                    }
                    IdentityState.VerificationViolation -> {
                        Icon(
                            imageVector = CompoundIcons.ErrorSolid(),
                            tint = ElementTheme.colors.iconCriticalPrimary,
                            contentDescription = null,
                        )
                    }
                    else -> Unit
                }

                when (sharedHistoryIcon) {
                    SharedHistoryIcon.NONE -> Unit
                    SharedHistoryIcon.SHARED -> Icon(
                        imageVector = CompoundIcons.History(),
                        tint = ElementTheme.colors.iconInfoPrimary,
                        contentDescription = stringResource(CommonStrings.common_shared_history),
                    )
                    SharedHistoryIcon.WORLD_READABLE -> Icon(
                        imageVector = CompoundIcons.UserProfileSolid(),
                        tint = ElementTheme.colors.iconInfoPrimary,
                        contentDescription = stringResource(CommonStrings.common_world_readable_history),
                    )
                }
            }
        },
        actions = menuActions,
        colors = TopAppBarDefaults.topAppBarColors(),
        windowInsets = WindowInsets(0.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@PreviewsDayNight
@Composable
internal fun MessagesViewTopBarPreview() = ElementPreview {
    @Composable
    fun AMessagesViewTopBar(
        roomName: String? = "Room name",
        roomAvatar: AvatarData = anAvatarData(
            name = "Room name",
            size = AvatarSize.TimelineRoom,
        ),
        isTombstoned: Boolean = false,
        heroes: ImmutableList<AvatarData> = persistentListOf(),
        dmUserIdentityState: IdentityState? = null,
        sharedHistoryIcon: SharedHistoryIcon = SharedHistoryIcon.NONE,
    ) = MessagesViewTopBar(
        roomName = roomName,
        roomAvatar = roomAvatar,
        isTombstoned = isTombstoned,
        heroes = heroes,
        dmUserIdentityState = dmUserIdentityState,
        sharedHistoryIcon = sharedHistoryIcon,
        onRoomDetailsClick = {},
        onBackClick = {},
    )
    Column {
        AMessagesViewTopBar()
        Spacer(Modifier.height(8.dp))
        AMessagesViewTopBar(
            roomName = null,
        )
        Spacer(Modifier.height(8.dp))
        AMessagesViewTopBar(
            roomName = "A DM with a very very very long name",
            dmUserIdentityState = IdentityState.Verified
        )
        Spacer(Modifier.height(8.dp))
        AMessagesViewTopBar(
            roomName = "A DM with a very very very long name",
            isTombstoned = true,
            dmUserIdentityState = IdentityState.VerificationViolation
        )
        Spacer(Modifier.height(8.dp))
        AMessagesViewTopBar(
            roomName = "A DM with shared history",
            dmUserIdentityState = IdentityState.Verified,
            sharedHistoryIcon = SharedHistoryIcon.SHARED,
        )
        Spacer(Modifier.height(8.dp))
        AMessagesViewTopBar(
            roomName = "A room with world_readable history",
            sharedHistoryIcon = SharedHistoryIcon.WORLD_READABLE,
        )
    }
}

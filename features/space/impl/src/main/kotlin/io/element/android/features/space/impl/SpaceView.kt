/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.space.impl

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.avatar.AvatarType
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.spaces.SpaceRoom
import io.element.android.libraries.matrix.ui.components.SpaceHeaderRootView
import io.element.android.libraries.matrix.ui.components.SpaceHeaderView
import io.element.android.libraries.matrix.ui.components.SpaceRoomItemView
import io.element.android.libraries.matrix.ui.model.getAvatarData
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.toImmutableList

@Composable
fun SpaceView(
    state: SpaceState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            SpaceViewTopBar(spaceRoom = null, onBackClick = onBackClick)
        },
        content = { padding ->
            Box(
                modifier = Modifier
                    .padding(padding)
                    .consumeWindowInsets(padding)
            ) {
                SpaceViewContent(state)
            }
        },
    )
}


@Composable
private fun SpaceViewContent(
    state: SpaceState,
    modifier: Modifier = Modifier,
){
    LazyColumn(modifier) {
        val parentSpace = state.parentSpace
        if (parentSpace != null) {
            item {
                SpaceHeaderView(
                    avatarData = parentSpace.getAvatarData(AvatarSize.SpaceHeader),
                    name = parentSpace.name,
                    topic = parentSpace.topic,
                    joinRule = parentSpace.joinRule,
                    heroes = parentSpace.heroes.toImmutableList(),
                    numberOfMembers = parentSpace.numJoinedMembers,
                    numberOfRooms = parentSpace.childrenCount,
                )
            }
        }
        state.children.forEach {
            item(it.roomId) {
                val isInvitation = it.state == CurrentUserMembership.INVITED
                SpaceRoomItemView(
                    spaceRoom = it,
                    showUnreadIndicator = isInvitation && it.roomId !in state.seenSpaceInvites,
                    hideAvatars = isInvitation && state.hideInvitesAvatar,
                    onClick = {

                    },
                    onLongClick = {

                    }
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SpaceViewTopBar(
    spaceRoom: SpaceRoom?,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        modifier = modifier,
        navigationIcon = {
            BackButton(onClick = onBackClick)
        },
        title = {
            if (spaceRoom != null) {
                SpaceAvatarAndNameRow(
                    name = spaceRoom.name,
                    avatarData = spaceRoom.getAvatarData(AvatarSize.TimelineRoom),
                )
            }
        },
        actions = {
        },
        windowInsets = WindowInsets(0.dp)
    )
}

@Composable
private fun SpaceAvatarAndNameRow(
    name: String?,
    avatarData: AvatarData,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(
            avatarData = avatarData,
            avatarType = AvatarType.Space(),
        )
        Text(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .semantics {
                    heading()
                },
            text = name ?: stringResource(CommonStrings.common_no_room_name),
            style = ElementTheme.typography.fontBodyLgMedium,
            fontStyle = FontStyle.Italic.takeIf { name == null },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@PreviewsDayNight
@Composable
internal fun SpaceViewPreview(
    @PreviewParameter(SpaceStateProvider::class) state: SpaceState
) = ElementPreview {
    SpaceView(
        state = state,
        onBackClick = {},
    )
}

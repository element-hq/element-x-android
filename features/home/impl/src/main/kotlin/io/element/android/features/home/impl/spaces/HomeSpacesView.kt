/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.spaces

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.ui.components.SpaceHeaderRootView
import io.element.android.libraries.matrix.ui.components.SpaceHeaderView
import io.element.android.libraries.matrix.ui.components.SpaceRoomItemView
import io.element.android.libraries.matrix.ui.model.getAvatarData
import kotlinx.collections.immutable.toImmutableList

@Composable
fun HomeSpacesView(
    state: HomeSpacesState,
    onSpaceClick: (RoomId) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier) {
        val space = state.space
        when (space) {
            CurrentSpace.Root -> {
                item {
                    SpaceHeaderRootView(
                        numberOfSpaces = state.spaceRooms.size,
                        // TODO
                        numberOfRooms = 0,
                    )
                }
            }
            is CurrentSpace.Space -> item {
                SpaceHeaderView(
                    avatarData = space.spaceRoom.getAvatarData(AvatarSize.SpaceHeader),
                    name = space.spaceRoom.name,
                    topic = space.spaceRoom.topic,
                    joinRule = space.spaceRoom.joinRule,
                    heroes = space.spaceRoom.heroes.toImmutableList(),
                    numberOfMembers = space.spaceRoom.numJoinedMembers,
                    numberOfRooms = space.spaceRoom.childrenCount,
                )
            }
        }
        state.spaceRooms.forEach { spaceRoom ->
            item(spaceRoom.roomId) {
                val isInvitation = spaceRoom.state == CurrentUserMembership.INVITED
                SpaceRoomItemView(
                    spaceRoom = spaceRoom,
                    showUnreadIndicator = isInvitation && spaceRoom.roomId !in state.seenSpaceInvites,
                    hideAvatars = isInvitation && state.hideInvitesAvatar,
                    onClick = {
                        onSpaceClick(spaceRoom.roomId)
                    },
                    onLongClick = {
                        // TODO
                    }
                )
            }
        }
    }
}

@PreviewsDayNight
@Composable
internal fun HomeSpacesViewPreview(
    @PreviewParameter(HomeSpacesStateProvider::class) state: HomeSpacesState,
) = ElementPreview {
    HomeSpacesView(
        state = state,
        onSpaceClick = {},
        modifier = Modifier,
    )
}

/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.spaces

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.HorizontalDivider
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
    lazyListState: LazyListState,
    onSpaceClick: (RoomId) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        state = lazyListState
    ) {
        val space = state.space
        when (space) {
            CurrentSpace.Root -> {
                item {
                    SpaceHeaderRootView(numberOfSpaces = state.spaceRooms.size)
                }
            }
            is CurrentSpace.Space -> item {
                SpaceHeaderView(
                    avatarData = space.spaceRoom.getAvatarData(AvatarSize.SpaceHeader),
                    name = space.spaceRoom.displayName,
                    topic = space.spaceRoom.topic,
                    visibility = space.spaceRoom.visibility,
                    heroes = space.spaceRoom.heroes.toImmutableList(),
                    numberOfMembers = space.spaceRoom.numJoinedMembers,
                )
            }
        }
        item {
            HorizontalDivider()
        }
        itemsIndexed(
            items = state.spaceRooms,
            key = { _, spaceRoom -> spaceRoom.roomId }
        ) { index, spaceRoom ->
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
                },
            )
            if (index != state.spaceRooms.lastIndex) {
                HorizontalDivider()
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
        lazyListState = rememberLazyListState(),
        onSpaceClick = {},
        modifier = Modifier,
    )
}

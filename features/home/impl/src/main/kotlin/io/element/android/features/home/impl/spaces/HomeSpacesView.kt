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
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.matrix.api.core.SpaceId
import io.element.android.libraries.matrix.api.room.CurrentUserMembership

@Composable
fun HomeSpacesView(
    state: HomeSpacesState,
    onSpaceClick: (SpaceId) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier) {
        state.spaceRooms.forEach {
            item(it.spaceId) {
                val isInvitation = it.state == CurrentUserMembership.INVITED
                HomeSpaceItemView(
                    spaceRoom = it,
                    showUnreadIndicator = isInvitation && it.spaceId !in state.seenSpaceInvites,
                    hideAvatars = isInvitation && state.hideInvitesAvatar,
                    onClick = {
                        onSpaceClick(it.spaceId)
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

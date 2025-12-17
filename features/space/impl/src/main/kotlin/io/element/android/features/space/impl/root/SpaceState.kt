/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.space.impl.root

import androidx.compose.runtime.Immutable
import io.element.android.features.invite.api.acceptdecline.AcceptDeclineInviteState
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.spaces.SpaceRoom
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.ImmutableSet

data class SpaceState(
    val currentSpace: SpaceRoom?,
    val children: ImmutableList<SpaceRoom>,
    val seenSpaceInvites: ImmutableSet<RoomId>,
    val hideInvitesAvatar: Boolean,
    val hasMoreToLoad: Boolean,
    val joinActions: ImmutableMap<RoomId, AsyncAction<Unit>>,
    val acceptDeclineInviteState: AcceptDeclineInviteState,
    val topicViewerState: TopicViewerState,
    val canAccessSpaceSettings: Boolean,
    val eventSink: (SpaceEvents) -> Unit
) {
    fun isJoining(spaceId: RoomId): Boolean = joinActions[spaceId] == AsyncAction.Loading
    val hasAnyFailure: Boolean = joinActions.values.any {
        it is AsyncAction.Failure
    }
}

@Immutable
sealed interface TopicViewerState {
    data object Hidden : TopicViewerState
    data class Shown(val topic: String) : TopicViewerState
}

/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.members.details

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.features.userprofile.api.UserProfilePresenterFactory
import io.element.android.features.userprofile.api.UserProfileState
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.ui.room.getRoomMemberAsState

/**
 * Presenter for room member details screen.
 * Rely on UserProfilePresenter, but override some fields with room member info when available.
 */
class RoomMemberDetailsPresenter @AssistedInject constructor(
    @Assisted private val roomMemberId: UserId,
    private val room: MatrixRoom,
    userProfilePresenterFactory: UserProfilePresenterFactory,
) : Presenter<UserProfileState> {
    interface Factory {
        fun create(roomMemberId: UserId): RoomMemberDetailsPresenter
    }

    private val userProfilePresenter = userProfilePresenterFactory.create(roomMemberId)

    @Composable
    override fun present(): UserProfileState {
        val roomMember by room.getRoomMemberAsState(roomMemberId)
        LaunchedEffect(Unit) {
            // Update room member info when opening this screen
            // We don't need to assign the result as it will be automatically propagated by `room.getRoomMemberAsState`
            room.getUpdatedMember(roomMemberId)
        }

        val roomUserName: String? by produceState(
            initialValue = roomMember?.displayName,
            key1 = roomMember,
        ) {
            value = room.userDisplayName(roomMemberId).getOrNull() ?: roomMember?.displayName
        }

        val roomUserAvatar: String? by produceState(
            initialValue = roomMember?.avatarUrl,
            key1 = roomMember,
        ) {
            value = room.userAvatarUrl(roomMemberId).getOrNull() ?: roomMember?.avatarUrl
        }

        val userProfileState = userProfilePresenter.present()

        return userProfileState.copy(
            userName = roomUserName ?: userProfileState.userName,
            avatarUrl = roomUserAvatar ?: userProfileState.avatarUrl,
        )
    }
}

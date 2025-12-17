/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.room

import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.encryption.identity.IdentityState
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.roomMembers
import io.element.android.libraries.matrix.ui.model.getAvatarData
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow

@OptIn(ExperimentalCoroutinesApi::class)
fun JoinedRoom.roomMemberIdentityStateChange(waitForEncryption: Boolean): Flow<ImmutableList<RoomMemberIdentityStateChange>> {
    val encryptionChangeFlow = flow {
        if (waitForEncryption) {
            // Room cannot become unencrypted, so it's ok to use first here
            roomInfoFlow.first { roomInfo -> roomInfo.isEncrypted == true }
        }
        emit(Unit)
    }
    return encryptionChangeFlow
        .flatMapLatest {
            combine(identityStateChangesFlow, membersStateFlow) { identityStateChanges, membersState ->
                identityStateChanges.map { identityStateChange ->
                    val member = membersState.roomMembers()
                        ?.find { roomMember -> roomMember.userId == identityStateChange.userId }
                        ?.toIdentityRoomMember()
                        ?: createDefaultRoomMemberForIdentityChange(identityStateChange.userId)
                    RoomMemberIdentityStateChange(
                        identityRoomMember = member,
                        identityState = identityStateChange.identityState,
                    )
                }.toImmutableList()
            }.distinctUntilChanged()
        }
}

private fun RoomMember.toIdentityRoomMember() = IdentityRoomMember(
    userId = userId,
    displayNameOrDefault = displayNameOrDefault,
    avatarData = getAvatarData(AvatarSize.ComposerAlert),
)

private fun createDefaultRoomMemberForIdentityChange(userId: UserId) = IdentityRoomMember(
    userId = userId,
    displayNameOrDefault = userId.extractedDisplayName,
    avatarData = AvatarData(
        id = userId.value,
        name = null,
        url = null,
        size = AvatarSize.ComposerAlert,
    ),
)

data class RoomMemberIdentityStateChange(
    val identityRoomMember: IdentityRoomMember,
    val identityState: IdentityState,
)

data class IdentityRoomMember(
    val userId: UserId,
    val displayNameOrDefault: String,
    val avatarData: AvatarData,
)

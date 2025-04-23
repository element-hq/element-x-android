/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.room

import androidx.compose.runtime.ProduceStateScope
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.encryption.identity.IdentityState
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.roomMembers
import io.element.android.libraries.matrix.ui.model.getAvatarData
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@OptIn(ExperimentalCoroutinesApi::class)
fun JoinedRoom.roomMemberIdentityStateChange(): Flow<ImmutableList<RoomMemberIdentityStateChange>> {
    return roomInfoFlow
        .filter {
            // Room cannot become unencrypted, so we can just apply a filter here.
            it.isEncrypted == true
        }
        .distinctUntilChanged()
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
                }.toPersistentList()
            }.distinctUntilChanged()
        }
}

fun ProduceStateScope<PersistentList<RoomMemberIdentityStateChange>>.observeRoomMemberIdentityStateChange(room: JoinedRoom) {
    room.roomMemberIdentityStateChange()
        .onEach { roomMemberIdentityStateChanges ->
            value = roomMemberIdentityStateChanges.toPersistentList()
        }
        .launchIn(this)
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

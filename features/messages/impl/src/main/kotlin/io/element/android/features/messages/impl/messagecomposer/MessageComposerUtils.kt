/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.messagecomposer

import androidx.compose.runtime.ProduceStateScope
import io.element.android.features.messages.impl.crypto.identity.IdentityRoomMember
import io.element.android.features.messages.impl.crypto.identity.RoomMemberIdentityStateChange
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.roomMembers
import io.element.android.libraries.matrix.ui.model.getAvatarData
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@OptIn(ExperimentalCoroutinesApi::class)
fun ProduceStateScope<PersistentList<RoomMemberIdentityStateChange>>.observeRoomMemberIdentityStateChange(room: MatrixRoom) {
    room.syncUpdateFlow
        .filter {
            // Room cannot become unencrypted, so we can just apply a filter here.
            room.isEncrypted
        }
        .distinctUntilChanged()
        .flatMapLatest {
            combine(room.identityStateChangesFlow, room.membersStateFlow) { identityStateChanges, membersState ->
                identityStateChanges.map { identityStateChange ->
                    val member = membersState.roomMembers()
                        ?.firstOrNull { roomMember -> roomMember.userId == identityStateChange.userId }
                        ?.toIdentityRoomMember()
                        ?: createDefaultRoomMemberForIdentityChange(identityStateChange.userId)
                    RoomMemberIdentityStateChange(
                        identityRoomMember = member,
                        identityState = identityStateChange.identityState,
                    )
                }
            }
                .distinctUntilChanged()
                .onEach { roomMemberIdentityStateChanges ->
                    value = roomMemberIdentityStateChanges.toPersistentList()
                }
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

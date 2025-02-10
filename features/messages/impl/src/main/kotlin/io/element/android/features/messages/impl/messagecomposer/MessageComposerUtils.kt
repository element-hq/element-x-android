/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.messagecomposer

import androidx.compose.runtime.ProduceStateScope
import io.element.android.features.messages.impl.crypto.identity.RoomMemberIdentityStateChange
import io.element.android.features.messages.impl.crypto.identity.createDefaultRoomMemberForIdentityChange
import io.element.android.features.messages.impl.crypto.identity.toIdentityRoomMember
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.roomMembers
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

/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.callnative.impl

import io.element.android.call.api.ElementCallRoomContext
import io.element.android.call.api.ElementCallRoomContextProvider
import io.element.android.call.api.ElementCallRoomMember
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.room.RoomMembersState
import io.element.android.libraries.matrix.api.room.roomMembers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import timber.log.Timber
import io.element.android.call.api.rtc.id.RoomId as ElementCallRoomId
import io.element.android.call.api.rtc.id.UserId as ElementCallUserId
import io.element.android.libraries.matrix.api.core.RoomId as MatrixRoomId

/**
 * Tells the call what the room is called and who is in it.
 *
 * The RTC layer knows a participant only as a member id and a user id - it has no reason to know
 * what anyone is called or what they look like. Element X does, so the join happens here rather than
 * in the call UI, and it goes through Element X's room cache so that names and avatars are the same
 * ones the rest of the app shows. The library ships a provider of its own that reads the SDK
 * directly; it is not used, because it would miss exactly that cache.
 */
class ElementXRoomContextProvider(
    private val matrixClient: MatrixClient,
) : ElementCallRoomContextProvider {
    override fun roomContext(roomId: ElementCallRoomId): Flow<ElementCallRoomContext> = flow {
        val room = matrixClient.getRoom(MatrixRoomId(roomId.value))
        if (room == null) {
            // Emits nothing rather than guessing: the call then shows user ids, which is better than
            // a name we made up.
            Timber.w("NativeCall: no room for ${roomId.value}, the call will show user ids")
            return@flow
        }
        room.use {
            coroutineScope {
                // The member list is loaded lazily, and a call is exactly the moment it is needed:
                // without this the tiles would show user ids until something else happened to ask.
                // Launched rather than awaited so the room name still arrives immediately.
                launch { room.updateMembers() }
                emitAll(
                    combine(room.roomInfoFlow, room.membersStateFlow) { info, membersState ->
                        ElementCallRoomContext(
                            displayName = info.name,
                            isDm = info.isDm,
                            members = membersState.toElementCallMembers(),
                        )
                    }
                )
            }
        }
    }

    private fun RoomMembersState.toElementCallMembers(): Map<ElementCallUserId, ElementCallRoomMember> =
        roomMembers().orEmpty().associate { member ->
            ElementCallUserId(member.userId.value) to ElementCallRoomMember(
                userId = ElementCallUserId(member.userId.value),
                displayName = member.displayName,
                avatarUrl = member.avatarUrl,
            )
        }
}

/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.rtc

import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.matrix.impl.util.mxCallbackFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.MembershipState
import org.matrix.rustcomponents.sdk.Room
import org.matrix.rustcomponents.sdk.RoomInfo
import org.matrix.rustcomponents.sdk.RoomInfoListener
import timber.log.Timber
import uniffi.matrix_sdk_base.EncryptionState

/**
 * The room's info now and on every change, for as long as it is collected.
 *
 * The current value is sent first: the SDK's subscription only reports what changes *after* it exists,
 * and a room whose name never changes during a call would otherwise never be named.
 */
internal fun Room.roomInfoUpdates(dispatchers: CoroutineDispatchers): Flow<RoomInfo> = mxCallbackFlow {
    val handle = subscribeToRoomInfoUpdates(
        object : RoomInfoListener {
            override fun call(roomInfo: RoomInfo) {
                trySend(roomInfo)
            }
        }
    )
    trySend(roomInfo())
    handle
}.flowOn(dispatchers.io)

/**
 * Whether the room is encrypted, once known and on change. Never a guess: the core treats cleartext
 * membership as valid in an unencrypted room, so "unknown" must not be reported as false.
 */
internal fun Room.encryptionUpdates(dispatchers: CoroutineDispatchers): Flow<Boolean> = roomInfoUpdates(dispatchers)
    .mapNotNull {
        when (it.encryptionState) {
            EncryptionState.ENCRYPTED -> true
            EncryptionState.NOT_ENCRYPTED -> false
            EncryptionState.UNKNOWN -> null
        }
    }
    .distinctUntilChanged()

/**
 * The joined members' user ids, ourselves included, re-read on every `m.room.member` change: the joined count
 * can stay put while the members change. The list comes from `members()`, not from those events, because the
 * store only holds the member state sync has lazily loaded; `members()` fetches the rest from the homeserver.
 */
internal fun Room.joinedMemberIds(dispatchers: CoroutineDispatchers): Flow<List<String>> = stateEventSets("m.room.member", dispatchers)
    .map { loadJoinedMemberIds(dispatchers) }
    .filter { it.isNotEmpty() }
    .distinctUntilChanged()

private suspend fun Room.loadJoinedMemberIds(dispatchers: CoroutineDispatchers): List<String> = withContext(dispatchers.io) {
    runCatchingExceptions {
        members().use { iterator ->
            iterator.nextChunk(iterator.len()).orEmpty()
                .filter { it.membership is MembershipState.Join }
                .map { it.userId }
        }
    }.onFailure {
        Timber.w(it, "MatrixRTC backend: cannot load the members of ${id()}")
    }.getOrDefault(emptyList())
}

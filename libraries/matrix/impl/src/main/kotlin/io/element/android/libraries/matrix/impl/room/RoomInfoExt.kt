/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room

import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.impl.util.mxCallbackFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import org.matrix.rustcomponents.sdk.Room
import org.matrix.rustcomponents.sdk.RoomInfo
import org.matrix.rustcomponents.sdk.RoomInfoListener

/**
 * Extract the heroes from the room info.
 * For now we only use heroes for direct rooms with 2 members.
 * Also we keep the heroes only if there is one single hero.
 */
fun RoomInfo.elementHeroes(): List<MatrixUser> {
    return heroes
        .takeIf { isDm }
        ?.takeIf { it.size == 1 }
        ?.map { it.map() }
        .orEmpty()
}

/**
 * The room's info now and on every change, for as long as it is collected.
 *
 * The initial value is emitted immediately. The flow is collected on the provided [CoroutineDispatchers.io].
 */
internal fun Room.roomInfoUpdates(dispatchers: CoroutineDispatchers): Flow<RoomInfo> = mxCallbackFlow {
    trySend(roomInfo())
    subscribeToRoomInfoUpdates(
        object : RoomInfoListener {
            override fun call(roomInfo: RoomInfo) {
                trySend(roomInfo)
            }
        }
    )
}.flowOn(dispatchers.io)

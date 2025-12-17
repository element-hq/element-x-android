/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.spaces

import io.element.android.libraries.matrix.impl.util.cancelAndDestroy
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import org.matrix.rustcomponents.sdk.SpaceListUpdate
import org.matrix.rustcomponents.sdk.SpaceRoom
import org.matrix.rustcomponents.sdk.SpaceRoomListEntriesListener
import org.matrix.rustcomponents.sdk.SpaceRoomListInterface
import org.matrix.rustcomponents.sdk.SpaceRoomListPaginationStateListener
import org.matrix.rustcomponents.sdk.SpaceRoomListSpaceListener
import timber.log.Timber
import uniffi.matrix_sdk_ui.SpaceRoomListPaginationState
import java.util.Optional

internal fun SpaceRoomListInterface.paginationStateFlow(): Flow<SpaceRoomListPaginationState> = callbackFlow {
    val listener = object : SpaceRoomListPaginationStateListener {
        override fun onUpdate(paginationState: SpaceRoomListPaginationState) {
            trySend(paginationState)
        }
    }
    // Send the initial value
    trySend(paginationState())
    // Then subscribe to updates
    val result = subscribeToPaginationStateUpdates(listener)
    awaitClose {
        result.cancelAndDestroy()
    }
}.catch {
    Timber.d(it, "paginationStateFlow() failed")
}.buffer(Channel.UNLIMITED)

internal fun SpaceRoomListInterface.spaceListUpdateFlow(): Flow<List<SpaceListUpdate>> =
    callbackFlow {
        val listener = object : SpaceRoomListEntriesListener {
            override fun onUpdate(rooms: List<SpaceListUpdate>) {
                trySendBlocking(rooms)
            }
        }
        Timber.d("Open spaceListUpdateFlow for SpaceRoomListInterface ${this@spaceListUpdateFlow}")
        val taskHandle = subscribeToRoomUpdate(listener)
        awaitClose {
            Timber.d("Close spaceListUpdateFlow for SpaceRoomListInterface ${this@spaceListUpdateFlow}")
            taskHandle.cancelAndDestroy()
        }
    }.catch {
        Timber.d(it, "spaceListUpdateFlow() failed")
    }.buffer(Channel.UNLIMITED)

internal fun SpaceRoomListInterface.spaceUpdateFlow(): Flow<Optional<SpaceRoom>> =
    callbackFlow {
        val listener = object : SpaceRoomListSpaceListener {
            override fun onUpdate(space: SpaceRoom?) {
                trySendBlocking(Optional.ofNullable(space))
            }
        }
        Timber.d("Open spaceUpdateFlow for SpaceRoomListInterface ${this@spaceUpdateFlow}")
        trySendBlocking(Optional.ofNullable(space()))
        val taskHandle = subscribeToSpaceUpdates(listener)
        awaitClose {
            Timber.d("Close spaceUpdateFlow for SpaceRoomListInterface ${this@spaceUpdateFlow}")
            taskHandle.cancelAndDestroy()
        }
    }.catch {
        Timber.d(it, "spaceUpdateFlow() failed")
    }.buffer(Channel.UNLIMITED)

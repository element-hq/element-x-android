/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ptt.api

import io.element.android.libraries.matrix.api.core.UserId
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * Immutable snapshot of a live PTT session, rendered by both the in-room banner and the backgrounded
 * home-screen button. Produced by [PttTransport.state].
 */
data class PttSessionState(
    val transport: PttTransportType,
    val connection: PttConnectionState,
    val floor: PttFloorState,
    val activeSpeakers: ImmutableList<UserId>,
    val participantCount: Int,
)

/** Connection lifecycle of the underlying transport. */
sealed interface PttConnectionState {
    data object Connecting : PttConnectionState
    data object Connected : PttConnectionState
    data class Disconnected(val reason: String?) : PttConnectionState
    data class Error(val cause: Throwable) : PttConnectionState
}

/**
 * Ownership of the half-duplex transmit "floor". Drives the live/deny styling and the go/deny tones.
 */
sealed interface PttFloorState {
    /** Nobody is transmitting; the floor is free to take. */
    data object Open : PttFloorState

    /** The local user requested the floor and is waiting for it to be granted. */
    data object Acquiring : PttFloorState

    /** The local user holds the floor and is transmitting. */
    data object HeldByMe : PttFloorState

    /** Another participant holds the floor; taking it is denied until they release it. */
    data class HeldByOther(val userId: UserId) : PttFloorState
}

/**
 * The state a freshly created, not-yet-joined transport should expose as its initial value, so
 * [PttTransport.state] can be a [kotlinx.coroutines.flow.StateFlow] with an immediate value.
 */
fun initialPttSessionState(transport: PttTransportType) = PttSessionState(
    transport = transport,
    connection = PttConnectionState.Connecting,
    floor = PttFloorState.Open,
    activeSpeakers = persistentListOf(),
    participantCount = 0,
)

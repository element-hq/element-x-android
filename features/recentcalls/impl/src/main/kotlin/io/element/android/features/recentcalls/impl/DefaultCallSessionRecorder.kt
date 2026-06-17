/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.recentcalls.impl

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.element.android.features.recentcalls.api.CallSessionRecorder
import io.element.android.features.recentcalls.api.CallSessionRecorderProvider
import io.element.android.features.recentcalls.api.RecentCallDirection
import io.element.android.features.recentcalls.api.RecentCallEntry
import io.element.android.features.recentcalls.api.RecentCallStatus
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.notification.CallIntent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@SingleIn(SessionScope::class)
@ContributesBinding(SessionScope::class)
@Inject
class DefaultCallSessionRecorder(
    matrixClient: MatrixClient,
    provider: CallSessionRecorderProvider,
) : CallSessionRecorder {
    private val ongoingByRoom = mutableMapOf<RoomId, OngoingCall>()
    private val _ongoingEntries = MutableStateFlow<List<RecentCallEntry>>(emptyList())
    override val ongoingEntries: StateFlow<List<RecentCallEntry>> = _ongoingEntries.asStateFlow()

    init {
        provider.register(matrixClient.sessionId, this)
    }

    override fun onIncomingRing(
        roomId: RoomId,
        roomDisplayName: String,
        avatarUrl: String?,
        isDirect: Boolean,
        counterpartUserId: UserId?,
        callIntent: CallIntent,
        timestamp: Long,
    ) {
        ongoingByRoom[roomId] = OngoingCall(
            roomDisplayName = roomDisplayName,
            avatarUrl = avatarUrl,
            isDirect = isDirect,
            counterpartUserId = counterpartUserId,
            callIntent = callIntent,
            direction = RecentCallDirection.INCOMING,
            startedAt = timestamp,
            joinedAt = null,
        )
        publishOngoing()
    }

    override fun onJoined(roomId: RoomId, timestamp: Long) {
        ongoingByRoom[roomId]?.let { ongoing ->
            ongoingByRoom[roomId] = ongoing.copy(joinedAt = timestamp)
            publishOngoing()
        }
    }

    override fun onMissed(roomId: RoomId, callIntent: CallIntent, timestamp: Long) {
        ongoingByRoom.remove(roomId)
        publishOngoing()
    }

    override fun onDeclined(roomId: RoomId, callIntent: CallIntent, timestamp: Long) {
        ongoingByRoom.remove(roomId)
        publishOngoing()
    }

    override fun onCompleted(roomId: RoomId, durationMs: Long) {
        ongoingByRoom.remove(roomId)
        publishOngoing()
    }

    private fun publishOngoing() {
        _ongoingEntries.update {
            ongoingByRoom.map { (roomId, call) ->
                RecentCallEntry(
                    id = "ongoing_${roomId.value}",
                    eventId = null,
                    roomId = roomId,
                    roomDisplayName = call.roomDisplayName,
                    avatarUrl = call.avatarUrl,
                    isDirect = call.isDirect,
                    counterpartUserId = call.counterpartUserId,
                    direction = call.direction,
                    status = RecentCallStatus.ONGOING,
                    callIntent = call.callIntent,
                    timestamp = call.joinedAt ?: call.startedAt,
                    durationMs = call.joinedAt?.let { joinedAt -> (System.currentTimeMillis() - joinedAt).coerceAtLeast(0) },
                )
            }
        }
    }

    private data class OngoingCall(
        val roomDisplayName: String,
        val avatarUrl: String?,
        val isDirect: Boolean,
        val counterpartUserId: UserId?,
        val callIntent: CallIntent,
        val direction: RecentCallDirection,
        val startedAt: Long,
        val joinedAt: Long?,
    )
}

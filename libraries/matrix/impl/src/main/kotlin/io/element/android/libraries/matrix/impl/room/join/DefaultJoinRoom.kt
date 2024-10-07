/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room.join

import com.squareup.anvil.annotations.ContributesBinding
import im.vector.app.features.analytics.plan.JoinedRoom
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomIdOrAlias
import io.element.android.libraries.matrix.api.room.join.JoinRoom
import io.element.android.libraries.matrix.api.roomlist.RoomSummary
import io.element.android.libraries.matrix.impl.analytics.toAnalyticsJoinedRoom
import io.element.android.services.analytics.api.AnalyticsService
import javax.inject.Inject

@ContributesBinding(SessionScope::class)
class DefaultJoinRoom @Inject constructor(
    private val client: MatrixClient,
    private val analyticsService: AnalyticsService,
) : JoinRoom {
    override suspend fun invoke(
        roomIdOrAlias: RoomIdOrAlias,
        serverNames: List<String>,
        trigger: JoinedRoom.Trigger
    ): Result<Unit> {
        return when (roomIdOrAlias) {
            is RoomIdOrAlias.Id -> {
                if (serverNames.isEmpty()) {
                    client.joinRoom(roomIdOrAlias.roomId)
                } else {
                    client.joinRoomByIdOrAlias(roomIdOrAlias, serverNames)
                }
            }
            is RoomIdOrAlias.Alias -> {
                client.joinRoomByIdOrAlias(roomIdOrAlias, serverNames = emptyList())
            }
        }.onSuccess { roomSummary ->
            client.captureJoinedRoomAnalytics(roomSummary, trigger)
        }.map { }
    }

    private suspend fun MatrixClient.captureJoinedRoomAnalytics(roomSummary: RoomSummary?, trigger: JoinedRoom.Trigger) {
        if (roomSummary == null) return
        getRoom(roomSummary.roomId)?.use { room ->
            analyticsService.capture(room.toAnalyticsJoinedRoom(trigger))
        }
    }
}

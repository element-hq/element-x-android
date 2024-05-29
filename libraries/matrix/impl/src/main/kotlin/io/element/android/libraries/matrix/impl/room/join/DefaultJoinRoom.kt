/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.libraries.matrix.impl.room.join

import com.squareup.anvil.annotations.ContributesBinding
import im.vector.app.features.analytics.plan.JoinedRoom
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.join.JoinRoom
import io.element.android.libraries.matrix.impl.analytics.toAnalyticsJoinedRoom
import io.element.android.services.analytics.api.AnalyticsService
import javax.inject.Inject

@ContributesBinding(SessionScope::class)
class DefaultJoinRoom @Inject constructor(
    private val client: MatrixClient,
    private val analyticsService: AnalyticsService,
) : JoinRoom {
    override suspend fun invoke(
        roomId: RoomId,
        serverNames: List<String>,
        trigger: JoinedRoom.Trigger,
    ): Result<Unit> {
        return if (serverNames.isEmpty()) {
            client.joinRoom(roomId)
        } else {
            client.joinRoomByIdOrAlias(roomId, serverNames)
        }.onSuccess {
            client.getRoom(roomId)?.use { room ->
                analyticsService.capture(room.toAnalyticsJoinedRoom(trigger))
            }
        }
    }
}

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

package io.element.android.libraries.matrix.test.room.join

import im.vector.app.features.analytics.plan.JoinedRoom
import io.element.android.libraries.matrix.api.core.RoomIdOrAlias
import io.element.android.libraries.matrix.api.room.join.JoinRoom
import io.element.android.tests.testutils.simulateLongTask

class FakeJoinRoom(
    var lambda: (RoomIdOrAlias, List<String>, JoinedRoom.Trigger) -> Result<Unit>
) : JoinRoom {
    override suspend fun invoke(
        roomIdOrAlias: RoomIdOrAlias,
        serverNames: List<String>,
        trigger: JoinedRoom.Trigger,
    ): Result<Unit> = simulateLongTask {
        lambda(roomIdOrAlias, serverNames, trigger)
    }
}

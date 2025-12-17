/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
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

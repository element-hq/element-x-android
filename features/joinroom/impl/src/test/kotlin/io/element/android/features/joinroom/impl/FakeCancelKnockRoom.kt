/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.joinroom.impl

import io.element.android.features.joinroom.impl.di.CancelKnockRoom
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.tests.testutils.simulateLongTask

class FakeCancelKnockRoom(
    var lambda: (RoomId) -> Result<Unit> = { Result.success(Unit) }
) : CancelKnockRoom {
    override suspend fun invoke(roomId: RoomId) = simulateLongTask {
        lambda(roomId)
    }
}

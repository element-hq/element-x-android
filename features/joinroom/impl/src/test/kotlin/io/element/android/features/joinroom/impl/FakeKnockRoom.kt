/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.joinroom.impl

import io.element.android.features.joinroom.impl.di.KnockRoom
import io.element.android.libraries.matrix.api.core.RoomIdOrAlias
import io.element.android.tests.testutils.simulateLongTask

class FakeKnockRoom(
    var lambda: (RoomIdOrAlias, String, List<String>) -> Result<Unit> = { _, _, _ -> Result.success(Unit) }
) : KnockRoom {
    override suspend fun invoke(roomIdOrAlias: RoomIdOrAlias, message: String, serverNames: List<String>): Result<Unit> = simulateLongTask {
        lambda(roomIdOrAlias, message, serverNames)
    }
}

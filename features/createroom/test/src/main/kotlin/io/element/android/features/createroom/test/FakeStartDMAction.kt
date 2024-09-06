/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.createroom.test

import androidx.compose.runtime.MutableState
import io.element.android.features.createroom.api.StartDMAction
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.test.A_ROOM_ID
import kotlinx.coroutines.delay

class FakeStartDMAction : StartDMAction {
    private var executeResult: AsyncAction<RoomId> = AsyncAction.Success(A_ROOM_ID)

    fun givenExecuteResult(result: AsyncAction<RoomId>) {
        executeResult = result
    }

    override suspend fun execute(userId: UserId, actionState: MutableState<AsyncAction<RoomId>>) {
        actionState.value = AsyncAction.Loading
        delay(1)
        actionState.value = executeResult
    }
}

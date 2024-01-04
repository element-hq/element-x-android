/*
 * Copyright (c) 2023 New Vector Ltd
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

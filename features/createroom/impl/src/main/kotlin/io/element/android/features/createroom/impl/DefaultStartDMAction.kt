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

package io.element.android.features.createroom.impl

import androidx.compose.runtime.MutableState
import com.squareup.anvil.annotations.ContributesBinding
import im.vector.app.features.analytics.plan.CreatedRoom
import io.element.android.features.createroom.api.StartDMAction
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.StartDMResult
import io.element.android.libraries.matrix.api.room.startDM
import io.element.android.services.analytics.api.AnalyticsService
import javax.inject.Inject

@ContributesBinding(SessionScope::class)
class DefaultStartDMAction @Inject constructor(
    private val matrixClient: MatrixClient,
    private val analyticsService: AnalyticsService,
) : StartDMAction {

    override suspend fun execute(userId: UserId, actionState: MutableState<AsyncAction<RoomId>>) {
        actionState.value = AsyncAction.Loading
        when (val result = matrixClient.startDM(userId)) {
            is StartDMResult.Success -> {
                if (result.isNew) {
                    analyticsService.capture(CreatedRoom(isDM = true))
                }
                actionState.value = AsyncAction.Success(result.roomId)
            }
            is StartDMResult.Failure -> {
                actionState.value = AsyncAction.Failure(result.throwable)
            }
        }
    }
}

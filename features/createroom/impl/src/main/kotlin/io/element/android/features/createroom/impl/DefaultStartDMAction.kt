/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.createroom.impl

import androidx.compose.runtime.MutableState
import com.squareup.anvil.annotations.ContributesBinding
import im.vector.app.features.analytics.plan.CreatedRoom
import io.element.android.features.createroom.api.ConfirmingStartDmWithMatrixUser
import io.element.android.features.createroom.api.StartDMAction
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.StartDMResult
import io.element.android.libraries.matrix.api.room.startDM
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.services.analytics.api.AnalyticsService
import javax.inject.Inject

@ContributesBinding(SessionScope::class)
class DefaultStartDMAction @Inject constructor(
    private val matrixClient: MatrixClient,
    private val analyticsService: AnalyticsService,
) : StartDMAction {
    override suspend fun execute(
        matrixUser: MatrixUser,
        createIfDmDoesNotExist: Boolean,
        actionState: MutableState<AsyncAction<RoomId>>,
    ) {
        actionState.value = AsyncAction.Loading
        when (val result = matrixClient.startDM(matrixUser.userId, createIfDmDoesNotExist)) {
            is StartDMResult.Success -> {
                if (result.isNew) {
                    analyticsService.capture(CreatedRoom(isDM = true))
                }
                actionState.value = AsyncAction.Success(result.roomId)
            }
            is StartDMResult.Failure -> {
                actionState.value = AsyncAction.Failure(result.throwable)
            }
            StartDMResult.DmDoesNotExist -> {
                actionState.value = ConfirmingStartDmWithMatrixUser(matrixUser = matrixUser)
            }
        }
    }
}

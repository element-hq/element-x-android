/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.createroom.api

import androidx.compose.runtime.MutableState
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId

interface StartDMAction {
    /**
     * Try to find an existing DM with the given user, or create one if none exists.
     * @param userId The user to start a DM with.
     * @param actionState The state to update with the result of the action.
     */
    suspend fun execute(userId: UserId, actionState: MutableState<AsyncAction<RoomId>>)
}

/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.roomcall.api

import androidx.compose.runtime.Immutable
import io.element.android.features.roomcall.api.RoomCallState.OnGoing
import io.element.android.features.roomcall.api.RoomCallState.StandBy

@Immutable
sealed interface RoomCallState {
    data class StandBy(
        val canStartCall: Boolean,
    ) : RoomCallState

    data class OnGoing(
        val canJoinCall: Boolean,
        val isUserInTheCall: Boolean,
        val isUserLocallyInTheCall: Boolean,
    ) : RoomCallState
}

fun RoomCallState.hasPermissionToJoin() = when (this) {
    is StandBy -> canStartCall
    is OnGoing -> canJoinCall
}

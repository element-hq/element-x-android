/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomcall.api

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

open class RoomCallStateProvider : PreviewParameterProvider<RoomCallState> {
    override val values: Sequence<RoomCallState> = sequenceOf(
        aStandByCallState(),
        aStandByCallState(canStartCall = false),
        anOngoingCallState(),
        anOngoingCallState(canJoinCall = false),
        anOngoingCallState(canJoinCall = true, isUserInTheCall = true),
        RoomCallState.Unavailable,
    )
}

fun anOngoingCallState(
    canJoinCall: Boolean = true,
    isUserInTheCall: Boolean = false,
    isUserLocallyInTheCall: Boolean = isUserInTheCall,
) = RoomCallState.OnGoing(
    canJoinCall = canJoinCall,
    isUserInTheCall = isUserInTheCall,
    isUserLocallyInTheCall = isUserLocallyInTheCall,
)

fun aStandByCallState(
    canStartCall: Boolean = true,
) = RoomCallState.StandBy(
    canStartCall = canStartCall,
)

/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.appnav.room

import io.element.android.libraries.core.coroutine.withPreviousValue
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.room.RoomInfo
import io.element.android.libraries.matrix.api.room.RoomMembershipObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

internal sealed interface RoomMembershipNavigation {
    data object EnterRoom : RoomMembershipNavigation
    data object Dismiss : RoomMembershipNavigation
    data object ShowJoinRoom : RoomMembershipNavigation
}

/**
 * Decide where the room flow must navigate, from the room membership reported by sync and the
 * local leave notification for that room.
 *
 * The two signals are concurrent and the local one may never arrive, so they are combined rather
 * than sampled: whichever arrives second re-evaluates the decision.
 */
internal fun roomMembershipNavigation(
    roomId: RoomId,
    roomInfoFlow: Flow<Optional<RoomInfo>>,
    membershipUpdates: Flow<RoomMembershipObserver.RoomMembershipUpdate>,
    scope: CoroutineScope,
): Flow<RoomMembershipNavigation> {
    val leftFromCurrentDeviceFlow = membershipUpdates
        .filter { it.roomId == roomId && !it.isUserInRoom }
        .map { true }
        .stateIn(scope, started = SharingStarted.Eagerly, initialValue = false)

    val currentMembershipFlow = roomInfoFlow
        .map { it.getOrNull()?.currentUserMembership }
        .distinctUntilChanged()
        .withPreviousValue()

    return combine(currentMembershipFlow, leftFromCurrentDeviceFlow) { (previousMembership, membership), leftFromCurrentDevice ->
        when {
            membership == CurrentUserMembership.JOINED -> RoomMembershipNavigation.EnterRoom
            membership == CurrentUserMembership.LEFT &&
                previousMembership == CurrentUserMembership.JOINED &&
                leftFromCurrentDevice -> RoomMembershipNavigation.Dismiss
            else -> RoomMembershipNavigation.ShowJoinRoom
        }
    }
}

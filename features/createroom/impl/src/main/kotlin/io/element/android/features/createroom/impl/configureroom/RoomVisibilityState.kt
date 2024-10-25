/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.createroom.impl.configureroom

sealed interface RoomVisibilityState {
    val roomAccess: RoomAccess

    data object Private : RoomVisibilityState {
        override val roomAccess: RoomAccess = RoomAccess.Invite
    }

    data class Public(
        val roomAddress: RoomAddress,
        override val roomAccess: RoomAccess
    ) : RoomVisibilityState
}

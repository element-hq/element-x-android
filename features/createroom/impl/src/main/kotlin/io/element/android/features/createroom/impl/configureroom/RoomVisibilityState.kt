/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.createroom.impl.configureroom

sealed interface RoomVisibilityState {
    data object Private : RoomVisibilityState

    data class Public(
        val roomAddress: RoomAddress,
        val roomAccess: RoomAccess
    ) : RoomVisibilityState
}

/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.createroom.impl.configureroom

import java.util.Optional

sealed interface RoomVisibilityState {
    data object Private : RoomVisibilityState

    data class Public(
        val roomAddress: RoomAddress,
        val roomAccess: RoomAccess,
    ) : RoomVisibilityState

    fun roomAddress(): Optional<String> {
        return when (this) {
            is Private -> Optional.empty()
            is Public -> Optional.of(roomAddress.value)
        }
    }
}

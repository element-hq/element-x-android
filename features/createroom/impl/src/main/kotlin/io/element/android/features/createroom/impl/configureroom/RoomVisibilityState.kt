/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.createroom.impl.configureroom

import java.util.Optional

sealed interface RoomVisibilityState {
    data object Private : RoomVisibilityState

    data object PrivateNotEncrypted : RoomVisibilityState // TCHAP room type

    data class Public(
        val roomAddress: RoomAddress,
        val roomAccess: RoomAccess,
    ) : RoomVisibilityState

    fun roomAddress(): Optional<String> {
        return when (this) {
            is PrivateNotEncrypted, // TCHAP room type
            is Private -> Optional.empty()
            is Public -> Optional.of(roomAddress.value)
        }
    }
}

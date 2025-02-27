/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.createroom.impl.joinbyaddress

sealed interface JoinRoomByAddressEvents {
    data object Dismiss : JoinRoomByAddressEvents
    data object Continue : JoinRoomByAddressEvents
    data class UpdateAddress(val address: String) : JoinRoomByAddressEvents
}

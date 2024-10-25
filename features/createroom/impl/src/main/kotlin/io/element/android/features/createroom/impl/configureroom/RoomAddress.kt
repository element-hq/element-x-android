/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.createroom.impl.configureroom

sealed interface RoomAddress {
    data class AutoFilled(val address: String) : RoomAddress
    data class Edited(val address: String) : RoomAddress
}

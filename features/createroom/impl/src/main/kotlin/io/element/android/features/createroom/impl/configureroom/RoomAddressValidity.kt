/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.createroom.impl.configureroom

import androidx.compose.runtime.Immutable

/**
 * Represents the validity state of a room address.
 * ie. whether it contains invalid characters, is already taken, or is valid.
 */
@Immutable
sealed interface RoomAddressValidity {
    data object Unknown : RoomAddressValidity
    data object InvalidSymbols : RoomAddressValidity
    data object NotAvailable : RoomAddressValidity
    data object Valid : RoomAddressValidity

    fun isError(): Boolean {
        return this is InvalidSymbols || this is NotAvailable
    }
}

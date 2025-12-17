/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.room.address

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
}

/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.createroom.impl.configureroom

/**
 * Represents the error state of a room address.
 */
sealed interface RoomAddressErrorState {
    data object InvalidCharacters : RoomAddressErrorState
    data object AlreadyExists : RoomAddressErrorState
    data object None : RoomAddressErrorState
}

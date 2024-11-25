/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.createroom.impl.configureroom

sealed class RoomAddress(open val value: String) {
    data class AutoFilled(override val value: String) : RoomAddress(value)
    data class Edited(override val value: String) : RoomAddress(value)
}

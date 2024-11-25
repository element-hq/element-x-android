/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room

import io.element.android.libraries.matrix.api.room.RoomType
import org.matrix.rustcomponents.sdk.RoomType as RustRoomType

fun RustRoomType.map(): RoomType {
    return when (this) {
        RustRoomType.Room -> RoomType.Room
        RustRoomType.Space -> RoomType.Space
        is RustRoomType.Custom -> RoomType.Other(this.value)
    }
}

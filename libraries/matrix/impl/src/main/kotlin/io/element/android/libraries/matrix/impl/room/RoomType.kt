/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room

import io.element.android.libraries.matrix.api.room.RoomType

fun String?.toRoomType(): RoomType {
    return when (this) {
        null -> RoomType.Room
        "m.space" -> RoomType.Space
        else -> RoomType.Other(this)
    }
}

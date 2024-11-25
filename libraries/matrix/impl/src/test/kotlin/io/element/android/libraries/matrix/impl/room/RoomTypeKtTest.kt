/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */
package io.element.android.libraries.matrix.impl.room

import io.element.android.libraries.matrix.api.room.RoomType
import org.junit.Test
import org.matrix.rustcomponents.sdk.RoomType as RustRoomType

class RoomTypeKtTest {
    @Test
    fun toRoomType() {
        assert(RustRoomType.Room.map() == RoomType.Room)
        assert(RustRoomType.Space.map() == RoomType.Space)
        assert(RustRoomType.Custom("m.other").map() == RoomType.Other("m.other"))
    }
}

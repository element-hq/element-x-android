/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */
package io.element.android.libraries.matrix.impl.room

import io.element.android.libraries.matrix.api.room.RoomType
import org.junit.Test

class RoomTypeKtTest {
    @Test
    fun toRoomType() {
        assert(null.toRoomType() == RoomType.Room)
        assert("m.space".toRoomType() == RoomType.Space)
        assert("m.other".toRoomType() == RoomType.Other("m.other"))
    }
}

/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
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

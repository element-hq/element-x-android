/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.spaces

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.room.RoomType
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID_2
import io.element.android.libraries.previewutils.room.aSpaceRoom
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SpaceRoomCacheTest {
    @Test
    fun `getSpaceRoomFlow emits items`() = runTest {
        val sut = SpaceRoomCache()
        sut.getSpaceRoomFlow(A_ROOM_ID).test {
            assertThat(awaitItem()).isNull()
            val room = aSpaceRoom(
                roomId = A_ROOM_ID,
                roomType = RoomType.Room,
            )
            sut.update(listOf(room))
            // Not a space, should not be cached
            expectNoEvents()
            val space = aSpaceRoom(
                roomId = A_ROOM_ID,
                roomType = RoomType.Space,
            )
            sut.update(listOf(space))
            assertThat(awaitItem()).isEqualTo(space)
            val spaceOther = aSpaceRoom(
                roomId = A_ROOM_ID_2,
                roomType = RoomType.Space,
            )
            sut.update(listOf(spaceOther))
            expectNoEvents()
        }
    }
}

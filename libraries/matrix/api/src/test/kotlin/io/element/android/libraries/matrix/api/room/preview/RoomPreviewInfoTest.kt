/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.room.preview

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.test.room.aRoomPreviewInfo
import org.junit.Test

class RoomPreviewInfoTest {
    @Test
    fun `isDm is true for a direct room with at most 2 active members`() {
        assertThat(aRoomPreviewInfo(isDirect = true, numberOfActiveMembers = 2).isDm).isTrue()
        assertThat(aRoomPreviewInfo(isDirect = true, numberOfActiveMembers = 1).isDm).isTrue()
    }

    @Test
    fun `isDm is false for a direct room with more than 2 active members`() {
        assertThat(aRoomPreviewInfo(isDirect = true, numberOfActiveMembers = 3).isDm).isFalse()
    }

    @Test
    fun `isDm falls back to the number of joined members when active members are unknown`() {
        assertThat(aRoomPreviewInfo(isDirect = true, numberOfActiveMembers = null, numberOfJoinedMembers = 2).isDm).isTrue()
        assertThat(aRoomPreviewInfo(isDirect = true, numberOfActiveMembers = null, numberOfJoinedMembers = 3).isDm).isFalse()
    }

    @Test
    fun `isDm is false when the room is not direct or when it is unknown`() {
        assertThat(aRoomPreviewInfo(isDirect = false, numberOfActiveMembers = 2).isDm).isFalse()
        assertThat(aRoomPreviewInfo(isDirect = null, numberOfActiveMembers = 2).isDm).isFalse()
    }
}

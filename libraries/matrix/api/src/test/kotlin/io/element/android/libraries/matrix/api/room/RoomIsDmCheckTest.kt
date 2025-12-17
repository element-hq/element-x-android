/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.room

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RoomIsDmCheckTest {
    @Test
    fun `a room is a DM only if it has at most 2 members and is direct`() {
        val isDirect = true
        val activeMembersCount = 2

        val isDm = isDm(isDirect, activeMembersCount)

        assertThat(isDm).isTrue()
    }

    @Test
    fun `a room can be a DM if it has also a single active user`() {
        val isDirect = true
        val activeMembersCount = 1

        val isDm = isDm(isDirect, activeMembersCount)

        assertThat(isDm).isTrue()
    }

    @Test
    fun `a room is not a DM if it's not direct`() {
        val isDirect = false
        val activeMembersCount = 2

        val isDm = isDm(isDirect, activeMembersCount)

        assertThat(isDm).isFalse()
    }

    @Test
    fun `a room is not a DM if it has more than 2 active users`() {
        val isDirect = true
        val activeMembersCount = 3

        val isDm = isDm(isDirect, activeMembersCount)

        assertThat(isDm).isFalse()
    }
}

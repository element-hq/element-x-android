/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

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

package io.element.android.features.roomdetails.members

import io.element.android.features.roomdetails.impl.members.PowerLevelRoomMemberComparator
import io.element.android.features.roomdetails.impl.members.aRoomMember
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.A_USER_ID_2
import io.element.android.libraries.matrix.test.A_USER_ID_3
import io.element.android.libraries.matrix.test.A_USER_ID_4
import io.element.android.libraries.matrix.test.A_USER_ID_5
import org.junit.Test

class PowerLevelRoomMemberComparatorTest {
    @Test
    fun `order is Admin, then Moderator, then User`() {
        val memberList = listOf(
            aRoomMember(userId = UserId("@admin:example.com"), powerLevel = 100),
            aRoomMember(userId = UserId("@moderator:example.com"), powerLevel = 50),
            aRoomMember(userId = UserId("@user:example.com"), powerLevel = 0),
        ).shuffled()

        val ordered = memberList.sortedWith(PowerLevelRoomMemberComparator())
        assert(ordered[0].userId == UserId("@admin:example.com"))
        assert(ordered[1].userId == UserId("@moderator:example.com"))
        assert(ordered[2].userId == UserId("@user:example.com"))
    }

    @Test
    fun `with the same power level, alphabetical ascending order for name is used`() {
        val memberList = listOf(
            aRoomMember(userId = A_USER_ID, displayName = "First - admin", powerLevel = 100),
            aRoomMember(userId = A_USER_ID_2, displayName = "Second - admin", powerLevel = 100),
            aRoomMember(userId = A_USER_ID_3, displayName = "Third - admin", powerLevel = 100),
            aRoomMember(userId = A_USER_ID_4, displayName = "First - user", powerLevel = 0),
            aRoomMember(userId = A_USER_ID_5, displayName = "Second - user", powerLevel = 0),
        ).shuffled()

        val ordered = memberList.sortedWith(PowerLevelRoomMemberComparator())
        assert(ordered[0].userId == A_USER_ID)
        assert(ordered[1].userId == A_USER_ID_2)
        assert(ordered[2].userId == A_USER_ID_3)
        assert(ordered[3].userId == A_USER_ID_4)
        assert(ordered[4].userId == A_USER_ID_5)
    }

    @Test
    fun `when no names are provided, alphabetical order uses user id`() {
        val memberList = listOf(
            aRoomMember(userId = A_USER_ID, displayName = "Z - LAST!", powerLevel = 100),
            aRoomMember(userId = A_USER_ID_2, powerLevel = 100),
            aRoomMember(userId = A_USER_ID_3, powerLevel = 100),
        ).shuffled()

        val ordered = memberList.sortedWith(PowerLevelRoomMemberComparator())
        assert(ordered[0].userId == A_USER_ID_2)
        assert(ordered[1].userId == A_USER_ID_3)
        assert(ordered[2].userId == A_USER_ID)
    }

    @Test
    fun `unicode characters are simplified and compared, order ignores case`() {
        val memberList = listOf(
            aRoomMember(userId = A_USER_ID, displayName = "First", powerLevel = 100),
            aRoomMember(userId = A_USER_ID_2, displayName = "Șecond", powerLevel = 100),
            aRoomMember(userId = A_USER_ID_3, displayName = "third", powerLevel = 100),
        ).shuffled()

        val ordered = memberList.sortedWith(PowerLevelRoomMemberComparator())
        assert(ordered[0].userId == A_USER_ID)
        assert(ordered[1].userId == A_USER_ID_2)
        assert(ordered[2].userId == A_USER_ID_3)
    }
}

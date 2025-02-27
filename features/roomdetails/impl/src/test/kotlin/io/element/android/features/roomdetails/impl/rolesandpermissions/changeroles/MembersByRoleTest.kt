/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.rolesandpermissions.changeroles

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.A_USER_ID_2
import io.element.android.libraries.matrix.test.A_USER_ID_3
import io.element.android.libraries.matrix.test.A_USER_ID_4
import io.element.android.libraries.matrix.test.A_USER_ID_5
import io.element.android.libraries.matrix.test.room.aRoomMember
import kotlinx.collections.immutable.persistentListOf
import org.junit.Test

class MembersByRoleTest {
    @Test
    fun `constructor - with single member list categorizes and sorts members`() {
        val members = listOf(
            aRoomMember(A_USER_ID_2, displayName = "Bob", role = RoomMember.Role.ADMIN),
            aRoomMember(A_USER_ID, displayName = "Alice", role = RoomMember.Role.ADMIN),
            aRoomMember(A_USER_ID_3, displayName = "Carol", role = RoomMember.Role.USER),
            aRoomMember(A_USER_ID_5, displayName = "Eve", role = RoomMember.Role.USER),
            aRoomMember(A_USER_ID_4, displayName = "David", role = RoomMember.Role.USER),
        )
        val membersByRole = MembersByRole(members = members)
        assertThat(membersByRole.admins).containsExactly(
            aRoomMember(A_USER_ID, displayName = "Alice", role = RoomMember.Role.ADMIN),
            aRoomMember(A_USER_ID_2, displayName = "Bob", role = RoomMember.Role.ADMIN),
        )
        assertThat(membersByRole.moderators).isEmpty()
        assertThat(membersByRole.members).containsExactly(
            aRoomMember(A_USER_ID_3, displayName = "Carol", role = RoomMember.Role.USER),
            aRoomMember(A_USER_ID_4, displayName = "David", role = RoomMember.Role.USER),
            aRoomMember(A_USER_ID_5, displayName = "Eve", role = RoomMember.Role.USER),
        )
    }

    @Test
    fun `isEmpty - only returns true with no members of any role`() {
        val emptyMembersByRole = MembersByRole(emptyList())
        assertThat(emptyMembersByRole.isEmpty()).isTrue()

        val membersByRoleWithAdmins = MembersByRole(
            admins = persistentListOf(aRoomMember(A_USER_ID, role = RoomMember.Role.ADMIN)),
            moderators = persistentListOf(),
            members = persistentListOf(),
        )
        assertThat(membersByRoleWithAdmins.isEmpty()).isFalse()

        val membersByRoleWithModerators = MembersByRole(
            admins = persistentListOf(),
            moderators = persistentListOf(aRoomMember(A_USER_ID, role = RoomMember.Role.MODERATOR)),
            members = persistentListOf(),
        )
        assertThat(membersByRoleWithModerators.isEmpty()).isFalse()

        val membersByRoleWithMembers = MembersByRole(
            admins = persistentListOf(),
            moderators = persistentListOf(),
            members = persistentListOf(aRoomMember(A_USER_ID, role = RoomMember.Role.USER)),
        )
        assertThat(membersByRoleWithMembers.isEmpty()).isFalse()
    }
}

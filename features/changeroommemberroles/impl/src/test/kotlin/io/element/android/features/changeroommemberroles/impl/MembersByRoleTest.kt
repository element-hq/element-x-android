/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.changeroommemberroles.impl

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.A_USER_ID_2
import io.element.android.libraries.matrix.test.A_USER_ID_3
import io.element.android.libraries.matrix.test.A_USER_ID_4
import io.element.android.libraries.matrix.test.A_USER_ID_5
import io.element.android.libraries.matrix.test.A_USER_ID_6
import io.element.android.libraries.matrix.test.A_USER_ID_7
import io.element.android.libraries.matrix.test.room.aRoomMember
import kotlinx.collections.immutable.persistentListOf
import org.junit.Test

class MembersByRoleTest {
    @Test
    fun `constructor - with single member list categorizes and sorts members`() {
        val members = listOf(
            aRoomMember(A_USER_ID_2, displayName = "Bob", role = RoomMember.Role.Admin),
            aRoomMember(A_USER_ID, displayName = "Alice", role = RoomMember.Role.Admin),
            aRoomMember(A_USER_ID_3, displayName = "Carol", role = RoomMember.Role.User),
            aRoomMember(A_USER_ID_5, displayName = "Eve", role = RoomMember.Role.User),
            aRoomMember(A_USER_ID_4, displayName = "David", role = RoomMember.Role.User),
            aRoomMember(A_USER_ID_6, displayName = "Justin", role = RoomMember.Role.Owner(isCreator = true)),
            aRoomMember(A_USER_ID_7, displayName = "Mallory", role = RoomMember.Role.Owner(isCreator = false)),
        )
        val membersByRole = MembersByRole(members = members)
        assertThat(membersByRole.owners).containsExactly(
            aRoomMember(A_USER_ID_6, displayName = "Justin", role = RoomMember.Role.Owner(isCreator = true)),
            aRoomMember(A_USER_ID_7, displayName = "Mallory", role = RoomMember.Role.Owner(isCreator = false)),
        )
        assertThat(membersByRole.admins).containsExactly(
            aRoomMember(A_USER_ID, displayName = "Alice", role = RoomMember.Role.Admin),
            aRoomMember(A_USER_ID_2, displayName = "Bob", role = RoomMember.Role.Admin),
        )
        assertThat(membersByRole.moderators).isEmpty()
        assertThat(membersByRole.members).containsExactly(
            aRoomMember(A_USER_ID_3, displayName = "Carol", role = RoomMember.Role.User),
            aRoomMember(A_USER_ID_4, displayName = "David", role = RoomMember.Role.User),
            aRoomMember(A_USER_ID_5, displayName = "Eve", role = RoomMember.Role.User),
        )
    }

    @Test
    fun `isEmpty - only returns true with no members of any role`() {
        val emptyMembersByRole = MembersByRole(emptyList())
        assertThat(emptyMembersByRole.isEmpty()).isTrue()

        val membersByRoleWithOwners = MembersByRole(
            owners = persistentListOf(aRoomMember(A_USER_ID, role = RoomMember.Role.Admin)),
            admins = persistentListOf(),
            moderators = persistentListOf(),
            members = persistentListOf(),
        )
        assertThat(membersByRoleWithOwners.isEmpty()).isFalse()

        val membersByRoleWithAdmins = MembersByRole(
            owners = persistentListOf(),
            admins = persistentListOf(aRoomMember(A_USER_ID, role = RoomMember.Role.Admin)),
            moderators = persistentListOf(),
            members = persistentListOf(),
        )
        assertThat(membersByRoleWithAdmins.isEmpty()).isFalse()

        val membersByRoleWithModerators = MembersByRole(
            owners = persistentListOf(),
            admins = persistentListOf(),
            moderators = persistentListOf(aRoomMember(A_USER_ID, role = RoomMember.Role.Moderator)),
            members = persistentListOf(),
        )
        assertThat(membersByRoleWithModerators.isEmpty()).isFalse()

        val membersByRoleWithMembers = MembersByRole(
            owners = persistentListOf(),
            admins = persistentListOf(),
            moderators = persistentListOf(),
            members = persistentListOf(aRoomMember(A_USER_ID, role = RoomMember.Role.User)),
        )
        assertThat(membersByRoleWithMembers.isEmpty()).isFalse()
    }
}

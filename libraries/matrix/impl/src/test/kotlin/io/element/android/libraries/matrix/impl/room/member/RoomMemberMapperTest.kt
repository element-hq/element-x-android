/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room.member

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.RoomMembershipState
import org.junit.Test
import uniffi.matrix_sdk.RoomMemberRole
import org.matrix.rustcomponents.sdk.MembershipState as RustMembershipState

class RoomMemberMapperTest {
    @Test
    fun mapRole() {
        assertThat(RoomMemberMapper.mapRole(RoomMemberRole.USER)).isEqualTo(RoomMember.Role.USER)
        assertThat(RoomMemberMapper.mapRole(RoomMemberRole.MODERATOR)).isEqualTo(RoomMember.Role.MODERATOR)
        assertThat(RoomMemberMapper.mapRole(RoomMemberRole.ADMINISTRATOR)).isEqualTo(RoomMember.Role.ADMIN)
    }

    @Test
    fun mapMembership() {
        assertThat(RoomMemberMapper.mapMembership(RustMembershipState.Ban)).isEqualTo(RoomMembershipState.BAN)
        assertThat(RoomMemberMapper.mapMembership(RustMembershipState.Invite)).isEqualTo(RoomMembershipState.INVITE)
        assertThat(RoomMemberMapper.mapMembership(RustMembershipState.Join)).isEqualTo(RoomMembershipState.JOIN)
        assertThat(RoomMemberMapper.mapMembership(RustMembershipState.Knock)).isEqualTo(RoomMembershipState.KNOCK)
        assertThat(RoomMemberMapper.mapMembership(RustMembershipState.Leave)).isEqualTo(RoomMembershipState.LEAVE)
    }
}

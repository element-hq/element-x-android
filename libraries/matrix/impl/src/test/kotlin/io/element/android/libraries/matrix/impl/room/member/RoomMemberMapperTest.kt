/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
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
        assertThat(RoomMemberMapper.mapRole(RoomMemberRole.USER, 0L)).isEqualTo(RoomMember.Role.User)
        assertThat(RoomMemberMapper.mapRole(RoomMemberRole.MODERATOR, 50L)).isEqualTo(RoomMember.Role.Moderator)
        assertThat(RoomMemberMapper.mapRole(RoomMemberRole.ADMINISTRATOR, 100L)).isEqualTo(RoomMember.Role.Admin)
        assertThat(RoomMemberMapper.mapRole(RoomMemberRole.ADMINISTRATOR, 150L)).isEqualTo(RoomMember.Role.Owner(isCreator = false))
        assertThat(RoomMemberMapper.mapRole(RoomMemberRole.CREATOR, Long.MAX_VALUE)).isEqualTo(RoomMember.Role.Owner(isCreator = true))

        // `null` power level defaults to USER role
        assertThat(RoomMemberMapper.mapRole(RoomMemberRole.ADMINISTRATOR, null)).isEqualTo(RoomMember.Role.Admin)

        // Power level is only taken into account for ADMINISTRATOR role
        assertThat(RoomMemberMapper.mapRole(RoomMemberRole.USER, 123L)).isEqualTo(RoomMember.Role.User)
        assertThat(RoomMemberMapper.mapRole(RoomMemberRole.MODERATOR, 1L)).isEqualTo(RoomMember.Role.Moderator)
        assertThat(RoomMemberMapper.mapRole(RoomMemberRole.CREATOR, 0L)).isEqualTo(RoomMember.Role.Owner(isCreator = true))
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

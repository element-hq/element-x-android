/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room.member

import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.RoomMembershipState
import uniffi.matrix_sdk.RoomMemberRole
import org.matrix.rustcomponents.sdk.MembershipState as RustMembershipState
import org.matrix.rustcomponents.sdk.RoomMember as RustRoomMember

object RoomMemberMapper {
    fun map(roomMember: RustRoomMember): RoomMember = RoomMember(
        userId = UserId(roomMember.userId),
        displayName = roomMember.displayName,
        avatarUrl = roomMember.avatarUrl,
        membership = mapMembership(roomMember.membership),
        isNameAmbiguous = roomMember.isNameAmbiguous,
        powerLevel = roomMember.powerLevel,
        normalizedPowerLevel = roomMember.normalizedPowerLevel,
        isIgnored = roomMember.isIgnored,
        role = mapRole(roomMember.suggestedRoleForPowerLevel),
    )

    fun mapRole(role: RoomMemberRole): RoomMember.Role =
        when (role) {
            RoomMemberRole.ADMINISTRATOR -> RoomMember.Role.ADMIN
            RoomMemberRole.MODERATOR -> RoomMember.Role.MODERATOR
            RoomMemberRole.USER -> RoomMember.Role.USER
        }

    fun mapMembership(membershipState: RustMembershipState): RoomMembershipState =
        when (membershipState) {
            RustMembershipState.Ban -> RoomMembershipState.BAN
            RustMembershipState.Invite -> RoomMembershipState.INVITE
            RustMembershipState.Join -> RoomMembershipState.JOIN
            RustMembershipState.Knock -> RoomMembershipState.KNOCK
            RustMembershipState.Leave -> RoomMembershipState.LEAVE
            is RustMembershipState.Custom -> TODO()
        }
}

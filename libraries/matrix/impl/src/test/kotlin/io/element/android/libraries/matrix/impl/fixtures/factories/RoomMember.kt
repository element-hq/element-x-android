/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.fixtures.factories

import io.element.android.libraries.matrix.api.core.UserId
import org.matrix.rustcomponents.sdk.MembershipState
import org.matrix.rustcomponents.sdk.PowerLevel
import org.matrix.rustcomponents.sdk.RoomMember
import uniffi.matrix_sdk.RoomMemberRole

fun aRustRoomMember(
    userId: UserId,
    displayName: String? = null,
    avatarUrl: String? = null,
    membership: MembershipState = MembershipState.Join,
    isNameAmbiguous: Boolean = false,
    powerLevel: PowerLevel = PowerLevel.Value(0L),
    isIgnored: Boolean = false,
    role: RoomMemberRole = RoomMemberRole.USER,
    membershipChangeReason: String? = null,
) = RoomMember(
    userId = userId.value,
    displayName = displayName,
    avatarUrl = avatarUrl,
    membership = membership,
    isNameAmbiguous = isNameAmbiguous,
    powerLevel = powerLevel,
    isIgnored = isIgnored,
    suggestedRoleForPowerLevel = role,
    membershipChangeReason = membershipChangeReason,
)

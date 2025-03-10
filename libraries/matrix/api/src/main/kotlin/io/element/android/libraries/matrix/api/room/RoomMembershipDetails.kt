/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.room

/**
 * Room membership details for the current user and the sender of the membership event.
 *
 * It also includes the reason the current user's membership changed, if any.
 */
data class RoomMembershipDetails(
    val currentUserMember: RoomMember,
    val senderMember: RoomMember?,
) {
    val membershipChangeReason: String? = currentUserMember.membershipChangeReason
}

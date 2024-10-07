/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.timeline.item.event

enum class MembershipChange {
    NONE,
    ERROR,
    JOINED,
    LEFT,
    BANNED,
    UNBANNED,
    KICKED,
    INVITED,
    KICKED_AND_BANNED,
    INVITATION_ACCEPTED,
    INVITATION_REJECTED,
    INVITATION_REVOKED,
    KNOCKED,
    KNOCK_ACCEPTED,
    KNOCK_RETRACTED,
    KNOCK_DENIED,
    NOT_IMPLEMENTED
}

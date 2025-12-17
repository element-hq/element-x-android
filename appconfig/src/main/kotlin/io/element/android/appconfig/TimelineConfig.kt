/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.appconfig

import io.element.android.libraries.matrix.api.room.StateEventType

object TimelineConfig {
    const val MAX_READ_RECEIPT_TO_DISPLAY = 3

    /**
     * Event types that will be filtered out from the timeline (i.e. not displayed).
     */
    val excludedEvents = listOf(
        StateEventType.CALL_MEMBER,
        StateEventType.ROOM_ALIASES,
        StateEventType.ROOM_CANONICAL_ALIAS,
        StateEventType.ROOM_GUEST_ACCESS,
        StateEventType.ROOM_HISTORY_VISIBILITY,
        StateEventType.ROOM_JOIN_RULES,
        StateEventType.ROOM_POWER_LEVELS,
        StateEventType.ROOM_SERVER_ACL,
        StateEventType.ROOM_TOMBSTONE,
        StateEventType.SPACE_CHILD,
        StateEventType.SPACE_PARENT,
        StateEventType.POLICY_RULE_ROOM,
        StateEventType.POLICY_RULE_SERVER,
        StateEventType.POLICY_RULE_USER,
    )
}

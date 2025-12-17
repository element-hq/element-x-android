/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.room

enum class StateEventType {
    POLICY_RULE_ROOM,
    POLICY_RULE_SERVER,
    POLICY_RULE_USER,
    CALL_MEMBER,
    ROOM_ALIASES,
    ROOM_AVATAR,
    ROOM_CANONICAL_ALIAS,
    ROOM_CREATE,
    ROOM_ENCRYPTION,
    ROOM_GUEST_ACCESS,
    ROOM_HISTORY_VISIBILITY,
    ROOM_JOIN_RULES,
    ROOM_MEMBER_EVENT,
    ROOM_NAME,
    ROOM_PINNED_EVENTS,
    ROOM_POWER_LEVELS,
    ROOM_SERVER_ACL,
    ROOM_THIRD_PARTY_INVITE,
    ROOM_TOMBSTONE,
    ROOM_TOPIC,
    SPACE_CHILD,
    SPACE_PARENT
}

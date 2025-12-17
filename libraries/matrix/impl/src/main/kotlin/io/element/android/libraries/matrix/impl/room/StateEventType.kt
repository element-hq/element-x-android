/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room

import io.element.android.libraries.matrix.api.room.StateEventType
import org.matrix.rustcomponents.sdk.StateEventType as RustStateEventType

fun StateEventType.map(): RustStateEventType = when (this) {
    StateEventType.POLICY_RULE_ROOM -> RustStateEventType.POLICY_RULE_ROOM
    StateEventType.POLICY_RULE_SERVER -> RustStateEventType.POLICY_RULE_SERVER
    StateEventType.POLICY_RULE_USER -> RustStateEventType.POLICY_RULE_USER
    StateEventType.CALL_MEMBER -> RustStateEventType.CALL_MEMBER
    StateEventType.ROOM_ALIASES -> RustStateEventType.ROOM_ALIASES
    StateEventType.ROOM_AVATAR -> RustStateEventType.ROOM_AVATAR
    StateEventType.ROOM_CANONICAL_ALIAS -> RustStateEventType.ROOM_CANONICAL_ALIAS
    StateEventType.ROOM_CREATE -> RustStateEventType.ROOM_CREATE
    StateEventType.ROOM_ENCRYPTION -> RustStateEventType.ROOM_ENCRYPTION
    StateEventType.ROOM_GUEST_ACCESS -> RustStateEventType.ROOM_GUEST_ACCESS
    StateEventType.ROOM_HISTORY_VISIBILITY -> RustStateEventType.ROOM_HISTORY_VISIBILITY
    StateEventType.ROOM_JOIN_RULES -> RustStateEventType.ROOM_JOIN_RULES
    StateEventType.ROOM_MEMBER_EVENT -> RustStateEventType.ROOM_MEMBER_EVENT
    StateEventType.ROOM_NAME -> RustStateEventType.ROOM_NAME
    StateEventType.ROOM_PINNED_EVENTS -> RustStateEventType.ROOM_PINNED_EVENTS
    StateEventType.ROOM_POWER_LEVELS -> RustStateEventType.ROOM_POWER_LEVELS
    StateEventType.ROOM_SERVER_ACL -> RustStateEventType.ROOM_SERVER_ACL
    StateEventType.ROOM_THIRD_PARTY_INVITE -> RustStateEventType.ROOM_THIRD_PARTY_INVITE
    StateEventType.ROOM_TOMBSTONE -> RustStateEventType.ROOM_TOMBSTONE
    StateEventType.ROOM_TOPIC -> RustStateEventType.ROOM_TOPIC
    StateEventType.SPACE_CHILD -> RustStateEventType.SPACE_CHILD
    StateEventType.SPACE_PARENT -> RustStateEventType.SPACE_PARENT
}

fun RustStateEventType.map(): StateEventType = when (this) {
    RustStateEventType.POLICY_RULE_ROOM -> StateEventType.POLICY_RULE_ROOM
    RustStateEventType.POLICY_RULE_SERVER -> StateEventType.POLICY_RULE_SERVER
    RustStateEventType.POLICY_RULE_USER -> StateEventType.POLICY_RULE_USER
    RustStateEventType.CALL_MEMBER -> StateEventType.CALL_MEMBER
    RustStateEventType.ROOM_ALIASES -> StateEventType.ROOM_ALIASES
    RustStateEventType.ROOM_AVATAR -> StateEventType.ROOM_AVATAR
    RustStateEventType.ROOM_CANONICAL_ALIAS -> StateEventType.ROOM_CANONICAL_ALIAS
    RustStateEventType.ROOM_CREATE -> StateEventType.ROOM_CREATE
    RustStateEventType.ROOM_ENCRYPTION -> StateEventType.ROOM_ENCRYPTION
    RustStateEventType.ROOM_GUEST_ACCESS -> StateEventType.ROOM_GUEST_ACCESS
    RustStateEventType.ROOM_HISTORY_VISIBILITY -> StateEventType.ROOM_HISTORY_VISIBILITY
    RustStateEventType.ROOM_JOIN_RULES -> StateEventType.ROOM_JOIN_RULES
    RustStateEventType.ROOM_MEMBER_EVENT -> StateEventType.ROOM_MEMBER_EVENT
    RustStateEventType.ROOM_NAME -> StateEventType.ROOM_NAME
    RustStateEventType.ROOM_PINNED_EVENTS -> StateEventType.ROOM_PINNED_EVENTS
    RustStateEventType.ROOM_POWER_LEVELS -> StateEventType.ROOM_POWER_LEVELS
    RustStateEventType.ROOM_SERVER_ACL -> StateEventType.ROOM_SERVER_ACL
    RustStateEventType.ROOM_THIRD_PARTY_INVITE -> StateEventType.ROOM_THIRD_PARTY_INVITE
    RustStateEventType.ROOM_TOMBSTONE -> StateEventType.ROOM_TOMBSTONE
    RustStateEventType.ROOM_TOPIC -> StateEventType.ROOM_TOPIC
    RustStateEventType.SPACE_CHILD -> StateEventType.SPACE_CHILD
    RustStateEventType.SPACE_PARENT -> StateEventType.SPACE_PARENT
}

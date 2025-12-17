/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.room.StateEventType
import org.junit.Test
import org.matrix.rustcomponents.sdk.StateEventType as RustStateEventType

class StateEventTypeTest {
    @Test
    fun `mapping Rust type should work`() {
        assertThat(RustStateEventType.CALL_MEMBER.map()).isEqualTo(StateEventType.CALL_MEMBER)
        assertThat(RustStateEventType.POLICY_RULE_ROOM.map()).isEqualTo(StateEventType.POLICY_RULE_ROOM)
        assertThat(RustStateEventType.POLICY_RULE_SERVER.map()).isEqualTo(StateEventType.POLICY_RULE_SERVER)
        assertThat(RustStateEventType.POLICY_RULE_USER.map()).isEqualTo(StateEventType.POLICY_RULE_USER)
        assertThat(RustStateEventType.ROOM_ALIASES.map()).isEqualTo(StateEventType.ROOM_ALIASES)
        assertThat(RustStateEventType.ROOM_AVATAR.map()).isEqualTo(StateEventType.ROOM_AVATAR)
        assertThat(RustStateEventType.ROOM_CANONICAL_ALIAS.map()).isEqualTo(StateEventType.ROOM_CANONICAL_ALIAS)
        assertThat(RustStateEventType.ROOM_CREATE.map()).isEqualTo(StateEventType.ROOM_CREATE)
        assertThat(RustStateEventType.ROOM_ENCRYPTION.map()).isEqualTo(StateEventType.ROOM_ENCRYPTION)
        assertThat(RustStateEventType.ROOM_GUEST_ACCESS.map()).isEqualTo(StateEventType.ROOM_GUEST_ACCESS)
        assertThat(RustStateEventType.ROOM_HISTORY_VISIBILITY.map()).isEqualTo(StateEventType.ROOM_HISTORY_VISIBILITY)
        assertThat(RustStateEventType.ROOM_JOIN_RULES.map()).isEqualTo(StateEventType.ROOM_JOIN_RULES)
        assertThat(RustStateEventType.ROOM_MEMBER_EVENT.map()).isEqualTo(StateEventType.ROOM_MEMBER_EVENT)
        assertThat(RustStateEventType.ROOM_NAME.map()).isEqualTo(StateEventType.ROOM_NAME)
        assertThat(RustStateEventType.ROOM_PINNED_EVENTS.map()).isEqualTo(StateEventType.ROOM_PINNED_EVENTS)
        assertThat(RustStateEventType.ROOM_POWER_LEVELS.map()).isEqualTo(StateEventType.ROOM_POWER_LEVELS)
        assertThat(RustStateEventType.ROOM_SERVER_ACL.map()).isEqualTo(StateEventType.ROOM_SERVER_ACL)
        assertThat(RustStateEventType.ROOM_THIRD_PARTY_INVITE.map()).isEqualTo(StateEventType.ROOM_THIRD_PARTY_INVITE)
        assertThat(RustStateEventType.ROOM_TOMBSTONE.map()).isEqualTo(StateEventType.ROOM_TOMBSTONE)
        assertThat(RustStateEventType.ROOM_TOPIC.map()).isEqualTo(StateEventType.ROOM_TOPIC)
        assertThat(RustStateEventType.SPACE_CHILD.map()).isEqualTo(StateEventType.SPACE_CHILD)
        assertThat(RustStateEventType.SPACE_PARENT.map()).isEqualTo(StateEventType.SPACE_PARENT)
    }

    @Test
    fun `mapping Kotlin type should work`() {
        assertThat(StateEventType.CALL_MEMBER.map()).isEqualTo(RustStateEventType.CALL_MEMBER)
        assertThat(StateEventType.POLICY_RULE_ROOM.map()).isEqualTo(RustStateEventType.POLICY_RULE_ROOM)
        assertThat(StateEventType.POLICY_RULE_SERVER.map()).isEqualTo(RustStateEventType.POLICY_RULE_SERVER)
        assertThat(StateEventType.POLICY_RULE_USER.map()).isEqualTo(RustStateEventType.POLICY_RULE_USER)
        assertThat(StateEventType.ROOM_ALIASES.map()).isEqualTo(RustStateEventType.ROOM_ALIASES)
        assertThat(StateEventType.ROOM_AVATAR.map()).isEqualTo(RustStateEventType.ROOM_AVATAR)
        assertThat(StateEventType.ROOM_CANONICAL_ALIAS.map()).isEqualTo(RustStateEventType.ROOM_CANONICAL_ALIAS)
        assertThat(StateEventType.ROOM_CREATE.map()).isEqualTo(RustStateEventType.ROOM_CREATE)
        assertThat(StateEventType.ROOM_ENCRYPTION.map()).isEqualTo(RustStateEventType.ROOM_ENCRYPTION)
        assertThat(StateEventType.ROOM_GUEST_ACCESS.map()).isEqualTo(RustStateEventType.ROOM_GUEST_ACCESS)
        assertThat(StateEventType.ROOM_HISTORY_VISIBILITY.map()).isEqualTo(RustStateEventType.ROOM_HISTORY_VISIBILITY)
        assertThat(StateEventType.ROOM_JOIN_RULES.map()).isEqualTo(RustStateEventType.ROOM_JOIN_RULES)
        assertThat(StateEventType.ROOM_MEMBER_EVENT.map()).isEqualTo(RustStateEventType.ROOM_MEMBER_EVENT)
        assertThat(StateEventType.ROOM_NAME.map()).isEqualTo(RustStateEventType.ROOM_NAME)
        assertThat(StateEventType.ROOM_PINNED_EVENTS.map()).isEqualTo(RustStateEventType.ROOM_PINNED_EVENTS)
        assertThat(StateEventType.ROOM_POWER_LEVELS.map()).isEqualTo(RustStateEventType.ROOM_POWER_LEVELS)
        assertThat(StateEventType.ROOM_SERVER_ACL.map()).isEqualTo(RustStateEventType.ROOM_SERVER_ACL)
        assertThat(StateEventType.ROOM_THIRD_PARTY_INVITE.map()).isEqualTo(RustStateEventType.ROOM_THIRD_PARTY_INVITE)
        assertThat(StateEventType.ROOM_TOMBSTONE.map()).isEqualTo(RustStateEventType.ROOM_TOMBSTONE)
        assertThat(StateEventType.ROOM_TOPIC.map()).isEqualTo(RustStateEventType.ROOM_TOPIC)
        assertThat(StateEventType.SPACE_CHILD.map()).isEqualTo(RustStateEventType.SPACE_CHILD)
        assertThat(StateEventType.SPACE_PARENT.map()).isEqualTo(RustStateEventType.SPACE_PARENT)
    }
}

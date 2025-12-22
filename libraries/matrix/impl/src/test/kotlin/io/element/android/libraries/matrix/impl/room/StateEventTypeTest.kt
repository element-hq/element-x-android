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
        assertThat(RustStateEventType.CallMember.map()).isEqualTo(StateEventType.CallMember)
        assertThat(RustStateEventType.PolicyRuleRoom.map()).isEqualTo(StateEventType.PolicyRuleRoom)
        assertThat(RustStateEventType.PolicyRuleServer.map()).isEqualTo(StateEventType.PolicyRuleServer)
        assertThat(RustStateEventType.PolicyRuleUser.map()).isEqualTo(StateEventType.PolicyRuleUser)
        assertThat(RustStateEventType.RoomAliases.map()).isEqualTo(StateEventType.RoomAliases)
        assertThat(RustStateEventType.RoomAvatar.map()).isEqualTo(StateEventType.RoomAvatar)
        assertThat(RustStateEventType.RoomCanonicalAlias.map()).isEqualTo(StateEventType.RoomCanonicalAlias)
        assertThat(RustStateEventType.RoomCreate.map()).isEqualTo(StateEventType.RoomCreate)
        assertThat(RustStateEventType.RoomEncryption.map()).isEqualTo(StateEventType.RoomEncryption)
        assertThat(RustStateEventType.RoomGuestAccess.map()).isEqualTo(StateEventType.RoomGuestAccess)
        assertThat(RustStateEventType.RoomHistoryVisibility.map()).isEqualTo(StateEventType.RoomHistoryVisibility)
        assertThat(RustStateEventType.RoomJoinRules.map()).isEqualTo(StateEventType.RoomJoinRules)
        assertThat(RustStateEventType.RoomMemberEvent.map()).isEqualTo(StateEventType.RoomMemberEvent)
        assertThat(RustStateEventType.RoomName.map()).isEqualTo(StateEventType.RoomName)
        assertThat(RustStateEventType.RoomPinnedEvents.map()).isEqualTo(StateEventType.RoomPinnedEvents)
        assertThat(RustStateEventType.RoomPowerLevels.map()).isEqualTo(StateEventType.RoomPowerLevels)
        assertThat(RustStateEventType.RoomServerAcl.map()).isEqualTo(StateEventType.RoomServerAcl)
        assertThat(RustStateEventType.RoomThirdPartyInvite.map()).isEqualTo(StateEventType.RoomThirdPartyInvite)
        assertThat(RustStateEventType.RoomTombstone.map()).isEqualTo(StateEventType.RoomTombstone)
        assertThat(RustStateEventType.RoomTopic.map()).isEqualTo(StateEventType.RoomTopic)
        assertThat(RustStateEventType.SpaceChild.map()).isEqualTo(StateEventType.SpaceChild)
        assertThat(RustStateEventType.SpaceParent.map()).isEqualTo(StateEventType.SpaceParent)
        assertThat(RustStateEventType.Custom("foo").map()).isEqualTo(StateEventType.Custom("foo"))
    }

    @Test
    fun `mapping Kotlin type should work`() {
        assertThat(StateEventType.CallMember.map()).isEqualTo(RustStateEventType.CallMember)
        assertThat(StateEventType.PolicyRuleRoom.map()).isEqualTo(RustStateEventType.PolicyRuleRoom)
        assertThat(StateEventType.PolicyRuleServer.map()).isEqualTo(RustStateEventType.PolicyRuleServer)
        assertThat(StateEventType.PolicyRuleUser.map()).isEqualTo(RustStateEventType.PolicyRuleUser)
        assertThat(StateEventType.RoomAliases.map()).isEqualTo(RustStateEventType.RoomAliases)
        assertThat(StateEventType.RoomAvatar.map()).isEqualTo(RustStateEventType.RoomAvatar)
        assertThat(StateEventType.RoomCanonicalAlias.map()).isEqualTo(RustStateEventType.RoomCanonicalAlias)
        assertThat(StateEventType.RoomCreate.map()).isEqualTo(RustStateEventType.RoomCreate)
        assertThat(StateEventType.RoomEncryption.map()).isEqualTo(RustStateEventType.RoomEncryption)
        assertThat(StateEventType.RoomGuestAccess.map()).isEqualTo(RustStateEventType.RoomGuestAccess)
        assertThat(StateEventType.RoomHistoryVisibility.map()).isEqualTo(RustStateEventType.RoomHistoryVisibility)
        assertThat(StateEventType.RoomJoinRules.map()).isEqualTo(RustStateEventType.RoomJoinRules)
        assertThat(StateEventType.RoomMemberEvent.map()).isEqualTo(RustStateEventType.RoomMemberEvent)
        assertThat(StateEventType.RoomName.map()).isEqualTo(RustStateEventType.RoomName)
        assertThat(StateEventType.RoomPinnedEvents.map()).isEqualTo(RustStateEventType.RoomPinnedEvents)
        assertThat(StateEventType.RoomPowerLevels.map()).isEqualTo(RustStateEventType.RoomPowerLevels)
        assertThat(StateEventType.RoomServerAcl.map()).isEqualTo(RustStateEventType.RoomServerAcl)
        assertThat(StateEventType.RoomThirdPartyInvite.map()).isEqualTo(RustStateEventType.RoomThirdPartyInvite)
        assertThat(StateEventType.RoomTombstone.map()).isEqualTo(RustStateEventType.RoomTombstone)
        assertThat(StateEventType.RoomTopic.map()).isEqualTo(RustStateEventType.RoomTopic)
        assertThat(StateEventType.SpaceChild.map()).isEqualTo(RustStateEventType.SpaceChild)
        assertThat(StateEventType.SpaceParent.map()).isEqualTo(RustStateEventType.SpaceParent)
        assertThat(StateEventType.Custom("foo").map()).isEqualTo(RustStateEventType.Custom("foo"))
    }
}

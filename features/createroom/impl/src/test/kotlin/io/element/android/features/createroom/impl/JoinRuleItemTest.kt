/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.createroom.impl

import com.google.common.truth.Truth.assertThat
import io.element.android.features.createroom.impl.configureroom.JoinRuleItem
import io.element.android.libraries.matrix.api.room.join.AllowRule
import io.element.android.libraries.matrix.api.room.join.JoinRule
import io.element.android.libraries.matrix.test.A_ROOM_ID
import kotlinx.collections.immutable.persistentListOf
import org.junit.Test

class JoinRuleItemTest {
    @Test
    fun `toJoinRule works as expected`() {
        assertThat(JoinRuleItem.Private.toJoinRule()).isEqualTo(JoinRule.Private)
        assertThat(JoinRuleItem.PublicVisibility.Public.toJoinRule()).isEqualTo(JoinRule.Public)
        assertThat(JoinRuleItem.PublicVisibility.AskToJoin.toJoinRule()).isEqualTo(JoinRule.Knock)
        assertThat(JoinRuleItem.PublicVisibility.Restricted(A_ROOM_ID).toJoinRule())
            .isEqualTo(JoinRule.Restricted(persistentListOf(AllowRule.RoomMembership(A_ROOM_ID))))
        assertThat(JoinRuleItem.PublicVisibility.AskToJoinRestricted(A_ROOM_ID).toJoinRule())
            .isEqualTo(JoinRule.KnockRestricted(persistentListOf(AllowRule.RoomMembership(A_ROOM_ID))))
    }
}

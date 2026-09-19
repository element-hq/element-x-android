/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.room.join.JoinRule
import kotlinx.collections.immutable.persistentListOf
import org.junit.Test

class TimelineEventFilterFactoryTest {
    @Test
    fun `membership and profile changes are hidden in unencrypted public rooms`() {
        assertThat(hidesMembershipAndProfileChanges(JoinRule.Public, isEncrypted = false)).isTrue()
    }

    @Test
    fun `membership and profile changes are shown in rooms that are not publicly joinable`() {
        val joinRules = listOf(
            JoinRule.Invite,
            JoinRule.Knock,
            JoinRule.Restricted(persistentListOf()),
            JoinRule.KnockRestricted(persistentListOf()),
            JoinRule.Custom("org.example.rule"),
        )
        for (joinRule in joinRules) {
            assertThat(hidesMembershipAndProfileChanges(joinRule, isEncrypted = false)).isFalse()
        }
    }

    @Test
    fun `membership and profile changes are shown when the room is encrypted or its encryption is unknown`() {
        assertThat(hidesMembershipAndProfileChanges(JoinRule.Public, isEncrypted = true)).isFalse()
        assertThat(hidesMembershipAndProfileChanges(JoinRule.Public, isEncrypted = null)).isFalse()
    }

    @Test
    fun `membership and profile changes are shown when the join rule is unknown`() {
        assertThat(hidesMembershipAndProfileChanges(joinRule = null, isEncrypted = false)).isFalse()
    }
}

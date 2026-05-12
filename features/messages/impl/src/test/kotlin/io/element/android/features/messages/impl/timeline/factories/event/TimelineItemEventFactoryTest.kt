/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.factories.event

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.RoomMembershipState
import io.element.android.libraries.matrix.api.timeline.item.event.ProfileDetails
import io.element.android.libraries.matrix.test.room.aRoomMember
import org.junit.Test

class TimelineItemEventFactoryTest {
    private val eventProfile = ProfileDetails.Ready(
        displayName = "Stale name",
        displayNameAmbiguous = false,
        avatarUrl = "mxc://example.org/stale",
    )

    @Test
    fun `withLiveMemberOverride uses the live member when present`() {
        val liveMember = aRoomMember(
            userId = UserId("@bob:server.org"),
            displayName = "Fresh name",
            avatarUrl = "mxc://example.org/fresh",
        )

        val result = eventProfile.withLiveMemberOverride(liveMember)

        assertThat(result).isEqualTo(
            ProfileDetails.Ready(
                displayName = "Fresh name",
                displayNameAmbiguous = false,
                avatarUrl = "mxc://example.org/fresh",
            )
        )
    }

    @Test
    fun `withLiveMemberOverride falls back to the event snapshot when no live member`() {
        val result = eventProfile.withLiveMemberOverride(liveMember = null)

        assertThat(result).isSameInstanceAs(eventProfile)
    }

    @Test
    fun `withLiveMemberOverride falls back when the live member is banned`() {
        val banned = aRoomMember(
            userId = UserId("@bob:server.org"),
            displayName = "Should be hidden",
            avatarUrl = "mxc://example.org/banned",
            membership = RoomMembershipState.BAN,
        )

        val result = eventProfile.withLiveMemberOverride(banned)

        assertThat(result).isSameInstanceAs(eventProfile)
    }

    @Test
    fun `withLiveMemberOverride propagates display-name ambiguity from the live member`() {
        val liveMember = aRoomMember(
            userId = UserId("@bob:server.org"),
            displayName = "Bob",
            avatarUrl = null,
            isNameAmbiguous = true,
        )

        val result = eventProfile.withLiveMemberOverride(liveMember)

        assertThat(result).isEqualTo(
            ProfileDetails.Ready(
                displayName = "Bob",
                displayNameAmbiguous = true,
                avatarUrl = null,
            )
        )
    }

    @Test
    fun `withLiveMemberOverride starts from Unavailable snapshot and still uses live data`() {
        val liveMember = aRoomMember(
            userId = UserId("@bob:server.org"),
            displayName = "Fresh name",
            avatarUrl = "mxc://example.org/fresh",
        )

        val result = ProfileDetails.Unavailable.withLiveMemberOverride(liveMember)

        assertThat(result).isEqualTo(
            ProfileDetails.Ready(
                displayName = "Fresh name",
                displayNameAmbiguous = false,
                avatarUrl = "mxc://example.org/fresh",
            )
        )
    }
}

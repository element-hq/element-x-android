/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.timeline.item.event

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.UserId
import org.junit.Test

private const val A_USER_ID = "@foo:example.org"
private val aUserId = UserId(A_USER_ID)

class ProfileTimelineDetailsTest {
    @Test
    fun `getDisambiguatedDisplayName of Unavailable should be equal to userId`() {
        assertThat(ProfileTimelineDetails.Unavailable.getDisambiguatedDisplayName(aUserId)).isEqualTo(A_USER_ID)
    }

    @Test
    fun `getDisambiguatedDisplayName of Error should be equal to userId`() {
        assertThat(ProfileTimelineDetails.Error("An error").getDisambiguatedDisplayName(aUserId)).isEqualTo(A_USER_ID)
    }

    @Test
    fun `getDisambiguatedDisplayName of Pending should be equal to userId`() {
        assertThat(ProfileTimelineDetails.Pending.getDisambiguatedDisplayName(aUserId)).isEqualTo(A_USER_ID)
    }

    @Test
    fun `getDisambiguatedDisplayName of Ready without display name should be equal to userId`() {
        assertThat(
            ProfileTimelineDetails.Ready(
                displayName = null,
                displayNameAmbiguous = false,
                avatarUrl = null,
            ).getDisambiguatedDisplayName(aUserId)
        ).isEqualTo(A_USER_ID)
    }

    @Test
    fun `getDisambiguatedDisplayName of Ready with display name should be equal to display name`() {
        assertThat(
            ProfileTimelineDetails.Ready(
                displayName = "Alice",
                displayNameAmbiguous = false,
                avatarUrl = null,
            ).getDisambiguatedDisplayName(aUserId)
        ).isEqualTo("Alice")
    }

    @Test
    fun `getDisambiguatedDisplayName of Ready with display name and ambiguous should be equal to display name with user id`() {
        assertThat(
            ProfileTimelineDetails.Ready(
                displayName = "Alice",
                displayNameAmbiguous = true,
                avatarUrl = null,
            ).getDisambiguatedDisplayName(aUserId)
        ).isEqualTo("Alice ($A_USER_ID)")
    }
}

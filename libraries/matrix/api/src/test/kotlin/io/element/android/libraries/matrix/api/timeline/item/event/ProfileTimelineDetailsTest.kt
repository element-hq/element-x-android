/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
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
        assertThat(ProfileDetails.Unavailable.getDisambiguatedDisplayName(aUserId)).isEqualTo(A_USER_ID)
    }

    @Test
    fun `getDisambiguatedDisplayName of Error should be equal to userId`() {
        assertThat(ProfileDetails.Error("An error").getDisambiguatedDisplayName(aUserId)).isEqualTo(A_USER_ID)
    }

    @Test
    fun `getDisambiguatedDisplayName of Pending should be equal to userId`() {
        assertThat(ProfileDetails.Pending.getDisambiguatedDisplayName(aUserId)).isEqualTo(A_USER_ID)
    }

    @Test
    fun `getDisambiguatedDisplayName of Ready without display name should be equal to userId`() {
        assertThat(
            ProfileDetails.Ready(
                displayName = null,
                displayNameAmbiguous = false,
                avatarUrl = null,
            ).getDisambiguatedDisplayName(aUserId)
        ).isEqualTo(A_USER_ID)
    }

    @Test
    fun `getDisambiguatedDisplayName of Ready with display name should be equal to display name`() {
        assertThat(
            ProfileDetails.Ready(
                displayName = "Alice",
                displayNameAmbiguous = false,
                avatarUrl = null,
            ).getDisambiguatedDisplayName(aUserId)
        ).isEqualTo("Alice")
    }

    @Test
    fun `getDisambiguatedDisplayName of Ready with display name and ambiguous should be equal to display name with user id`() {
        assertThat(
            ProfileDetails.Ready(
                displayName = "Alice",
                displayNameAmbiguous = true,
                avatarUrl = null,
            ).getDisambiguatedDisplayName(aUserId)
        ).isEqualTo("Alice ($A_USER_ID)")
    }
}

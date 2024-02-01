/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

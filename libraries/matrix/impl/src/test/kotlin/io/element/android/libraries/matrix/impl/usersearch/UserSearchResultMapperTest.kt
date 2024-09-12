/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.usersearch

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.user.MatrixSearchUserResults
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.test.A_USER_ID
import kotlinx.collections.immutable.toImmutableList
import org.junit.Test
import org.matrix.rustcomponents.sdk.SearchUsersResults
import org.matrix.rustcomponents.sdk.UserProfile

class UserSearchResultMapperTest {
    @Test
    fun `map limited list`() {
        assertThat(
            UserSearchResultMapper.map(
                SearchUsersResults(
                    results = listOf(UserProfile(A_USER_ID.value, "displayName", "avatarUrl")),
                    limited = true,
                )
            )
        )
            .isEqualTo(
                MatrixSearchUserResults(
                    results = listOf(MatrixUser(A_USER_ID, "displayName", "avatarUrl")).toImmutableList(),
                    limited = true,
                )
            )
    }

    @Test
    fun `map not limited list`() {
        assertThat(
            UserSearchResultMapper.map(
                SearchUsersResults(
                    listOf(UserProfile(A_USER_ID.value, "displayName", "avatarUrl")),
                    false,
                )
            )
        )
            .isEqualTo(
                MatrixSearchUserResults(
                    results = listOf(MatrixUser(A_USER_ID, "displayName", "avatarUrl")).toImmutableList(),
                    limited = false,
                )
            )
    }
}

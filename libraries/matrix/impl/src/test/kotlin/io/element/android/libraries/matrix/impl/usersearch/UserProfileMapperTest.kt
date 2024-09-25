/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.usersearch

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.impl.fixtures.factories.aRustUserProfile
import io.element.android.libraries.matrix.test.A_USER_ID
import org.junit.Test

class UserProfileMapperTest {
    @Test
    fun map() {
        assertThat(UserProfileMapper.map(aRustUserProfile(A_USER_ID.value, "displayName", "avatarUrl")))
            .isEqualTo(MatrixUser(A_USER_ID, "displayName", "avatarUrl"))
    }
}

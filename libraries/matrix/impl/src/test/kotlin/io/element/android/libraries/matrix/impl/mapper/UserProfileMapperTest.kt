/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.mapper

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.impl.fixtures.factories.aRustUserProfile
import io.element.android.libraries.matrix.test.A_USER_ID
import org.junit.Test

class UserProfileMapperTest {
    @Test
    fun map() {
        assertThat(aRustUserProfile(A_USER_ID.value, "displayName", "avatarUrl").map())
            .isEqualTo(MatrixUser(A_USER_ID, "displayName", "avatarUrl"))
    }
}

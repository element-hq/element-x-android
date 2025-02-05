/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.model

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.test.A_USER_ID
import org.junit.Test

class MatrixUserExtensionsTest {
    @Test
    fun `getAvatarData should return the expected value`() {
        val matrixUser = MatrixUser(
            userId = A_USER_ID,
            displayName = "displayName",
            avatarUrl = "avatarUrl",
        )
        val expected = AvatarData(
            id = A_USER_ID.value,
            name = "displayName",
            url = "avatarUrl",
            size = AvatarSize.UserHeader,
        )
        assertThat(matrixUser.getAvatarData(AvatarSize.UserHeader)).isEqualTo(expected)
    }

    @Test
    fun `getBestName should return the display name is available`() {
        val matrixUser = MatrixUser(
            userId = A_USER_ID,
            displayName = "displayName",
        )
        assertThat(matrixUser.getBestName()).isEqualTo("displayName")
    }

    @Test
    fun `getBestName should return the id when name is not available`() {
        val matrixUser = MatrixUser(
            userId = A_USER_ID,
            displayName = null,
        )
        assertThat(matrixUser.getBestName()).isEqualTo(A_USER_ID.value)
    }

    @Test
    fun `getBestName should return the id when name is empty`() {
        val matrixUser = MatrixUser(
            userId = A_USER_ID,
            displayName = "",
        )
        assertThat(matrixUser.getBestName()).isEqualTo(A_USER_ID.value)
    }

    @Test
    fun `getFullName should return the display name is available and the userId`() {
        val matrixUser = MatrixUser(
            userId = A_USER_ID,
            displayName = "displayName",
        )
        assertThat(matrixUser.getFullName()).isEqualTo("displayName (@alice:server.org)")
    }

    @Test
    fun `getBestName should return only the id when name is not available`() {
        val matrixUser = MatrixUser(
            userId = A_USER_ID,
            displayName = null,
        )
        assertThat(matrixUser.getFullName()).isEqualTo(A_USER_ID.value)
    }
}

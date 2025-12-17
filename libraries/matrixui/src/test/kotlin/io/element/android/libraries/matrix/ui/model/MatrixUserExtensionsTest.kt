/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.model

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.withConfigurationAndContext
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MatrixUserExtensionsTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

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
    fun `getFullName should return the display name is available and the userId`() = runTest {
        val matrixUser = MatrixUser(
            userId = A_USER_ID,
            displayName = "displayName",
        )
        moleculeFlow(RecompositionMode.Immediate) {
            withConfigurationAndContext {
                matrixUser.getFullName()
            }
        }.test {
            assertThat(awaitItem()).isEqualTo("displayName (@alice:server.org)")
        }
    }

    @Test
    fun `getBestName should return only the id when name is not available`() = runTest {
        val matrixUser = MatrixUser(
            userId = A_USER_ID,
            displayName = null,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            matrixUser.getFullName()
        }.test {
            assertThat(awaitItem()).isEqualTo(A_USER_ID.value)
        }
    }
}

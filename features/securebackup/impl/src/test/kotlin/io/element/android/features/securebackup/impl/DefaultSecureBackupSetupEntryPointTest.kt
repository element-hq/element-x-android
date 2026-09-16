/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securebackup.impl

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.testing.junit4.util.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import io.element.android.features.securebackup.api.SecureBackupSetupEntryPoint
import io.element.android.features.securebackup.impl.setup.SecureBackupSetupNode
import io.element.android.features.securebackup.impl.setup.SecureBackupSetupPresenter
import io.element.android.features.securebackup.impl.setup.SecureBackupSetupStateMachine
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import io.element.android.libraries.matrix.test.encryption.FakeEncryptionService
import io.element.android.tests.testutils.node.TestParentNode
import org.junit.Rule
import org.junit.Test

class DefaultSecureBackupSetupEntryPointTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `test node builder`() {
        val entryPoint = DefaultSecureBackupSetupEntryPoint()
        val parentNode = TestParentNode.create { buildContext, plugins ->
            SecureBackupSetupNode(
                buildContext = buildContext,
                plugins = plugins,
                presenterFactory = object : SecureBackupSetupPresenter.Factory {
                    override fun create(isChangeRecoveryKeyUserStory: Boolean) = SecureBackupSetupPresenter(
                        isChangeRecoveryKeyUserStory = isChangeRecoveryKeyUserStory,
                        stateMachine = SecureBackupSetupStateMachine(),
                        encryptionService = FakeEncryptionService(),
                    )
                },
                snackbarDispatcher = SnackbarDispatcher(),
            )
        }
        val inputs = SecureBackupSetupEntryPoint.Inputs(isChangeRecoveryKeyUserStory = true)
        val result = entryPoint.createNode(
            parentNode = parentNode,
            buildContext = BuildContext.root(null),
            inputs = inputs,
        )
        assertThat(result).isInstanceOf(SecureBackupSetupNode::class.java)
        assertThat(result.plugins).contains(SecureBackupSetupNode.Inputs(isChangeRecoveryKeyUserStory = true))
    }
}

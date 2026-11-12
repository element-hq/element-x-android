/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securebackup.impl

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.bumble.appyx.core.modality.BuildContext
import com.google.common.truth.Truth.assertThat
import io.element.android.features.securebackup.api.SecureBackupRecoveryKeyEntryPoint
import io.element.android.features.securebackup.impl.check.SecureBackupCheckKeyNode
import io.element.android.features.securebackup.impl.check.SecureBackupCheckKeyPresenter
import io.element.android.features.securebackup.impl.generate.SecureBackupGenerateKeyNode
import io.element.android.features.securebackup.impl.generate.SecureBackupGenerateKeyPresenter
import io.element.android.features.securebackup.impl.tools.RecoveryKeyTools
import io.element.android.libraries.androidutils.clipboard.FakeClipboardHelper
import io.element.android.libraries.androidutils.toast.FakeToastHelper
import io.element.android.libraries.matrix.test.encryption.FakeEncryptionService
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.node.TestParentNode
import org.junit.Rule
import org.junit.Test

class DefaultSecureBackupRecoveryKeyEntryPointTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val callback = object : SecureBackupRecoveryKeyEntryPoint.Callback {
        override fun onDone() = lambdaError()
    }

    @Test
    fun `test node builder for generating a key`() {
        val entryPoint = DefaultSecureBackupRecoveryKeyEntryPoint()
        val parentNode = TestParentNode.create { buildContext, plugins ->
            SecureBackupGenerateKeyNode(
                buildContext = buildContext,
                plugins = plugins,
                presenterFactory = object : SecureBackupGenerateKeyPresenter.Factory {
                    override fun create(isReplacingExistingKey: Boolean) = SecureBackupGenerateKeyPresenter(
                        isReplacingExistingKey = isReplacingExistingKey,
                        encryptionService = FakeEncryptionService(),
                        clipboardHelper = FakeClipboardHelper(),
                        toastHelper = FakeToastHelper(),
                    )
                },
            )
        }
        val result = entryPoint.createNode(
            parentNode = parentNode,
            buildContext = BuildContext.root(null),
            inputs = SecureBackupRecoveryKeyEntryPoint.Inputs(
                mode = SecureBackupRecoveryKeyEntryPoint.Mode.Generate(isReplacingExistingKey = true),
                doneButtonText = 1,
            ),
            callback = callback,
        )
        assertThat(result).isInstanceOf(SecureBackupGenerateKeyNode::class.java)
        assertThat(result.plugins).contains(callback)
        assertThat(result.plugins).contains(SecureBackupGenerateKeyNode.Inputs(isReplacingExistingKey = true, doneButtonText = 1))
    }

    @Test
    fun `test node builder for checking a key`() {
        val entryPoint = DefaultSecureBackupRecoveryKeyEntryPoint()
        val parentNode = TestParentNode.create { buildContext, plugins ->
            SecureBackupCheckKeyNode(
                buildContext = buildContext,
                plugins = plugins,
                presenter = SecureBackupCheckKeyPresenter(
                    encryptionService = FakeEncryptionService(),
                    recoveryKeyTools = RecoveryKeyTools(),
                ),
            )
        }
        val result = entryPoint.createNode(
            parentNode = parentNode,
            buildContext = BuildContext.root(null),
            inputs = SecureBackupRecoveryKeyEntryPoint.Inputs(
                mode = SecureBackupRecoveryKeyEntryPoint.Mode.Check,
                doneButtonText = 2,
            ),
            callback = callback,
        )
        assertThat(result).isInstanceOf(SecureBackupCheckKeyNode::class.java)
        assertThat(result.plugins).contains(callback)
        assertThat(result.plugins).contains(SecureBackupCheckKeyNode.Inputs(doneButtonText = 2))
    }
}

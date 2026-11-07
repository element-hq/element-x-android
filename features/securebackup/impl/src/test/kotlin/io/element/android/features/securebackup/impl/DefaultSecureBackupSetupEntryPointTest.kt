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
import io.element.android.features.securebackup.impl.generate.SecureBackupGenerateKeyNode
import io.element.android.features.securebackup.impl.generate.SecureBackupGenerateKeyPresenter
import io.element.android.libraries.androidutils.clipboard.FakeClipboardHelper
import io.element.android.libraries.androidutils.toast.FakeToastHelper
import io.element.android.libraries.matrix.test.encryption.FakeEncryptionService
import io.element.android.libraries.ui.strings.CommonStrings
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
        val inputs = SecureBackupSetupEntryPoint.Inputs(isChangeRecoveryKeyUserStory = true)
        val result = entryPoint.createNode(
            parentNode = parentNode,
            buildContext = BuildContext.root(null),
            inputs = inputs,
        )
        assertThat(result).isInstanceOf(SecureBackupGenerateKeyNode::class.java)
        assertThat(result.plugins).contains(
            SecureBackupGenerateKeyNode.Inputs(isReplacingExistingKey = true, doneButtonText = CommonStrings.action_done)
        )
    }
}

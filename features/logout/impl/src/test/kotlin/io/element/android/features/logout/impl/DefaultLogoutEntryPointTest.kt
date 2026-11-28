/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.logout.impl

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.testing.junit4.util.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import io.element.android.features.logout.api.LogoutEntryPoint
import io.element.android.features.securebackup.api.SecureBackupRecoveryKeyEntryPoint
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.node.TestParentNode
import org.junit.Rule
import org.junit.Test

class DefaultLogoutEntryPointTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `test node builder`() {
        val entryPoint = DefaultLogoutEntryPoint()

        val parentNode = TestParentNode.create { buildContext, plugins ->
            LogoutFlowNode(
                buildContext = buildContext,
                plugins = plugins,
                recoveryKeyEntryPoint = object : SecureBackupRecoveryKeyEntryPoint {
                    override fun createNode(
                        parentNode: Node,
                        buildContext: BuildContext,
                        inputs: SecureBackupRecoveryKeyEntryPoint.Inputs,
                        callback: SecureBackupRecoveryKeyEntryPoint.Callback,
                    ): Node = lambdaError()
                },
            )
        }
        val callback = object : LogoutEntryPoint.Callback {
            override fun navigateToSecureBackup() = lambdaError()
            override fun onDismiss() = lambdaError()
        }
        val result = entryPoint.createNode(
            parentNode = parentNode,
            buildContext = BuildContext.root(null),
            callback = callback,
        )
        assertThat(result).isInstanceOf(LogoutFlowNode::class.java)
        assertThat(result.plugins).contains(callback)
    }
}

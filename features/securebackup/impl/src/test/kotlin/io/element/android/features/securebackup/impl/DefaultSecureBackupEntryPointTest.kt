/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securebackup.impl

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bumble.appyx.core.modality.BuildContext
import com.google.common.truth.Truth.assertThat
import io.element.android.features.securebackup.api.SecureBackupEntryPoint
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.node.TestParentNode
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DefaultSecureBackupEntryPointTest {
    @Test
    fun `test node builder`() {
        val entryPoint = DefaultSecureBackupEntryPoint()
        val parentNode = TestParentNode.create { buildContext, plugins ->
            SecureBackupFlowNode(
                buildContext = buildContext,
                plugins = plugins,
            )
        }
        val callback = object : SecureBackupEntryPoint.Callback {
            override fun onDone() = lambdaError()
        }
        val params = SecureBackupEntryPoint.Params(SecureBackupEntryPoint.InitialTarget.ResetIdentity)
        val result = entryPoint.nodeBuilder(parentNode, BuildContext.root(null))
            .params(params)
            .callback(callback)
            .build()
        assertThat(result).isInstanceOf(SecureBackupFlowNode::class.java)
        assertThat(result.plugins).contains(params)
        assertThat(result.plugins).contains(callback)
    }
}

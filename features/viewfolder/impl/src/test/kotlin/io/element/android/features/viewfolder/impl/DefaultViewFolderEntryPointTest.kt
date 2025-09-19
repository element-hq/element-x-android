/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.viewfolder.impl

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.testing.junit4.util.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import io.element.android.features.viewfolder.api.ViewFolderEntryPoint
import io.element.android.features.viewfolder.impl.root.ViewFolderFlowNode
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.node.TestParentNode
import org.junit.Rule
import org.junit.Test

class DefaultViewFolderEntryPointTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `test node builder`() {
        val entryPoint = DefaultViewFolderEntryPoint()
        val parentNode = TestParentNode.create { buildContext, plugins ->
            ViewFolderFlowNode(
                buildContext = buildContext,
                plugins = plugins,
            )
        }
        val callback = object : ViewFolderEntryPoint.Callback {
            override fun onDone() = lambdaError()
        }
        val params = ViewFolderEntryPoint.Params(
            rootPath = "path",
        )
        val result = entryPoint.nodeBuilder(parentNode, BuildContext.root(null))
            .params(params)
            .callback(callback)
            .build()
        assertThat(result).isInstanceOf(ViewFolderFlowNode::class.java)
        assertThat(result.plugins).contains(ViewFolderFlowNode.Inputs(params.rootPath))
        assertThat(result.plugins).contains(callback)
    }
}

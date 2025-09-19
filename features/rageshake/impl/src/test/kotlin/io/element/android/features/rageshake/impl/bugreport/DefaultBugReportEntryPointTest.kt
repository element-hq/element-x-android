/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rageshake.impl.bugreport

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.testing.junit4.util.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import io.element.android.features.rageshake.api.bugreport.BugReportEntryPoint
import io.element.android.features.viewfolder.api.ViewFolderEntryPoint
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.node.TestParentNode
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class DefaultBugReportEntryPointTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `test node builder`() = runTest {
        val entryPoint = DefaultBugReportEntryPoint()
        val parentNode = TestParentNode.create { buildContext, plugins ->
            BugReportFlowNode(
                buildContext = buildContext,
                plugins = plugins,
                viewFolderEntryPoint = object : ViewFolderEntryPoint {
                    override fun nodeBuilder(parentNode: Node, buildContext: BuildContext) = lambdaError()
                },
            )
        }
        val callback = object : BugReportEntryPoint.Callback {
            override fun onDone() = lambdaError()
        }
        val result = entryPoint.nodeBuilder(parentNode, BuildContext.root(null))
            .callback(callback)
            .build()
        assertThat(result).isInstanceOf(BugReportFlowNode::class.java)
        assertThat(result.plugins).contains(callback)
    }
}

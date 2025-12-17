/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rageshake.impl.bugreport

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.testing.junit4.util.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import io.element.android.features.rageshake.api.bugreport.BugReportEntryPoint
import io.element.android.features.viewfolder.test.FakeViewFolderEntryPoint
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
                viewFolderEntryPoint = FakeViewFolderEntryPoint(),
            )
        }
        val callback = object : BugReportEntryPoint.Callback {
            override fun onDone() = lambdaError()
        }
        val result = entryPoint.createNode(
            parentNode = parentNode,
            buildContext = BuildContext.root(null),
            callback = callback,
        )
        assertThat(result).isInstanceOf(BugReportFlowNode::class.java)
        assertThat(result.plugins).contains(callback)
    }
}

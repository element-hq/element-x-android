/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.poll.impl.history

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.google.common.truth.Truth.assertThat
import io.element.android.features.poll.api.create.CreatePollEntryPoint
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.node.TestParentNode
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DefaultPollHistoryEntryPointTest {
    @Test
    fun `test node builder`() = runTest {
        val entryPoint = DefaultPollHistoryEntryPoint()
        val parentNode = TestParentNode.create { buildContext, plugins ->
            PollHistoryFlowNode(
                buildContext = buildContext,
                plugins = plugins,
                createPollEntryPoint = object : CreatePollEntryPoint {
                    override fun nodeBuilder(parentNode: Node, buildContext: BuildContext) = lambdaError()
                }
            )
        }
        val result = entryPoint.createNode(parentNode, BuildContext.root(null))
        assertThat(result).isInstanceOf(PollHistoryFlowNode::class.java)
    }
}

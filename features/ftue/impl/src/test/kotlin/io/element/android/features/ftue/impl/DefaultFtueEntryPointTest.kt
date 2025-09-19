/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ftue.impl

import android.content.Context
import android.content.Intent
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.testing.junit4.util.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import io.element.android.features.lockscreen.api.LockScreenEntryPoint
import io.element.android.services.analytics.test.FakeAnalyticsService
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.node.TestParentNode
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class DefaultFtueEntryPointTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `test node builder`() = runTest {
        val entryPoint = DefaultFtueEntryPoint()
        val parentNode = TestParentNode.create { buildContext, plugins ->
            FtueFlowNode(
                buildContext = buildContext,
                plugins = plugins,
                analyticsEntryPoint = { _, _ -> lambdaError() },
                defaultFtueService = createDefaultFtueService(),
                analyticsService = FakeAnalyticsService(),
                lockScreenEntryPoint = object : LockScreenEntryPoint {
                    override fun nodeBuilder(
                        parentNode: com.bumble.appyx.core.node.Node,
                        buildContext: BuildContext,
                        navTarget: LockScreenEntryPoint.Target
                    ): LockScreenEntryPoint.NodeBuilder {
                        lambdaError()
                    }

                    override fun pinUnlockIntent(context: Context): Intent {
                        lambdaError()
                    }
                },
            )
        }
        val result = entryPoint.createNode(parentNode, BuildContext.root(null))
        assertThat(result).isInstanceOf(FtueFlowNode::class.java)
    }
}

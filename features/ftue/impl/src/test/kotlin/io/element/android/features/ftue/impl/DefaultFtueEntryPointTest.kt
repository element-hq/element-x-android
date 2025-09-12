/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ftue.impl

import android.content.Context
import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bumble.appyx.core.modality.BuildContext
import com.google.common.truth.Truth.assertThat
import io.element.android.features.lockscreen.api.LockScreenEntryPoint
import io.element.android.services.analytics.test.FakeAnalyticsService
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.node.TestParentNode
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DefaultFtueEntryPointTest {
    @Test
    fun `test node builder`() = runTest {
        val entryPoint = DefaultFtueEntryPoint()
        val parentNode = TestParentNode.create { buildContext, plugins ->
            FtueFlowNode(
                buildContext = buildContext,
                plugins = plugins,
                analyticsEntryPoint = { _, _ -> lambdaError() },
                ftueState = createDefaultFtueService(),
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
        val result = entryPoint.nodeBuilder(parentNode, BuildContext.root(null))
            .build()
        assertThat(result).isInstanceOf(FtueFlowNode::class.java)
    }
}

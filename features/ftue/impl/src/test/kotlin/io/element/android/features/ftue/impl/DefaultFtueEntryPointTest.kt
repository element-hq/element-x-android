/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ftue.impl

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.testing.junit4.util.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import io.element.android.features.lockscreen.test.FakeLockScreenEntryPoint
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
                lockScreenEntryPoint = FakeLockScreenEntryPoint(),
            )
        }
        val result = entryPoint.createNode(parentNode, BuildContext.root(null))
        assertThat(result).isInstanceOf(FtueFlowNode::class.java)
    }
}

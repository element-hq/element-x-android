/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.lockscreen.impl

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.testing.junit4.util.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import io.element.android.features.lockscreen.api.LockScreenEntryPoint
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.node.TestParentNode
import org.junit.Rule
import org.junit.Test

class DefaultLockScreenEntryPointTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `test node builder Setup`() {
        val entryPoint = DefaultLockScreenEntryPoint()
        val parentNode = TestParentNode.create { buildContext, plugins ->
            LockScreenFlowNode(
                buildContext = buildContext,
                plugins = plugins,
            )
        }
        val callback = object : LockScreenEntryPoint.Callback {
            override fun onSetupDone() = lambdaError()
        }
        val navTarget = LockScreenEntryPoint.Target.Setup
        val result = entryPoint.createNode(
            parentNode = parentNode,
            buildContext = BuildContext.root(null),
            navTarget = navTarget,
            callback = callback,
        )
        assertThat(result).isInstanceOf(LockScreenFlowNode::class.java)
        assertThat(result.plugins).contains(LockScreenFlowNode.Inputs(LockScreenFlowNode.NavTarget.Setup))
        assertThat(result.plugins).contains(callback)
    }

    @Test
    fun `test node builder Settings`() {
        val entryPoint = DefaultLockScreenEntryPoint()
        val parentNode = TestParentNode.create { buildContext, plugins ->
            LockScreenFlowNode(
                buildContext = buildContext,
                plugins = plugins,
            )
        }
        val callback = object : LockScreenEntryPoint.Callback {
            override fun onSetupDone() = lambdaError()
        }
        val navTarget = LockScreenEntryPoint.Target.Settings
        val result = entryPoint.createNode(
            parentNode = parentNode,
            buildContext = BuildContext.root(null),
            navTarget = navTarget,
            callback = callback,
        )
        assertThat(result).isInstanceOf(LockScreenFlowNode::class.java)
        assertThat(result.plugins).contains(LockScreenFlowNode.Inputs(LockScreenFlowNode.NavTarget.Settings))
        assertThat(result.plugins).contains(callback)
    }
}

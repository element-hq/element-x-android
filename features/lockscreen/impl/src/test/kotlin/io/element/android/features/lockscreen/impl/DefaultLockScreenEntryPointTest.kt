/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.lockscreen.impl

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.bumble.appyx.core.modality.BuildContext
import com.google.common.truth.Truth.assertThat
import io.element.android.features.lockscreen.api.LockScreenEntryPoint
import io.element.android.features.lockscreen.impl.unlock.activity.PinUnlockActivity
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.node.TestParentNode
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DefaultLockScreenEntryPointTest {
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
        val result = entryPoint.nodeBuilder(parentNode, BuildContext.root(null), navTarget)
            .callback(callback)
            .build()
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
        val result = entryPoint.nodeBuilder(parentNode, BuildContext.root(null), navTarget)
            .callback(callback)
            .build()
        assertThat(result).isInstanceOf(LockScreenFlowNode::class.java)
        assertThat(result.plugins).contains(LockScreenFlowNode.Inputs(LockScreenFlowNode.NavTarget.Settings))
        assertThat(result.plugins).contains(callback)
    }

    @Test
    fun `test pin unlock intent`() {
        val entryPoint = DefaultLockScreenEntryPoint()
        val result = entryPoint.pinUnlockIntent(InstrumentationRegistry.getInstrumentation().context)
        assertThat(result.component?.className).isEqualTo(PinUnlockActivity::class.qualifiedName)
    }
}

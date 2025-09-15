/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.troubleshoot.impl

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.bumble.appyx.core.modality.BuildContext
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.troubleshoot.api.NotificationTroubleShootEntryPoint
import io.element.android.services.analytics.test.FakeScreenTracker
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.node.TestParentNode
import org.junit.Rule
import org.junit.Test

class DefaultNotificationTroubleShootEntryPointTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun `test node builder`() {
        val entryPoint = DefaultNotificationTroubleShootEntryPoint()
        val parentNode = TestParentNode.create { buildContext, plugins ->
            TroubleshootNotificationsNode(
                buildContext = buildContext,
                plugins = plugins,
                presenter = createTroubleshootNotificationsPresenter(),
                screenTracker = FakeScreenTracker(),
            )
        }
        val callback = object : NotificationTroubleShootEntryPoint.Callback {
            override fun onDone() = lambdaError()
        }
        val result = entryPoint.nodeBuilder(parentNode, BuildContext.root(null))
            .callback(callback)
            .build()
        assertThat(result).isInstanceOf(TroubleshootNotificationsNode::class.java)
        assertThat(result.plugins).contains(callback)
    }
}

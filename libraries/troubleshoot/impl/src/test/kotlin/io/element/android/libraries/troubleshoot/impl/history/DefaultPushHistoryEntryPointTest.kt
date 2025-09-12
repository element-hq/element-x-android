/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.troubleshoot.impl.history

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bumble.appyx.core.modality.BuildContext
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.push.test.FakePushService
import io.element.android.libraries.troubleshoot.api.PushHistoryEntryPoint
import io.element.android.services.analytics.test.FakeScreenTracker
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.node.TestParentNode
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DefaultPushHistoryEntryPointTest {
    @Test
    fun `test node builder`() {
        val entryPoint = DefaultPushHistoryEntryPoint()
        val parentNode = TestParentNode.create { buildContext, plugins ->
            PushHistoryNode(
                buildContext = buildContext,
                plugins = plugins,
                presenter = PushHistoryPresenter(
                    pushService = FakePushService(),
                ),
                screenTracker = FakeScreenTracker(),
            )
        }
        val callback = object : PushHistoryEntryPoint.Callback {
            override fun onDone() = lambdaError()
            override fun onItemClick(sessionId: SessionId, roomId: RoomId, eventId: EventId) = lambdaError()
        }
        val result = entryPoint.nodeBuilder(parentNode, BuildContext.root(null))
            .callback(callback)
            .build()
        assertThat(result).isInstanceOf(PushHistoryNode::class.java)
        assertThat(result.plugins).contains(callback)
    }
}

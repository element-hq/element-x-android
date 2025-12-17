/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.troubleshoot.impl.history

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.bumble.appyx.core.modality.BuildContext
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.push.test.FakePushService
import io.element.android.libraries.troubleshoot.api.PushHistoryEntryPoint
import io.element.android.services.analytics.test.FakeScreenTracker
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.node.TestParentNode
import org.junit.Rule
import org.junit.Test

class DefaultPushHistoryEntryPointTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun `test node builder`() {
        val entryPoint = DefaultPushHistoryEntryPoint()
        val parentNode = TestParentNode.create { buildContext, plugins ->
            PushHistoryNode(
                buildContext = buildContext,
                plugins = plugins,
                presenterFactory = {
                    PushHistoryPresenter(
                        pushHistoryNavigator = object : PushHistoryNavigator {
                            override fun navigateTo(roomId: RoomId, eventId: EventId) = lambdaError()
                        },
                        pushService = FakePushService(),
                        matrixClient = FakeMatrixClient(),
                    )
                },
                screenTracker = FakeScreenTracker(),
            )
        }
        val callback = object : PushHistoryEntryPoint.Callback {
            override fun onDone() = lambdaError()
            override fun navigateToEvent(roomId: RoomId, eventId: EventId) = lambdaError()
        }
        val result = entryPoint.createNode(
            parentNode = parentNode,
            buildContext = BuildContext.root(null),
            callback = callback,
        )
        assertThat(result).isInstanceOf(PushHistoryNode::class.java)
        assertThat(result.plugins).contains(callback)
    }
}

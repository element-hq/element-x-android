/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messagesearch.impl

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.bumble.appyx.core.modality.BuildContext
import com.google.common.truth.Truth.assertThat
import io.element.android.features.messagesearch.api.MessageSearchEntryPoint
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.node.TestParentNode
import org.junit.Rule
import org.junit.Test

class DefaultMessageSearchEntryPointTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun `test node builder - room scoped`() {
        assertNodeBuiltWith(roomId = A_ROOM_ID)
    }

    @Test
    fun `test node builder - global`() {
        assertNodeBuiltWith(roomId = null)
    }

    private fun assertNodeBuiltWith(roomId: RoomId?) {
        val entryPoint = DefaultMessageSearchEntryPoint()
        val callback = object : MessageSearchEntryPoint.Callback {
            override fun navigateToEvent(roomId: RoomId, eventId: EventId) = lambdaError()
        }
        val parentNode = TestParentNode.create { buildContext, plugins ->
            MessageSearchNode(
                buildContext = buildContext,
                plugins = plugins,
                presenterFactory = { createdRoomId ->
                    assertThat(createdRoomId).isEqualTo(roomId)
                    createMessageSearchPresenter(roomId = createdRoomId)
                },
            )
        }
        val result = entryPoint.createNode(
            parentNode = parentNode,
            buildContext = BuildContext.root(null),
            roomId = roomId,
            callback = callback,
        )
        assertThat(result).isInstanceOf(MessageSearchNode::class.java)
        assertThat(result.plugins).contains(MessageSearchNode.Inputs(roomId))
        assertThat(result.plugins).contains(callback)
    }
}

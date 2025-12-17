/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.space.impl

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.testing.junit4.util.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import io.element.android.features.space.api.SpaceEntryPoint
import io.element.android.features.space.impl.di.FakeSpaceFlowGraph
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.room.FakeJoinedRoom
import io.element.android.libraries.matrix.test.spaces.FakeSpaceRoomList
import io.element.android.libraries.matrix.test.spaces.FakeSpaceService
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.node.TestParentNode
import org.junit.Rule
import org.junit.Test

class DefaultSpaceEntryPointTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `test node builder`() {
        val entryPoint = DefaultSpaceEntryPoint()
        val nodeInputs = SpaceEntryPoint.Inputs(A_ROOM_ID)
        val parentNode = TestParentNode.create { buildContext, plugins ->
            SpaceFlowNode(
                buildContext = buildContext,
                plugins = plugins,
                spaceService = FakeSpaceService(
                    spaceRoomListResult = { _: RoomId -> FakeSpaceRoomList(A_ROOM_ID) }
                ),
                room = FakeJoinedRoom(),
                graphFactory = FakeSpaceFlowGraph.Factory
            )
        }
        val callback = object : SpaceEntryPoint.Callback {
            override fun navigateToRoom(roomId: RoomId, viaParameters: List<String>) = lambdaError()
            override fun navigateToRoomMemberList() = lambdaError()
        }
        val result = entryPoint.createNode(
            parentNode = parentNode,
            buildContext = BuildContext.root(null),
            inputs = nodeInputs,
            callback = callback,
        )
        assertThat(result).isInstanceOf(SpaceFlowNode::class.java)
        assertThat(result.plugins).contains(nodeInputs)
        assertThat(result.plugins).contains(callback)
    }
}

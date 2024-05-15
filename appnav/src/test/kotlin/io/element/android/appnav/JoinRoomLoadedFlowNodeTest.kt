/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.appnav

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.node.node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.navmodel.backstack.activeElement
import com.bumble.appyx.testing.junit4.util.MainDispatcherRule
import com.bumble.appyx.testing.unit.common.helper.parentNodeTestHelper
import com.google.common.truth.Truth.assertThat
import io.element.android.appnav.di.RoomComponentFactory
import io.element.android.appnav.room.RoomNavigationTarget
import io.element.android.appnav.room.joined.JoinedRoomLoadedFlowNode
import io.element.android.features.messages.api.MessagesEntryPoint
import io.element.android.features.roomdetails.api.RoomDetailsEntryPoint
import io.element.android.libraries.architecture.childNode
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.services.appnavstate.test.FakeAppNavigationStateService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class JoinRoomLoadedFlowNodeTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private class FakeMessagesEntryPoint : MessagesEntryPoint, MessagesEntryPoint.NodeBuilder {
        var buildContext: BuildContext? = null
        var nodeId: String? = null
        var parameters: MessagesEntryPoint.Params? = null
        var callback: MessagesEntryPoint.Callback? = null

        override fun nodeBuilder(parentNode: Node, buildContext: BuildContext): MessagesEntryPoint.NodeBuilder {
            this.buildContext = buildContext
            return this
        }

        override fun params(params: MessagesEntryPoint.Params): MessagesEntryPoint.NodeBuilder {
            parameters = params
            return this
        }

        override fun callback(callback: MessagesEntryPoint.Callback): MessagesEntryPoint.NodeBuilder {
            this.callback = callback
            return this
        }

        override fun build(): Node {
            return node(buildContext!!) {}.also {
                nodeId = it.id
            }
        }
    }

    private class FakeRoomComponentFactory : RoomComponentFactory {
        override fun create(room: MatrixRoom): Any {
            return Unit
        }
    }

    private class FakeRoomDetailsEntryPoint : RoomDetailsEntryPoint {
        var nodeId: String? = null

        override fun nodeBuilder(parentNode: Node, buildContext: BuildContext): RoomDetailsEntryPoint.NodeBuilder {
            return object : RoomDetailsEntryPoint.NodeBuilder {
                override fun params(params: RoomDetailsEntryPoint.Params): RoomDetailsEntryPoint.NodeBuilder {
                    return this
                }

                override fun callback(callback: RoomDetailsEntryPoint.Callback): RoomDetailsEntryPoint.NodeBuilder {
                    return this
                }

                override fun build(): Node {
                    return node(buildContext) {}.also {
                        nodeId = it.id
                    }
                }
            }
        }
    }

    private fun createJoinedRoomLoadedFlowNode(
        plugins: List<Plugin>,
        messagesEntryPoint: MessagesEntryPoint = FakeMessagesEntryPoint(),
        roomDetailsEntryPoint: RoomDetailsEntryPoint = FakeRoomDetailsEntryPoint(),
        coroutineScope: CoroutineScope,
    ) = JoinedRoomLoadedFlowNode(
        buildContext = BuildContext.root(savedStateMap = null),
        plugins = plugins,
        messagesEntryPoint = messagesEntryPoint,
        roomDetailsEntryPoint = roomDetailsEntryPoint,
        appNavigationStateService = FakeAppNavigationStateService(),
        appCoroutineScope = coroutineScope,
        roomComponentFactory = FakeRoomComponentFactory(),
        matrixClient = FakeMatrixClient(),
    )

    @Test
    fun `given a room flow node when initialized then it loads messages entry point`() = runTest {
        // GIVEN
        val room = FakeMatrixRoom()
        val fakeMessagesEntryPoint = FakeMessagesEntryPoint()
        val inputs = JoinedRoomLoadedFlowNode.Inputs(room, RoomNavigationTarget.Messages())
        val roomFlowNode = createJoinedRoomLoadedFlowNode(
            plugins = listOf(inputs),
            messagesEntryPoint = fakeMessagesEntryPoint,
            coroutineScope = this
        )
        // WHEN
        val roomFlowNodeTestHelper = roomFlowNode.parentNodeTestHelper()

        // THEN
        assertThat(roomFlowNode.backstack.activeElement).isEqualTo(JoinedRoomLoadedFlowNode.NavTarget.Messages())
        roomFlowNodeTestHelper.assertChildHasLifecycle(JoinedRoomLoadedFlowNode.NavTarget.Messages(), Lifecycle.State.CREATED)
        val messagesNode = roomFlowNode.childNode(JoinedRoomLoadedFlowNode.NavTarget.Messages())!!
        assertThat(messagesNode.id).isEqualTo(fakeMessagesEntryPoint.nodeId)
    }

    @Test
    fun `given a room flow node when callback on room details is triggered then it loads room details entry point`() = runTest {
        // GIVEN
        val room = FakeMatrixRoom()
        val fakeMessagesEntryPoint = FakeMessagesEntryPoint()
        val fakeRoomDetailsEntryPoint = FakeRoomDetailsEntryPoint()
        val inputs = JoinedRoomLoadedFlowNode.Inputs(room, RoomNavigationTarget.Messages())
        val roomFlowNode = createJoinedRoomLoadedFlowNode(
            plugins = listOf(inputs),
            messagesEntryPoint = fakeMessagesEntryPoint,
            roomDetailsEntryPoint = fakeRoomDetailsEntryPoint,
            coroutineScope = this
        )
        val roomFlowNodeTestHelper = roomFlowNode.parentNodeTestHelper()
        // WHEN
        fakeMessagesEntryPoint.callback?.onRoomDetailsClicked()
        // THEN
        roomFlowNodeTestHelper.assertChildHasLifecycle(JoinedRoomLoadedFlowNode.NavTarget.RoomDetails, Lifecycle.State.CREATED)
        val roomDetailsNode = roomFlowNode.childNode(JoinedRoomLoadedFlowNode.NavTarget.RoomDetails)!!
        assertThat(roomDetailsNode.id).isEqualTo(fakeRoomDetailsEntryPoint.nodeId)
    }
}

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
import com.google.common.truth.Truth
import io.element.android.features.messages.api.MessagesEntryPoint
import io.element.android.features.roomdetails.api.RoomDetailsEntryPoint
import io.element.android.libraries.architecture.childNode
import io.element.android.libraries.matrix.api.room.RoomMembershipObserver
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.services.appnavstate.test.NoopAppNavigationStateService
import org.junit.Rule
import org.junit.Test

class RoomFlowNodeTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private class FakeMessagesEntryPoint : MessagesEntryPoint {

        var nodeId: String? = null
        var callback: MessagesEntryPoint.Callback? = null

        override fun createNode(parentNode: Node, buildContext: BuildContext, callback: MessagesEntryPoint.Callback): Node {
            return node(buildContext) {}.also {
                nodeId = it.id
                this.callback = callback
            }
        }
    }

    private class FakeRoomDetailsEntryPoint : RoomDetailsEntryPoint {

        var nodeId: String? = null

        override fun createRoomDetailsNode(parentNode: Node, buildContext: BuildContext, plugins: List<Plugin>): Node {
            return node(buildContext) {}.also {
                nodeId = it.id
            }
        }
    }

    private fun aRoomFlowNode(
        plugins: List<Plugin>,
        messagesEntryPoint: MessagesEntryPoint = FakeMessagesEntryPoint(),
        roomDetailsEntryPoint: RoomDetailsEntryPoint = FakeRoomDetailsEntryPoint(),
    ) = RoomFlowNode(
        buildContext = BuildContext.root(savedStateMap = null),
        plugins = plugins,
        messagesEntryPoint = messagesEntryPoint,
        roomDetailsEntryPoint = roomDetailsEntryPoint,
        appNavigationStateService = NoopAppNavigationStateService(),
        roomMembershipObserver = RoomMembershipObserver()
    )

    @Test
    fun `given a room flow node when initialized then it loads messages entry point`() {
        // GIVEN
        val room = FakeMatrixRoom()
        val fakeMessagesEntryPoint = FakeMessagesEntryPoint()
        val inputs = RoomFlowNode.Inputs(room)
        val roomFlowNode = aRoomFlowNode(listOf(inputs), fakeMessagesEntryPoint)
        // WHEN
        val roomFlowNodeTestHelper = roomFlowNode.parentNodeTestHelper()

        // THEN
        Truth.assertThat(roomFlowNode.backstack.activeElement).isEqualTo(RoomFlowNode.NavTarget.Messages)
        roomFlowNodeTestHelper.assertChildHasLifecycle(RoomFlowNode.NavTarget.Messages, Lifecycle.State.CREATED)
        val messagesNode = roomFlowNode.childNode(RoomFlowNode.NavTarget.Messages)!!
        Truth.assertThat(messagesNode.id).isEqualTo(fakeMessagesEntryPoint.nodeId)
    }

    @Test
    fun `given a room flow node when callback on room details is triggered then it loads room details entry point`() {
        // GIVEN
        val room = FakeMatrixRoom()
        val fakeMessagesEntryPoint = FakeMessagesEntryPoint()
        val fakeRoomDetailsEntryPoint = FakeRoomDetailsEntryPoint()
        val inputs = RoomFlowNode.Inputs(room)
        val roomFlowNode = aRoomFlowNode(listOf(inputs), fakeMessagesEntryPoint, fakeRoomDetailsEntryPoint)
        val roomFlowNodeTestHelper = roomFlowNode.parentNodeTestHelper()
        // WHEN
        fakeMessagesEntryPoint.callback?.onRoomDetailsClicked()
        // THEN
        roomFlowNodeTestHelper.assertChildHasLifecycle(RoomFlowNode.NavTarget.RoomDetails, Lifecycle.State.CREATED)
        val roomDetailsNode = roomFlowNode.childNode(RoomFlowNode.NavTarget.RoomDetails)!!
        Truth.assertThat(roomDetailsNode.id).isEqualTo(fakeRoomDetailsEntryPoint.nodeId)
    }
}

/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
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
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.room.FakeBaseRoom
import io.element.android.libraries.matrix.test.room.FakeJoinedRoom
import io.element.android.services.appnavstate.api.ActiveRoomsHolder
import io.element.android.services.appnavstate.test.FakeAppNavigationStateService
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class JoinedRoomLoadedFlowNodeTest {
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
        override fun create(room: JoinedRoom): Any {
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

    private fun TestScope.createJoinedRoomLoadedFlowNode(
        plugins: List<Plugin>,
        messagesEntryPoint: MessagesEntryPoint = FakeMessagesEntryPoint(),
        roomDetailsEntryPoint: RoomDetailsEntryPoint = FakeRoomDetailsEntryPoint(),
        activeRoomsHolder: ActiveRoomsHolder = ActiveRoomsHolder(),
    ) = JoinedRoomLoadedFlowNode(
        buildContext = BuildContext.root(savedStateMap = null),
        plugins = plugins,
        messagesEntryPoint = messagesEntryPoint,
        roomDetailsEntryPoint = roomDetailsEntryPoint,
        appNavigationStateService = FakeAppNavigationStateService(),
        sessionCoroutineScope = this,
        roomComponentFactory = FakeRoomComponentFactory(),
        matrixClient = FakeMatrixClient(),
        activeRoomsHolder = activeRoomsHolder,
    )

    @Test
    fun `given a room flow node when initialized then it loads messages entry point`() = runTest {
        // GIVEN
        val room = FakeJoinedRoom(baseRoom = FakeBaseRoom(updateMembersResult = {}))
        val fakeMessagesEntryPoint = FakeMessagesEntryPoint()
        val inputs = JoinedRoomLoadedFlowNode.Inputs(room, RoomNavigationTarget.Messages())
        val roomFlowNode = createJoinedRoomLoadedFlowNode(
            plugins = listOf(inputs),
            messagesEntryPoint = fakeMessagesEntryPoint,
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
        val room = FakeJoinedRoom(baseRoom = FakeBaseRoom(updateMembersResult = {}))
        val fakeMessagesEntryPoint = FakeMessagesEntryPoint()
        val fakeRoomDetailsEntryPoint = FakeRoomDetailsEntryPoint()
        val inputs = JoinedRoomLoadedFlowNode.Inputs(room, RoomNavigationTarget.Messages())
        val roomFlowNode = createJoinedRoomLoadedFlowNode(
            plugins = listOf(inputs),
            messagesEntryPoint = fakeMessagesEntryPoint,
            roomDetailsEntryPoint = fakeRoomDetailsEntryPoint,
        )
        val roomFlowNodeTestHelper = roomFlowNode.parentNodeTestHelper()
        // WHEN
        fakeMessagesEntryPoint.callback?.onRoomDetailsClick()
        // THEN
        roomFlowNodeTestHelper.assertChildHasLifecycle(JoinedRoomLoadedFlowNode.NavTarget.RoomDetails, Lifecycle.State.CREATED)
        val roomDetailsNode = roomFlowNode.childNode(JoinedRoomLoadedFlowNode.NavTarget.RoomDetails)!!
        assertThat(roomDetailsNode.id).isEqualTo(fakeRoomDetailsEntryPoint.nodeId)
    }

    @Test
    fun `the ActiveRoomsHolder will be updated with the loaded room on create`() = runTest {
        // GIVEN
        val room = FakeJoinedRoom(baseRoom = FakeBaseRoom(updateMembersResult = {}))
        val fakeMessagesEntryPoint = FakeMessagesEntryPoint()
        val fakeRoomDetailsEntryPoint = FakeRoomDetailsEntryPoint()
        val inputs = JoinedRoomLoadedFlowNode.Inputs(room, RoomNavigationTarget.Messages())
        val activeRoomsHolder = ActiveRoomsHolder()
        val roomFlowNode = createJoinedRoomLoadedFlowNode(
            plugins = listOf(inputs),
            messagesEntryPoint = fakeMessagesEntryPoint,
            roomDetailsEntryPoint = fakeRoomDetailsEntryPoint,
            activeRoomsHolder = activeRoomsHolder,
        )

        assertThat(activeRoomsHolder.getActiveRoom(A_SESSION_ID)).isNull()
        val roomFlowNodeTestHelper = roomFlowNode.parentNodeTestHelper()
        // WHEN
        roomFlowNodeTestHelper.assertChildHasLifecycle(JoinedRoomLoadedFlowNode.NavTarget.Messages(null), Lifecycle.State.CREATED)
        // THEN
        assertThat(activeRoomsHolder.getActiveRoom(A_SESSION_ID)).isNotNull()
    }

    @Test
    fun `the ActiveRoomsHolder will be removed on destroy`() = runTest {
        // GIVEN
        val room = FakeJoinedRoom(baseRoom = FakeBaseRoom(updateMembersResult = {}))
        val fakeMessagesEntryPoint = FakeMessagesEntryPoint()
        val fakeRoomDetailsEntryPoint = FakeRoomDetailsEntryPoint()
        val inputs = JoinedRoomLoadedFlowNode.Inputs(room, RoomNavigationTarget.Messages())
        val activeRoomsHolder = ActiveRoomsHolder().apply {
            addRoom(room)
        }
        val roomFlowNode = createJoinedRoomLoadedFlowNode(
            plugins = listOf(inputs),
            messagesEntryPoint = fakeMessagesEntryPoint,
            roomDetailsEntryPoint = fakeRoomDetailsEntryPoint,
            activeRoomsHolder = activeRoomsHolder,
        )
        val roomFlowNodeTestHelper = roomFlowNode.parentNodeTestHelper()
        roomFlowNodeTestHelper.assertChildHasLifecycle(JoinedRoomLoadedFlowNode.NavTarget.Messages(null), Lifecycle.State.CREATED)
        assertThat(activeRoomsHolder.getActiveRoom(A_SESSION_ID)).isNotNull()
        // WHEN
        roomFlowNode.updateLifecycleState(Lifecycle.State.DESTROYED)
        // THEN
        roomFlowNodeTestHelper.assertChildHasLifecycle(JoinedRoomLoadedFlowNode.NavTarget.Messages(null), Lifecycle.State.DESTROYED)
        assertThat(activeRoomsHolder.getActiveRoom(A_SESSION_ID)).isNull()
    }
}

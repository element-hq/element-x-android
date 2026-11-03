/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.appnav.room

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.bumble.appyx.core.modality.AncestryInfo
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.node.node
import com.bumble.appyx.navmodel.backstack.activeElement
import com.bumble.appyx.testing.junit4.util.MainDispatcherRule
import com.bumble.appyx.testing.unit.common.helper.parentNodeTestHelper
import com.bumble.appyx.utils.customisations.NodeCustomisationDirectoryImpl
import com.google.common.truth.Truth.assertThat
import io.element.android.appnav.room.joined.JoinedRoomFlowNode
import io.element.android.features.joinroom.api.JoinRoomEntryPoint
import io.element.android.features.roomaliasresolver.api.RoomAliasResolverEntryPoint
import io.element.android.libraries.architecture.AssistedNodeFactory
import io.element.android.libraries.matrix.api.core.RoomIdOrAlias
import io.element.android.libraries.matrix.api.core.toRoomIdOrAlias
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.room.RoomInfo
import io.element.android.libraries.matrix.api.room.RoomMembershipObserver
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.room.aRoomInfo
import io.element.android.libraries.matrix.ui.room.LoadingRoomStateFlowFactory
import io.element.android.services.analytics.test.FakeAnalyticsService
import io.element.android.tests.testutils.node.FakeNodeFactoriesBindings
import io.element.android.tests.testutils.node.FakeParentNode
import io.element.android.tests.testutils.robolectric.RobolectricTest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.util.Optional
import im.vector.app.features.analytics.plan.JoinedRoom as JoinedRoomAnalyticsEvent

class RoomFlowNodeTest : RobolectricTest() {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `when the room is already joined, the node navigates to the joined room`() = runTest {
        val roomInfoFlow = MutableStateFlow(Optional.of(aRoomInfo(currentUserMembership = CurrentUserMembership.JOINED)))
        val roomFlowNode = createRoomFlowNode(roomInfoFlow = roomInfoFlow)
        roomFlowNode.parentNodeTestHelper()
        assertThat(roomFlowNode.backstack.activeElement).isEqualTo(RoomFlowNode.NavTarget.JoinedRoom(A_ROOM_ID))
    }

    @Test
    fun `when the room is not known yet, the node navigates to the join room screen instead of staying on loading`() = runTest {
        val roomInfoFlow = MutableStateFlow(Optional.empty<RoomInfo>())
        val roomFlowNode = createRoomFlowNode(roomInfoFlow = roomInfoFlow)
        roomFlowNode.parentNodeTestHelper()
        assertThat(roomFlowNode.backstack.activeElement).isEqualTo(
            RoomFlowNode.NavTarget.JoinRoom(
                roomId = A_ROOM_ID,
                serverNames = emptyList(),
                trigger = JoinedRoomAnalyticsEvent.Trigger.Invite,
            )
        )
    }

    @Test
    fun `when the user joins an invited room, the node navigates to the joined room`() = runTest {
        // The room info flow comes from the client and not from a room instance, so it keeps emitting
        // even after the room instance used to join the room has been closed.
        val roomInfoFlow = MutableStateFlow(Optional.of(aRoomInfo(currentUserMembership = CurrentUserMembership.INVITED)))
        val roomFlowNode = createRoomFlowNode(roomInfoFlow = roomInfoFlow)
        roomFlowNode.parentNodeTestHelper()
        assertThat(roomFlowNode.backstack.activeElement).isInstanceOf(RoomFlowNode.NavTarget.JoinRoom::class.java)
        // WHEN the room is joined
        roomInfoFlow.value = Optional.of(aRoomInfo(currentUserMembership = CurrentUserMembership.JOINED))
        // THEN we are not stuck on the join room screen
        assertThat(roomFlowNode.backstack.activeElement).isEqualTo(RoomFlowNode.NavTarget.JoinedRoom(A_ROOM_ID))
    }

    @Test
    fun `when the user leaves the room from another device, the node navigates back to the join room screen`() = runTest {
        val roomInfoFlow = MutableStateFlow(Optional.of(aRoomInfo(currentUserMembership = CurrentUserMembership.JOINED)))
        val roomFlowNode = createRoomFlowNode(roomInfoFlow = roomInfoFlow)
        roomFlowNode.parentNodeTestHelper()
        assertThat(roomFlowNode.backstack.activeElement).isEqualTo(RoomFlowNode.NavTarget.JoinedRoom(A_ROOM_ID))
        // WHEN the room is left from another device
        roomInfoFlow.value = Optional.of(aRoomInfo(currentUserMembership = CurrentUserMembership.LEFT))
        // THEN
        assertThat(roomFlowNode.backstack.activeElement).isInstanceOf(RoomFlowNode.NavTarget.JoinRoom::class.java)
    }

    private fun createRoomFlowNode(
        roomInfoFlow: MutableStateFlow<Optional<RoomInfo>>,
        roomIdOrAlias: RoomIdOrAlias = A_ROOM_ID.toRoomIdOrAlias(),
    ): RoomFlowNode {
        val client = FakeMatrixClient().apply {
            getRoomInfoFlowLambda = { roomInfoFlow }
        }
        val parentNode = FakeParentNode(
            graph = FakeNodeFactoriesBindings(
                mapOf(
                    JoinedRoomFlowNode::class to AssistedNodeFactory { buildContext, plugins ->
                        JoinedRoomFlowNode(
                            buildContext = buildContext,
                            plugins = plugins,
                            loadingRoomStateFlowFactory = LoadingRoomStateFlowFactory(client),
                            analyticsService = FakeAnalyticsService(),
                        )
                    }
                )
            )
        )
        val inputs = RoomFlowNode.Inputs(
            roomIdOrAlias = roomIdOrAlias,
            roomDescription = Optional.empty(),
            serverNames = emptyList(),
            trigger = Optional.empty(),
            initialElement = RoomNavigationTarget.Root(),
        )
        return RoomFlowNode(
            buildContext = BuildContext(
                ancestryInfo = AncestryInfo.Child(anchor = parentNode),
                savedStateMap = null,
                customisations = NodeCustomisationDirectoryImpl(),
            ),
            plugins = listOf(inputs),
            client = client,
            joinRoomEntryPoint = FakeJoinRoomEntryPoint(),
            roomAliasResolverEntryPoint = FakeRoomAliasResolverEntryPoint(),
            membershipObserver = RoomMembershipObserver(),
            analyticsService = FakeAnalyticsService(),
        )
    }

    private class FakeJoinRoomEntryPoint : JoinRoomEntryPoint {
        override fun createNode(
            parentNode: Node,
            buildContext: BuildContext,
            inputs: JoinRoomEntryPoint.Inputs,
        ): Node = node(buildContext) {}
    }

    private class FakeRoomAliasResolverEntryPoint : RoomAliasResolverEntryPoint {
        override fun createNode(
            parentNode: Node,
            buildContext: BuildContext,
            params: RoomAliasResolverEntryPoint.Params,
            callback: RoomAliasResolverEntryPoint.Callback,
        ): Node = node(buildContext) {}
    }
}

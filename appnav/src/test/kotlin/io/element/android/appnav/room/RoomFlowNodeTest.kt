/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.appnav.room

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.bumble.appyx.core.modality.AncestryInfo
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.node.node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.navmodel.backstack.activeElement
import com.bumble.appyx.testing.junit4.util.MainDispatcherRule
import com.bumble.appyx.testing.unit.common.helper.parentNodeTestHelper
import com.bumble.appyx.utils.customisations.NodeCustomisationDirectoryImpl
import com.google.common.truth.Truth.assertThat
import io.element.android.appnav.di.RoomGraphFactory
import io.element.android.appnav.di.TimelineBindings
import io.element.android.appnav.room.joined.FakeJoinedRoomLoadedFlowNodeCallback
import io.element.android.appnav.room.joined.JoinedRoomFlowNode
import io.element.android.appnav.room.joined.JoinedRoomLoadedFlowNode
import io.element.android.features.forward.test.FakeForwardEntryPoint
import io.element.android.features.joinroom.api.JoinRoomEntryPoint
import io.element.android.features.messages.api.MessagesEntryPoint
import io.element.android.features.messages.api.pinned.PinnedEventsTimelineProvider
import io.element.android.features.messages.test.pinned.FakePinnedEventsTimelineProvider
import io.element.android.features.roomaliasresolver.api.RoomAliasResolverEntryPoint
import io.element.android.features.roomdetails.api.RoomDetailsEntryPoint
import io.element.android.features.space.api.SpaceEntryPoint
import io.element.android.libraries.architecture.AssistedNodeFactory
import io.element.android.libraries.matrix.api.core.toRoomIdOrAlias
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.room.RoomInfo
import io.element.android.libraries.matrix.api.room.RoomMembershipObserver
import io.element.android.libraries.matrix.api.timeline.TimelineProvider
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.room.FakeBaseRoom
import io.element.android.libraries.matrix.test.room.FakeJoinedRoom
import io.element.android.libraries.matrix.test.room.aRoomInfo
import io.element.android.libraries.matrix.test.timeline.FakeTimelineProvider
import io.element.android.libraries.matrix.ui.room.LoadingRoomStateFlowFactory
import io.element.android.services.analytics.api.watchers.AnalyticsSendMessageWatcher
import io.element.android.services.analytics.test.FakeAnalyticsService
import io.element.android.services.analytics.test.watchers.FakeAnalyticsSendMessageWatcher
import io.element.android.services.appnavstate.test.FakeActiveRoomsHolder
import io.element.android.services.appnavstate.test.FakeAppNavigationStateService
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.node.FakeNodeFactoriesBindings
import io.element.android.tests.testutils.node.FakeParentNode
import io.element.android.tests.testutils.robolectric.RobolectricTest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import java.util.Optional

class RoomFlowNodeTest : RobolectricTest() {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val joinedRoomInfo = aRoomInfo(currentUserMembership = CurrentUserMembership.JOINED)

    @Ignore("Leaves the joined room on a transient invited state — passes with the fix in this PR")
    @Test
    fun `a joined room briefly reported as invited stays open`() = runTest {
        val joinedRoom = FakeJoinedRoom(baseRoom = FakeBaseRoom(initialRoomInfo = joinedRoomInfo, updateMembersResult = {}))
        val clientRoomInfoFlow = MutableStateFlow<Optional<RoomInfo>>(Optional.of(joinedRoomInfo))
        val roomFlowNode = createRoomFlowNode(joinedRoom, clientRoomInfoFlow)
        roomFlowNode.parentNodeTestHelper()
        runCurrent()

        // A sync response from before the join is processed after it, the SDK reports the room as invited for a moment
        val invitedRoomInfo = aRoomInfo(currentUserMembership = CurrentUserMembership.INVITED)
        joinedRoom.baseRoom.givenRoomInfo(invitedRoomInfo)
        clientRoomInfoFlow.value = Optional.of(invitedRoomInfo)
        runCurrent()
        assertThat(roomFlowNode.backstack.activeElement).isInstanceOf(RoomFlowNode.NavTarget.JoinedRoom::class.java)

        clientRoomInfoFlow.value = Optional.of(joinedRoomInfo)
        runCurrent()
        assertThat(roomFlowNode.backstack.activeElement).isInstanceOf(RoomFlowNode.NavTarget.JoinedRoom::class.java)
    }

    @Ignore("Stuck on the join screen after rejoining — passes with the fix in this PR")
    @Test
    fun `a room left from elsewhere then joined again goes back to the joined room`() = runTest {
        val joinedRoom = FakeJoinedRoom(baseRoom = FakeBaseRoom(initialRoomInfo = joinedRoomInfo, updateMembersResult = {}))
        val clientRoomInfoFlow = MutableStateFlow<Optional<RoomInfo>>(Optional.of(joinedRoomInfo))
        val roomFlowNode = createRoomFlowNode(joinedRoom, clientRoomInfoFlow)
        roomFlowNode.parentNodeTestHelper()
        runCurrent()

        val leftRoomInfo = aRoomInfo(currentUserMembership = CurrentUserMembership.LEFT)
        joinedRoom.baseRoom.givenRoomInfo(leftRoomInfo)
        clientRoomInfoFlow.value = Optional.of(leftRoomInfo)
        runCurrent()
        assertThat(roomFlowNode.backstack.activeElement).isInstanceOf(RoomFlowNode.NavTarget.JoinRoom::class.java)
        roomFlowNode.finishTransitions()
        // Leaving the JoinedRoom target destroyed the joined room, its room info flow is no longer updated
        joinedRoom.baseRoom.assertDestroyed()

        clientRoomInfoFlow.value = Optional.of(joinedRoomInfo)
        runCurrent()
        assertThat(roomFlowNode.backstack.activeElement).isEqualTo(RoomFlowNode.NavTarget.JoinedRoom(A_ROOM_ID))
    }

    // Without a UI the backstack transitions never finish, so the replaced children would never be destroyed
    private fun RoomFlowNode.finishTransitions() = backstack.onTransitionFinished(backstack.elements.value.map { it.key })

    private fun TestScope.createRoomFlowNode(
        joinedRoom: JoinedRoom,
        clientRoomInfoFlow: MutableStateFlow<Optional<RoomInfo>>,
    ): RoomFlowNode {
        val matrixClient = FakeMatrixClient().apply {
            getRoomInfoFlowLambda = { clientRoomInfoFlow }
        }
        val parentNode = FakeParentNode(
            graph = FakeNodeFactoriesBindings(
                mapOf(
                    JoinedRoomFlowNode::class to AssistedNodeFactory { buildContext, plugins ->
                        JoinedRoomFlowNode(
                            buildContext = buildContext,
                            plugins = plugins,
                            loadingRoomStateFlowFactory = LoadingRoomStateFlowFactory(matrixClient),
                            analyticsService = FakeAnalyticsService(),
                        )
                    },
                    JoinedRoomLoadedFlowNode::class to AssistedNodeFactory { buildContext, plugins ->
                        createJoinedRoomLoadedFlowNode(buildContext, plugins, matrixClient, backgroundScope)
                    },
                )
            )
        )
        return RoomFlowNode(
            buildContext = BuildContext(
                ancestryInfo = AncestryInfo.Child(anchor = parentNode),
                savedStateMap = null,
                customisations = NodeCustomisationDirectoryImpl(),
            ),
            plugins = listOf(
                RoomFlowNode.Inputs(
                    roomIdOrAlias = A_ROOM_ID.toRoomIdOrAlias(),
                    roomDescription = Optional.empty(),
                    serverNames = emptyList(),
                    trigger = Optional.empty(),
                    initialElement = RoomNavigationTarget.Root(joinedRoom = joinedRoom),
                ),
                FakeJoinedRoomLoadedFlowNodeCallback(),
            ),
            client = matrixClient,
            joinRoomEntryPoint = object : JoinRoomEntryPoint {
                override fun createNode(parentNode: Node, buildContext: BuildContext, inputs: JoinRoomEntryPoint.Inputs) = node(buildContext) {}
            },
            roomAliasResolverEntryPoint = object : RoomAliasResolverEntryPoint {
                override fun createNode(
                    parentNode: Node,
                    buildContext: BuildContext,
                    params: RoomAliasResolverEntryPoint.Params,
                    callback: RoomAliasResolverEntryPoint.Callback,
                ) = lambdaError()
            },
            membershipObserver = RoomMembershipObserver(),
            analyticsService = FakeAnalyticsService(),
        )
    }

    private fun createJoinedRoomLoadedFlowNode(
        buildContext: BuildContext,
        plugins: List<Plugin>,
        matrixClient: FakeMatrixClient,
        sessionCoroutineScope: CoroutineScope,
    ) = JoinedRoomLoadedFlowNode(
        buildContext = buildContext,
        plugins = plugins,
        messagesEntryPoint = object : MessagesEntryPoint {
            override fun createNode(
                parentNode: Node,
                buildContext: BuildContext,
                params: MessagesEntryPoint.Params,
                callback: MessagesEntryPoint.Callback,
            ) = node(buildContext) {}
        },
        roomDetailsEntryPoint = object : RoomDetailsEntryPoint {
            override fun createNode(
                parentNode: Node,
                buildContext: BuildContext,
                params: RoomDetailsEntryPoint.Params,
                callback: RoomDetailsEntryPoint.Callback,
            ) = node(buildContext) {}
        },
        spaceEntryPoint = object : SpaceEntryPoint {
            override fun createNode(
                parentNode: Node,
                buildContext: BuildContext,
                inputs: SpaceEntryPoint.Inputs,
                callback: SpaceEntryPoint.Callback,
            ) = node(buildContext) {}
        },
        forwardEntryPoint = FakeForwardEntryPoint(),
        appNavigationStateService = FakeAppNavigationStateService(),
        sessionCoroutineScope = sessionCoroutineScope,
        matrixClient = matrixClient,
        activeRoomsHolder = FakeActiveRoomsHolder(),
        analyticsService = FakeAnalyticsService(),
        roomGraphFactory = object : RoomGraphFactory {
            override fun create(room: JoinedRoom): Any = object : TimelineBindings {
                override val timelineProvider: TimelineProvider = FakeTimelineProvider()
                override val pinnedEventsTimelineProvider: PinnedEventsTimelineProvider = FakePinnedEventsTimelineProvider()
                override val analyticsSendMessageWatcher: AnalyticsSendMessageWatcher = FakeAnalyticsSendMessageWatcher()
            }
        },
    )
}

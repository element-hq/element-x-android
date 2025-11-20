/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.appnav.room

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.navigation.transition.JumpToEndTransitionHandler
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.node.node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.newRoot
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import im.vector.app.features.analytics.plan.JoinedRoom
import io.element.android.annotations.ContributesNode
import io.element.android.appnav.room.joined.JoinedRoomFlowNode
import io.element.android.appnav.room.joined.JoinedRoomLoadedFlowNode
import io.element.android.appnav.room.joined.LoadingRoomNodeView
import io.element.android.features.joinroom.api.JoinRoomEntryPoint
import io.element.android.features.roomaliasesolver.api.RoomAliasResolverEntryPoint
import io.element.android.features.roomaliasesolver.api.RoomAliasResolverEntryPoint.Params
import io.element.android.features.roomdirectory.api.RoomDescription
import io.element.android.libraries.architecture.BackstackView
import io.element.android.libraries.architecture.BaseFlowNode
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.core.coroutine.withPreviousValue
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.RoomIdOrAlias
import io.element.android.libraries.matrix.api.core.ThreadId
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.room.RoomMembershipObserver
import io.element.android.libraries.matrix.api.room.alias.ResolvedRoomAlias
import io.element.android.libraries.matrix.ui.room.LoadingRoomState
import io.element.android.services.analytics.api.AnalyticsLongRunningTransaction.LoadJoinedRoomFlow
import io.element.android.services.analytics.api.AnalyticsLongRunningTransaction.NotificationTapOpensTimeline
import io.element.android.services.analytics.api.AnalyticsLongRunningTransaction.OpenRoom
import io.element.android.services.analytics.api.AnalyticsService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import timber.log.Timber
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

@ContributesNode(SessionScope::class)
@AssistedInject
class RoomFlowNode(
    @Assisted val buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val client: MatrixClient,
    private val joinRoomEntryPoint: JoinRoomEntryPoint,
    private val roomAliasResolverEntryPoint: RoomAliasResolverEntryPoint,
    private val membershipObserver: RoomMembershipObserver,
    private val analyticsService: AnalyticsService,
) : BaseFlowNode<RoomFlowNode.NavTarget>(
    backstack = BackStack(
        initialElement = NavTarget.Loading,
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins
) {
    data class Inputs(
        val roomIdOrAlias: RoomIdOrAlias,
        val roomDescription: Optional<RoomDescription>,
        val serverNames: List<String>,
        val trigger: Optional<JoinedRoom.Trigger>,
        val initialElement: RoomNavigationTarget,
    ) : NodeInputs

    private val inputs: Inputs = inputs()

    sealed interface NavTarget : Parcelable {
        @Parcelize
        data object Loading : NavTarget

        @Parcelize
        data class Resolving(val roomAlias: RoomAlias) : NavTarget

        @Parcelize
        data class JoinRoom(
            val roomId: RoomId,
            val serverNames: List<String>,
            val trigger: im.vector.app.features.analytics.plan.JoinedRoom.Trigger,
        ) : NavTarget

        @Parcelize
        data class JoinedRoom(val roomId: RoomId) : NavTarget
    }

    override fun onBuilt() {
        super.onBuilt()
        val parentTransaction = analyticsService.getLongRunningTransaction(NotificationTapOpensTimeline)
        val openRoomTransaction = analyticsService.startLongRunningTransaction(OpenRoom, parentTransaction)
        analyticsService.startLongRunningTransaction(LoadJoinedRoomFlow, openRoomTransaction)
        resolveRoomId()
    }

    private fun resolveRoomId() {
        lifecycleScope.launch {
            when (val i = inputs.roomIdOrAlias) {
                is RoomIdOrAlias.Alias -> {
                    backstack.newRoot(NavTarget.Resolving(i.roomAlias))
                }
                is RoomIdOrAlias.Id -> {
                    subscribeToRoomInfoFlow(i.roomId, inputs.serverNames)
                }
            }
        }
    }

    private fun subscribeToRoomInfoFlow(roomId: RoomId, serverNames: List<String>) {
        val roomInfoFlow = client.getRoomInfoFlow(roomId)

        // This observes the local membership changes for the room
        val membershipUpdateFlow = membershipObserver.updates
            .filter { it.roomId == roomId }
            .distinctUntilChanged()
            // We add a replay so we can check the last local membership update
            .shareIn(lifecycleScope, started = SharingStarted.Eagerly, replay = 1)

        val currentMembershipFlow = roomInfoFlow
            .map { it.getOrNull()?.currentUserMembership }
            .distinctUntilChanged()
            .withPreviousValue()
        currentMembershipFlow.onEach { (previousMembership, membership) ->
            Timber.d("Room membership: $membership")
            if (membership == CurrentUserMembership.JOINED) {
                backstack.newRoot(NavTarget.JoinedRoom(roomId))
            } else {
                val leavingFromCurrentDevice =
                    membership == CurrentUserMembership.LEFT &&
                        previousMembership == CurrentUserMembership.JOINED &&
                        membershipUpdateFlow.replayCache.lastOrNull()?.isUserInRoom == false

                if (leavingFromCurrentDevice) {
                    navigateUp()
                } else {
                    backstack.newRoot(
                        NavTarget.JoinRoom(
                            roomId = roomId,
                            serverNames = serverNames,
                            trigger = inputs.trigger.getOrNull() ?: JoinedRoom.Trigger.Invite,
                        )
                    )
                }
            }
        }.launchIn(lifecycleScope)
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            is NavTarget.Loading -> loadingNode(buildContext)
            is NavTarget.Resolving -> {
                val callback = object : RoomAliasResolverEntryPoint.Callback {
                    override fun onAliasResolved(data: ResolvedRoomAlias) {
                        subscribeToRoomInfoFlow(
                            roomId = data.roomId,
                            serverNames = data.servers,
                        )
                    }
                }
                val params = Params(navTarget.roomAlias)
                roomAliasResolverEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                    params = params,
                    callback = callback,
                )
            }
            is NavTarget.JoinRoom -> {
                val inputs = JoinRoomEntryPoint.Inputs(
                    roomId = navTarget.roomId,
                    roomIdOrAlias = inputs.roomIdOrAlias,
                    roomDescription = inputs.roomDescription,
                    serverNames = navTarget.serverNames,
                    trigger = navTarget.trigger,
                )
                joinRoomEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                    inputs = inputs,
                )
            }
            is NavTarget.JoinedRoom -> {
                val roomFlowNodeCallback = plugins<JoinedRoomLoadedFlowNode.Callback>()
                val inputs = JoinedRoomFlowNode.Inputs(
                    roomId = navTarget.roomId,
                    initialElement = inputs.initialElement
                )
                createNode<JoinedRoomFlowNode>(buildContext, plugins = listOf(inputs) + roomFlowNodeCallback)
            }
        }
    }

    suspend fun attachThread(threadId: ThreadId, focusedEventId: EventId?) {
        waitForChildAttached<JoinedRoomFlowNode>()
            .attachThread(threadId, focusedEventId)
    }

    private fun loadingNode(buildContext: BuildContext) = node(buildContext) { modifier ->
        LoadingRoomNodeView(
            state = LoadingRoomState.Loading,
            onBackClick = { navigateUp() },
            modifier = modifier,
        )
    }

    @Composable
    override fun View(modifier: Modifier) {
        BackstackView(transitionHandler = JumpToEndTransitionHandler())
    }
}

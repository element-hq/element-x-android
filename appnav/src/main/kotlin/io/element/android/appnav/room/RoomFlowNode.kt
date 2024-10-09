/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.appnav.room

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import im.vector.app.features.analytics.plan.JoinedRoom
import io.element.android.anvilannotations.ContributesNode
import io.element.android.appnav.room.joined.JoinedRoomFlowNode
import io.element.android.appnav.room.joined.JoinedRoomLoadedFlowNode
import io.element.android.appnav.room.joined.LoadingRoomNodeView
import io.element.android.appnav.room.joined.LoadingRoomState
import io.element.android.features.joinroom.api.JoinRoomEntryPoint
import io.element.android.features.networkmonitor.api.NetworkMonitor
import io.element.android.features.networkmonitor.api.NetworkStatus
import io.element.android.features.roomaliasesolver.api.RoomAliasResolverEntryPoint
import io.element.android.features.roomdirectory.api.RoomDescription
import io.element.android.libraries.architecture.BackstackView
import io.element.android.libraries.architecture.BaseFlowNode
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.core.bool.orFalse
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.RoomIdOrAlias
import io.element.android.libraries.matrix.api.core.toRoomIdOrAlias
import io.element.android.libraries.matrix.api.getRoomInfoFlow
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.room.alias.ResolvedRoomAlias
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import timber.log.Timber
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

@ContributesNode(SessionScope::class)
class RoomFlowNode @AssistedInject constructor(
    @Assisted val buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val client: MatrixClient,
    private val joinRoomEntryPoint: JoinRoomEntryPoint,
    private val roomAliasResolverEntryPoint: RoomAliasResolverEntryPoint,
    private val networkMonitor: NetworkMonitor,
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
        val roomInfoFlow = client.getRoomInfoFlow(roomIdOrAlias = roomId.toRoomIdOrAlias())
        val isSpaceFlow = roomInfoFlow.map { it.getOrNull()?.isSpace.orFalse() }.distinctUntilChanged()
        val currentMembershipFlow = roomInfoFlow.map { it.getOrNull()?.currentUserMembership }.distinctUntilChanged()
        combine(currentMembershipFlow, isSpaceFlow) { membership, isSpace ->
            Timber.d("Room membership: $membership")
            when (membership) {
                CurrentUserMembership.JOINED -> {
                    if (isSpace) {
                        // It should not happen, but probably due to an issue in the sliding sync,
                        // we can have a space here in case the space has just been joined.
                        // So navigate to the JoinRoom target for now, which will
                        // handle the space not supported screen
                        backstack.newRoot(
                            NavTarget.JoinRoom(
                                roomId = roomId,
                                serverNames = serverNames,
                                trigger = inputs.trigger.getOrNull() ?: JoinedRoom.Trigger.Invite,
                            )
                        )
                    } else {
                        backstack.newRoot(NavTarget.JoinedRoom(roomId))
                    }
                }
                CurrentUserMembership.LEFT -> {
                    // Left the room, navigate out of this flow
                    navigateUp()
                }
                else -> {
                    // Was invited or the room is not known, display the join room screen
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
                val params = RoomAliasResolverEntryPoint.Params(navTarget.roomAlias)
                roomAliasResolverEntryPoint.nodeBuilder(this, buildContext)
                    .callback(callback)
                    .params(params)
                    .build()
            }
            is NavTarget.JoinRoom -> {
                val inputs = JoinRoomEntryPoint.Inputs(
                    roomId = navTarget.roomId,
                    roomIdOrAlias = inputs.roomIdOrAlias,
                    roomDescription = inputs.roomDescription,
                    serverNames = navTarget.serverNames,
                    trigger = navTarget.trigger,
                )
                joinRoomEntryPoint.createNode(this, buildContext, inputs)
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

    private fun loadingNode(buildContext: BuildContext) = node(buildContext) { modifier ->
        val networkStatus by networkMonitor.connectivity.collectAsState()
        LoadingRoomNodeView(
            state = LoadingRoomState.Loading,
            hasNetworkConnection = networkStatus == NetworkStatus.Online,
            onBackClick = { navigateUp() },
            modifier = modifier,
        )
    }

    @Composable
    override fun View(modifier: Modifier) {
        BackstackView(transitionHandler = JumpToEndTransitionHandler())
    }
}

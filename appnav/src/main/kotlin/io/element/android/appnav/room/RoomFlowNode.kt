/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.appnav.room

import android.os.Parcelable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
import io.element.android.anvilannotations.ContributesNode
import io.element.android.appnav.room.joined.JoinedRoomFlowNode
import io.element.android.appnav.room.joined.JoinedRoomLoadedFlowNode
import io.element.android.features.joinroom.api.JoinRoomEntryPoint
import io.element.android.features.roomdirectory.api.RoomDescription
import io.element.android.libraries.architecture.BackstackView
import io.element.android.libraries.architecture.BaseFlowNode
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.designsystem.components.async.AsyncFailure
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.RoomIdOrAlias
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.room.RoomMembershipObserver
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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
    private val roomMembershipObserver: RoomMembershipObserver,
    private val joinRoomEntryPoint: JoinRoomEntryPoint,
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
        val initialElement: RoomNavigationTarget = RoomNavigationTarget.Messages(),
    ) : NodeInputs

    private val inputs: Inputs = inputs()

    sealed interface NavTarget : Parcelable {
        @Parcelize
        data object Loading : NavTarget

        @Parcelize
        data class JoinRoom(val roomId: RoomId) : NavTarget

        @Parcelize
        data class RoomAliasError(val roomAlias: RoomAlias) : NavTarget

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
                    client.resolveRoomAlias(i.roomAlias)
                        .onFailure {
                            Timber.e(it, "Failed to resolve room alias")
                            backstack.newRoot(NavTarget.RoomAliasError(i.roomAlias))
                        }
                        .onSuccess {
                            subscribeToRoomInfoFlow(it)
                        }
                }
                is RoomIdOrAlias.Id -> {
                    subscribeToRoomInfoFlow(i.roomId)
                }
            }
        }
    }

    private fun subscribeToRoomInfoFlow(roomId: RoomId) {
        client.getRoomInfoFlow(
            roomId
        ).onEach { roomInfo ->
            Timber.d("Room membership: ${roomInfo.map { it.currentUserMembership }}")
            val info = roomInfo.getOrNull()
            if (info?.currentUserMembership == CurrentUserMembership.JOINED) {
                backstack.newRoot(NavTarget.JoinedRoom(roomId))
            } else {
                backstack.newRoot(NavTarget.JoinRoom(roomId))
            }
        }
            .launchIn(lifecycleScope)

        // When leaving the room from this session only, navigate up.
        roomMembershipObserver.updates
            .filter { update -> update.roomId == roomId && !update.isUserInRoom }
            .onEach {
                navigateUp()
            }
            .launchIn(lifecycleScope)
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            NavTarget.Loading -> loadingNode(buildContext)
            is NavTarget.JoinRoom -> {
                val inputs = JoinRoomEntryPoint.Inputs(
                    roomId = navTarget.roomId,
                    roomIdOrAlias = inputs.roomIdOrAlias,
                    roomDescription = inputs.roomDescription,
                )
                joinRoomEntryPoint.createNode(this, buildContext, inputs)
            }
            is NavTarget.JoinedRoom -> {
                val roomFlowNodeCallback = plugins<JoinedRoomLoadedFlowNode.Callback>()
                val inputs = JoinedRoomFlowNode.Inputs(navTarget.roomId, initialElement = inputs.initialElement)
                createNode<JoinedRoomFlowNode>(buildContext, plugins = listOf(inputs) + roomFlowNodeCallback)
            }
            is NavTarget.RoomAliasError -> roomAliasErrorNode(buildContext, navTarget.roomAlias)
        }
    }

    private fun loadingNode(buildContext: BuildContext) = node(buildContext) {
        Box(modifier = it.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }

    private fun roomAliasErrorNode(buildContext: BuildContext, roomAlias: RoomAlias) = node(buildContext) {
        Box(modifier = it.fillMaxSize(), contentAlignment = Alignment.Center) {
            AsyncFailure(
                throwable = Exception("Unable to resolve alias ${roomAlias.value}"),
                onRetry = { resolveRoomId() },
            )
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        BackstackView(transitionHandler = JumpToEndTransitionHandler())
    }
}

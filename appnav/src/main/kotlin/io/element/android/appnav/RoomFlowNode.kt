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

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.bumble.appyx.core.composable.Children
import com.bumble.appyx.core.lifecycle.subscribe
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.push
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.features.messages.api.MessagesEntryPoint
import io.element.android.features.roomdetails.api.RoomDetailsEntryPoint
import io.element.android.features.roomdetails.api.RoomMemberDetailsInput
import io.element.android.libraries.architecture.BackstackNode
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.architecture.animation.rememberDefaultTransitionHandler
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.RoomMembershipObserver
import io.element.android.services.appnavstate.api.AppNavigationStateService
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import timber.log.Timber

@ContributesNode(SessionScope::class)
class RoomFlowNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val messagesEntryPoint: MessagesEntryPoint,
    private val roomDetailsEntryPoint: RoomDetailsEntryPoint,
    private val appNavigationStateService: AppNavigationStateService,
    roomMembershipObserver: RoomMembershipObserver,
) : BackstackNode<RoomFlowNode.NavTarget>(
    backstack = BackStack(
        initialElement = plugins.filterIsInstance(Inputs::class.java).first().initialElement,
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins,
) {

    interface LifecycleCallback : NodeLifecycleCallback {
        fun onFlowCreated(room: MatrixRoom) = Unit
        fun onFlowReleased(room: MatrixRoom) = Unit
    }

    data class Inputs(
        val room: MatrixRoom,
        val initialElement: NavTarget = NavTarget.Messages,
    ) : NodeInputs

    private val inputs: Inputs = inputs()

    init {
        lifecycle.subscribe(
            onCreate = {
                Timber.v("OnCreate")
                plugins<LifecycleCallback>().forEach { it.onFlowCreated(inputs.room) }
                appNavigationStateService.onNavigateToRoom(id, inputs.room.roomId)
                fetchRoomMembers()
            },
            onDestroy = {
                Timber.v("OnDestroy")
                inputs.room.close()
                plugins<LifecycleCallback>().forEach { it.onFlowReleased(inputs.room) }
                appNavigationStateService.onLeavingRoom(id)
            }
        )
        roomMembershipObserver.updates
            .filter { update -> update.roomId == inputs.room.roomId && !update.isUserInRoom }
            .onEach {
                navigateUp()
            }
            .launchIn(lifecycleScope)
        inputs<Inputs>()
    }

    private fun fetchRoomMembers() = lifecycleScope.launch {
        val room = inputs.room
        room.updateMembers()
            .onFailure {
                Timber.e(it, "Fail to fetch members for room ${room.roomId}")
            }.onSuccess {
                Timber.v("Success fetching members for room ${room.roomId}")
            }
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            NavTarget.Messages -> {
                val callback = object : MessagesEntryPoint.Callback {
                    override fun onRoomDetailsClicked() {
                        backstack.push(NavTarget.RoomDetails)
                    }

                    override fun onUserDataClicked(userId: UserId) {
                        backstack.push(NavTarget.RoomMemberDetails(userId))
                    }
                }
                messagesEntryPoint.createNode(this, buildContext, callback)
            }
            NavTarget.RoomDetails -> {
                roomDetailsEntryPoint.createRoomDetailsNode(this, buildContext, emptyList())
            }
            is NavTarget.RoomMemberDetails -> {
                val plugins = listOf(RoomMemberDetailsInput(navTarget.userId))
                roomDetailsEntryPoint.createRoomMemberDetailsNode(this, buildContext, plugins)
            }
        }
    }

    sealed interface NavTarget : Parcelable {
        @Parcelize
        object Messages : NavTarget

        @Parcelize
        object RoomDetails : NavTarget

        @Parcelize
        data class RoomMemberDetails(val userId: UserId) : NavTarget
    }

    private val timeline = inputs.room.timeline()

    @Composable
    override fun View(modifier: Modifier) {

        DisposableEffect(Unit) {
            timeline.initialize()
            onDispose {
                timeline.dispose()
            }
        }

        Children(
            navModel = backstack,
            modifier = modifier,
            transitionHandler = rememberDefaultTransitionHandler(),
        )
    }
}

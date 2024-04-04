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

package io.element.android.appnav.room.joined

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.bumble.appyx.core.lifecycle.subscribe
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.push
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.appnav.di.RoomComponentFactory
import io.element.android.appnav.room.RoomNavigationTarget
import io.element.android.features.messages.api.MessagesEntryPoint
import io.element.android.features.roomdetails.api.RoomDetailsEntryPoint
import io.element.android.libraries.architecture.BackstackView
import io.element.android.libraries.architecture.BaseFlowNode
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.di.DaggerComponentOwner
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.RoomMembershipObserver
import io.element.android.services.appnavstate.api.AppNavigationStateService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import timber.log.Timber

@ContributesNode(SessionScope::class)
class JoinedRoomLoadedFlowNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val messagesEntryPoint: MessagesEntryPoint,
    private val roomDetailsEntryPoint: RoomDetailsEntryPoint,
    private val appNavigationStateService: AppNavigationStateService,
    private val appCoroutineScope: CoroutineScope,
    roomComponentFactory: RoomComponentFactory,
    roomMembershipObserver: RoomMembershipObserver,
) : BaseFlowNode<JoinedRoomLoadedFlowNode.NavTarget>(
    backstack = BackStack(
        initialElement = when(plugins.filterIsInstance(Inputs::class.java).first().initialElement){
            RoomNavigationTarget.Messages -> NavTarget.Messages
            RoomNavigationTarget.Details -> NavTarget.RoomDetails
            RoomNavigationTarget.NotificationSettings -> NavTarget.RoomNotificationSettings
        },
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins,
), DaggerComponentOwner {
    interface Callback : Plugin {
        fun onOpenRoom(roomId: RoomId)
        fun onForwardedToSingleRoom(roomId: RoomId)
        fun onOpenGlobalNotificationSettings()
    }

    data class Inputs(
        val room: MatrixRoom,
        val initialElement: RoomNavigationTarget = RoomNavigationTarget.Messages,
    ) : NodeInputs

    private val inputs: Inputs = inputs()
    private val callbacks = plugins.filterIsInstance<Callback>()
    override val daggerComponent = roomComponentFactory.create(inputs.room)

    init {
        lifecycle.subscribe(
            onCreate = {
                Timber.v("OnCreate => ${inputs.room.roomId}")
                appNavigationStateService.onNavigateToRoom(id, inputs.room.roomId)
                fetchRoomMembers()
            },
            onResume = {
                appCoroutineScope.launch {
                    inputs.room.subscribeToSync()
                }
            },
            onPause = {
                appCoroutineScope.launch {
                    inputs.room.unsubscribeFromSync()
                }
            },
            onDestroy = {
                Timber.v("OnDestroy")
                appNavigationStateService.onLeavingRoom(id)
            }
        )
    }

    private fun fetchRoomMembers() = lifecycleScope.launch {
        inputs.room.updateMembers()
    }

    private fun createRoomDetailsNode(buildContext: BuildContext, initialTarget: RoomDetailsEntryPoint.InitialTarget): Node {
        val callback = object : RoomDetailsEntryPoint.Callback {
            override fun onOpenGlobalNotificationSettings() {
                callbacks.forEach { it.onOpenGlobalNotificationSettings() }
            }

            override fun onOpenRoom(roomId: RoomId) {
                callbacks.forEach { it.onOpenRoom(roomId) }
            }
        }
        return roomDetailsEntryPoint.nodeBuilder(this, buildContext)
            .params(RoomDetailsEntryPoint.Params(initialTarget))
            .callback(callback)
            .build()
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

                    override fun onForwardedToSingleRoom(roomId: RoomId) {
                        callbacks.forEach { it.onForwardedToSingleRoom(roomId) }
                    }
                }
                messagesEntryPoint.createNode(this, buildContext, callback)
            }
            NavTarget.RoomDetails -> {
                createRoomDetailsNode(buildContext, RoomDetailsEntryPoint.InitialTarget.RoomDetails)
            }
            is NavTarget.RoomMemberDetails -> {
                createRoomDetailsNode(buildContext, RoomDetailsEntryPoint.InitialTarget.RoomMemberDetails(navTarget.userId))
            }
            NavTarget.RoomNotificationSettings -> {
                createRoomDetailsNode(buildContext, RoomDetailsEntryPoint.InitialTarget.RoomNotificationSettings)
            }
        }
    }

    sealed interface NavTarget : Parcelable {
        @Parcelize
        data object Messages : NavTarget

        @Parcelize
        data object RoomDetails : NavTarget

        @Parcelize
        data class RoomMemberDetails(val userId: UserId) : NavTarget

        @Parcelize
        data object RoomNotificationSettings : NavTarget
    }

    @Composable
    override fun View(modifier: Modifier) {
        // Rely on the View Lifecycle in addition to the Node Lifecycle,
        // because this node enters 'onDestroy' before his children, so it can leads to
        // using the room in a child node where it's already closed.
        DisposableEffect(Unit) {
            onDispose {
                if (lifecycle.currentState == Lifecycle.State.DESTROYED) {
                    inputs.room.destroy()
                }
            }
        }
        BackstackView()
    }
}

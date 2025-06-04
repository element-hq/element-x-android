/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.appnav.room.joined

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
import io.element.android.libraries.di.annotations.SessionCoroutineScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.permalink.PermalinkData
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.services.appnavstate.api.ActiveRoomsHolder
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
    @SessionCoroutineScope
    private val sessionCoroutineScope: CoroutineScope,
    private val matrixClient: MatrixClient,
    private val activeRoomsHolder: ActiveRoomsHolder,
    roomComponentFactory: RoomComponentFactory,
) : BaseFlowNode<JoinedRoomLoadedFlowNode.NavTarget>(
    backstack = BackStack(
        initialElement = when (val input = plugins.filterIsInstance<Inputs>().first().initialElement) {
            is RoomNavigationTarget.Messages -> NavTarget.Messages(input.focusedEventId)
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
        fun onPermalinkClick(data: PermalinkData, pushToBackstack: Boolean)
        fun onForwardedToSingleRoom(roomId: RoomId)
        fun onOpenGlobalNotificationSettings()
    }

    data class Inputs(
        val room: JoinedRoom,
        val initialElement: RoomNavigationTarget,
    ) : NodeInputs

    private val inputs: Inputs = inputs()
    private val callbacks = plugins.filterIsInstance<Callback>()
    override val daggerComponent = roomComponentFactory.create(inputs.room)

    init {
        lifecycle.subscribe(
            onCreate = {
                Timber.v("OnCreate => ${inputs.room.roomId}")
                appNavigationStateService.onNavigateToRoom(id, inputs.room.roomId)
                activeRoomsHolder.addRoom(inputs.room)
                fetchRoomMembers()
                trackVisitedRoom()
            },
            onResume = {
                sessionCoroutineScope.launch {
                    inputs.room.subscribeToSync()
                }
            },
            onDestroy = {
                Timber.v("OnDestroy")
                activeRoomsHolder.removeRoom(inputs.room.sessionId, inputs.room.roomId)
                inputs.room.destroy()
                appNavigationStateService.onLeavingRoom(id)
            }
        )
    }

    private fun trackVisitedRoom() = lifecycleScope.launch {
        matrixClient.trackRecentlyVisitedRoom(inputs.room.roomId)
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

            override fun onPermalinkClick(data: PermalinkData, pushToBackstack: Boolean) {
                callbacks.forEach { it.onPermalinkClick(data, pushToBackstack) }
            }

            override fun onForwardedToSingleRoom(roomId: RoomId) {
                callbacks.forEach { it.onForwardedToSingleRoom(roomId) }
            }
        }
        return roomDetailsEntryPoint.nodeBuilder(this, buildContext)
            .params(RoomDetailsEntryPoint.Params(initialTarget))
            .callback(callback)
            .build()
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            is NavTarget.Messages -> {
                createMessagesNode(buildContext, navTarget)
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

    private fun createMessagesNode(
        buildContext: BuildContext,
        navTarget: NavTarget.Messages,
    ): Node {
        val callback = object : MessagesEntryPoint.Callback {
            override fun onRoomDetailsClick() {
                backstack.push(NavTarget.RoomDetails)
            }

            override fun onUserDataClick(userId: UserId) {
                backstack.push(NavTarget.RoomMemberDetails(userId))
            }

            override fun onPermalinkClick(data: PermalinkData, pushToBackstack: Boolean) {
                callbacks.forEach { it.onPermalinkClick(data, pushToBackstack) }
            }

            override fun onForwardedToSingleRoom(roomId: RoomId) {
                callbacks.forEach { it.onForwardedToSingleRoom(roomId) }
            }
        }
        val params = MessagesEntryPoint.Params(
            MessagesEntryPoint.InitialTarget.Messages(navTarget.focusedEventId)
        )
        return messagesEntryPoint.nodeBuilder(this, buildContext)
            .params(params)
            .callback(callback)
            .build()
    }

    sealed interface NavTarget : Parcelable {
        @Parcelize
        data class Messages(val focusedEventId: EventId? = null) : NavTarget

        @Parcelize
        data object RoomDetails : NavTarget

        @Parcelize
        data class RoomMemberDetails(val userId: UserId) : NavTarget

        @Parcelize
        data object RoomNotificationSettings : NavTarget
    }

    @Composable
    override fun View(modifier: Modifier) {
        BackstackView()
    }
}

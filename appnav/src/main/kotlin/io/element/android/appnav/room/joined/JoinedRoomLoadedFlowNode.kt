/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
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
import com.bumble.appyx.navmodel.backstack.operation.pop
import com.bumble.appyx.navmodel.backstack.operation.push
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import io.element.android.annotations.ContributesNode
import io.element.android.appnav.di.RoomGraphFactory
import io.element.android.appnav.di.TimelineBindings
import io.element.android.appnav.room.RoomNavigationTarget
import io.element.android.features.forward.api.ForwardEntryPoint
import io.element.android.features.messages.api.MessagesEntryPoint
import io.element.android.features.roomdetails.api.RoomDetailsEntryPoint
import io.element.android.features.space.api.SpaceEntryPoint
import io.element.android.libraries.architecture.BackstackView
import io.element.android.libraries.architecture.BaseFlowNode
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.architecture.callback
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.architecture.waitForChildAttached
import io.element.android.libraries.di.DependencyInjectionGraphOwner
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.di.annotations.SessionCoroutineScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.ThreadId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.permalink.PermalinkData
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.services.analytics.api.AnalyticsLongRunningTransaction.LoadJoinedRoomFlow
import io.element.android.services.analytics.api.AnalyticsLongRunningTransaction.LoadMessagesUi
import io.element.android.services.analytics.api.AnalyticsLongRunningTransaction.OpenRoom
import io.element.android.services.analytics.api.AnalyticsService
import io.element.android.services.analytics.api.finishLongRunningTransaction
import io.element.android.services.appnavstate.api.ActiveRoomsHolder
import io.element.android.services.appnavstate.api.AppNavigationStateService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import timber.log.Timber

@ContributesNode(SessionScope::class)
@AssistedInject
class JoinedRoomLoadedFlowNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val messagesEntryPoint: MessagesEntryPoint,
    private val roomDetailsEntryPoint: RoomDetailsEntryPoint,
    private val spaceEntryPoint: SpaceEntryPoint,
    private val forwardEntryPoint: ForwardEntryPoint,
    private val appNavigationStateService: AppNavigationStateService,
    @SessionCoroutineScope
    private val sessionCoroutineScope: CoroutineScope,
    private val matrixClient: MatrixClient,
    private val activeRoomsHolder: ActiveRoomsHolder,
    private val analyticsService: AnalyticsService,
    roomGraphFactory: RoomGraphFactory,
) : BaseFlowNode<JoinedRoomLoadedFlowNode.NavTarget>(
    backstack = BackStack(
        initialElement = initialElement(plugins),
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins,
), DependencyInjectionGraphOwner {
    interface Callback : Plugin {
        fun navigateToRoom(roomId: RoomId, serverNames: List<String>)
        fun handlePermalinkClick(data: PermalinkData, pushToBackstack: Boolean)
        fun navigateToGlobalNotificationSettings()
    }

    data class Inputs(
        val room: JoinedRoom,
        val initialElement: RoomNavigationTarget,
    ) : NodeInputs

    private val inputs: Inputs = inputs()
    private val callback: Callback = callback()
    override val graph = roomGraphFactory.create(inputs.room)

    init {
        lifecycle.subscribe(
            onCreate = {
                val parent = analyticsService.getLongRunningTransaction(OpenRoom)
                analyticsService.startLongRunningTransaction(LoadMessagesUi, parent)
                Timber.v("OnCreate => ${inputs.room.roomId}")
                appNavigationStateService.onNavigateToRoom(id, inputs.room.roomId)
                activeRoomsHolder.addRoom(inputs.room)
                fetchRoomMembers()
                trackVisitedRoom()
            },
            onResume = {
                analyticsService.finishLongRunningTransaction(LoadJoinedRoomFlow)
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
            override fun navigateToGlobalNotificationSettings() {
                callback.navigateToGlobalNotificationSettings()
            }

            override fun navigateToRoom(roomId: RoomId, serverNames: List<String>) {
                callback.navigateToRoom(roomId, serverNames)
            }

            override fun handlePermalinkClick(data: PermalinkData, pushToBackstack: Boolean) {
                callback.handlePermalinkClick(data, pushToBackstack)
            }

            override fun startForwardEventFlow(eventId: EventId, fromPinnedEvents: Boolean) {
                backstack.push(NavTarget.ForwardEvent(eventId, fromPinnedEvents))
            }
        }
        return roomDetailsEntryPoint.createNode(
            parentNode = this,
            buildContext = buildContext,
            params = RoomDetailsEntryPoint.Params(initialTarget),
            callback = callback,
        )
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
            NavTarget.RoomMemberList -> {
                createRoomDetailsNode(buildContext, RoomDetailsEntryPoint.InitialTarget.RoomMemberList)
            }
            NavTarget.Space -> {
                createSpaceNode(buildContext)
            }
            is NavTarget.ForwardEvent -> {
                val timelineProvider = if (navTarget.fromPinnedEvents) {
                    (graph as TimelineBindings).pinnedEventsTimelineProvider
                } else {
                    (graph as TimelineBindings).timelineProvider
                }
                val params = ForwardEntryPoint.Params(navTarget.eventId, timelineProvider)
                val callback = object : ForwardEntryPoint.Callback {
                    override fun onDone(roomIds: List<RoomId>) {
                        backstack.pop()
                        roomIds.singleOrNull()?.let { roomId ->
                            callback.navigateToRoom(roomId, emptyList())
                        }
                    }
                }
                forwardEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                    params = params,
                    callback = callback,
                )
            }
        }
    }

    private fun createSpaceNode(buildContext: BuildContext): Node {
        val callback = object : SpaceEntryPoint.Callback {
            override fun navigateToRoom(roomId: RoomId, viaParameters: List<String>) {
                callback.navigateToRoom(roomId, viaParameters)
            }

            override fun navigateToRoomMemberList() {
                backstack.push(NavTarget.RoomMemberList)
            }
        }
        return spaceEntryPoint.createNode(
            parentNode = this,
            buildContext = buildContext,
            inputs = SpaceEntryPoint.Inputs(roomId = inputs.room.roomId),
            callback = callback,
        )
    }

    private fun createMessagesNode(
        buildContext: BuildContext,
        navTarget: NavTarget.Messages,
    ): Node {
        val callback = object : MessagesEntryPoint.Callback {
            override fun navigateToRoomDetails() {
                backstack.push(NavTarget.RoomDetails)
            }

            override fun navigateToRoomMemberDetails(userId: UserId) {
                backstack.push(NavTarget.RoomMemberDetails(userId))
            }

            override fun handlePermalinkClick(data: PermalinkData, pushToBackstack: Boolean) {
                callback.handlePermalinkClick(data, pushToBackstack)
            }

            override fun forwardEvent(eventId: EventId, fromPinnedEvents: Boolean) {
                backstack.push(NavTarget.ForwardEvent(eventId, fromPinnedEvents))
            }

            override fun navigateToRoom(roomId: RoomId) {
                callback.navigateToRoom(roomId, emptyList())
            }
        }
        val params = MessagesEntryPoint.Params(
            MessagesEntryPoint.InitialTarget.Messages(navTarget.focusedEventId)
        )
        return messagesEntryPoint.createNode(
            parentNode = this,
            buildContext = buildContext,
            params = params,
            callback = callback,
        )
    }

    sealed interface NavTarget : Parcelable {
        @Parcelize
        data object Space : NavTarget

        @Parcelize
        data class Messages(
            val focusedEventId: EventId? = null,
        ) : NavTarget

        @Parcelize
        data object RoomDetails : NavTarget

        @Parcelize
        data object RoomMemberList : NavTarget

        @Parcelize
        data class RoomMemberDetails(val userId: UserId) : NavTarget

        @Parcelize
        data class ForwardEvent(val eventId: EventId, val fromPinnedEvents: Boolean) : NavTarget

        @Parcelize
        data object RoomNotificationSettings : NavTarget
    }

    suspend fun attachThread(threadId: ThreadId, focusedEventId: EventId?) {
        val messageNode = waitForChildAttached<Node, NavTarget> { navTarget ->
            navTarget is NavTarget.Messages
        }
        (messageNode as? MessagesEntryPoint.NodeProxy)?.attachThread(threadId, focusedEventId)
    }

    @Composable
    override fun View(modifier: Modifier) {
        BackstackView()
    }
}

private fun initialElement(plugins: List<Plugin>): JoinedRoomLoadedFlowNode.NavTarget {
    val input = plugins.filterIsInstance<JoinedRoomLoadedFlowNode.Inputs>().single()
    return when (input.initialElement) {
        is RoomNavigationTarget.Root -> {
            if (input.room.roomInfoFlow.value.isSpace) {
                JoinedRoomLoadedFlowNode.NavTarget.Space
            } else {
                JoinedRoomLoadedFlowNode.NavTarget.Messages(input.initialElement.eventId)
            }
        }
        RoomNavigationTarget.Details -> JoinedRoomLoadedFlowNode.NavTarget.RoomDetails
        RoomNavigationTarget.NotificationSettings -> JoinedRoomLoadedFlowNode.NavTarget.RoomNotificationSettings
    }
}

/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl

import android.app.Activity
import android.os.Parcelable
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import com.bumble.appyx.core.lifecycle.subscribe
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.node.node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.pop
import com.bumble.appyx.navmodel.backstack.operation.push
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import im.vector.app.features.analytics.plan.MobileScreen
import io.element.android.annotations.ContributesNode
import io.element.android.features.home.api.HomeEntryPoint
import io.element.android.features.home.impl.components.RoomListMenuAction
import io.element.android.features.home.impl.model.RoomListRoomSummary
import io.element.android.features.home.impl.roomlist.RoomListEvents
import io.element.android.features.invite.api.InviteData
import io.element.android.features.invite.api.acceptdecline.AcceptDeclineInviteView
import io.element.android.features.invite.api.declineandblock.DeclineInviteAndBlockEntryPoint
import io.element.android.features.leaveroom.api.LeaveRoomRenderer
import io.element.android.features.logout.api.direct.DirectLogoutView
import io.element.android.features.reportroom.api.ReportRoomEntryPoint
import io.element.android.features.rolesandpermissions.api.ChangeRoomMemberRolesEntryPoint
import io.element.android.features.rolesandpermissions.api.ChangeRoomMemberRolesListType
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.BackstackView
import io.element.android.libraries.architecture.BaseFlowNode
import io.element.android.libraries.architecture.appyx.launchMolecule
import io.element.android.libraries.architecture.callback
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.deeplink.api.usecase.InviteFriendsUseCase
import io.element.android.libraries.designsystem.components.ProgressDialog
import io.element.android.libraries.designsystem.utils.DelayedVisibility
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.di.annotations.SessionCoroutineScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.services.analytics.api.AnalyticsService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import timber.log.Timber
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.milliseconds

@ContributesNode(SessionScope::class)
@AssistedInject
class HomeFlowNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val matrixClient: MatrixClient,
    private val presenter: HomePresenter,
    private val inviteFriendsUseCase: InviteFriendsUseCase,
    private val analyticsService: AnalyticsService,
    private val acceptDeclineInviteView: AcceptDeclineInviteView,
    private val directLogoutView: DirectLogoutView,
    private val reportRoomEntryPoint: ReportRoomEntryPoint,
    private val declineInviteAndBlockUserEntryPoint: DeclineInviteAndBlockEntryPoint,
    private val changeRoomMemberRolesEntryPoint: ChangeRoomMemberRolesEntryPoint,
    private val leaveRoomRenderer: LeaveRoomRenderer,
    @SessionCoroutineScope private val sessionCoroutineScope: CoroutineScope,
) : BaseFlowNode<HomeFlowNode.NavTarget>(
    backstack = BackStack(
        initialElement = NavTarget.Root,
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins
) {
    private val callback: HomeEntryPoint.Callback = callback()
    private val stateFlow = launchMolecule { presenter.present() }

    override fun onBuilt() {
        super.onBuilt()
        lifecycle.subscribe(
            onResume = {
                analyticsService.screen(MobileScreen(screenName = MobileScreen.ScreenName.Home))
            }
        )
        whenChildAttached {
            commonLifecycle: Lifecycle,
            changeRoomMemberRolesNode: ChangeRoomMemberRolesEntryPoint.NodeProxy,
            ->
            commonLifecycle.coroutineScope.launch {
                val isNewOwnerSelected = changeRoomMemberRolesNode.waitForCompletion()
                withContext(NonCancellable) {
                    backstack.pop()
                    if (isNewOwnerSelected) {
                        onNewOwnersSelected(changeRoomMemberRolesNode.roomId)
                    }
                }
            }
        }
    }

    sealed interface NavTarget : Parcelable {
        @Parcelize
        data object Root : NavTarget

        @Parcelize
        data class ReportRoom(val roomId: RoomId) : NavTarget

        @Parcelize
        data class DeclineInviteAndBlockUser(val inviteData: InviteData) : NavTarget

        @Parcelize
        data class SelectNewOwnersWhenLeavingRoom(val roomId: RoomId) : NavTarget
    }

    private fun navigateToReportRoom(roomId: RoomId) {
        backstack.push(NavTarget.ReportRoom(roomId))
    }

    private fun navigateToDeclineInviteAndBlockUser(roomSummary: RoomListRoomSummary) {
        backstack.push(NavTarget.DeclineInviteAndBlockUser(roomSummary.toInviteData()))
    }

    private fun onMenuActionClick(activity: Activity, roomListMenuAction: RoomListMenuAction) {
        when (roomListMenuAction) {
            RoomListMenuAction.InviteFriends -> {
                inviteFriendsUseCase.execute(activity)
            }
            RoomListMenuAction.ReportBug -> {
                callback.navigateToBugReport()
            }
        }
    }

    private fun navigateToSelectNewOwnersWhenLeavingRoom(roomId: RoomId) {
        backstack.push(NavTarget.SelectNewOwnersWhenLeavingRoom(roomId))
    }

    private fun onNewOwnersSelected(roomId: RoomId) {
        stateFlow.value.roomListState.eventSink(RoomListEvents.LeaveRoom(roomId, needsConfirmation = false))
    }

    private fun rootNode(buildContext: BuildContext): Node {
        return node(buildContext) { modifier ->
            val state by stateFlow.collectAsState()
            val activity = requireNotNull(LocalActivity.current)

            val loadingJoinedRoomJob = remember { mutableStateOf<AsyncData<Job>>(AsyncData.Uninitialized) }
            if (loadingJoinedRoomJob.value.isLoading()) {
                DelayedVisibility(duration = 400.milliseconds) {
                    ProgressDialog(
                        onDismissRequest = {
                            loadingJoinedRoomJob.value.dataOrNull()?.cancel()
                            loadingJoinedRoomJob.value = AsyncData.Uninitialized
                        }
                    )
                }
            }

            fun navigateToRoom(
                roomId: RoomId,
            ) {
                if (!loadingJoinedRoomJob.value.isUninitialized()) {
                    Timber.w("Already loading a room, ignoring navigateToRoom for $roomId")
                    return
                }

                val job = sessionCoroutineScope.launch {
                    runCatchingExceptions {
                        matrixClient.getJoinedRoom(roomId)
                    }.fold(
                        onSuccess = { joinedRoom ->
                            if (isActive) {
                                callback.navigateToRoom(roomId, joinedRoom)
                                loadingJoinedRoomJob.value = AsyncData.Success(coroutineContext.job)
                                // Wait a bit before resetting the state to avoid allowing to open several rooms
                                delay(200.milliseconds)
                                loadingJoinedRoomJob.value = AsyncData.Uninitialized
                            }
                        },
                        onFailure = {
                            // If the operation wasn't cancelled, navigate without the room, using the room id
                            if (it !is CancellationException) {
                                callback.navigateToRoom(roomId, null)
                            }
                            loadingJoinedRoomJob.value = AsyncData.Failure(error = it, prevData = coroutineContext.job)
                            // Wait a bit before resetting the state to avoid allowing to open several rooms
                            delay(200.milliseconds)
                            loadingJoinedRoomJob.value = AsyncData.Uninitialized
                        }
                    )
                }
                loadingJoinedRoomJob.value = AsyncData.Loading(job)
            }

            HomeView(
                homeState = state,
                onRoomClick = ::navigateToRoom,
                onSettingsClick = callback::navigateToSettings,
                onStartChatClick = callback::navigateToCreateRoom,
                onCreateSpaceClick = callback::navigateToCreateSpace,
                onSetUpRecoveryClick = callback::navigateToSetUpRecovery,
                onConfirmRecoveryKeyClick = callback::navigateToEnterRecoveryKey,
                onRoomSettingsClick = callback::navigateToRoomSettings,
                onMenuActionClick = { onMenuActionClick(activity, it) },
                onReportRoomClick = ::navigateToReportRoom,
                onDeclineInviteAndBlockUser = ::navigateToDeclineInviteAndBlockUser,
                modifier = modifier,
                acceptDeclineInviteView = {
                    acceptDeclineInviteView.Render(
                        state = state.roomListState.acceptDeclineInviteState,
                        onAcceptInviteSuccess = ::navigateToRoom,
                        onDeclineInviteSuccess = { },
                        modifier = Modifier
                    )
                },
                leaveRoomView = {
                    leaveRoomRenderer.Render(
                        state = state.roomListState.leaveRoomState,
                        onSelectNewOwners = ::navigateToSelectNewOwnersWhenLeavingRoom,
                        modifier = Modifier
                    )
                }
            )
            directLogoutView.Render(state.directLogoutState)
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        BackstackView()
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            is NavTarget.ReportRoom -> {
                reportRoomEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                    roomId = navTarget.roomId,
                )
            }
            is NavTarget.DeclineInviteAndBlockUser -> {
                declineInviteAndBlockUserEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                    inviteData = navTarget.inviteData,
                )
            }
            is NavTarget.SelectNewOwnersWhenLeavingRoom -> {
                val room = runBlocking { matrixClient.getJoinedRoom(navTarget.roomId) } ?: error("Room ${navTarget.roomId} not found")
                changeRoomMemberRolesEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                    room = room,
                    listType = ChangeRoomMemberRolesListType.SelectNewOwnersWhenLeaving,
                )
            }
            NavTarget.Root -> rootNode(buildContext)
        }
    }
}

/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomlist.impl

import android.app.Activity
import android.os.Parcelable
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.lifecycle.subscribe
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.node.node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.push
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import im.vector.app.features.analytics.plan.MobileScreen
import io.element.android.anvilannotations.ContributesNode
import io.element.android.features.invite.api.InviteData
import io.element.android.features.invite.api.acceptdecline.AcceptDeclineInviteView
import io.element.android.features.invite.api.declineandblock.DeclineInviteAndBlockEntryPoint
import io.element.android.features.logout.api.direct.DirectLogoutView
import io.element.android.features.reportroom.api.ReportRoomEntryPoint
import io.element.android.features.roomlist.api.RoomListEntryPoint
import io.element.android.features.roomlist.impl.components.RoomListMenuAction
import io.element.android.features.roomlist.impl.model.RoomListRoomSummary
import io.element.android.libraries.architecture.BackstackView
import io.element.android.libraries.architecture.BaseFlowNode
import io.element.android.libraries.deeplink.usecase.InviteFriendsUseCase
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.services.analytics.api.AnalyticsService
import kotlinx.parcelize.Parcelize

@ContributesNode(SessionScope::class)
class RoomListFlowNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenter: RoomListPresenter,
    private val inviteFriendsUseCase: InviteFriendsUseCase,
    private val analyticsService: AnalyticsService,
    private val acceptDeclineInviteView: AcceptDeclineInviteView,
    private val directLogoutView: DirectLogoutView,
    private val reportRoomEntryPoint: ReportRoomEntryPoint,
    private val declineInviteAndBlockUserEntryPoint: DeclineInviteAndBlockEntryPoint,
) : BaseFlowNode<RoomListFlowNode.NavTarget>(
    backstack = BackStack(
        initialElement = NavTarget.Root,
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins
) {
    init {
        lifecycle.subscribe(
            onResume = {
                analyticsService.screen(MobileScreen(screenName = MobileScreen.ScreenName.Home))
            }
        )
    }

    sealed interface NavTarget : Parcelable {
        @Parcelize
        data object Root : NavTarget

        @Parcelize
        data class ReportRoom(val roomId: RoomId) : NavTarget

        @Parcelize
        data class DeclineInviteAndBlockUser(val inviteData: InviteData) : NavTarget
    }

    private fun onRoomClick(roomId: RoomId) {
        plugins<RoomListEntryPoint.Callback>().forEach { it.onRoomClick(roomId) }
    }

    private fun onOpenSettings() {
        plugins<RoomListEntryPoint.Callback>().forEach { it.onSettingsClick() }
    }

    private fun onCreateRoomClick() {
        plugins<RoomListEntryPoint.Callback>().forEach { it.onCreateRoomClick() }
    }

    private fun onSetUpRecoveryClick() {
        plugins<RoomListEntryPoint.Callback>().forEach { it.onSetUpRecoveryClick() }
    }

    private fun onSessionConfirmRecoveryKeyClick() {
        plugins<RoomListEntryPoint.Callback>().forEach { it.onSessionConfirmRecoveryKeyClick() }
    }

    private fun onRoomSettingsClick(roomId: RoomId) {
        plugins<RoomListEntryPoint.Callback>().forEach { it.onRoomSettingsClick(roomId) }
    }

    private fun onReportRoomClick(roomId: RoomId) {
        backstack.push(NavTarget.ReportRoom(roomId))
    }

    private fun onDeclineInviteAndBlockUserClick(roomSummary: RoomListRoomSummary) {
        backstack.push(NavTarget.DeclineInviteAndBlockUser(roomSummary.toInviteData()))
    }

    private fun onMenuActionClick(activity: Activity, roomListMenuAction: RoomListMenuAction) {
        when (roomListMenuAction) {
            RoomListMenuAction.InviteFriends -> {
                inviteFriendsUseCase.execute(activity)
            }
            RoomListMenuAction.ReportBug -> {
                plugins<RoomListEntryPoint.Callback>().forEach { it.onReportBugClick() }
            }
        }
    }

    fun rootNode(buildContext: BuildContext): Node {
        return node(buildContext) { modifier ->
            val state = presenter.present()
            val activity = requireNotNull(LocalActivity.current)

            RoomListView(
                state = state,
                onRoomClick = this::onRoomClick,
                onSettingsClick = this::onOpenSettings,
                onCreateRoomClick = this::onCreateRoomClick,
                onSetUpRecoveryClick = this::onSetUpRecoveryClick,
                onConfirmRecoveryKeyClick = this::onSessionConfirmRecoveryKeyClick,
                onRoomSettingsClick = this::onRoomSettingsClick,
                onMenuActionClick = { onMenuActionClick(activity, it) },
                onReportRoomClick = this::onReportRoomClick,
                onDeclineInviteAndBlockUser = this::onDeclineInviteAndBlockUserClick,
                modifier = modifier,
            ) {
                acceptDeclineInviteView.Render(
                    state = state.acceptDeclineInviteState,
                    onAcceptInviteSuccess = this::onRoomClick,
                    onDeclineInviteSuccess = { },
                    modifier = Modifier
                )
            }

            directLogoutView.Render(state.directLogoutState)
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        BackstackView()
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            is NavTarget.ReportRoom -> reportRoomEntryPoint.createNode(this, buildContext, navTarget.roomId)
            is NavTarget.DeclineInviteAndBlockUser -> declineInviteAndBlockUserEntryPoint.createNode(this, buildContext, navTarget.inviteData)
            NavTarget.Root -> rootNode(buildContext)
        }
    }
}

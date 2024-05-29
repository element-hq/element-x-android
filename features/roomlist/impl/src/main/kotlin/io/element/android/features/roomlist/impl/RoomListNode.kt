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

package io.element.android.features.roomlist.impl

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.bumble.appyx.core.lifecycle.subscribe
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import im.vector.app.features.analytics.plan.MobileScreen
import io.element.android.anvilannotations.ContributesNode
import io.element.android.features.invite.api.response.AcceptDeclineInviteView
import io.element.android.features.roomlist.api.RoomListEntryPoint
import io.element.android.features.roomlist.impl.components.RoomListMenuAction
import io.element.android.libraries.deeplink.usecase.InviteFriendsUseCase
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.services.analytics.api.AnalyticsService

@ContributesNode(SessionScope::class)
class RoomListNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenter: RoomListPresenter,
    private val inviteFriendsUseCase: InviteFriendsUseCase,
    private val analyticsService: AnalyticsService,
    private val acceptDeclineInviteView: AcceptDeclineInviteView,
) : Node(buildContext, plugins = plugins) {
    init {
        lifecycle.subscribe(
            onResume = {
                analyticsService.screen(MobileScreen(screenName = MobileScreen.ScreenName.Home))
            }
        )
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

    private fun onSessionConfirmRecoveryKeyClick() {
        plugins<RoomListEntryPoint.Callback>().forEach { it.onSessionConfirmRecoveryKeyClick() }
    }

    private fun onRoomSettingsClick(roomId: RoomId) {
        plugins<RoomListEntryPoint.Callback>().forEach { it.onRoomSettingsClick(roomId) }
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

    private fun onRoomDirectorySearchClick() {
        plugins<RoomListEntryPoint.Callback>().forEach { it.onRoomDirectorySearchClick() }
    }

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        val activity = LocalContext.current as Activity
        RoomListView(
            state = state,
            onRoomClick = this::onRoomClick,
            onSettingsClick = this::onOpenSettings,
            onCreateRoomClick = this::onCreateRoomClick,
            onConfirmRecoveryKeyClick = this::onSessionConfirmRecoveryKeyClick,
            onRoomSettingsClick = this::onRoomSettingsClick,
            onMenuActionClick = { onMenuActionClick(activity, it) },
            onRoomDirectorySearchClick = this::onRoomDirectorySearchClick,
            modifier = modifier,
        ) {
            acceptDeclineInviteView.Render(
                state = state.acceptDeclineInviteState,
                onAcceptInvite = this::onRoomClick,
                onDeclineInvite = { },
                modifier = Modifier
            )
        }
    }
}

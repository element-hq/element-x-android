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
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.features.roomlist.api.RoomListEntryPoint
import io.element.android.features.roomlist.impl.components.RoomListMenuAction
import io.element.android.libraries.deeplink.usecase.InviteFriendsUseCase
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.core.RoomId

@ContributesNode(SessionScope::class)
class RoomListNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenter: RoomListPresenter,
    private val inviteFriendsUseCase: InviteFriendsUseCase,
) : Node(buildContext, plugins = plugins) {

    private fun onRoomClicked(roomId: RoomId) {
        plugins<RoomListEntryPoint.Callback>().forEach { it.onRoomClicked(roomId) }
    }

    private fun onOpenSettings() {
        plugins<RoomListEntryPoint.Callback>().forEach { it.onSettingsClicked() }
    }

    private fun onCreateRoomClicked() {
        plugins<RoomListEntryPoint.Callback>().forEach { it.onCreateRoomClicked() }
    }

    private fun onSessionVerificationClicked() {
        plugins<RoomListEntryPoint.Callback>().forEach { it.onSessionVerificationClicked() }
    }

    private fun onInvitesClicked() {
        plugins<RoomListEntryPoint.Callback>().forEach { it.onInvitesClicked() }
    }

    private fun onRoomSettingsClicked(roomId: RoomId) {
        plugins<RoomListEntryPoint.Callback>().forEach { it.onRoomSettingsClicked(roomId) }
    }

    private fun onMenuActionClicked(activity: Activity, roomListMenuAction: RoomListMenuAction) {
        when (roomListMenuAction) {
            RoomListMenuAction.InviteFriends -> {
                inviteFriendsUseCase.execute(activity)
            }
            RoomListMenuAction.ReportBug -> {
                plugins<RoomListEntryPoint.Callback>().forEach { it.onReportBugClicked() }
            }
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        val activity = LocalContext.current as Activity
        RoomListView(
            state = state,
            onRoomClicked = this::onRoomClicked,
            onSettingsClicked = this::onOpenSettings,
            onCreateRoomClicked = this::onCreateRoomClicked,
            onVerifyClicked = this::onSessionVerificationClicked,
            onInvitesClicked = this::onInvitesClicked,
            onRoomSettingsClicked = this::onRoomSettingsClicked,
            onMenuActionClicked = { onMenuActionClicked(activity, it) },
            modifier = modifier,
        )
    }
}

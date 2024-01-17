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

package io.element.android.features.roomdetails.impl

import android.content.Context
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
import io.element.android.libraries.androidutils.system.startSharePlainTextIntent
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.matrix.api.permalink.PermalinkBuilder
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.services.analytics.api.AnalyticsService
import timber.log.Timber
import io.element.android.libraries.androidutils.R as AndroidUtilsR

@ContributesNode(RoomScope::class)
class RoomDetailsNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenter: RoomDetailsPresenter,
    private val room: MatrixRoom,
    private val analyticsService: AnalyticsService,
) : Node(buildContext, plugins = plugins) {
    interface Callback : Plugin {
        fun openRoomMemberList()
        fun openInviteMembers()
        fun editRoomDetails()
        fun openRoomNotificationSettings()
        fun openAvatarPreview(name: String, url: String)
        fun openPollHistory()
    }

    private val callbacks = plugins<Callback>()

    init {
        lifecycle.subscribe(
            onResume = {
                analyticsService.screen(MobileScreen(screenName = MobileScreen.ScreenName.RoomDetails))
            }
        )
    }

    private fun openRoomMemberList() {
        callbacks.forEach { it.openRoomMemberList() }
    }

    private fun openRoomNotificationSettings() {
        callbacks.forEach { it.openRoomNotificationSettings() }
    }

    private fun invitePeople() {
        callbacks.forEach { it.openInviteMembers() }
    }

    private fun openPollHistory() {
        callbacks.forEach { it.openPollHistory() }
    }

    private fun onShareRoom(context: Context) {
        val alias = room.alias ?: room.alternativeAliases.firstOrNull()
        val permalinkResult = alias?.let { PermalinkBuilder.permalinkForRoomAlias(it) }
            ?: PermalinkBuilder.permalinkForRoomId(room.roomId)
        permalinkResult.onSuccess { permalink ->
            context.startSharePlainTextIntent(
                activityResultLauncher = null,
                chooserTitle = context.getString(R.string.screen_room_details_share_room_title),
                text = permalink,
                noActivityFoundMessage = context.getString(AndroidUtilsR.string.error_no_compatible_app_found)
            )
        }.onFailure {
            Timber.e(it)
        }
    }

    private fun onShareMember(context: Context, member: RoomMember) {
        val permalinkResult = PermalinkBuilder.permalinkForUser(member.userId)
        permalinkResult.onSuccess { permalink ->
            context.startSharePlainTextIntent(
                activityResultLauncher = null,
                chooserTitle = context.getString(R.string.screen_room_details_share_room_title),
                text = permalink,
                noActivityFoundMessage = context.getString(AndroidUtilsR.string.error_no_compatible_app_found)
            )
        }.onFailure {
            Timber.e(it)
        }
    }

    private fun onEditRoomDetails() {
        callbacks.forEach { it.editRoomDetails() }
    }

    private fun openAvatarPreview(name: String, url: String) {
        callbacks.forEach { it.openAvatarPreview(name, url) }
    }

    @Composable
    override fun View(modifier: Modifier) {
        val context = LocalContext.current
        val state = presenter.present()

        fun onShareRoom() {
            this.onShareRoom(context)
        }

        fun onShareMember(roomMember: RoomMember) {
            this.onShareMember(context, roomMember)
        }

        fun onActionClicked(action: RoomDetailsAction) {
            when (action) {
                RoomDetailsAction.Edit -> onEditRoomDetails()
                RoomDetailsAction.AddTopic -> onEditRoomDetails()
            }
        }

        RoomDetailsView(
            state = state,
            modifier = modifier,
            goBack = this::navigateUp,
            onActionClicked = ::onActionClicked,
            onShareRoom = ::onShareRoom,
            onShareMember = ::onShareMember,
            openRoomMemberList = ::openRoomMemberList,
            openRoomNotificationSettings = ::openRoomNotificationSettings,
            invitePeople = ::invitePeople,
            openAvatarPreview = ::openAvatarPreview,
            openPollHistory = ::openPollHistory,
        )
    }
}

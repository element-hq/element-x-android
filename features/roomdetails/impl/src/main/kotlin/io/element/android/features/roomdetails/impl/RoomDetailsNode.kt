/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import com.bumble.appyx.core.lifecycle.subscribe
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import im.vector.app.features.analytics.plan.MobileScreen
import io.element.android.anvilannotations.ContributesNode
import io.element.android.features.leaveroom.api.LeaveRoomRenderer
import io.element.android.libraries.androidutils.system.startSharePlainTextIntent
import io.element.android.libraries.architecture.appyx.launchMolecule
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.BaseRoom
import io.element.android.services.analytics.api.AnalyticsService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import io.element.android.libraries.androidutils.R as AndroidUtilsR

@ContributesNode(RoomScope::class)
class RoomDetailsNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenter: RoomDetailsPresenter,
    private val room: BaseRoom,
    private val analyticsService: AnalyticsService,
    private val leaveRoomRenderer: LeaveRoomRenderer,
) : Node(buildContext, plugins = plugins) {
    interface Callback : Plugin {
        fun openRoomMemberList()
        fun openInviteMembers()
        fun editRoomDetails()
        fun openRoomNotificationSettings()
        fun openAvatarPreview(name: String, url: String)
        fun openPollHistory()
        fun openMediaGallery()
        fun openAdminSettings()
        fun openPinnedMessagesList()
        fun openKnockRequestsList()
        fun openSecurityAndPrivacy()
        fun openDmUserProfile(userId: UserId)
        fun onJoinCall()
        fun openReportRoom()
        fun onSelectNewOwnersWhenLeaving()
    }

    private val callback = plugins<Callback>().first()

    init {
        lifecycle.subscribe(
            onResume = {
                analyticsService.screen(MobileScreen(screenName = MobileScreen.ScreenName.RoomDetails))
            }
        )
    }

    private fun openRoomMemberList() {
        callback.openRoomMemberList()
    }

    private fun openRoomNotificationSettings() {
        callback.openRoomNotificationSettings()
    }

    private fun invitePeople() {
        callback.openInviteMembers()
    }

    private fun openPollHistory() {
        callback.openPollHistory()
    }

    private fun openMediaGallery() {
        callback.openMediaGallery()
    }

    private fun onJoinCall() {
        callback.onJoinCall()
    }

    private fun CoroutineScope.onShareRoom(context: Context) = launch {
        room.getPermalink()
            .onSuccess { permalink ->
                context.startSharePlainTextIntent(
                    activityResultLauncher = null,
                    chooserTitle = context.getString(R.string.screen_room_details_share_room_title),
                    text = permalink,
                    noActivityFoundMessage = context.getString(AndroidUtilsR.string.error_no_compatible_app_found)
                )
            }
            .onFailure {
                Timber.e(it)
            }
    }

    private fun onEditRoomDetails() {
        callback.editRoomDetails()
    }

    private fun openAvatarPreview(name: String, url: String) {
        callback.openAvatarPreview(name, url)
    }

    private fun openAdminSettings() {
        callback.openAdminSettings()
    }

    private fun openPinnedMessages() {
        callback.openPinnedMessagesList()
    }

    private fun openKnockRequestsLists() {
        callback.openKnockRequestsList()
    }

    private fun openSecurityAndPrivacy() {
        callback.openSecurityAndPrivacy()
    }

    private fun onProfileClick(userId: UserId) {
        callback.openDmUserProfile(userId)
    }

    private fun onReportRoomClick() {
        callback.openReportRoom()
    }

    private fun onSelectNewOwnersWhenLeaving() {
        return callback.onSelectNewOwnersWhenLeaving()
    }

    private val stateFlow = launchMolecule { presenter.present() }

    fun onNewOwnersSelected() {
        stateFlow.value.eventSink(RoomDetailsEvent.LeaveRoom(needsConfirmation = false))
    }

    @Composable
    override fun View(modifier: Modifier) {
        val context = LocalContext.current
        val state by stateFlow.collectAsState()

        fun onShareRoom() {
            lifecycleScope.onShareRoom(context)
        }

        fun onActionClick(action: RoomDetailsAction) {
            when (action) {
                RoomDetailsAction.Edit -> onEditRoomDetails()
                RoomDetailsAction.AddTopic -> onEditRoomDetails()
            }
        }

        RoomDetailsView(
            state = state,
            modifier = modifier,
            goBack = this::navigateUp,
            onActionClick = ::onActionClick,
            onShareRoom = ::onShareRoom,
            openRoomMemberList = ::openRoomMemberList,
            openRoomNotificationSettings = ::openRoomNotificationSettings,
            invitePeople = ::invitePeople,
            openAvatarPreview = ::openAvatarPreview,
            openPollHistory = ::openPollHistory,
            openMediaGallery = ::openMediaGallery,
            openAdminSettings = this::openAdminSettings,
            onJoinCallClick = ::onJoinCall,
            onPinnedMessagesClick = ::openPinnedMessages,
            onKnockRequestsClick = ::openKnockRequestsLists,
            onSecurityAndPrivacyClick = ::openSecurityAndPrivacy,
            onProfileClick = ::onProfileClick,
            onReportRoomClick = ::onReportRoomClick,
            leaveRoomView = {
                leaveRoomRenderer.Render(
                    state = state.leaveRoomState,
                    onSelectNewOwners = { onSelectNewOwnersWhenLeaving() },
                    modifier = Modifier
                )
            }
        )
    }
}

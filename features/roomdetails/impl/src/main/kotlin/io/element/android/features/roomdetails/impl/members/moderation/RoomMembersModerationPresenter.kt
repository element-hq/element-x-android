/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.members.moderation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import im.vector.app.features.analytics.plan.RoomModeration
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runUpdatingState
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.RoomMembershipState
import io.element.android.libraries.matrix.ui.room.canBanAsState
import io.element.android.libraries.matrix.ui.room.canKickAsState
import io.element.android.libraries.matrix.ui.room.isDmAsState
import io.element.android.libraries.matrix.ui.room.userPowerLevelAsState
import io.element.android.services.analytics.api.AnalyticsService
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import javax.inject.Inject

class RoomMembersModerationPresenter @Inject constructor(
    private val room: MatrixRoom,
    private val dispatchers: CoroutineDispatchers,
    private val analyticsService: AnalyticsService,
) : Presenter<RoomMembersModerationState> {
    private var selectedMember by mutableStateOf<RoomMember?>(null)

    @Composable
    override fun present(): RoomMembersModerationState {
        val coroutineScope = rememberCoroutineScope()
        val syncUpdateFlow = room.syncUpdateFlow.collectAsState()
        val canBan by room.canBanAsState(syncUpdateFlow.value)
        val canKick by room.canKickAsState(syncUpdateFlow.value)
        val isDm by room.isDmAsState(syncUpdateFlow.value)
        val currentUserMemberPowerLevel by room.userPowerLevelAsState(syncUpdateFlow.value)

        val canDisplayModerationActions by remember {
            derivedStateOf { !isDm && (canBan || canKick) }
        }
        val canDisplayBannedUsers by remember {
            derivedStateOf { !isDm && canBan }
        }
        val moderationActions by remember {
            derivedStateOf {
                buildList {
                    selectedMember?.let { roomMember ->
                        add(ModerationAction.DisplayProfile(roomMember.userId))
                        if (currentUserMemberPowerLevel > roomMember.powerLevel) {
                            if (canKick) {
                                add(ModerationAction.KickUser(roomMember.userId))
                            }
                            if (canBan) {
                                add(ModerationAction.BanUser(roomMember.userId))
                            }
                        }
                    }
                }.toPersistentList()
            }
        }

        val kickUserAsyncAction =
            remember { mutableStateOf(AsyncAction.Uninitialized as AsyncAction<Unit>) }
        val banUserAsyncAction =
            remember { mutableStateOf(AsyncAction.Uninitialized as AsyncAction<Unit>) }
        val unbanUserAsyncAction =
            remember { mutableStateOf(AsyncAction.Uninitialized as AsyncAction<Unit>) }

        fun handleEvent(event: RoomMembersModerationEvents) {
            when (event) {
                is RoomMembersModerationEvents.SelectRoomMember -> {
                    if (event.roomMember.membership == RoomMembershipState.BAN && canBan) {
                        // In this case the view will render a dialog to confirm the unbanning of the user
                        unbanUserAsyncAction.value = ConfirmingRoomMemberAction(event.roomMember)
                    } else {
                        // In this case the view will render a bottom sheet.
                        selectedMember = event.roomMember
                    }
                }
                is RoomMembersModerationEvents.KickUser -> {
                    selectedMember?.let {
                        coroutineScope.kickUser(it.userId, kickUserAsyncAction)
                    }
                    selectedMember = null
                }
                is RoomMembersModerationEvents.BanUser -> {
                    if (banUserAsyncAction.value.isConfirming()) {
                        selectedMember?.let {
                            coroutineScope.banUser(it.userId, banUserAsyncAction)
                        }
                        selectedMember = null
                    } else {
                        banUserAsyncAction.value = AsyncAction.ConfirmingNoParams
                    }
                }
                is RoomMembersModerationEvents.UnbanUser -> {
                    // We are already confirming when we are reaching this point
                    coroutineScope.unbanUser(event.userId, unbanUserAsyncAction)
                }
                is RoomMembersModerationEvents.Reset -> {
                    selectedMember = null
                    kickUserAsyncAction.value = AsyncAction.Uninitialized
                    banUserAsyncAction.value = AsyncAction.Uninitialized
                    unbanUserAsyncAction.value = AsyncAction.Uninitialized
                }
            }
        }

        return RoomMembersModerationState(
            canDisplayModerationActions = canDisplayModerationActions,
            selectedRoomMember = selectedMember,
            actions = moderationActions,
            kickUserAsyncAction = kickUserAsyncAction.value,
            banUserAsyncAction = banUserAsyncAction.value,
            unbanUserAsyncAction = unbanUserAsyncAction.value,
            canDisplayBannedUsers = canDisplayBannedUsers,
            eventSink = { handleEvent(it) },
        )
    }

    private fun CoroutineScope.kickUser(
        userId: UserId,
        kickUserAction: MutableState<AsyncAction<Unit>>,
    ) = runActionAndWaitForMembershipChange(kickUserAction) {
        analyticsService.capture(RoomModeration(RoomModeration.Action.KickMember))
        room.kickUser(userId)
    }

    private fun CoroutineScope.banUser(
        userId: UserId,
        banUserAction: MutableState<AsyncAction<Unit>>,
    ) = runActionAndWaitForMembershipChange(banUserAction) {
        analyticsService.capture(RoomModeration(RoomModeration.Action.BanMember))
        room.banUser(userId)
    }

    private fun CoroutineScope.unbanUser(
        userId: UserId,
        unbanUserAction: MutableState<AsyncAction<Unit>>,
    ) = runActionAndWaitForMembershipChange(unbanUserAction) {
        analyticsService.capture(RoomModeration(RoomModeration.Action.UnbanMember))
        room.unbanUser(userId)
    }

    private fun <T> CoroutineScope.runActionAndWaitForMembershipChange(
        action: MutableState<AsyncAction<T>>,
        block: suspend () -> Result<T>
    ) {
        launch(dispatchers.io) {
            action.runUpdatingState {
                val result = block()
                if (result.isSuccess) {
                    room.membersStateFlow.drop(1).take(1)
                }
                result
            }
        }
    }
}

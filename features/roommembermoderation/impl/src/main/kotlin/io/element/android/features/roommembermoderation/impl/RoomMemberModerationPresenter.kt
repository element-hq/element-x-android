/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roommembermoderation.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import im.vector.app.features.analytics.plan.RoomModeration
import io.element.android.features.roommembermoderation.api.ModerationAction
import io.element.android.features.roommembermoderation.api.ModerationActionState
import io.element.android.features.roommembermoderation.api.RoomMemberModerationEvents
import io.element.android.features.roommembermoderation.api.RoomMemberModerationState
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runUpdatingState
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.RoomMembershipState
import io.element.android.libraries.matrix.api.room.roomMembers
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.ui.room.canBanAsState
import io.element.android.libraries.matrix.ui.room.canKickAsState
import io.element.android.libraries.matrix.ui.room.userPowerLevelAsState
import io.element.android.services.analytics.api.AnalyticsService
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import javax.inject.Inject

class RoomMemberModerationPresenter @Inject constructor(
    private val room: JoinedRoom,
    private val dispatchers: CoroutineDispatchers,
    private val analyticsService: AnalyticsService,
) : Presenter<RoomMemberModerationState> {
    @Composable
    override fun present(): RoomMemberModerationState {
        val coroutineScope = rememberCoroutineScope()
        val syncUpdateFlow = room.syncUpdateFlow.collectAsState()
        val canBan = room.canBanAsState(syncUpdateFlow.value)
        val canKick = room.canKickAsState(syncUpdateFlow.value)
        val currentUserMemberPowerLevel = room.userPowerLevelAsState(syncUpdateFlow.value)

        val kickUserAsyncAction =
            remember { mutableStateOf(AsyncAction.Uninitialized as AsyncAction<Unit>) }
        val banUserAsyncAction =
            remember { mutableStateOf(AsyncAction.Uninitialized as AsyncAction<Unit>) }
        val unbanUserAsyncAction =
            remember { mutableStateOf(AsyncAction.Uninitialized as AsyncAction<Unit>) }
        var selectedUser by remember {
            mutableStateOf<MatrixUser?>(null)
        }
        val moderationActions = remember { mutableStateOf(persistentListOf<ModerationActionState>()) }

        fun handleEvent(event: RoomMemberModerationEvents) {
            when (event) {
                is RoomMemberModerationEvents.ShowActionsForUser -> {
                    selectedUser = event.user
                    val member = room.membersStateFlow.value.roomMembers()?.firstOrNull {
                        it.userId == event.user.userId
                    }
                    moderationActions.value = computeModerationActions(
                        member = member,
                        canKick = canKick.value,
                        canBan = canBan.value,
                        currentUserMemberPowerLevel = currentUserMemberPowerLevel.value,
                    )
                }
                is RoomMemberModerationEvents.ProcessAction -> {
                    when (event.action) {
                        is ModerationAction.DisplayProfile -> Unit
                        is ModerationAction.KickUser -> {
                            selectedUser = event.targetUser
                            kickUserAsyncAction.value = AsyncAction.ConfirmingNoParams
                        }
                        is ModerationAction.BanUser -> {
                            selectedUser = event.targetUser
                            banUserAsyncAction.value = AsyncAction.ConfirmingNoParams
                        }
                        is ModerationAction.UnbanUser -> {
                            selectedUser = event.targetUser
                            unbanUserAsyncAction.value = AsyncAction.ConfirmingNoParams
                        }
                    }
                }
                is InternalRoomMemberModerationEvents.DoKickUser -> {
                    selectedUser?.let {
                        coroutineScope.kickUser(it.userId, event.reason, kickUserAsyncAction)
                    }
                    selectedUser = null
                }
                is InternalRoomMemberModerationEvents.DoBanUser -> {
                    selectedUser?.let {
                        coroutineScope.banUser(it.userId, event.reason, banUserAsyncAction)
                    }
                    selectedUser = null
                }
                is InternalRoomMemberModerationEvents.DoUnbanUser -> {
                    selectedUser?.let {
                        coroutineScope.unbanUser(it.userId, unbanUserAsyncAction)
                    }
                    selectedUser = null
                }
                is InternalRoomMemberModerationEvents.Reset -> {
                    selectedUser = null
                    kickUserAsyncAction.value = AsyncAction.Uninitialized
                    banUserAsyncAction.value = AsyncAction.Uninitialized
                    unbanUserAsyncAction.value = AsyncAction.Uninitialized
                }
            }
        }

        return InternalRoomMemberModerationState(
            canKick = canKick.value,
            canBan = canBan.value,
            selectedUser = selectedUser,
            actions = moderationActions.value,
            kickUserAsyncAction = kickUserAsyncAction.value,
            banUserAsyncAction = banUserAsyncAction.value,
            unbanUserAsyncAction = unbanUserAsyncAction.value,
            eventSink = { handleEvent(it) },
        )
    }

    private fun computeModerationActions(
        member: RoomMember?,
        canKick: Boolean,
        canBan: Boolean,
        currentUserMemberPowerLevel: Long,
    ): PersistentList<ModerationActionState> {
        return buildList {
            add(ModerationActionState(action = ModerationAction.DisplayProfile, isEnabled = true))
            // Assume the member is a regular user when it's unknown
            val targetMemberPowerLevel = member?.powerLevel ?: 0
            val canModerateThisUser = currentUserMemberPowerLevel > targetMemberPowerLevel
            // Assume the member is joined when it's unknown
            val membership = member?.membership ?: RoomMembershipState.JOIN
            if (canKick) {
                val isKickEnabled = canModerateThisUser && membership.isActive()
                add(ModerationActionState(action = ModerationAction.KickUser, isEnabled = isKickEnabled))
            }
            if (canBan) {
                if (membership == RoomMembershipState.BAN) {
                    add(ModerationActionState(action = ModerationAction.UnbanUser, isEnabled = canModerateThisUser))
                } else {
                    add(ModerationActionState(action = ModerationAction.BanUser, isEnabled = canModerateThisUser))
                }
            }
        }.toPersistentList()
    }

    private fun CoroutineScope.kickUser(
        userId: UserId,
        reason: String,
        kickUserAction: MutableState<AsyncAction<Unit>>,
    ) = runActionAndWaitForMembershipChange(kickUserAction) {
        analyticsService.capture(RoomModeration(RoomModeration.Action.KickMember))
        room.kickUser(
            userId = userId,
            reason = reason.takeIf { it.isNotBlank() },
        )
    }

    private fun CoroutineScope.banUser(
        userId: UserId,
        reason: String,
        banUserAction: MutableState<AsyncAction<Unit>>,
    ) = runActionAndWaitForMembershipChange(banUserAction) {
        analyticsService.capture(RoomModeration(RoomModeration.Action.BanMember))
        room.banUser(
            userId = userId,
            reason = reason.takeIf { it.isNotBlank() },
        )
    }

    private fun CoroutineScope.unbanUser(
        userId: UserId,
        unbanUserAction: MutableState<AsyncAction<Unit>>,
    ) = runActionAndWaitForMembershipChange(unbanUserAction) {
        analyticsService.capture(RoomModeration(RoomModeration.Action.UnbanMember))
        room.unbanUser(userId = userId)
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

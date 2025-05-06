/*
 * Copyright 2024 New Vector Ltd.
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
import io.element.android.features.roommembermoderation.api.RoomMemberModerationEvents
import io.element.android.features.roommembermoderation.api.RoomMemberModerationState
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runUpdatingState
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.room.RoomMember
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
    private var selectedMember by mutableStateOf<AsyncData<RoomMember>>(AsyncData.Uninitialized)

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

        val moderationActions = remember { mutableStateOf(persistentListOf<ModerationAction>()) }

        fun handleEvent(event: RoomMemberModerationEvents) {
            when (event) {
                is RoomMemberModerationEvents.RenderActions -> {
                    selectedMember = AsyncData.Success(event.roomMember)
                    moderationActions.value = computeModerationActions(
                        member = event.roomMember,
                        canKick = canKick.value,
                        canBan = canBan.value,
                        currentUserMemberPowerLevel = currentUserMemberPowerLevel.value,
                    )
                }
                is RoomMemberModerationEvents.ProcessAction -> {
                    when(val action = event.action) {
                        is ModerationAction.DisplayProfile -> Unit
                        is ModerationAction.KickUser -> {
                            selectedMember = AsyncData.Success(action.member)
                            kickUserAsyncAction.value = AsyncAction.ConfirmingNoParams
                        }
                        is ModerationAction.BanUser -> {
                            selectedMember = AsyncData.Success(action.member)
                            banUserAsyncAction.value = AsyncAction.ConfirmingNoParams
                        }
                        is ModerationAction.UnbanUser -> {
                            selectedMember = AsyncData.Success(action.member)
                            unbanUserAsyncAction.value = AsyncAction.ConfirmingNoParams
                        }
                    }
                }
                is InternalRoomMemberModerationEvents.DoKickUser -> {
                    selectedMember.dataOrNull()?.let {
                        coroutineScope.kickUser(it.userId, event.reason, kickUserAsyncAction)
                    }
                    selectedMember = AsyncData.Uninitialized
                }
                is InternalRoomMemberModerationEvents.DoBanUser -> {
                    selectedMember.dataOrNull()?.let {
                        coroutineScope.banUser(it.userId, event.reason, banUserAsyncAction)
                    }
                    selectedMember = AsyncData.Uninitialized
                }
                is InternalRoomMemberModerationEvents.Reset -> {
                    selectedMember = AsyncData.Uninitialized
                    kickUserAsyncAction.value = AsyncAction.Uninitialized
                    banUserAsyncAction.value = AsyncAction.Uninitialized
                    unbanUserAsyncAction.value = AsyncAction.Uninitialized
                }
                is InternalRoomMemberModerationEvents.DoUnbanUser -> {
                    selectedMember.dataOrNull()?.let {
                        coroutineScope.unbanUser(it.userId, unbanUserAsyncAction)
                    }
                    selectedMember = AsyncData.Uninitialized
                }
            }
        }

        return InternalRoomMemberModerationState(
            canKick = canKick.value,
            canBan = canBan.value,
            selectedRoomMember = selectedMember,
            actions = moderationActions.value,
            kickUserAsyncAction = kickUserAsyncAction.value,
            banUserAsyncAction = banUserAsyncAction.value,
            unbanUserAsyncAction = unbanUserAsyncAction.value,
            eventSink = { handleEvent(it) },
        )
    }

    private fun computeModerationActions(
        member: RoomMember,
        canKick: Boolean,
        canBan: Boolean,
        currentUserMemberPowerLevel: Long,
    ): PersistentList<ModerationAction> {
        return buildList {
            add(ModerationAction.DisplayProfile(member))
            if (canKick && member.powerLevel < currentUserMemberPowerLevel) {
                add(ModerationAction.KickUser(member))
            }
            if (canBan && member.powerLevel < currentUserMemberPowerLevel) {
                add(ModerationAction.BanUser(member))
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

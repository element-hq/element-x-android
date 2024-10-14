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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import im.vector.app.features.analytics.plan.RoomModeration
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runUpdatingState
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.core.extensions.finally
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.RoomMembershipState
import io.element.android.libraries.matrix.api.room.isDm
import io.element.android.libraries.matrix.api.room.powerlevels.canBan
import io.element.android.libraries.matrix.api.room.powerlevels.canKick
import io.element.android.services.analytics.api.AnalyticsService
import kotlinx.collections.immutable.persistentListOf
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

    private suspend fun canBan() = room.canBan().getOrDefault(false)
    private suspend fun canKick() = room.canKick().getOrDefault(false)

    @Composable
    override fun present(): RoomMembersModerationState {
        val coroutineScope = rememberCoroutineScope()
        var moderationActions by remember { mutableStateOf(persistentListOf<ModerationAction>()) }

        val syncUpdateFlow = room.syncUpdateFlow.collectAsState()
        val canDisplayModerationActions by produceState(
            initialValue = false,
            key1 = syncUpdateFlow.value
        ) {
            value = !room.isDm && (canBan() || canKick())
        }
        val kickUserAsyncAction =
            remember { mutableStateOf(AsyncAction.Uninitialized as AsyncAction<Unit>) }
        val banUserAsyncAction =
            remember { mutableStateOf(AsyncAction.Uninitialized as AsyncAction<Unit>) }
        val unbanUserAsyncAction =
            remember { mutableStateOf(AsyncAction.Uninitialized as AsyncAction<Unit>) }

        val canDisplayBannedUsers by produceState(initialValue = false) {
            value = !room.isDm && canBan()
        }

        fun handleEvent(event: RoomMembersModerationEvents) {
            when (event) {
                is RoomMembersModerationEvents.SelectRoomMember -> {
                    coroutineScope.launch {
                        selectedMember = event.roomMember
                        if (event.roomMember.membership == RoomMembershipState.BAN && canBan()) {
                            unbanUserAsyncAction.value = AsyncAction.ConfirmingNoParams
                        } else {
                            moderationActions = buildList {
                                add(ModerationAction.DisplayProfile(event.roomMember.userId))
                                val currentUserMemberPowerLevel = room.userRole(room.sessionId)
                                    .getOrDefault(RoomMember.Role.USER)
                                    .powerLevel
                                if (currentUserMemberPowerLevel > event.roomMember.powerLevel) {
                                    if (canKick()) {
                                        add(ModerationAction.KickUser(event.roomMember.userId))
                                    }
                                    if (canBan()) {
                                        add(ModerationAction.BanUser(event.roomMember.userId))
                                    }
                                }
                            }.toPersistentList()
                        }
                    }
                }
                is RoomMembersModerationEvents.KickUser -> {
                    moderationActions = persistentListOf()
                    selectedMember?.let {
                        coroutineScope.kickUser(it.userId, kickUserAsyncAction)
                    }
                }
                is RoomMembersModerationEvents.BanUser -> {
                    if (banUserAsyncAction.value.isConfirming()) {
                        moderationActions = persistentListOf()
                        selectedMember?.let {
                            coroutineScope.banUser(it.userId, banUserAsyncAction)
                        }
                    } else {
                        banUserAsyncAction.value = AsyncAction.ConfirmingNoParams
                    }
                }
                is RoomMembersModerationEvents.UnbanUser -> {
                    if (unbanUserAsyncAction.value.isConfirming()) {
                        moderationActions = persistentListOf()
                        selectedMember?.let {
                            coroutineScope.unbanUser(it.userId, unbanUserAsyncAction)
                        }
                    } else {
                        unbanUserAsyncAction.value = AsyncAction.ConfirmingNoParams
                    }
                }
                is RoomMembersModerationEvents.Reset -> {
                    selectedMember = null
                    moderationActions = persistentListOf()
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
        room.kickUser(userId).finally { selectedMember = null }
    }

    private fun CoroutineScope.banUser(
        userId: UserId,
        banUserAction: MutableState<AsyncAction<Unit>>,
    ) = runActionAndWaitForMembershipChange(banUserAction) {
        analyticsService.capture(RoomModeration(RoomModeration.Action.BanMember))
        room.banUser(userId).finally { selectedMember = null }
    }

    private fun CoroutineScope.unbanUser(
        userId: UserId,
        unbanUserAction: MutableState<AsyncAction<Unit>>,
    ) = runActionAndWaitForMembershipChange(unbanUserAction) {
        analyticsService.capture(RoomModeration(RoomModeration.Action.UnbanMember))
        room.unbanUser(userId).finally { selectedMember = null }
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

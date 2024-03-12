/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.features.roomdetails.impl.members.moderation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.runUpdatingState
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.core.extensions.finally
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.RoomMembershipState
import io.element.android.libraries.matrix.api.room.powerlevels.canBan
import io.element.android.libraries.matrix.api.room.powerlevels.canKick
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import javax.inject.Inject

@ContributesBinding(RoomScope::class)
class DefaultRoomMembersModerationPresenter @Inject constructor(
    private val room: MatrixRoom,
    private val featureFlagService: FeatureFlagService,
    private val dispatchers: CoroutineDispatchers,
) : RoomMembersModerationPresenter {
    private var selectedMember by mutableStateOf<RoomMember?>(null)

    private suspend fun canBan() = room.canBan().getOrDefault(false)
    private suspend fun canKick() = room.canKick().getOrDefault(false)

    override suspend fun canDisplayModerationActions(): Boolean {
        val isRoomModerationEnabled = featureFlagService.isFeatureEnabled(FeatureFlags.RoomModeration)
        val isDm = room.isDm && room.isEncrypted
        return isRoomModerationEnabled && !isDm && (canBan() || canKick())
    }

    @Composable
    override fun present(): RoomMembersModerationState {
        val coroutineScope = rememberCoroutineScope()
        var moderationActions by remember { mutableStateOf(persistentListOf<ModerationAction>()) }

        val kickUserAsyncAction = remember { mutableStateOf(AsyncAction.Uninitialized as AsyncAction<Unit>) }
        val banUserAsyncAction = remember { mutableStateOf(AsyncAction.Uninitialized as AsyncAction<Unit>) }
        val unbanUserAsyncAction = remember { mutableStateOf(AsyncAction.Uninitialized as AsyncAction<Unit>) }

        val canDisplayBannedUsers by produceState(initialValue = false) {
            value = featureFlagService.isFeatureEnabled(FeatureFlags.RoomModeration) && !room.isDm && canBan()
        }

        fun handleEvent(event: RoomMembersModerationEvents) {
            when (event) {
                is RoomMembersModerationEvents.SelectRoomMember -> {
                    coroutineScope.launch {
                        selectedMember = event.roomMember
                        if (event.roomMember.membership == RoomMembershipState.BAN && canBan()) {
                            unbanUserAsyncAction.value = AsyncAction.Confirming
                        } else {
                            moderationActions = buildList {
                                add(ModerationAction.DisplayProfile(event.roomMember.userId))
                                val currentUserMemberPowerLevel = room.userRole(room.sessionId).getOrDefault(RoomMember.Role.USER).powerLevel
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
                        banUserAsyncAction.value = AsyncAction.Confirming
                    }
                }
                is RoomMembersModerationEvents.UnbanUser -> {
                    if (unbanUserAsyncAction.value.isConfirming()) {
                        moderationActions = persistentListOf()
                        selectedMember?.let {
                            coroutineScope.unbanUser(it.userId, unbanUserAsyncAction)
                        }
                    } else {
                        unbanUserAsyncAction.value = AsyncAction.Confirming
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
        room.kickUser(userId).finally { selectedMember = null }
    }

    private fun CoroutineScope.banUser(
        userId: UserId,
        banUserAction: MutableState<AsyncAction<Unit>>,
    ) = runActionAndWaitForMembershipChange(banUserAction) {
        room.banUser(userId).finally { selectedMember = null }
    }

    private fun CoroutineScope.unbanUser(
        userId: UserId,
        unbanUserAction: MutableState<AsyncAction<Unit>>,
    ) = runActionAndWaitForMembershipChange(unbanUserAction) {
        room.unbanUser(userId).finally { selectedMember = null }
    }

    private fun <T> CoroutineScope.runActionAndWaitForMembershipChange(action: MutableState<AsyncAction<T>>, block: suspend () -> Result<T>) {
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

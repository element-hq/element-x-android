/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.preferences.impl.notifications.edit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runUpdatingStateNoSuccess
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.matrix.api.notificationsettings.NotificationSettingsService
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.matrix.api.roomlist.RoomListService
import io.element.android.libraries.matrix.api.roomlist.RoomSummary
import io.element.android.libraries.matrix.ui.model.getAvatarData
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.text.Collator
import kotlin.time.Duration.Companion.seconds

class EditDefaultNotificationSettingPresenter @AssistedInject constructor(
    private val notificationSettingsService: NotificationSettingsService,
    @Assisted private val isOneToOne: Boolean,
    private val roomListService: RoomListService,
) : Presenter<EditDefaultNotificationSettingState> {
    @AssistedFactory
    interface Factory {
        fun create(oneToOne: Boolean): EditDefaultNotificationSettingPresenter
    }

    @Composable
    override fun present(): EditDefaultNotificationSettingState {
        var displayMentionsOnlyDisclaimer by remember { mutableStateOf(false) }

        val mode: MutableState<RoomNotificationMode?> = remember {
            mutableStateOf(null)
        }

        val changeNotificationSettingAction: MutableState<AsyncAction<Unit>> = remember { mutableStateOf(AsyncAction.Uninitialized) }

        val roomsWithUserDefinedMode: MutableState<List<EditNotificationSettingRoomInfo>> = remember {
            mutableStateOf(emptyList())
        }

        val localCoroutineScope = rememberCoroutineScope()
        LaunchedEffect(Unit) {
            fetchSettings(mode)
            observeNotificationSettings(mode, changeNotificationSettingAction)
            observeRoomSummaries(roomsWithUserDefinedMode)
            displayMentionsOnlyDisclaimer = !notificationSettingsService.canHomeServerPushEncryptedEventsToDevice().getOrDefault(true)
        }

        fun handleEvents(event: EditDefaultNotificationSettingStateEvents) {
            when (event) {
                is EditDefaultNotificationSettingStateEvents.SetNotificationMode -> {
                    localCoroutineScope.setDefaultNotificationMode(event.mode, changeNotificationSettingAction)
                }
                EditDefaultNotificationSettingStateEvents.ClearError -> changeNotificationSettingAction.value = AsyncAction.Uninitialized
            }
        }

        return EditDefaultNotificationSettingState(
            isOneToOne = isOneToOne,
            mode = mode.value,
            roomsWithUserDefinedMode = roomsWithUserDefinedMode.value.toImmutableList(),
            changeNotificationSettingAction = changeNotificationSettingAction.value,
            displayMentionsOnlyDisclaimer = displayMentionsOnlyDisclaimer,
            eventSink = ::handleEvents
        )
    }

    private fun CoroutineScope.fetchSettings(mode: MutableState<RoomNotificationMode?>) = launch {
        mode.value = notificationSettingsService.getDefaultRoomNotificationMode(isEncrypted = true, isOneToOne = isOneToOne).getOrThrow()
    }

    @OptIn(FlowPreview::class)
    private fun CoroutineScope.observeNotificationSettings(
        mode: MutableState<RoomNotificationMode?>,
        changeNotificationSettingAction: MutableState<AsyncAction<Unit>>,
    ) {
        notificationSettingsService.notificationSettingsChangeFlow
            .debounce(0.5.seconds)
            .onEach {
                fetchSettings(mode)
                changeNotificationSettingAction.value = AsyncAction.Uninitialized
            }
            .launchIn(this)
    }

    private fun CoroutineScope.observeRoomSummaries(roomsWithUserDefinedMode: MutableState<List<EditNotificationSettingRoomInfo>>) {
        roomListService.allRooms
            .summaries
            .onEach { roomSummaries ->
                updateRoomsWithUserDefinedMode(roomSummaries, roomsWithUserDefinedMode)
            }
            .launchIn(this)
    }

    private suspend fun updateRoomsWithUserDefinedMode(
        summaries: List<RoomSummary>,
        roomsWithUserDefinedMode: MutableState<List<EditNotificationSettingRoomInfo>>
    ) {
        val roomWithUserDefinedRules: Set<String> = notificationSettingsService.getRoomsWithUserDefinedRules().getOrDefault(emptyList()).toSet()
        roomsWithUserDefinedMode.value = summaries
            .filter { roomSummary ->
                roomWithUserDefinedRules.contains(roomSummary.roomId.value) && roomSummary.isOneToOne == isOneToOne
            }
            .map { roomSummary ->
                EditNotificationSettingRoomInfo(
                    roomId = roomSummary.roomId,
                    name = roomSummary.info.name,
                    heroesAvatar = roomSummary.info.heroes.map { hero ->
                        hero.getAvatarData(AvatarSize.CustomRoomNotificationSetting)
                    }.toImmutableList(),
                    avatarData = roomSummary.info.getAvatarData(AvatarSize.CustomRoomNotificationSetting),
                    notificationMode = roomSummary.info.userDefinedNotificationMode,
                )
            }
            // locale sensitive sorting
            .sortedWith(compareBy(Collator.getInstance()) { roomSummary -> roomSummary.name })
    }

    private fun CoroutineScope.setDefaultNotificationMode(mode: RoomNotificationMode, action: MutableState<AsyncAction<Unit>>) = launch {
        action.runUpdatingStateNoSuccess {
            // On modern clients, we don't have different settings for encrypted and non-encrypted rooms (Legacy clients did).
            notificationSettingsService.setDefaultRoomNotificationMode(isEncrypted = true, mode = mode, isOneToOne = isOneToOne)
                .map {
                    notificationSettingsService.setDefaultRoomNotificationMode(isEncrypted = false, mode = mode, isOneToOne = isOneToOne)
                }
        }
    }
}

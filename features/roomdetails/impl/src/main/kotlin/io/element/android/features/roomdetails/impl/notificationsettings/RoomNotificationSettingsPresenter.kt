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

package io.element.android.features.roomdetails.impl.notificationsettings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runCatchingUpdatingState
import io.element.android.libraries.matrix.api.notificationsettings.NotificationSettingsService
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.matrix.api.room.RoomNotificationSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class RoomNotificationSettingsPresenter @AssistedInject constructor(
    private val room: MatrixRoom,
    private val notificationSettingsService: NotificationSettingsService,
    @Assisted private val showUserDefinedSettingStyle: Boolean,
) : Presenter<RoomNotificationSettingsState> {
    @AssistedFactory
    interface Factory {
        fun create(showUserDefinedSettingStyle: Boolean): RoomNotificationSettingsPresenter
    }

    @Composable
    override fun present(): RoomNotificationSettingsState {
        var shouldDisplayMentionsOnlyDisclaimer by remember { mutableStateOf(false) }
        val defaultRoomNotificationMode: MutableState<RoomNotificationMode?> = rememberSaveable {
            mutableStateOf(null)
        }
        val localCoroutineScope = rememberCoroutineScope()
        val setNotificationSettingAction: MutableState<AsyncAction<Unit>> = remember { mutableStateOf(AsyncAction.Uninitialized) }
        val restoreDefaultAction: MutableState<AsyncAction<Unit>> = remember { mutableStateOf(AsyncAction.Uninitialized) }

        val roomNotificationSettings: MutableState<AsyncData<RoomNotificationSettings>> = remember {
            mutableStateOf(AsyncData.Uninitialized)
        }

        // We store state of which mode the user has set via the notification service before the new push settings have been updated.
        // We show this state immediately to the user and debounce updates to notification settings to hide some invalid states returned
        // by the rust sdk during these two events that cause the radio buttons ot toggle quickly back and forth.
        // This is a client side work-around until bulk push rule updates are supported.
        // ref: https://github.com/matrix-org/matrix-spec-proposals/pull/3934
        val pendingRoomNotificationMode: MutableState<RoomNotificationMode?> = remember {
            mutableStateOf(null)
        }

        // We store state of whether the user has set the notifications settings to default or custom via the notification service.
        // We show this state immediately to the user and debounce updates to notification settings to hide some invalid states returned
        // by the rust sdk during these two events that cause the switch ot toggle quickly back and forth.
        // This is a client side work-around until bulk push rule updates are supported.
        // ref: https://github.com/matrix-org/matrix-spec-proposals/pull/3934
        val pendingSetDefault: MutableState<Boolean?> = remember {
            mutableStateOf(null)
        }

        LaunchedEffect(Unit) {
            getDefaultRoomNotificationMode(defaultRoomNotificationMode)
            fetchNotificationSettings(pendingRoomNotificationMode, roomNotificationSettings)
            observeNotificationSettings(pendingRoomNotificationMode, roomNotificationSettings)
            shouldDisplayMentionsOnlyDisclaimer = room.isEncrypted && !notificationSettingsService.canHomeServerPushEncryptedEventsToDevice().getOrDefault(true)
        }

        fun handleEvents(event: RoomNotificationSettingsEvents) {
            when (event) {
                is RoomNotificationSettingsEvents.RoomNotificationModeChanged -> {
                    localCoroutineScope.setRoomNotificationMode(event.mode, pendingRoomNotificationMode, pendingSetDefault, setNotificationSettingAction)
                }
                is RoomNotificationSettingsEvents.SetNotificationMode -> {
                    if (event.isDefault) {
                        localCoroutineScope.restoreDefaultRoomNotificationMode(restoreDefaultAction, pendingSetDefault)
                    } else {
                        defaultRoomNotificationMode.value?.let {
                            localCoroutineScope.setRoomNotificationMode(it, pendingRoomNotificationMode, pendingSetDefault, setNotificationSettingAction)
                        }
                    }
                }
                is RoomNotificationSettingsEvents.DeleteCustomNotification -> {
                    localCoroutineScope.restoreDefaultRoomNotificationMode(restoreDefaultAction, pendingSetDefault)
                }
                RoomNotificationSettingsEvents.ClearSetNotificationError -> {
                    setNotificationSettingAction.value = AsyncAction.Uninitialized
                }
                RoomNotificationSettingsEvents.ClearRestoreDefaultError -> {
                    restoreDefaultAction.value = AsyncAction.Uninitialized
                }
            }
        }

        return RoomNotificationSettingsState(
            showUserDefinedSettingStyle = showUserDefinedSettingStyle,
            roomName = room.displayName,
            roomNotificationSettings = roomNotificationSettings.value,
            pendingRoomNotificationMode = pendingRoomNotificationMode.value,
            pendingSetDefault = pendingSetDefault.value,
            defaultRoomNotificationMode = defaultRoomNotificationMode.value,
            setNotificationSettingAction = setNotificationSettingAction.value,
            restoreDefaultAction = restoreDefaultAction.value,
            displayMentionsOnlyDisclaimer = shouldDisplayMentionsOnlyDisclaimer,
            eventSink = ::handleEvents,
        )
    }

    @OptIn(FlowPreview::class)
    private fun CoroutineScope.observeNotificationSettings(
        pendingModeState: MutableState<RoomNotificationMode?>,
        roomNotificationSettings: MutableState<AsyncData<RoomNotificationSettings>>
    ) {
        notificationSettingsService.notificationSettingsChangeFlow
            .debounce(0.5.seconds)
            .onEach {
                fetchNotificationSettings(pendingModeState, roomNotificationSettings)
            }
            .launchIn(this)
    }

    private fun CoroutineScope.fetchNotificationSettings(
        pendingModeState: MutableState<RoomNotificationMode?>,
        roomNotificationSettings: MutableState<AsyncData<RoomNotificationSettings>>
    ) = launch {
        suspend {
            pendingModeState.value = null
            notificationSettingsService.getRoomNotificationSettings(room.roomId, room.isEncrypted, room.isOneToOne).getOrThrow()
        }.runCatchingUpdatingState(roomNotificationSettings)
    }

    private fun CoroutineScope.getDefaultRoomNotificationMode(
        defaultRoomNotificationMode: MutableState<RoomNotificationMode?>
    ) = launch {
        defaultRoomNotificationMode.value = notificationSettingsService.getDefaultRoomNotificationMode(
            room.isEncrypted,
            room.isOneToOne
        ).getOrThrow()
    }

    private fun CoroutineScope.setRoomNotificationMode(
        mode: RoomNotificationMode,
        pendingModeState: MutableState<RoomNotificationMode?>,
        pendingDefaultState: MutableState<Boolean?>,
        action: MutableState<AsyncAction<Unit>>
    ) = launch {
        suspend {
            pendingModeState.value = mode
            pendingDefaultState.value = false
            val result = notificationSettingsService.setRoomNotificationMode(room.roomId, mode)
            if (result.isFailure) {
                pendingModeState.value = null
                pendingDefaultState.value = null
            }
            result.getOrThrow()
        }.runCatchingUpdatingState(action)
    }

    private fun CoroutineScope.restoreDefaultRoomNotificationMode(
        action: MutableState<AsyncAction<Unit>>,
        pendingDefaultState: MutableState<Boolean?>
    ) = launch {
        suspend {
            pendingDefaultState.value = true
            val result = notificationSettingsService.restoreDefaultRoomNotificationMode(room.roomId)
            if (result.isFailure) {
                pendingDefaultState.value = null
            }
            result.getOrThrow()
        }.runCatchingUpdatingState(action)
    }
}

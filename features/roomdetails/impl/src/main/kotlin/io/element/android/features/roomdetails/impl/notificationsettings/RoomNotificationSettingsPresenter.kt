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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.notificationsettings.NotificationSettingsService
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.matrix.api.room.roomNotificationSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

class RoomNotificationSettingsPresenter @Inject constructor(
    private val room: MatrixRoom,
    private val notificationSettingsService: NotificationSettingsService,
) : Presenter<RoomNotificationSettingsState> {

    @Composable
    override fun present(): RoomNotificationSettingsState {
        val defaultRoomNotificationMode: MutableState<RoomNotificationMode?> = rememberSaveable {
            mutableStateOf(null)
        }
        val localCoroutineScope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            getDefaultRoomNotificationMode(defaultRoomNotificationMode)
            observeNotificationSettings()
        }

        val roomNotificationSettingsState by room.roomNotificationSettingsStateFlow.collectAsState()

        fun handleEvents(event: RoomNotificationSettingsEvents) {
            when (event) {
                is RoomNotificationSettingsEvents.RoomNotificationModeChanged -> {
                    localCoroutineScope.setRoomNotificationMode(event.mode)
                }
                RoomNotificationSettingsEvents.DefaultNotificationModeSelected -> {
                    localCoroutineScope.restoreDefaultRoomNotificationMode()
                }
            }
        }

        return RoomNotificationSettingsState(
            roomNotificationSettings = roomNotificationSettingsState.roomNotificationSettings(),
            defaultRoomNotificationMode = defaultRoomNotificationMode.value,
            eventSink = ::handleEvents,
        )
    }

    private fun CoroutineScope.observeNotificationSettings() {
        notificationSettingsService.notificationSettingsChangeFlow.onEach {
            room.updateRoomNotificationSettings()
        }.launchIn(this)
    }

    private fun CoroutineScope.getDefaultRoomNotificationMode(defaultRoomNotificationMode: MutableState<RoomNotificationMode?>) = launch {
        defaultRoomNotificationMode.value = notificationSettingsService.getDefaultRoomNotificationMode(
            room.isEncrypted,
            room.joinedMemberCount.toULong()
        ).getOrThrow()
    }

    private fun CoroutineScope.setRoomNotificationMode(mode: RoomNotificationMode) = launch {
        notificationSettingsService.setRoomNotificationMode(room.roomId, mode)
    }

    private fun CoroutineScope.restoreDefaultRoomNotificationMode() = launch {
        notificationSettingsService.restoreDefaultRoomNotificationMode(room.roomId)
    }
}

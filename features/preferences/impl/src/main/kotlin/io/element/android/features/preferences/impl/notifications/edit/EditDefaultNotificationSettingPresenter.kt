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

package io.element.android.features.preferences.impl.notifications.edit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.notificationsettings.NotificationSettingsService
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class EditDefaultNotificationSettingPresenter @AssistedInject constructor(
    private val notificationSettingsService: NotificationSettingsService,
    @Assisted private val isOneToOne: Boolean,
) : Presenter<EditDefaultNotificationSettingState> {
    @AssistedFactory
    interface Factory {
        fun create(oneToOne: Boolean): EditDefaultNotificationSettingPresenter
    }
    @Composable
    override fun present(): EditDefaultNotificationSettingState {

        val mode: MutableState<RoomNotificationMode?> = remember {
            mutableStateOf(null)
        }
        val localCoroutineScope = rememberCoroutineScope()
        LaunchedEffect(Unit) {
            fetchSettings(mode)
            observeNotificationSettings(mode)
        }

        fun handleEvents(event: EditDefaultNotificationSettingStateEvents) {
            when (event) {
                is EditDefaultNotificationSettingStateEvents.SetNotificationMode -> localCoroutineScope.setDefaultNotificationMode(event.mode)
            }
        }

        return EditDefaultNotificationSettingState(
            isOneToOne = isOneToOne,
            mode = mode.value,
            eventSink = ::handleEvents
        )
    }

    private fun CoroutineScope.fetchSettings(mode: MutableState<RoomNotificationMode?>) = launch {
        mode.value = notificationSettingsService.getDefaultRoomNotificationMode(isEncrypted = true, isOneToOne = isOneToOne).getOrThrow()
    }

    @OptIn(FlowPreview::class)
    private fun CoroutineScope.observeNotificationSettings(mode: MutableState<RoomNotificationMode?>) {
        notificationSettingsService.notificationSettingsChangeFlow
            .debounce(0.5.seconds)
            .onEach {
                fetchSettings(mode)
            }
            .launchIn(this)
    }

    private fun CoroutineScope.setDefaultNotificationMode(mode: RoomNotificationMode) = launch {
        // On modern clients, we don't have different settings for encrypted and non-encrypted rooms (Legacy clients did).
        notificationSettingsService.setDefaultRoomNotificationMode(isEncrypted = true, mode = mode, isOneToOne = isOneToOne)
        notificationSettingsService.setDefaultRoomNotificationMode(isEncrypted = false, mode = mode, isOneToOne = isOneToOne)
    }

}

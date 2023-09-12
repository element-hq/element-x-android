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

package io.element.android.features.preferences.impl.notifications

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.notificationsettings.NotificationSettingsService
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.pushstore.api.UserPushStore
import io.element.android.libraries.pushstore.api.UserPushStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

class NotificationSettingsPresenter @Inject constructor(
    private val notificationSettingsService: NotificationSettingsService,
    private val userPushStoreFactory: UserPushStoreFactory,
    private val matrixClient: MatrixClient,
    private val systemNotificationsEnabledProvider: SystemNotificationsEnabledProvider
) : Presenter<NotificationSettingsState> {
    @Composable
    override fun present(): NotificationSettingsState {
        val userPushStore = userPushStoreFactory.create(matrixClient.sessionId)
        val systemNotificationsEnabled: MutableState<Boolean> = remember {
            mutableStateOf(systemNotificationsEnabledProvider.notificationsEnabled())
        }

        val localCoroutineScope = rememberCoroutineScope()
        val appNotificationsEnabled = userPushStore
            .getNotificationEnabledForDevice()
            .collectAsState(initial = false)

        val matrixSettings: MutableState<NotificationSettingsState.MatrixNotificationSettings> = remember {
            mutableStateOf(NotificationSettingsState.MatrixNotificationSettings.Uninitialized)
        }

        LaunchedEffect(Unit) {
            fetchSettings(matrixSettings)
            observeNotificationSettings(matrixSettings)
        }

        fun handleEvents(event: NotificationSettingsEvents) {
            when (event) {
                is NotificationSettingsEvents.SetAtRoomNotificationsEnabled -> localCoroutineScope.setAtRoomNotificationsEnabled(event.enabled)
                is NotificationSettingsEvents.SetCallNotificationsEnabled -> localCoroutineScope.setCallNotificationsEnabled(event.enabled)
                is NotificationSettingsEvents.SetDefaultGroupNotificationMode -> localCoroutineScope.setDefaultGroupNotificationMode(event.mode)
                is NotificationSettingsEvents.SetDefaultOneToOneNotificationMode -> localCoroutineScope.setDefaultOneToOneNotificationMode(event.mode)
                is NotificationSettingsEvents.SetNotificationsEnabled -> localCoroutineScope.setNotificationsEnabled(userPushStore, event.enabled)
                NotificationSettingsEvents.ClearConfigurationMismatchError -> matrixSettings.value = NotificationSettingsState.MatrixNotificationSettings.InvalidNotificationSettingsState(fixFailed = false)
                NotificationSettingsEvents.FixConfigurationMismatch -> localCoroutineScope.fixConfigurationMismatch(matrixSettings)
                NotificationSettingsEvents.RefreshSystemNotificationsEnabled -> systemNotificationsEnabled.value = systemNotificationsEnabledProvider.notificationsEnabled()
            }
        }

        return NotificationSettingsState(
            matrixNotificationSettings = matrixSettings.value,
            appNotificationSettings = NotificationSettingsState.AppNotificationSettings(
                systemNotificationsEnabled = systemNotificationsEnabled.value,
                appNotificationsEnabled = appNotificationsEnabled.value
            ),
            eventSink = ::handleEvents
        )
    }

    @OptIn(FlowPreview::class)
    private fun CoroutineScope.observeNotificationSettings(target: MutableState<NotificationSettingsState.MatrixNotificationSettings>) {
        notificationSettingsService.notificationSettingsChangeFlow
            .debounce(0.5.seconds)
            .onEach {
                fetchSettings(target)
            }
            .launchIn(this)
    }

    private fun CoroutineScope.fetchSettings(target: MutableState<NotificationSettingsState.MatrixNotificationSettings>) = launch {
        val groupDefaultMode = notificationSettingsService.getDefaultRoomNotificationMode(isEncrypted = false, isOneToOne = false).getOrThrow()
        val encryptedGroupDefaultMode = notificationSettingsService.getDefaultRoomNotificationMode(isEncrypted = true, isOneToOne = false).getOrThrow()

        val oneToOneDefaultMode = notificationSettingsService.getDefaultRoomNotificationMode(isEncrypted = false, isOneToOne = true).getOrThrow()
        val encryptedOneToOneDefaultMode = notificationSettingsService.getDefaultRoomNotificationMode(isEncrypted = true, isOneToOne = true).getOrThrow()

        if(groupDefaultMode != encryptedGroupDefaultMode || oneToOneDefaultMode != encryptedOneToOneDefaultMode) {
            target.value = NotificationSettingsState.MatrixNotificationSettings.InvalidNotificationSettingsState(fixFailed = false)
            return@launch
        }

        val callNotificationsEnabled = notificationSettingsService.isCallEnabled().getOrThrow()
        val atRoomNotificationsEnabled = notificationSettingsService.isRoomMentionEnabled().getOrThrow()

        target.value = NotificationSettingsState.MatrixNotificationSettings.ValidNotificationSettingsState(
            atRoomNotificationsEnabled = atRoomNotificationsEnabled,
            callNotificationsEnabled = callNotificationsEnabled,
            defaultGroupNotificationMode = encryptedGroupDefaultMode,
            defaultOneToOneNotificationMode = encryptedOneToOneDefaultMode,
        )
    }

    private fun CoroutineScope.fixConfigurationMismatch(target: MutableState<NotificationSettingsState.MatrixNotificationSettings>) = launch {
        runCatching {
            val groupDefaultMode = notificationSettingsService.getDefaultRoomNotificationMode(isEncrypted = false, isOneToOne = false).getOrThrow()
            val encryptedGroupDefaultMode = notificationSettingsService.getDefaultRoomNotificationMode(isEncrypted = true, isOneToOne = false).getOrThrow()

            if (groupDefaultMode != encryptedGroupDefaultMode) {
                notificationSettingsService.setDefaultRoomNotificationMode(
                    isEncrypted = encryptedGroupDefaultMode != RoomNotificationMode.ALL_MESSAGES,
                    mode = RoomNotificationMode.ALL_MESSAGES,
                    isOneToOne = false,
                )
            }

            val oneToOneDefaultMode = notificationSettingsService.getDefaultRoomNotificationMode(isEncrypted = false, isOneToOne = true).getOrThrow()
            val encryptedOneToOneDefaultMode = notificationSettingsService.getDefaultRoomNotificationMode(isEncrypted = true, isOneToOne = true).getOrThrow()

            if (oneToOneDefaultMode != encryptedOneToOneDefaultMode) {
                notificationSettingsService.setDefaultRoomNotificationMode(
                    isEncrypted = encryptedOneToOneDefaultMode != RoomNotificationMode.ALL_MESSAGES,
                    mode = RoomNotificationMode.ALL_MESSAGES,
                    isOneToOne = true,
                )
            }
        }.fold(
            onSuccess = {},
            onFailure = {
                target.value = NotificationSettingsState.MatrixNotificationSettings.InvalidNotificationSettingsState(fixFailed = true)
            }
        )
    }

    private fun CoroutineScope.setAtRoomNotificationsEnabled(enabled: Boolean) = launch {
        notificationSettingsService.setRoomMentionEnabled(enabled)
    }

    private fun CoroutineScope.setCallNotificationsEnabled(enabled: Boolean) = launch {
        notificationSettingsService.setCallEnabled(enabled)
    }

    private fun CoroutineScope.setDefaultGroupNotificationMode(mode: RoomNotificationMode) = launch {
        notificationSettingsService.setDefaultRoomNotificationMode(false, mode, false)
        notificationSettingsService.setDefaultRoomNotificationMode(true, mode, false)
    }

    private fun CoroutineScope.setDefaultOneToOneNotificationMode(mode: RoomNotificationMode) = launch {
        notificationSettingsService.setDefaultRoomNotificationMode(false, mode, true)
        notificationSettingsService.setDefaultRoomNotificationMode(true, mode, true)
    }

    private fun CoroutineScope.setNotificationsEnabled(userPushStore: UserPushStore, enabled: Boolean) = launch {
        userPushStore.setNotificationEnabledForDevice(enabled)
    }

    private fun CoroutineScope.getDefaultRoomNotificationMode(isOneToOne: Boolean, defaultRoomNotificationMode: MutableState<RoomNotificationMode?>) = launch {
        val encryptedMode = notificationSettingsService.getDefaultRoomNotificationMode(true, isOneToOne).getOrThrow()
        val unencryptedMode = notificationSettingsService.getDefaultRoomNotificationMode(false, isOneToOne).getOrThrow()
        if (encryptedMode == unencryptedMode) {
            defaultRoomNotificationMode.value
        } else {
            defaultRoomNotificationMode.value = null
        }
    }
}

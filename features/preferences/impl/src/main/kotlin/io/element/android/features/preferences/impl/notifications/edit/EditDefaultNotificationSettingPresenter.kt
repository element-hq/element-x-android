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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runCatchingUpdatingState
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.notificationsettings.NotificationSettingsService
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.matrix.api.roomlist.RoomListService
import io.element.android.libraries.matrix.api.roomlist.RoomSummary
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
    private val matrixClient: MatrixClient,
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

        val changeNotificationSettingAction: MutableState<AsyncData<Unit>> = remember { mutableStateOf(AsyncData.Uninitialized) }

        val roomsWithUserDefinedMode: MutableState<List<RoomSummary.Filled>> = remember {
            mutableStateOf(listOf())
        }

        val localCoroutineScope = rememberCoroutineScope()
        LaunchedEffect(Unit) {
            fetchSettings(mode)
            observeNotificationSettings(mode)
            observeRoomSummaries(roomsWithUserDefinedMode)
            displayMentionsOnlyDisclaimer = !notificationSettingsService.canHomeServerPushEncryptedEventsToDevice().getOrDefault(true)
        }

        fun handleEvents(event: EditDefaultNotificationSettingStateEvents) {
            when (event) {
                is EditDefaultNotificationSettingStateEvents.SetNotificationMode -> {
                    localCoroutineScope.setDefaultNotificationMode(event.mode, changeNotificationSettingAction)
                }
                EditDefaultNotificationSettingStateEvents.ClearError -> changeNotificationSettingAction.value = AsyncData.Uninitialized
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
    private fun CoroutineScope.observeNotificationSettings(mode: MutableState<RoomNotificationMode?>) {
        notificationSettingsService.notificationSettingsChangeFlow
            .debounce(0.5.seconds)
            .onEach {
                fetchSettings(mode)
            }
            .launchIn(this)
    }

    private fun CoroutineScope.observeRoomSummaries(roomsWithUserDefinedMode: MutableState<List<RoomSummary.Filled>>) {
        roomListService.allRooms
            .summaries
            .onEach {
                updateRoomsWithUserDefinedMode(it, roomsWithUserDefinedMode)
            }
            .launchIn(this)
    }

    private fun CoroutineScope.updateRoomsWithUserDefinedMode(
        summaries: List<RoomSummary>,
        roomsWithUserDefinedMode: MutableState<List<RoomSummary.Filled>>
    ) = launch {
        val roomWithUserDefinedRules: Set<String> = notificationSettingsService.getRoomsWithUserDefinedRules().getOrThrow().toSet()

        val sortedSummaries = summaries
            .filterIsInstance<RoomSummary.Filled>()
            .filter {
                val room = matrixClient.getRoom(it.details.roomId) ?: return@filter false
                roomWithUserDefinedRules.contains(it.identifier()) && isOneToOne == room.isOneToOne
            }
            // locale sensitive sorting
            .sortedWith(compareBy(Collator.getInstance()){ it.details.name })

        roomsWithUserDefinedMode.value = sortedSummaries
    }

    private fun CoroutineScope.setDefaultNotificationMode(mode: RoomNotificationMode,  action: MutableState<AsyncData<Unit>>) = launch {
        suspend {
            // On modern clients, we don't have different settings for encrypted and non-encrypted rooms (Legacy clients did).
            notificationSettingsService.setDefaultRoomNotificationMode(isEncrypted = true, mode = mode, isOneToOne = isOneToOne).getOrThrow()
            notificationSettingsService.setDefaultRoomNotificationMode(isEncrypted = false, mode = mode, isOneToOne = isOneToOne).getOrThrow()
        }.runCatchingUpdatingState(action)
    }

}

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

        val changeNotificationSettingAction: MutableState<AsyncAction<Unit>> = remember { mutableStateOf(AsyncAction.Uninitialized) }

        val roomsWithUserDefinedMode: MutableState<List<RoomSummary>> = remember {
            mutableStateOf(listOf())
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

    private fun CoroutineScope.observeRoomSummaries(roomsWithUserDefinedMode: MutableState<List<RoomSummary>>) {
        roomListService.allRooms
            .summaries
            .onEach {
                updateRoomsWithUserDefinedMode(it, roomsWithUserDefinedMode)
            }
            .launchIn(this)
    }

    private fun CoroutineScope.updateRoomsWithUserDefinedMode(
        summaries: List<RoomSummary>,
        roomsWithUserDefinedMode: MutableState<List<RoomSummary>>
    ) = launch {
        val roomWithUserDefinedRules: Set<String> = notificationSettingsService.getRoomsWithUserDefinedRules().getOrThrow().toSet()

        val sortedSummaries = summaries
            .filterIsInstance<RoomSummary>()
            .filter {
                val room = matrixClient.getRoom(it.roomId) ?: return@filter false
                roomWithUserDefinedRules.contains(it.roomId.value) && isOneToOne == room.isOneToOne
            }
            // locale sensitive sorting
            .sortedWith(compareBy(Collator.getInstance()) { it.name })

        roomsWithUserDefinedMode.value = sortedSummaries
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

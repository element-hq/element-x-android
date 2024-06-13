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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.features.roomdetails.impl.R
import io.element.android.libraries.core.bool.orTrue
import io.element.android.libraries.designsystem.components.async.AsyncActionView
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.ListItemStyle
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar

@Composable
fun UserDefinedRoomNotificationSettingsView(
    state: RoomNotificationSettingsState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            UserDefinedRoomNotificationSettingsTopBar(
                roomName = state.roomName,
                onBackClick = { onBackClick() }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
                .consumeWindowInsets(padding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            val roomNotificationSettings = state.roomNotificationSettings.dataOrNull()
            if (roomNotificationSettings != null && state.displayNotificationMode != null) {
                RoomNotificationSettingsOptions(
                    selected = state.displayNotificationMode,
                    enabled = !state.displayIsDefault.orTrue(),
                    displayMentionsOnlyDisclaimer = state.displayMentionsOnlyDisclaimer,
                    onSelectOption = {
                        state.eventSink(RoomNotificationSettingsEvents.ChangeRoomNotificationMode(it.mode))
                    },
                )
            }

            ListItem(
                headlineContent = { Text(stringResource(R.string.screen_room_notification_settings_edit_remove_setting)) },
                style = ListItemStyle.Destructive,
                onClick = {
                    state.eventSink(RoomNotificationSettingsEvents.DeleteCustomNotification)
                }
            )

            AsyncActionView(
                async = state.setNotificationSettingAction,
                onSuccess = {},
                errorMessage = { stringResource(R.string.screen_notification_settings_edit_failed_updating_default_mode) },
                onErrorDismiss = { state.eventSink(RoomNotificationSettingsEvents.ClearSetNotificationError) },
            )

            AsyncActionView(
                async = state.restoreDefaultAction,
                onSuccess = { onBackClick() },
                errorMessage = { stringResource(R.string.screen_notification_settings_edit_failed_updating_default_mode) },
                onErrorDismiss = { state.eventSink(RoomNotificationSettingsEvents.ClearRestoreDefaultError) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserDefinedRoomNotificationSettingsTopBar(
    roomName: String,
    onBackClick: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                text = roomName,
            )
        },
        navigationIcon = { BackButton(onClick = onBackClick) },
    )
}

@PreviewsDayNight
@Composable
internal fun UserDefinedRoomNotificationSettingsViewPreview(
    @PreviewParameter(UserDefinedRoomNotificationSettingsStateProvider::class) state: RoomNotificationSettingsState
) = ElementPreview {
    UserDefinedRoomNotificationSettingsView(
        state = state,
        onBackClick = {},
    )
}

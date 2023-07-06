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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.element.android.features.roomdetails.impl.R
import io.element.android.libraries.core.bool.orTrue
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.components.preferences.PreferenceCategory
import io.element.android.libraries.designsystem.components.preferences.PreferenceSwitch
import io.element.android.libraries.designsystem.components.preferences.PreferenceText
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.components.CenterAlignedTopAppBar
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.api.room.RoomNotificationMode

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RoomNotificationSettingsView(
    state: RoomNotificationSettingsState,
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            RoomNotificationSettingsTopBar(
                onBackPressed = { onBackPressed() }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(padding)
                .consumeWindowInsets(padding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
//            PreferenceSwitch(
//                isChecked = state.formState.sendLogs,
//                onCheckedChange = { eventSink(BugReportEvents.SetSendLog(it)) },
//                enabled = isFormEnabled,
//                title = stringResource(id = R.string.screen_bug_report_include_logs),
//                subtitle = stringResource(id = R.string.screen_bug_report_logs_description),
//            )
            val subtitle = when(state.defaultRoomNotificationMode) {
                RoomNotificationMode.ALL_MESSAGES -> "All messages"
                RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY -> "Mentions and keywords"
                RoomNotificationMode.MUTE -> "Mute"
                null -> ""
            }


            PreferenceCategory(title = "Notify me in this chat for") {
                PreferenceSwitch(
                    isChecked = state.roomNotificationSettings?.isDefault.orTrue(),
                    onCheckedChange = {
                        state.eventSink(RoomNotificationSettingsEvents.DefaultNotificationModeSelected)
                    },
                    title = "Match default setting",
                    subtitle = subtitle,
                    enabled = state.roomNotificationSettings != null
                )

                PreferenceText(
                    title = "Allow custom setting",
                    subtitle = "Turning this on will override yout default setting",
                    enabled = state.roomNotificationSettings != null && !state.roomNotificationSettings.isDefault,
                )

                if (state.roomNotificationSettings != null) {
                    RoomNotificationSettingsOptions(
                        modifier = modifier,
                        selected = state.roomNotificationSettings.mode,
                        enabled = !state.roomNotificationSettings.isDefault,
                        onOptionSelected = {
                            state.eventSink(RoomNotificationSettingsEvents.RoomNotificationModeChanged(it.mode))
                        },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomNotificationSettingsTopBar(
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit = {},
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = stringResource(R.string.screen_room_details_notification_title),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
        },
        navigationIcon = { BackButton(onClick = onBackPressed) },
    )
}

@Composable
fun RoomNotificationSettingsOptions(
    selected: RoomNotificationMode?,
    modifier: Modifier = Modifier,
    enabled: Boolean,
    onOptionSelected: (RoomNotificationSettingsItem) -> Unit = {},
) {
    val items = roomNotificationSettingsItems()
    Column(modifier = modifier.selectableGroup()) {
        items.forEach { item ->
            RoomNotificationSettingsOption(
                roomNotificationSettingsItem = item,
                isSelected = selected == item.mode,
                onOptionSelected = onOptionSelected,
                enabled = enabled
            )
        }
    }
}

@Preview
@Composable
fun RoomNotificationSettingsLightPreview(@PreviewParameter(RoomNotificationSettingsStateProvider::class) state: RoomNotificationSettingsState) =
    ElementPreviewLight { ContentToPreview(state) }

@Preview
@Composable
fun RoomNotificationSettingsDarkPreview(@PreviewParameter(RoomNotificationSettingsStateProvider::class) state: RoomNotificationSettingsState) =
    ElementPreviewDark { ContentToPreview(state) }

@Composable
private fun ContentToPreview(state: RoomNotificationSettingsState) {
    RoomNotificationSettingsView(state)
}

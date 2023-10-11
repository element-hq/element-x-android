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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import io.element.android.libraries.androidutils.system.startNotificationSettingsIntent
import io.element.android.libraries.designsystem.components.dialogs.ErrorDialog
import io.element.android.libraries.designsystem.components.preferences.PreferenceCategory
import io.element.android.libraries.designsystem.components.preferences.PreferenceSwitch
import io.element.android.libraries.designsystem.components.preferences.PreferenceText
import io.element.android.libraries.designsystem.components.preferences.PreferencePage
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.ButtonSize
import io.element.android.libraries.designsystem.theme.components.Surface
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.utils.CommonDrawables
import io.element.android.libraries.designsystem.utils.OnLifecycleEvent
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.theme.ElementTheme
import io.element.android.libraries.ui.strings.CommonStrings

/**
 * A view that allows a user edit their global notification settings.
 */
@Composable
fun NotificationSettingsView(
    state: NotificationSettingsState,
    onOpenEditDefault: (isOneToOne: Boolean) -> Unit,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> state.eventSink.invoke(NotificationSettingsEvents.RefreshSystemNotificationsEnabled)
            else -> Unit
        }
    }
    PreferencePage(
        modifier = modifier,
        onBackPressed = onBackPressed,
        title = stringResource(id = CommonStrings.screen_notification_settings_title)
    ) {

        when (state.matrixSettings) {
            is NotificationSettingsState.MatrixSettings.Invalid -> InvalidNotificationSettingsView(
                showError = state.matrixSettings.fixFailed,
                onContinueClicked = { state.eventSink(NotificationSettingsEvents.FixConfigurationMismatch) },
                onDismissError = { state.eventSink(NotificationSettingsEvents.ClearConfigurationMismatchError) },
            )
            NotificationSettingsState.MatrixSettings.Uninitialized -> return@PreferencePage
            is NotificationSettingsState.MatrixSettings.Valid -> NotificationSettingsContentView(
                matrixSettings = state.matrixSettings,
                systemSettings = state.appSettings,
                onNotificationsEnabledChanged = { state.eventSink(NotificationSettingsEvents.SetNotificationsEnabled(it)) },
                onGroupChatsClicked = { onOpenEditDefault(false) },
                onDirectChatsClicked = { onOpenEditDefault(true) },
                onMentionNotificationsChanged = { state.eventSink(NotificationSettingsEvents.SetAtRoomNotificationsEnabled(it)) },
//                onCallsNotificationsChanged = { state.eventSink(NotificationSettingsEvents.SetCallNotificationsEnabled(it)) },
            )
        }
    }
}

@Composable
private fun NotificationSettingsContentView(
    matrixSettings: NotificationSettingsState.MatrixSettings.Valid,
    systemSettings: NotificationSettingsState.AppSettings,
    onNotificationsEnabledChanged: (Boolean) -> Unit,
    onGroupChatsClicked: () -> Unit,
    onDirectChatsClicked: () -> Unit,
    onMentionNotificationsChanged: (Boolean) -> Unit,
//    onCallsNotificationsChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    if (systemSettings.appNotificationsEnabled && !systemSettings.systemNotificationsEnabled) {
        PreferenceText(
            iconResourceId = CommonDrawables.ic_compound_notifications_solid_off,
            title = stringResource(id = CommonStrings.screen_notification_settings_system_notifications_turned_off),
            subtitle = stringResource(
                id = CommonStrings.screen_notification_settings_system_notifications_action_required,
                stringResource(id = CommonStrings.screen_notification_settings_system_notifications_action_required_content_link)
            ),
            onClick = {
                context.startNotificationSettingsIntent()
            }
        )
    }

    PreferenceSwitch(
        modifier = modifier,
        title = stringResource(id = CommonStrings.screen_notification_settings_enable_notifications),
        isChecked = systemSettings.appNotificationsEnabled,
        switchAlignment = Alignment.Top,
        onCheckedChange = onNotificationsEnabledChanged
    )

    if (systemSettings.appNotificationsEnabled) {
        PreferenceCategory(title = stringResource(id = CommonStrings.screen_notification_settings_notification_section_title)) {
            PreferenceText(
                title = stringResource(id = CommonStrings.screen_notification_settings_group_chats),
                subtitle = getTitleForRoomNotificationMode(mode = matrixSettings.defaultGroupNotificationMode),
                onClick = onGroupChatsClicked
            )

            PreferenceText(
                title = stringResource(id = CommonStrings.screen_notification_settings_direct_chats),
                subtitle = getTitleForRoomNotificationMode(mode = matrixSettings.defaultOneToOneNotificationMode),
                onClick = onDirectChatsClicked
            )
        }

        PreferenceCategory(title = stringResource(id = CommonStrings.screen_notification_settings_mode_mentions)) {
            PreferenceSwitch(
                modifier = Modifier,
                title = stringResource(id = CommonStrings.screen_notification_settings_room_mention_label),
                isChecked = matrixSettings.atRoomNotificationsEnabled,
                switchAlignment = Alignment.Top,
                onCheckedChange = onMentionNotificationsChanged
            )
        }
        // We are removing the call notification toggle until call support has been added
//            PreferenceCategory(title = stringResource(id = CommonStrings.screen_notification_settings_additional_settings_section_title)) {
//                PreferenceSwitch(
//                    modifier = Modifier,
//                    title = stringResource(id = CommonStrings.screen_notification_settings_calls_label),
//                    isChecked = matrixSettings.callNotificationsEnabled,
//                    switchAlignment = Alignment.Top,
//                    onCheckedChange = onCallsNotificationsChanged
//                )
//            }
    }
}

@Composable
private fun getTitleForRoomNotificationMode(mode: RoomNotificationMode?) =
    when (mode) {
        RoomNotificationMode.ALL_MESSAGES -> stringResource(id = CommonStrings.screen_notification_settings_edit_mode_all_messages)
        RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY -> stringResource(id = CommonStrings.screen_notification_settings_edit_mode_mentions_and_keywords)
        RoomNotificationMode.MUTE -> stringResource(id = CommonStrings.common_mute)
        null -> ""
    }

@Composable
private fun InvalidNotificationSettingsView(
    showError: Boolean,
    onContinueClicked: () -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Surface(
            Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row {
                    Text(
                        stringResource(CommonStrings.screen_notification_settings_configuration_mismatch),
                        modifier = Modifier.weight(1f),
                        style = ElementTheme.typography.fontBodyLgMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Start,
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    stringResource(CommonStrings.screen_notification_settings_configuration_mismatch_description),
                    style = ElementTheme.typography.fontBodyMdRegular,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    text = stringResource(CommonStrings.action_continue),
                    size = ButtonSize.Medium,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onContinueClicked,
                )
            }
        }
    }
    if (showError) {
        ErrorDialog(
            title = stringResource(id = CommonStrings.dialog_title_error),
            content = stringResource(id = CommonStrings.screen_notification_settings_failed_fixing_configuration),
            onDismiss = onDismissError
        )
    }
}

@PreviewsDayNight
@Composable
internal fun NotificationSettingsViewPreview(@PreviewParameter(NotificationSettingsStateProvider::class) state: NotificationSettingsState) = ElementPreview {
    NotificationSettingsView(
        state = state,
        onBackPressed = {},
        onOpenEditDefault = {},
    )
}

@PreviewsDayNight
@Composable
internal fun InvalidNotificationSettingsViewPreview() = ElementPreview {
    InvalidNotificationSettingsView(
        showError = false,
        onContinueClicked = {},
        onDismissError = {},
    )
}

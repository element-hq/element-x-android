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

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import io.element.android.libraries.designsystem.components.dialogs.ErrorDialog
import io.element.android.libraries.designsystem.components.preferences.PreferenceCategory
import io.element.android.libraries.designsystem.components.preferences.PreferenceSwitch
import io.element.android.libraries.designsystem.components.preferences.PreferenceText
import io.element.android.libraries.designsystem.components.preferences.PreferenceView
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.ButtonSize
import io.element.android.libraries.designsystem.theme.components.Surface
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.utils.OnLifecycleEvent
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.theme.ElementTheme
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.libraries.ui.strings.R
@Composable
fun NotificationSettingsView(
    state: NotificationSettingsState,
    onOpenEditDefault: (Boolean) -> Unit,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> state.eventSink.invoke(NotificationSettingsEvents.RefreshSystemNotificationsEnabled)
            else -> Unit
        }
    }
    PreferenceView(
        modifier = modifier,
        onBackPressed = onBackPressed,
        title = stringResource(id = CommonStrings.screen_notification_settings_title)
    ) {

        when (state.matrixNotificationSettings) {
            is NotificationSettingsState.MatrixNotificationSettings.InvalidNotificationSettingsState -> InvalidNotificationSettingsView(
                showError = state.matrixNotificationSettings.fixFailed,
                onContinueClicked = { state.eventSink(NotificationSettingsEvents.FixConfigurationMismatch) },
                onDismissError = { state.eventSink(NotificationSettingsEvents.ClearConfigurationMismatchError) },
                modifier = modifier,
            )
            NotificationSettingsState.MatrixNotificationSettings.Uninitialized -> return@PreferenceView
            is NotificationSettingsState.MatrixNotificationSettings.ValidNotificationSettingsState -> NotificationSettingsContentView(
                matrixSettings = state.matrixNotificationSettings,
                systemSettings = state.appNotificationSettings,
                onNotificationsEnabledChanged = { state.eventSink(NotificationSettingsEvents.SetNotificationsEnabled(it))},
                onGroupChatsClicked = { onOpenEditDefault(false) },
                onDirectChatsClicked = { onOpenEditDefault(true) },
                onMentionNotificationsChanged = { state.eventSink(NotificationSettingsEvents.SetAtRoomNotificationsEnabled(it)) },
                onCallsNotificationsChanged = { state.eventSink(NotificationSettingsEvents.SetCallNotificationsEnabled(it)) },
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun NotificationSettingsContentView(
    matrixSettings: NotificationSettingsState.MatrixNotificationSettings.ValidNotificationSettingsState,
    systemSettings: NotificationSettingsState.AppNotificationSettings,
    onNotificationsEnabledChanged: (Boolean) -> Unit,
    onGroupChatsClicked: () -> Unit,
    onDirectChatsClicked: () -> Unit,
    onMentionNotificationsChanged: (Boolean) -> Unit,
    onCallsNotificationsChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
        val context = LocalContext.current
        if (systemSettings.appNotificationsEnabled && !systemSettings.systemNotificationsEnabled) {
            PreferenceText(
                icon = Icons.Filled.NotificationsOff,
                title = stringResource(id = CommonStrings.screen_notification_settings_system_notifications_turned_off),
                subtitle = stringResource(id = CommonStrings.screen_notification_settings_system_notifications_action_required,
                    stringResource(id = CommonStrings.screen_notification_settings_system_notifications_action_required_content_link)),
                onClick = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    val uri: Uri = Uri.fromParts("package", context.packageName, null)
                    intent.data = uri
                    context.startActivity(intent)
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

            PreferenceCategory(title = stringResource(id = CommonStrings.screen_notification_settings_additional_settings_section_title)) {
                PreferenceSwitch(
                    modifier = Modifier,
                    title = stringResource(id = CommonStrings.screen_notification_settings_calls_label),
                    isChecked = matrixSettings.callNotificationsEnabled,
                    switchAlignment = Alignment.Top,
                    onCheckedChange = onCallsNotificationsChanged
                )
            }
        }
}


@Composable
private fun getTitleForRoomNotificationMode(mode: RoomNotificationMode?) =
when(mode) {
    RoomNotificationMode.ALL_MESSAGES -> stringResource(id = R.string.screen_notification_settings_edit_mode_all_messages)
    RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY -> stringResource(id = R.string.screen_notification_settings_edit_mode_mentions_and_keywords)
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
                        stringResource(R.string.screen_notification_settings_configuration_mismatch),
                        modifier = Modifier.weight(1f),
                        style = ElementTheme.typography.fontBodyLgMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Start,
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    stringResource(R.string.screen_notification_settings_configuration_mismatch_description),
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
    if(showError) {
        ErrorDialog(
            title = stringResource(id = CommonStrings.dialog_title_error),
            content = stringResource(id = CommonStrings.screen_notification_settings_failed_fixing_configuration),
            onDismiss = onDismissError
        )
    }
}

@Preview
@Composable
internal fun NotificationSettingsViewLightPreview(@PreviewParameter(NotificationSettingsStateProvider::class) state: NotificationSettingsState) =
    ElementPreviewLight { ContentToPreview(state) }

@Preview
@Composable
internal fun NotificationSettingsViewDarkPreview(@PreviewParameter(NotificationSettingsStateProvider::class) state: NotificationSettingsState) =
    ElementPreviewDark { ContentToPreview(state) }

@Composable
private fun ContentToPreview(state: NotificationSettingsState) {
    NotificationSettingsView(
        state = state,
        onBackPressed = {},
        onOpenEditDefault = {},
    )
}


@Preview
@Composable
internal fun InvalidNotificationSettingsViewightPreview(@PreviewParameter(NotificationSettingsStateProvider::class) state: NotificationSettingsState) =
    ElementPreviewLight { InvalidNotificationSettingsContentToPreview(state) }

@Preview
@Composable
internal fun InvalidNotificationSettingsViewDarkPreview(@PreviewParameter(NotificationSettingsStateProvider::class) state: NotificationSettingsState) =
    ElementPreviewDark { InvalidNotificationSettingsContentToPreview(state) }

@Composable
private fun InvalidNotificationSettingsContentToPreview(state: NotificationSettingsState) {
    InvalidNotificationSettingsView(
        showError = false,
        onContinueClicked = {},
        onDismissError = {},
    )
}

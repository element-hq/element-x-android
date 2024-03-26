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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.Lifecycle
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.preferences.impl.R
import io.element.android.libraries.androidutils.system.startNotificationSettingsIntent
import io.element.android.libraries.designsystem.atomic.molecules.DialogLikeBannerMolecule
import io.element.android.libraries.designsystem.components.async.AsyncActionView
import io.element.android.libraries.designsystem.components.dialogs.ErrorDialog
import io.element.android.libraries.designsystem.components.preferences.PreferenceCategory
import io.element.android.libraries.designsystem.components.preferences.PreferencePage
import io.element.android.libraries.designsystem.components.preferences.PreferenceSwitch
import io.element.android.libraries.designsystem.components.preferences.PreferenceText
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.utils.OnLifecycleEvent
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.ui.strings.CommonStrings

/**
 * A view that allows a user edit their global notification settings.
 */
@Composable
fun NotificationSettingsView(
    state: NotificationSettingsState,
    onOpenEditDefault: (isOneToOne: Boolean) -> Unit,
    onTroubleshootNotificationsClicked: () -> Unit,
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
        title = stringResource(id = R.string.screen_notification_settings_title)
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
                // TODO We are removing the call notification toggle until support for call notifications has been added
//                onCallsNotificationsChanged = { state.eventSink(NotificationSettingsEvents.SetCallNotificationsEnabled(it)) },
                onInviteForMeNotificationsChanged = { state.eventSink(NotificationSettingsEvents.SetInviteForMeNotificationsEnabled(it)) },
                onTroubleshootNotificationsClicked = onTroubleshootNotificationsClicked,
            )
        }
        AsyncActionView(
            async = state.changeNotificationSettingAction,
            errorMessage = { stringResource(R.string.screen_notification_settings_edit_failed_updating_default_mode) },
            onErrorDismiss = { state.eventSink(NotificationSettingsEvents.ClearNotificationChangeError) },
            onSuccess = {},
        )
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
    // TODO We are removing the call notification toggle until support for call notifications has been added
//    onCallsNotificationsChanged: (Boolean) -> Unit,
    onInviteForMeNotificationsChanged: (Boolean) -> Unit,
    onTroubleshootNotificationsClicked: () -> Unit,
) {
    val context = LocalContext.current
    if (systemSettings.appNotificationsEnabled && !systemSettings.systemNotificationsEnabled) {
        PreferenceText(
            icon = CompoundIcons.NotificationsOffSolid(),
            title = stringResource(id = R.string.screen_notification_settings_system_notifications_turned_off),
            subtitle = stringResource(
                id = R.string.screen_notification_settings_system_notifications_action_required,
                stringResource(id = R.string.screen_notification_settings_system_notifications_action_required_content_link)
            ),
            onClick = {
                context.startNotificationSettingsIntent()
            }
        )
    }

    PreferenceSwitch(
        title = stringResource(id = R.string.screen_notification_settings_enable_notifications),
        isChecked = systemSettings.appNotificationsEnabled,
        switchAlignment = Alignment.Top,
        onCheckedChange = onNotificationsEnabledChanged
    )

    if (systemSettings.appNotificationsEnabled) {
        PreferenceCategory(title = stringResource(id = R.string.screen_notification_settings_notification_section_title)) {
            PreferenceText(
                title = stringResource(id = R.string.screen_notification_settings_group_chats),
                subtitle = getTitleForRoomNotificationMode(mode = matrixSettings.defaultGroupNotificationMode),
                onClick = onGroupChatsClicked
            )

            PreferenceText(
                title = stringResource(id = R.string.screen_notification_settings_direct_chats),
                subtitle = getTitleForRoomNotificationMode(mode = matrixSettings.defaultOneToOneNotificationMode),
                onClick = onDirectChatsClicked
            )
        }

        PreferenceCategory(title = stringResource(id = R.string.screen_notification_settings_mode_mentions)) {
            PreferenceSwitch(
                modifier = Modifier,
                title = stringResource(id = R.string.screen_notification_settings_room_mention_label),
                isChecked = matrixSettings.atRoomNotificationsEnabled,
                switchAlignment = Alignment.Top,
                onCheckedChange = onMentionNotificationsChanged
            )
        }
        PreferenceCategory(title = stringResource(id = R.string.screen_notification_settings_additional_settings_section_title)) {
            // TODO We are removing the call notification toggle until support for call notifications has been added
//                PreferenceSwitch(
//                    modifier = Modifier,
//                    title = stringResource(id = CommonStrings.screen_notification_settings_calls_label),
//                    isChecked = matrixSettings.callNotificationsEnabled,
//                    switchAlignment = Alignment.Top,
//                    onCheckedChange = onCallsNotificationsChanged
//                )
            PreferenceSwitch(
                modifier = Modifier,
                title = stringResource(id = R.string.screen_notification_settings_invite_for_me_label),
                isChecked = matrixSettings.inviteForMeNotificationsEnabled,
                switchAlignment = Alignment.Top,
                onCheckedChange = onInviteForMeNotificationsChanged
            )
        }
        PreferenceCategory(title = "Troubleshoot") {
            PreferenceText(
                modifier = Modifier,
                title = "Troubleshoot notifications",
                onClick = onTroubleshootNotificationsClicked
            )
        }
    }
}

@Composable
private fun getTitleForRoomNotificationMode(mode: RoomNotificationMode?) =
    when (mode) {
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
) {
    DialogLikeBannerMolecule(
        title = stringResource(R.string.screen_notification_settings_configuration_mismatch),
        content = stringResource(R.string.screen_notification_settings_configuration_mismatch_description),
        onSubmitClicked = onContinueClicked,
        onDismissClicked = null,
    )

    if (showError) {
        ErrorDialog(
            title = stringResource(id = CommonStrings.dialog_title_error),
            content = stringResource(id = R.string.screen_notification_settings_failed_fixing_configuration),
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
        onTroubleshootNotificationsClicked = {},
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

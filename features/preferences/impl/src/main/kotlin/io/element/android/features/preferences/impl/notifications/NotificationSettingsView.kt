/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.notifications

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.progressSemantics
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.preferences.impl.R
import io.element.android.libraries.androidutils.system.startNotificationSettingsIntent
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.components.Announcement
import io.element.android.libraries.designsystem.components.AnnouncementType
import io.element.android.libraries.designsystem.components.async.AsyncActionView
import io.element.android.libraries.designsystem.components.dialogs.ErrorDialog
import io.element.android.libraries.designsystem.components.dialogs.ListOption
import io.element.android.libraries.designsystem.components.dialogs.SingleSelectionDialog
import io.element.android.libraries.designsystem.components.list.ListItemContent
import io.element.android.libraries.designsystem.components.preferences.PreferenceCategory
import io.element.android.libraries.designsystem.components.preferences.PreferencePage
import io.element.android.libraries.designsystem.components.preferences.PreferenceSwitch
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.IconSource
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.utils.OnLifecycleEvent
import io.element.android.libraries.fullscreenintent.api.FullScreenIntentPermissionsEvents
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.toImmutableList

/**
 * A view that allows a user edit their global notification settings.
 */
@Composable
fun NotificationSettingsView(
    state: NotificationSettingsState,
    onOpenEditDefault: (isOneToOne: Boolean) -> Unit,
    onTroubleshootNotificationsClick: () -> Unit,
    onBackClick: () -> Unit,
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
        onBackClick = onBackClick,
        title = stringResource(id = R.string.screen_notification_settings_title)
    ) {
        when (state.matrixSettings) {
            is NotificationSettingsState.MatrixSettings.Invalid -> InvalidNotificationSettingsView(
                showError = state.matrixSettings.fixFailed,
                onContinueClick = { state.eventSink(NotificationSettingsEvents.FixConfigurationMismatch) },
                onDismissError = { state.eventSink(NotificationSettingsEvents.ClearConfigurationMismatchError) },
            )
            NotificationSettingsState.MatrixSettings.Uninitialized -> return@PreferencePage
            is NotificationSettingsState.MatrixSettings.Valid -> NotificationSettingsContentView(
                matrixSettings = state.matrixSettings,
                state = state,
                onNotificationsEnabledChange = { state.eventSink(NotificationSettingsEvents.SetNotificationsEnabled(it)) },
                onGroupChatsClick = { onOpenEditDefault(false) },
                onDirectChatsClick = { onOpenEditDefault(true) },
                onMentionNotificationsChange = { state.eventSink(NotificationSettingsEvents.SetAtRoomNotificationsEnabled(it)) },
                // TODO We are removing the call notification toggle until support for call notifications has been added
//                onCallsNotificationsChanged = { state.eventSink(NotificationSettingsEvents.SetCallNotificationsEnabled(it)) },
                onInviteForMeNotificationsChange = { state.eventSink(NotificationSettingsEvents.SetInviteForMeNotificationsEnabled(it)) },
                onTroubleshootNotificationsClick = onTroubleshootNotificationsClick,
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
    state: NotificationSettingsState,
    onNotificationsEnabledChange: (Boolean) -> Unit,
    onGroupChatsClick: () -> Unit,
    onDirectChatsClick: () -> Unit,
    onMentionNotificationsChange: (Boolean) -> Unit,
    // TODO We are removing the call notification toggle until support for call notifications has been added
//    onCallsNotificationsChanged: (Boolean) -> Unit,
    onInviteForMeNotificationsChange: (Boolean) -> Unit,
    onTroubleshootNotificationsClick: () -> Unit,
) {
    val context = LocalContext.current
    val systemSettings: NotificationSettingsState.AppSettings = state.appSettings
    if (systemSettings.appNotificationsEnabled && !systemSettings.systemNotificationsEnabled) {
        ListItem(
            leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.NotificationsOffSolid())),
            headlineContent = {
                Text(stringResource(id = R.string.screen_notification_settings_system_notifications_turned_off))
            },
            supportingContent = {
                Text(
                    stringResource(
                        id = R.string.screen_notification_settings_system_notifications_action_required,
                        stringResource(id = R.string.screen_notification_settings_system_notifications_action_required_content_link)
                    )
                )
            },
            onClick = {
                context.startNotificationSettingsIntent()
            }
        )
    }

    PreferenceSwitch(
        title = stringResource(id = R.string.screen_notification_settings_enable_notifications),
        isChecked = systemSettings.appNotificationsEnabled,
        onCheckedChange = onNotificationsEnabledChange
    )

    if (systemSettings.appNotificationsEnabled) {
        if (!state.fullScreenIntentPermissionsState.permissionGranted) {
            PreferenceCategory {
                ListItem(
                    leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.VoiceCallSolid())),
                    headlineContent = {
                        Text(stringResource(id = R.string.full_screen_intent_banner_title))
                    },
                    supportingContent = {
                        Text(stringResource(R.string.full_screen_intent_banner_message))
                    },
                    onClick = {
                        state.fullScreenIntentPermissionsState.eventSink(FullScreenIntentPermissionsEvents.OpenSettings)
                    }
                )
            }
        }
        PreferenceCategory(title = stringResource(id = R.string.screen_notification_settings_notification_section_title)) {
            ListItem(
                headlineContent = {
                    Text(stringResource(id = R.string.screen_notification_settings_group_chats))
                },
                supportingContent = {
                    Text(getTitleForRoomNotificationMode(mode = matrixSettings.defaultGroupNotificationMode))
                },
                onClick = onGroupChatsClick
            )
            ListItem(
                headlineContent = {
                    Text(stringResource(id = R.string.screen_notification_settings_direct_chats))
                },
                supportingContent = {
                    Text(getTitleForRoomNotificationMode(mode = matrixSettings.defaultOneToOneNotificationMode))
                },
                onClick = onDirectChatsClick
            )
        }

        PreferenceCategory(title = stringResource(id = R.string.screen_notification_settings_mode_mentions)) {
            PreferenceSwitch(
                modifier = Modifier,
                title = stringResource(id = R.string.screen_notification_settings_room_mention_label),
                isChecked = matrixSettings.atRoomNotificationsEnabled,
                onCheckedChange = onMentionNotificationsChange
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
                onCheckedChange = onInviteForMeNotificationsChange
            )
        }
        PreferenceCategory(title = stringResource(id = R.string.troubleshoot_notifications_entry_point_section)) {
            ListItem(
                headlineContent = {
                    Text(stringResource(id = R.string.troubleshoot_notifications_entry_point_title))
                },
                onClick = onTroubleshootNotificationsClick
            )
        }
        if (state.showAdvancedSettings) {
            PreferenceCategory(title = stringResource(id = CommonStrings.common_advanced_settings)) {
                ListItem(
                    headlineContent = {
                        Text(text = stringResource(id = R.string.screen_advanced_settings_push_provider_android))
                    },
                    trailingContent = when (state.currentPushDistributor) {
                        AsyncData.Uninitialized,
                        is AsyncData.Loading -> ListItemContent.Custom {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .progressSemantics()
                                    .size(20.dp),
                                strokeWidth = 2.dp
                            )
                        }
                        is AsyncData.Failure -> ListItemContent.Text(
                            stringResource(id = CommonStrings.common_error)
                        )
                        is AsyncData.Success -> ListItemContent.Text(
                            state.currentPushDistributor.dataOrNull()?.name ?: ""
                        )
                    },
                    onClick = {
                        if (state.currentPushDistributor.isReady()) {
                            state.eventSink(NotificationSettingsEvents.ChangePushProvider)
                        }
                    }
                )
            }
            if (state.showChangePushProviderDialog) {
                SingleSelectionDialog(
                    title = stringResource(id = R.string.screen_advanced_settings_choose_distributor_dialog_title_android),
                    options = state.availablePushDistributors.map { distributor ->
                        // If there are several distributors with the same name, use the full name
                        val title = if (state.availablePushDistributors.count { it.name == distributor.name } > 1) {
                            distributor.fullName
                        } else {
                            distributor.name
                        }
                        ListOption(title = title)
                    }.toImmutableList(),
                    initialSelection = state.availablePushDistributors.indexOf(state.currentPushDistributor.dataOrNull()),
                    onSelectOption = { index ->
                        state.eventSink(
                            NotificationSettingsEvents.SetPushProvider(index)
                        )
                    },
                    onDismissRequest = { state.eventSink(NotificationSettingsEvents.CancelChangePushProvider) },
                )
            }
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
    onContinueClick: () -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Announcement(
        title = stringResource(R.string.screen_notification_settings_configuration_mismatch),
        description = stringResource(R.string.screen_notification_settings_configuration_mismatch_description),
        type = AnnouncementType.Actionable(
            onActionClick = onContinueClick,
            actionText = stringResource(CommonStrings.action_continue),
            onDismissClick = null,
        ),
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )

    if (showError) {
        ErrorDialog(
            title = stringResource(id = CommonStrings.dialog_title_error),
            content = stringResource(id = R.string.screen_notification_settings_failed_fixing_configuration),
            onSubmit = onDismissError
        )
    }
}

@PreviewsDayNight
@Composable
internal fun NotificationSettingsViewPreview(@PreviewParameter(NotificationSettingsStateProvider::class) state: NotificationSettingsState) = ElementPreview {
    NotificationSettingsView(
        state = state,
        onBackClick = {},
        onOpenEditDefault = {},
        onTroubleshootNotificationsClick = {},
    )
}

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
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.features.roomdetails.impl.R
import io.element.android.libraries.core.bool.orTrue
import io.element.android.libraries.designsystem.components.async.AsyncView
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.components.preferences.PreferenceCategory
import io.element.android.libraries.designsystem.components.preferences.PreferenceSwitch
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.text.buildAnnotatedStringWithStyledPart
import io.element.android.libraries.designsystem.theme.aliasScreenTitle
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun RoomNotificationSettingsView(
    state: RoomNotificationSettingsState,
    onShowGlobalNotifications: () -> Unit,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state.showUserDefinedSettingStyle) {
        UserDefinedRoomNotificationSettingsView(
            state = state,
            modifier = modifier,
            onBackPressed = onBackPressed,
        )
    } else {
        RoomSpecificNotificationSettingsView(
            state = state,
            modifier = modifier,
            onShowGlobalNotifications = onShowGlobalNotifications,
            onBackPressed = onBackPressed,
        )
    }
}

@Composable
private fun RoomSpecificNotificationSettingsView(
    state: RoomNotificationSettingsState,
    onShowGlobalNotifications: () -> Unit,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            RoomNotificationSettingsTopBar(
                onBackPressed = { onBackPressed() }
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
            PreferenceSwitch(
                isChecked = !state.displayIsDefault.orTrue(),
                onCheckedChange = {
                    state.eventSink(RoomNotificationSettingsEvents.SetNotificationMode(!it))
                },
                title = stringResource(id = R.string.screen_room_notification_settings_allow_custom),
                subtitle = stringResource(id = R.string.screen_room_notification_settings_allow_custom_footnote),
                enabled = roomNotificationSettings != null
            )
            if (state.displayIsDefault.orTrue()) {
                PreferenceCategory(title = stringResource(id = R.string.screen_room_notification_settings_default_setting_title)) {
                    val text = buildAnnotatedStringWithStyledPart(
                        R.string.screen_room_notification_settings_default_setting_footnote,
                        R.string.screen_room_notification_settings_default_setting_footnote_content_link,
                        color = Color.Unspecified,
                        underline = false,
                        bold = true,
                    )
                    ClickableText(
                        text = text,
                        onClick = {
                            onShowGlobalNotifications()
                        },
                        modifier = Modifier
                            .padding(start = 16.dp, bottom = 16.dp, end = 16.dp),
                        style = ElementTheme.typography.fontBodyMdRegular
                            .copy(
                                color = MaterialTheme.colorScheme.secondary,
                                textAlign = TextAlign.Center,
                            )
                    )
                    if (state.defaultRoomNotificationMode != null) {
                        val defaultModeTitle = when (state.defaultRoomNotificationMode) {
                            RoomNotificationMode.ALL_MESSAGES -> stringResource(id = R.string.screen_room_notification_settings_mode_all_messages)
                            RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY -> {
                                stringResource(id = R.string.screen_room_notification_settings_mode_mentions_and_keywords)
                            }
                            RoomNotificationMode.MUTE -> stringResource(id = CommonStrings.common_mute)
                        }
                        val displayMentionsOnlyDisclaimer = state.displayMentionsOnlyDisclaimer
                            && state.defaultRoomNotificationMode == RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY
                        RoomNotificationSettingsOption(
                            roomNotificationSettingsItem = RoomNotificationSettingsItem(state.defaultRoomNotificationMode, defaultModeTitle),
                            isSelected = true,
                            onOptionSelected = { },
                            displayMentionsOnlyDisclaimer = displayMentionsOnlyDisclaimer,
                            enabled = true
                        )
                    }
                }
            } else {
                PreferenceCategory(title = stringResource(id = R.string.screen_room_notification_settings_custom_settings_title)) {
                    RoomNotificationSettingsOptions(
                        selected = state.displayNotificationMode,
                        enabled = !state.displayIsDefault.orTrue(),
                        displayMentionsOnlyDisclaimer = state.displayMentionsOnlyDisclaimer,
                        onOptionSelected = {
                            state.eventSink(RoomNotificationSettingsEvents.RoomNotificationModeChanged(it.mode))
                        },
                    )
                }
            }

            AsyncView(
                async = state.setNotificationSettingAction,
                onSuccess = {},
                errorMessage = { stringResource(R.string.screen_notification_settings_edit_failed_updating_default_mode) },
                onErrorDismiss = { state.eventSink(RoomNotificationSettingsEvents.ClearSetNotificationError) },
            )

            AsyncView(
                async = state.restoreDefaultAction,
                onSuccess = {},
                errorMessage = { stringResource(R.string.screen_notification_settings_edit_failed_updating_default_mode) },
                onErrorDismiss = { state.eventSink(RoomNotificationSettingsEvents.ClearRestoreDefaultError) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoomNotificationSettingsTopBar(
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = stringResource(R.string.screen_room_details_notification_title),
                style = ElementTheme.typography.aliasScreenTitle,
            )
        },
        navigationIcon = { BackButton(onClick = onBackPressed) },
    )
}

@PreviewsDayNight
@Composable
internal fun RoomNotificationSettingsPreview(
    @PreviewParameter(RoomNotificationSettingsStateProvider::class) state: RoomNotificationSettingsState
) = ElementPreview {
    RoomNotificationSettingsView(
        state = state,
        onShowGlobalNotifications = {},
        onBackPressed = {},
    )
}

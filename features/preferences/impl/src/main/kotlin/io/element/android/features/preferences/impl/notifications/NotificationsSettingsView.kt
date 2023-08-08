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

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.libraries.designsystem.components.preferences.PreferenceCategory
import io.element.android.libraries.designsystem.components.preferences.PreferenceSwitch
import io.element.android.libraries.designsystem.components.preferences.PreferenceText
import io.element.android.libraries.designsystem.components.preferences.PreferenceView
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun NotificationsSettingsView(
    state: NotificationsSettingsState,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PreferenceView(
        modifier = modifier,
        onBackPressed = onBackPressed,
        title = stringResource(id = CommonStrings.screen_notification_settings_title)
    ) {

        if (state.isEnabled && !state.hasSystemPermission) {
            PreferenceText(
                icon = Icons.Filled.NotificationsOff,
                title = stringResource(id = CommonStrings.screen_notification_settings_system_notifications_turned_off),
                subtitle = stringResource(id = CommonStrings.screen_notification_settings_system_notifications_action_required,
                    stringResource(id = CommonStrings.screen_notification_settings_system_notifications_action_required_content_link)),
                onClick = {}
            )
        }

        PreferenceSwitch(
            modifier = modifier,
            title = stringResource(id = CommonStrings.screen_notification_settings_enable_notifications),
            isChecked = state.isEnabled,
//            onCheckedChange = ::onEnabledChanged,
            switchAlignment = Alignment.Top,
        )

        if (state.isEnabled) {
            PreferenceCategory(title = stringResource(id = CommonStrings.screen_notification_settings_notification_section_title)) {
                PreferenceText(
                    title = stringResource(id = CommonStrings.screen_notification_settings_group_chats),
                    subtitle = "All messages"
                )

                PreferenceText(
                    title = stringResource(id = CommonStrings.screen_notification_settings_direct_chats),
                    subtitle = "Mentions and Keywords"
                )
            }

            PreferenceCategory(title = stringResource(id = CommonStrings.screen_notification_settings_mode_mentions)) {
                PreferenceSwitch(
                    modifier = modifier,
                    title = stringResource(id = CommonStrings.screen_notification_settings_room_mention_label),
                    isChecked = state.notifyMeOnRoom,
//            onCheckedChange = ::onEnabledChanged,
                    switchAlignment = Alignment.Top,
                )
            }

            PreferenceCategory(title = stringResource(id = CommonStrings.screen_notification_settings_additional_settings_section_title)) {
                PreferenceSwitch(
                    modifier = modifier,
                    title = stringResource(id = CommonStrings.screen_notification_settings_calls_label),
                    isChecked = state.acceptCalls,
//            onCheckedChange = ::onEnabledChanged,
                    switchAlignment = Alignment.Top,
                )
            }
        }
    }
}

@Preview
@Composable
internal fun AboutViewLightPreview(@PreviewParameter(NotificationsSettingsStateProvider::class) state: NotificationsSettingsState) =
    ElementPreviewLight { ContentToPreview(state) }

@Preview
@Composable
internal fun AboutViewDarkPreview(@PreviewParameter(NotificationsSettingsStateProvider::class) state: NotificationsSettingsState) =
    ElementPreviewDark { ContentToPreview(state) }

@Composable
private fun ContentToPreview(state: NotificationsSettingsState) {
    NotificationsSettingsView(
        state = state,
        onBackPressed = {},
    )
}

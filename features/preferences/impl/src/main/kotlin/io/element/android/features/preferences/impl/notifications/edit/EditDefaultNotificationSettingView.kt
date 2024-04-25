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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.features.preferences.impl.R
import io.element.android.libraries.designsystem.components.async.AsyncActionView
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.list.ListItemContent
import io.element.android.libraries.designsystem.components.preferences.PreferenceCategory
import io.element.android.libraries.designsystem.components.preferences.PreferencePage
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.ui.strings.CommonStrings

/**
 * A view that allows a user to edit the default notification setting for rooms. This can be set separately
 * for one-to-one and group rooms, indicated by [EditDefaultNotificationSettingState.isOneToOne].
 */
@Composable
fun EditDefaultNotificationSettingView(
    state: EditDefaultNotificationSettingState,
    openRoomNotificationSettings: (roomId: RoomId) -> Unit,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val title = if (state.isOneToOne) {
        R.string.screen_notification_settings_direct_chats
    } else {
        R.string.screen_notification_settings_group_chats
    }
    PreferencePage(
        modifier = modifier,
        onBackPressed = onBackPressed,
        title = stringResource(id = title)
    ) {
        // Only ALL_MESSAGES and MENTIONS_AND_KEYWORDS_ONLY are valid global defaults.
        val validModes = listOf(RoomNotificationMode.ALL_MESSAGES, RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY)

        val categoryTitle = if (state.isOneToOne) {
            R.string.screen_notification_settings_edit_screen_direct_section_header
        } else {
            R.string.screen_notification_settings_edit_screen_group_section_header
        }
        PreferenceCategory(title = stringResource(id = categoryTitle)) {
            if (state.mode != null) {
                Column(modifier = Modifier.selectableGroup()) {
                    validModes.forEach { item ->
                        DefaultNotificationSettingOption(
                            mode = item,
                            isSelected = state.mode == item,
                            displayMentionsOnlyDisclaimer = state.displayMentionsOnlyDisclaimer,
                            onOptionSelected = { state.eventSink(EditDefaultNotificationSettingStateEvents.SetNotificationMode(it)) }
                        )
                    }
                }
            }
        }
        if (state.roomsWithUserDefinedMode.isNotEmpty()) {
            PreferenceCategory(title = stringResource(id = R.string.screen_notification_settings_edit_custom_settings_section_title)) {
                state.roomsWithUserDefinedMode.forEach { summary ->
                    val subtitle = when (summary.details.userDefinedNotificationMode) {
                        RoomNotificationMode.ALL_MESSAGES -> stringResource(id = R.string.screen_notification_settings_edit_mode_all_messages)
                        RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY -> {
                            stringResource(id = R.string.screen_notification_settings_edit_mode_mentions_and_keywords)
                        }
                        RoomNotificationMode.MUTE -> stringResource(id = CommonStrings.common_mute)
                        null -> ""
                    }
                    val avatarData = AvatarData(
                        id = summary.identifier(),
                        name = summary.details.name,
                        url = summary.details.avatarUrl,
                        size = AvatarSize.CustomRoomNotificationSetting,
                    )
                    ListItem(
                        headlineContent = {
                            val roomName = summary.details.name
                            Text(
                                text = roomName ?: stringResource(id = CommonStrings.common_no_room_name),
                                fontStyle = FontStyle.Italic.takeIf { roomName == null }
                            )
                        },
                        supportingContent = {
                            Text(text = subtitle)
                        },
                        leadingContent = ListItemContent.Custom {
                            Avatar(avatarData = avatarData)
                        },
                        onClick = {
                            openRoomNotificationSettings(summary.details.roomId)
                        }
                    )
                }
            }
        }
        AsyncActionView(
            async = state.changeNotificationSettingAction,
            errorMessage = { stringResource(R.string.screen_notification_settings_edit_failed_updating_default_mode) },
            onErrorDismiss = { state.eventSink(EditDefaultNotificationSettingStateEvents.ClearError) },
            onSuccess = {},
        )
    }
}

@PreviewsDayNight
@Composable
internal fun EditDefaultNotificationSettingViewPreview(
    @PreviewParameter(EditDefaultNotificationSettingStateProvider::class) state: EditDefaultNotificationSettingState
) = ElementPreview {
    EditDefaultNotificationSettingView(
        state = state,
        openRoomNotificationSettings = {},
        onBackPressed = {},
    )
}

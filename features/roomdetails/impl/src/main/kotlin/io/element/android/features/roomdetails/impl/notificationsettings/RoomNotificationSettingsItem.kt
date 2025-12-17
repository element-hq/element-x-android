/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.notificationsettings

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.element.android.features.roomdetails.impl.R
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

data class RoomNotificationSettingsItem(
    val mode: RoomNotificationMode,
    val title: String,
)

@Composable
fun roomNotificationSettingsItems(): ImmutableList<RoomNotificationSettingsItem> {
    return RoomNotificationMode.entries
        .map {
            when (it) {
                RoomNotificationMode.ALL_MESSAGES -> RoomNotificationSettingsItem(
                    mode = it,
                    title = stringResource(R.string.screen_room_notification_settings_mode_all_messages),
                )
                RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY -> RoomNotificationSettingsItem(
                    mode = it,
                    title = stringResource(R.string.screen_room_notification_settings_mode_mentions_and_keywords),
                )
                RoomNotificationMode.MUTE -> RoomNotificationSettingsItem(
                    mode = it,
                    title = stringResource(CommonStrings.common_mute),
                )
            }
        }
        .toImmutableList()
}

/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.preferences.impl.notifications.edit

import io.element.android.libraries.matrix.api.room.RoomNotificationMode

sealed interface EditDefaultNotificationSettingStateEvents {
    data class SetNotificationMode(val mode: RoomNotificationMode) : EditDefaultNotificationSettingStateEvents
    data object ClearError : EditDefaultNotificationSettingStateEvents
}

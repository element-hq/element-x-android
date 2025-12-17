/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.notifications.edit

import io.element.android.libraries.matrix.api.room.RoomNotificationMode

sealed interface EditDefaultNotificationSettingStateEvents {
    data class SetNotificationMode(val mode: RoomNotificationMode) : EditDefaultNotificationSettingStateEvents
    data object ClearError : EditDefaultNotificationSettingStateEvents
}

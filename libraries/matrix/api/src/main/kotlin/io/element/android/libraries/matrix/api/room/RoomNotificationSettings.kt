/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.room

data class RoomNotificationSettings(
    val mode: RoomNotificationMode,
    val isDefault: Boolean,
)

enum class RoomNotificationMode {
    ALL_MESSAGES,
    MENTIONS_AND_KEYWORDS_ONLY,
    MUTE
}

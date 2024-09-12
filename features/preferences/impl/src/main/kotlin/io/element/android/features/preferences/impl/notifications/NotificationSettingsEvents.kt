/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.preferences.impl.notifications

sealed interface NotificationSettingsEvents {
    data object RefreshSystemNotificationsEnabled : NotificationSettingsEvents
    data class SetNotificationsEnabled(val enabled: Boolean) : NotificationSettingsEvents
    data class SetAtRoomNotificationsEnabled(val enabled: Boolean) : NotificationSettingsEvents
    data class SetCallNotificationsEnabled(val enabled: Boolean) : NotificationSettingsEvents
    data class SetInviteForMeNotificationsEnabled(val enabled: Boolean) : NotificationSettingsEvents
    data object FixConfigurationMismatch : NotificationSettingsEvents
    data object ClearConfigurationMismatchError : NotificationSettingsEvents
    data object ClearNotificationChangeError : NotificationSettingsEvents
    data object ChangePushProvider : NotificationSettingsEvents
    data object CancelChangePushProvider : NotificationSettingsEvents
    data class SetPushProvider(val index: Int) : NotificationSettingsEvents
}

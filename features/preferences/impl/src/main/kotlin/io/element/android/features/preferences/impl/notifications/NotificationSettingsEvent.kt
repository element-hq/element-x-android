/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.notifications

import io.element.android.libraries.preferences.api.store.NotificationSound

sealed interface NotificationSettingsEvent {
    data object RefreshSystemNotificationsEnabled : NotificationSettingsEvent
    data class SetNotificationsEnabled(val enabled: Boolean) : NotificationSettingsEvent
    data class SetAtRoomNotificationsEnabled(val enabled: Boolean) : NotificationSettingsEvent
    data class SetCallNotificationsEnabled(val enabled: Boolean) : NotificationSettingsEvent
    data class SetInviteForMeNotificationsEnabled(val enabled: Boolean) : NotificationSettingsEvent
    data class SetConversationNotificationsEnabled(val enabled: Boolean) : NotificationSettingsEvent
    data object FixConfigurationMismatch : NotificationSettingsEvent
    data object ClearConfigurationMismatchError : NotificationSettingsEvent
    data object ClearNotificationChangeError : NotificationSettingsEvent
    data object ChangePushProvider : NotificationSettingsEvent
    data object CancelChangePushProvider : NotificationSettingsEvent
    data class SetPushProvider(val index: Int) : NotificationSettingsEvent
    data class SetMessageSound(val sound: NotificationSound) : NotificationSettingsEvent
    data class SetCallRingtone(val sound: NotificationSound) : NotificationSettingsEvent
    data object DismissMessageSoundCopyError : NotificationSettingsEvent
    data object DismissCallRingtoneCopyError : NotificationSettingsEvent
    data object ShowMessageSoundDialog : NotificationSettingsEvent
    data object DismissMessageSoundDialog : NotificationSettingsEvent
    data class SelectMessageSoundPreset(val sound: NotificationSound) : NotificationSettingsEvent
    data object LaunchMessageSoundPicker : NotificationSettingsEvent
    data object ShowCallRingtoneDialog : NotificationSettingsEvent
    data object DismissCallRingtoneDialog : NotificationSettingsEvent
    data class SelectCallRingtonePreset(val sound: NotificationSound) : NotificationSettingsEvent
    data object LaunchCallRingtonePicker : NotificationSettingsEvent
}

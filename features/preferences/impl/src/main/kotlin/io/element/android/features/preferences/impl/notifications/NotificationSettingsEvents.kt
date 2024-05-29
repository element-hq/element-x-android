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

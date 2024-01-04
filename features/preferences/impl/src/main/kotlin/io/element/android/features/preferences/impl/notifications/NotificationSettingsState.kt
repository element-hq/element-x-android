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

import androidx.compose.runtime.Immutable
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.room.RoomNotificationMode

@Immutable
data class NotificationSettingsState(
    val matrixSettings: MatrixSettings,
    val appSettings: AppSettings,
    val changeNotificationSettingAction: AsyncData<Unit>,
    val eventSink: (NotificationSettingsEvents) -> Unit,
) {
    sealed interface MatrixSettings {
        data object Uninitialized :  MatrixSettings
        data class Valid(
            val atRoomNotificationsEnabled: Boolean,
            val callNotificationsEnabled: Boolean,
            val inviteForMeNotificationsEnabled: Boolean,
            val defaultGroupNotificationMode: RoomNotificationMode?,
            val defaultOneToOneNotificationMode: RoomNotificationMode?,
        ) : MatrixSettings

        data class Invalid(
            val fixFailed: Boolean
        ) :  MatrixSettings
    }

    data class AppSettings(
        val systemNotificationsEnabled: Boolean,
        val appNotificationsEnabled: Boolean,
    )
}





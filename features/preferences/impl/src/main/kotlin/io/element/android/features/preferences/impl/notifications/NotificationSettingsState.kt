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
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.fullscreenintent.api.FullScreenIntentPermissionsState
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import kotlinx.collections.immutable.ImmutableList

@Immutable
data class NotificationSettingsState(
    val matrixSettings: MatrixSettings,
    val appSettings: AppSettings,
    val changeNotificationSettingAction: AsyncAction<Unit>,
    val currentPushDistributor: AsyncData<String>,
    val availablePushDistributors: ImmutableList<String>,
    val showChangePushProviderDialog: Boolean,
    val fullScreenIntentPermissionsState: FullScreenIntentPermissionsState,
    val eventSink: (NotificationSettingsEvents) -> Unit,
) {
    sealed interface MatrixSettings {
        data object Uninitialized : MatrixSettings
        data class Valid(
            val atRoomNotificationsEnabled: Boolean,
            val callNotificationsEnabled: Boolean,
            val inviteForMeNotificationsEnabled: Boolean,
            val defaultGroupNotificationMode: RoomNotificationMode?,
            val defaultOneToOneNotificationMode: RoomNotificationMode?,
        ) : MatrixSettings

        data class Invalid(
            val fixFailed: Boolean
        ) : MatrixSettings
    }

    data class AppSettings(
        val systemNotificationsEnabled: Boolean,
        val appNotificationsEnabled: Boolean,
    )

    /**
     * Whether the advanced settings should be shown.
     * This is true if the current push distributor is in a failure state or if there are multiple push distributors available.
     */
    val showAdvancedSettings: Boolean = currentPushDistributor.isFailure() || availablePushDistributors.size > 1
}

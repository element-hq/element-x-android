/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.notifications

import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.fullscreenintent.api.FullScreenIntentPermissionsState
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.preferences.api.store.NotificationSound
import io.element.android.libraries.pushproviders.api.Distributor
import kotlinx.collections.immutable.ImmutableList

data class NotificationSettingsState(
    val matrixSettings: MatrixSettings,
    val appSettings: AppSettings,
    val changeNotificationSettingAction: AsyncAction<Unit>,
    val currentPushDistributor: AsyncData<Distributor>,
    val availablePushDistributors: ImmutableList<Distributor>,
    val showChangePushProviderDialog: Boolean,
    val fullScreenIntentPermissionsState: FullScreenIntentPermissionsState,
    /** Persisted message-sound choice. The corresponding [messageSoundDisplayName] is its label. */
    val messageSound: NotificationSound,
    /**
     * Label for [messageSound]. For [NotificationSound.SystemDefault] / [NotificationSound.Silent]
     * this is set synchronously from string resources. For [NotificationSound.Custom] it's
     * resolved asynchronously via [io.element.android.libraries.push.api.notifications.SoundDisplayNameResolver],
     * so the field starts empty and updates once the lookup settles.
     */
    val messageSoundDisplayName: String,
    /** Persisted call-ringtone choice. The corresponding [callRingtoneDisplayName] is its label. */
    val callRingtone: NotificationSound,
    /** See [messageSoundDisplayName] — same async-resolution contract. */
    val callRingtoneDisplayName: String,
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

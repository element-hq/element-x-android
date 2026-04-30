/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.preferences.api.store

import androidx.compose.runtime.Immutable

/**
 * The user's choice of notification sound for a notification channel.
 */
@Immutable
sealed interface NotificationSound {
    /** Use the channel's bundled default sound (the in-app message sound or the system ringtone). */
    data object SystemDefault : NotificationSound

    /** Produce no sound. */
    data object Silent : NotificationSound

    /** Use the ringtone at [uri]. */
    data class Custom(val uri: String) : NotificationSound

    companion object {
        // String? round-trip used by [AppPreferencesStore]:
        //   null -> SystemDefault, "silent" -> Silent, else -> Custom(uri).
        private const val STORED_SILENT = "silent"

        fun fromStored(value: String?): NotificationSound = when (value) {
            null -> SystemDefault
            STORED_SILENT -> Silent
            else -> Custom(value)
        }

        fun NotificationSound.toStored(): String? = when (this) {
            SystemDefault -> null
            Silent -> STORED_SILENT
            is Custom -> uri
        }
    }
}

/**
 * Snapshot of the persisted notification-sound state, returned by
 * [AppPreferencesStore.getNotificationSoundChannelConfig] in a single read.
 */
data class NotificationSoundChannelConfig(
    val messageSound: NotificationSound,
    val messageSoundVersion: Int,
    val callRingtone: NotificationSound,
    val callRingtoneVersion: Int,
)

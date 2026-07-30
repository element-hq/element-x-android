/*
 * Copyright (c) 2026 Element Creations Ltd.
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
    /** Use the system's default tone for this channel (e.g. notification or ringtone). */
    data object SystemDefault : NotificationSound

    /**
     * Use the bundled in-app sound. Semantically meaningful only for the noisy message channel,
     * where it resolves to `R.raw.message`; the ringing-call channel has no bundled tone and
     * treats this the same as [SystemDefault].
     */
    data object ElementDefault : NotificationSound

    /**
     * Use the bundled in-app fade sound. Like [ElementDefault] this is meaningful only for the
     * noisy message channel (`R.raw.element_fade`); the ringing-call channel has no bundled tone
     * and treats this the same as [SystemDefault].
     */
    data object ElementFade : NotificationSound

    /** Produce no sound. */
    data object Silent : NotificationSound

    /** Use the ringtone at [uri]. */
    data class Custom(val uri: String) : NotificationSound

    companion object {
        // String? round-trip used by [AppPreferencesStore]:
        //   null -> SystemDefault, "silent" -> Silent, "element_default" -> ElementDefault,
        //   "element_fade" -> ElementFade, else -> Custom(uri).
        private const val STORED_SILENT = "silent"
        private const val STORED_ELEMENT_DEFAULT = "element_default"
        private const val STORED_ELEMENT_FADE = "element_fade"

        fun fromStored(value: String?): NotificationSound = when (value) {
            null -> SystemDefault
            STORED_SILENT -> Silent
            STORED_ELEMENT_DEFAULT -> ElementDefault
            STORED_ELEMENT_FADE -> ElementFade
            else -> Custom(value)
        }

        fun NotificationSound.toStored(): String? = when (this) {
            SystemDefault -> null
            Silent -> STORED_SILENT
            ElementDefault -> STORED_ELEMENT_DEFAULT
            ElementFade -> STORED_ELEMENT_FADE
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
    val messageSoundDisplayName: String?,
    val callRingtone: NotificationSound,
    val callRingtoneVersion: Int,
    val callRingtoneDisplayName: String?,
)

/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.preferences.api.store

/**
 * Persisted state describing which notification sound(s) the user previously selected could not be
 * resolved at app boot — file removed, permission revoked, etc. The affected channel is rebuilt
 * with [NotificationSound.SystemDefault] and the user is informed via the home-screen banner.
 *
 * Single source of truth: a single atomic write replaces the prior pair of independent booleans, so
 * the banner can never observe an "impossible" partial state.
 */
enum class NotificationSoundUnavailableState {
    None,
    MessageSound,
    CallRingtone,
    Both,
    ;

    fun withoutMessageSound(): NotificationSoundUnavailableState = when (this) {
        None, CallRingtone -> this
        MessageSound -> None
        Both -> CallRingtone
    }

    fun withoutCallRingtone(): NotificationSoundUnavailableState = when (this) {
        None, MessageSound -> this
        CallRingtone -> None
        Both -> MessageSound
    }

    companion object {
        fun from(messageUnavailable: Boolean, callRingtoneUnavailable: Boolean): NotificationSoundUnavailableState = when {
            messageUnavailable && callRingtoneUnavailable -> Both
            messageUnavailable -> MessageSound
            callRingtoneUnavailable -> CallRingtone
            else -> None
        }
    }
}

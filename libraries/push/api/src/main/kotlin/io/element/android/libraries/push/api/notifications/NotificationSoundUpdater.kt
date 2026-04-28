/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.api.notifications

import io.element.android.libraries.preferences.api.store.NotificationSound

/**
 * Recreates the notification channels backing the noisy-message and ringing-call sounds when the
 * user picks a new sound in settings. Android does not allow [android.app.NotificationChannel.setSound]
 * to be modified after channel creation, so a new versioned channel ID is created instead.
 */
interface NotificationSoundUpdater {
    fun recreateNoisyChannel(sound: NotificationSound, version: Int)

    fun recreateRingingCallChannel(sound: NotificationSound, version: Int)
}

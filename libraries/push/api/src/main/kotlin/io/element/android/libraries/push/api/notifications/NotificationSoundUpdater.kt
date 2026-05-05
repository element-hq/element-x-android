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
 * Reads and writes the sound on the message / ringing-call channels. Writes create a new
 * versioned channel because Android forbids mutating sound after creation.
 */
interface NotificationSoundUpdater {
    fun recreateNoisyChannel(sound: NotificationSound, version: Int)

    fun recreateRingingCallChannel(sound: NotificationSound, version: Int)

    /** Current channel sound classified into [NotificationSound]. Null when the channel doesn't exist. */
    suspend fun readNoisyChannelSound(): NotificationSound?

    /** See [readNoisyChannelSound]. */
    suspend fun readRingingCallChannelSound(): NotificationSound?
}

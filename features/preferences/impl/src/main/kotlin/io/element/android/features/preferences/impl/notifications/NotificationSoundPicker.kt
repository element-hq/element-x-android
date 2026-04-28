/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.notifications

import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import androidx.core.content.IntentCompat
import androidx.core.net.toUri
import io.element.android.libraries.preferences.api.store.NotificationSound

/**
 * Builds an `ACTION_RINGTONE_PICKER` intent for [type] (notification or ringtone), pre-selecting
 * the user's [current] choice and showing the system [defaultUri] as the "Default" option.
 */
internal fun buildRingtonePickerIntent(
    type: Int,
    current: NotificationSound,
    defaultUri: Uri,
): Intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
    putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, type)
    putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
    putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
    putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, defaultUri)
    val existingUri: Uri? = when (current) {
        NotificationSound.SystemDefault -> defaultUri
        NotificationSound.Silent -> null
        is NotificationSound.Custom -> current.uri.toUri()
    }
    putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, existingUri)
}

/**
 * Maps the URI returned by the ringtone picker into a [NotificationSound]:
 *  - null URI → the user picked "Silent",
 *  - URI matching [defaultUri] → the user picked "Default",
 *  - any other URI → the user picked a specific ringtone.
 */
internal fun Intent.toPickedNotificationSound(defaultUri: Uri): NotificationSound {
    val pickedUri: Uri? = IntentCompat.getParcelableExtra(this, RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java)
    return when {
        pickedUri == null -> NotificationSound.Silent
        pickedUri == defaultUri -> NotificationSound.SystemDefault
        else -> NotificationSound.Custom(pickedUri.toString())
    }
}

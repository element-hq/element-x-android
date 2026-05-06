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
import android.provider.Settings
import androidx.core.net.toUri
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.preferences.api.store.NotificationSound
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NotificationSoundPickerTest {
    @Test
    fun `buildRingtonePickerIntent encodes SystemDefault as the system default URI`() {
        val intent = buildRingtonePickerIntent(
            type = RingtoneManager.TYPE_NOTIFICATION,
            current = NotificationSound.SystemDefault,
            defaultUri = Settings.System.DEFAULT_NOTIFICATION_URI,
        )
        assertThat(intent.action).isEqualTo(RingtoneManager.ACTION_RINGTONE_PICKER)
        assertThat(intent.getIntExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, -1)).isEqualTo(RingtoneManager.TYPE_NOTIFICATION)
        assertThat(intent.getBooleanExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, false)).isTrue()
        assertThat(intent.getBooleanExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)).isTrue()
        @Suppress("DEPRECATION")
        val existing: Uri? = intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI)
        assertThat(existing).isEqualTo(Settings.System.DEFAULT_NOTIFICATION_URI)
    }

    @Test
    fun `buildRingtonePickerIntent encodes Silent as a null existing URI`() {
        val intent = buildRingtonePickerIntent(
            type = RingtoneManager.TYPE_RINGTONE,
            current = NotificationSound.Silent,
            defaultUri = Settings.System.DEFAULT_RINGTONE_URI,
        )
        @Suppress("DEPRECATION")
        val existing: Uri? = intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI)
        assertThat(existing).isNull()
    }

    @Test
    fun `buildRingtonePickerIntent encodes Custom as the parsed URI`() {
        val intent = buildRingtonePickerIntent(
            type = RingtoneManager.TYPE_NOTIFICATION,
            current = NotificationSound.Custom("content://media/internal/audio/media/42"),
            defaultUri = Settings.System.DEFAULT_NOTIFICATION_URI,
        )
        @Suppress("DEPRECATION")
        val existing: Uri? = intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI)
        assertThat(existing).isEqualTo("content://media/internal/audio/media/42".toUri())
    }

    @Test
    fun `toPickedNotificationSound maps null URI to Silent`() {
        val result = Intent().apply {
            putExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, null as Uri?)
        }
        assertThat(result.toPickedNotificationSound(Settings.System.DEFAULT_NOTIFICATION_URI))
            .isEqualTo(NotificationSound.Silent)
    }

    @Test
    fun `toPickedNotificationSound maps default URI to SystemDefault`() {
        val result = Intent().apply {
            putExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Settings.System.DEFAULT_NOTIFICATION_URI)
        }
        assertThat(result.toPickedNotificationSound(Settings.System.DEFAULT_NOTIFICATION_URI))
            .isEqualTo(NotificationSound.SystemDefault)
    }

    @Test
    fun `toPickedNotificationSound maps any other URI to Custom`() {
        val customUri = "content://media/internal/audio/media/42".toUri()
        val result = Intent().apply {
            putExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, customUri)
        }
        assertThat(result.toPickedNotificationSound(Settings.System.DEFAULT_NOTIFICATION_URI))
            .isEqualTo(NotificationSound.Custom(customUri.toString()))
    }
}

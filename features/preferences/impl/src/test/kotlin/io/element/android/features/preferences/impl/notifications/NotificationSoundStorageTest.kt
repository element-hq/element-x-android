/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.notifications

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.preferences.api.store.NotificationSound
import io.element.android.libraries.preferences.api.store.NotificationSound.Companion.toStored
import org.junit.Test

class NotificationSoundStorageTest {
    @Test
    fun `fromStored - null maps to SystemDefault`() {
        assertThat(NotificationSound.fromStored(null)).isEqualTo(NotificationSound.SystemDefault)
    }

    @Test
    fun `fromStored - silent sentinel maps to Silent`() {
        assertThat(NotificationSound.fromStored("silent")).isEqualTo(NotificationSound.Silent)
    }

    @Test
    fun `fromStored - element_default sentinel maps to ElementDefault`() {
        assertThat(NotificationSound.fromStored("element_default")).isEqualTo(NotificationSound.ElementDefault)
    }

    @Test
    fun `fromStored - any other string is treated as a Custom URI`() {
        assertThat(NotificationSound.fromStored("content://media/42"))
            .isEqualTo(NotificationSound.Custom("content://media/42"))
    }

    @Test
    fun `toStored - SystemDefault encodes as null`() {
        assertThat(NotificationSound.SystemDefault.toStored()).isNull()
    }

    @Test
    fun `toStored - ElementDefault encodes as element_default`() {
        assertThat(NotificationSound.ElementDefault.toStored()).isEqualTo("element_default")
    }

    @Test
    fun `toStored - Silent encodes as silent`() {
        assertThat(NotificationSound.Silent.toStored()).isEqualTo("silent")
    }

    @Test
    fun `toStored - Custom encodes as the URI string`() {
        assertThat(NotificationSound.Custom("content://x").toStored()).isEqualTo("content://x")
    }

    @Test
    fun `round-trip preserves every variant`() {
        val variants = listOf(
            NotificationSound.SystemDefault,
            NotificationSound.ElementDefault,
            NotificationSound.Silent,
            NotificationSound.Custom("content://media/42"),
        )
        for (sound in variants) {
            assertThat(NotificationSound.fromStored(sound.toStored())).isEqualTo(sound)
        }
    }
}

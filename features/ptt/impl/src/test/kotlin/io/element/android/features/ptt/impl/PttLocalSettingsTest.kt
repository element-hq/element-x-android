/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ptt.impl

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.tests.testutils.robolectric.RobolectricTest
import org.junit.Before
import org.junit.Test

class PttLocalSettingsTest : RobolectricTest() {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun clearSettings() {
        PttLocalSettings.preferences(context).edit().clear().commit()
    }

    @Test
    fun `connection fields are isolated by account and password is not persisted`() {
        val settings = PttLocalSettings(context)
        settings.saveServer(A_SESSION_ID, PttServerSettings(" mumble.example.org ", "64739", " alice ", "secret"))
        val saved = settings.server(A_SESSION_ID)
        val metadata = interimMumbleConfig(A_SESSION_ID, A_ROOM_ID, saved).transportMetadata
        assertThat(metadata["host"]).isEqualTo("mumble.example.org")
        assertThat(metadata["port"]).isEqualTo("64739")
        assertThat(metadata["username"]).isEqualTo("alice")
        assertThat(metadata["password"]).isEqualTo("secret")
        assertThat(settings.server(SessionId("@bob:example.org")).password).isEmpty()
        val restored = PttLocalSettings(context).server(A_SESSION_ID)
        assertThat(restored.host).isEqualTo("mumble.example.org")
        assertThat(restored.password).isEmpty()
        assertThat(PttLocalSettings.preferences(context).all.values).doesNotContain("secret")
        assertThat(PttServerSettings(password = "secret").toString()).doesNotContain("secret")
    }

    @Test
    fun `device toggles persist across settings instances`() {
        PttLocalSettings(context).apply {
            setOverlayEnabled(false)
            setLockScreenEnabled(false)
            setCovert(true)
        }
        val restored = PttLocalSettings(context)
        assertThat(restored.overlayEnabled.value).isFalse()
        assertThat(restored.lockScreenEnabled.value).isFalse()
        assertThat(restored.covert).isTrue()
    }

    @Test
    fun `manual server rejects absent host URLs and invalid ports`() {
        assertThat(PttServerSettings("", "64738").isValid).isFalse()
        assertThat(PttServerSettings("https://example.org", "64738").isValid).isFalse()
        for (port in listOf("0", "65536", "abc", "")) {
            assertThat(PttServerSettings("example.org", port).isValid).isFalse()
        }
        assertThat(PttServerSettings("example.org", "65535").isValid).isTrue()
        assertThat(PttServerSettings("::1", "64738").isValid).isTrue()
    }
}

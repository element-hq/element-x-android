/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications.sound

import android.content.Context
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.push.api.notifications.sound.NotificationSoundCopier
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class DefaultNotificationSoundCopierTest {
    private val context: Context get() = RuntimeEnvironment.getApplication()

    @Test
    fun `sanitizeDisplayName - trims surrounding whitespace`() {
        assertThat(sanitizeDisplayName("  Hello World  ")).isEqualTo("Hello World")
    }

    @Test
    fun `sanitizeDisplayName - DEL and control characters are dropped`() {
        // Build the raw string programmatically — embedding control chars as source-file bytes
        // doesn't survive editor / tool round-tripping reliably.
        val bel = 0x07.toChar()
        val tab = 0x09.toChar()
        val newline = 0x0A.toChar()
        val del = 0x7F.toChar()
        val raw = "${bel}Hello$tab${newline}World$del"
        assertThat(sanitizeDisplayName(raw)).isEqualTo("HelloWorld")
    }

    @Test
    fun `sanitizeDisplayName - truncates oversized input to 256 chars`() {
        val long = "x".repeat(500)
        val sanitized = sanitizeDisplayName(long)
        assertThat(sanitized).isNotNull()
        assertThat(sanitized!!.length).isEqualTo(256)
    }

    @Test
    fun `sanitizeDisplayName - returns null when nothing survives sanitization`() {
        assertThat(sanitizeDisplayName("")).isNull()
        assertThat(sanitizeDisplayName("    ")).isNull()
        val controls = (0..0x1F).joinToString(separator = "") { it.toChar().toString() } + 0x7F.toChar()
        assertThat(sanitizeDisplayName(controls)).isNull()
    }

    @Test
    fun `sanitizeDisplayName - preserves unicode beyond ASCII`() {
        val input = "Element Tone " + 0x266A.toChar()
        assertThat(sanitizeDisplayName(input)).isEqualTo(input)
    }

    @Test
    fun `copyToAppFiles - rejects file scheme`() = runTest {
        val copier = DefaultNotificationSoundCopier(context)
        val result = copier.copyToAppFiles("file:///tmp/evil.mp3", NotificationSoundCopier.SoundSlot.Message)
        assertThat(result).isEqualTo(NotificationSoundCopier.CopyResult.UnplayableSource)
    }

    @Test
    fun `copyToAppFiles - rejects http scheme`() = runTest {
        val copier = DefaultNotificationSoundCopier(context)
        val result = copier.copyToAppFiles("https://attacker.example.com/sound.mp3", NotificationSoundCopier.SoundSlot.Call)
        assertThat(result).isEqualTo(NotificationSoundCopier.CopyResult.UnplayableSource)
    }

    @Test
    fun `copyToAppFiles - rejects URI without a scheme`() = runTest {
        val copier = DefaultNotificationSoundCopier(context)
        val result = copier.copyToAppFiles("not-a-real-uri", NotificationSoundCopier.SoundSlot.Message)
        assertThat(result).isEqualTo(NotificationSoundCopier.CopyResult.UnplayableSource)
    }
}

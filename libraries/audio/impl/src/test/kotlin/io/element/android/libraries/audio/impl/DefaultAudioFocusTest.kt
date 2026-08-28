/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Copyright 2026 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.audio.impl

import android.media.AudioAttributes
import android.media.AudioManager
import androidx.core.content.getSystemService
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.audio.api.AudioFocusRequester
import io.element.android.tests.testutils.robolectric.RobolectricTest
import org.junit.Test
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf

class DefaultAudioFocusTest : RobolectricTest() {
    @Test
    fun `voice message playback requests focus for media, not for a call`() {
        assertThat(usageOf(AudioFocusRequester.VoiceMessage)).isEqualTo(AudioAttributes.USAGE_MEDIA)
    }

    @Test
    fun `media viewer playback requests focus for media`() {
        assertThat(usageOf(AudioFocusRequester.MediaViewer)).isEqualTo(AudioAttributes.USAGE_MEDIA)
    }

    @Test
    fun `voice message recording requests focus for voice communication`() {
        assertThat(usageOf(AudioFocusRequester.RecordVoiceMessage)).isEqualTo(AudioAttributes.USAGE_VOICE_COMMUNICATION)
    }

    @Test
    fun `element call requests focus for voice communication`() {
        assertThat(usageOf(AudioFocusRequester.ElementCall)).isEqualTo(AudioAttributes.USAGE_VOICE_COMMUNICATION)
    }

    @Test
    fun `a transient focus loss does not pause a voice message`() {
        assertThat(losesFocusOn(AudioFocusRequester.VoiceMessage, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT)).isFalse()
        assertThat(losesFocusOn(AudioFocusRequester.VoiceMessage, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK)).isFalse()
    }

    @Test
    fun `a permanent focus loss pauses a voice message`() {
        assertThat(losesFocusOn(AudioFocusRequester.VoiceMessage, AudioManager.AUDIOFOCUS_LOSS)).isTrue()
    }

    @Test
    fun `a transient focus loss does not stop a voice message recording`() {
        assertThat(losesFocusOn(AudioFocusRequester.RecordVoiceMessage, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT)).isFalse()
    }

    @Test
    fun `a transient focus loss still pauses the media viewer`() {
        assertThat(losesFocusOn(AudioFocusRequester.MediaViewer, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT)).isTrue()
    }

    private fun losesFocusOn(requester: AudioFocusRequester, focusChange: Int): Boolean {
        val context = RuntimeEnvironment.getApplication()
        val audioManager = requireNotNull(context.getSystemService<AudioManager>())
        var focusLost = false
        DefaultAudioFocus(context).requestAudioFocus(requester) { focusLost = true }
        val request = requireNotNull(shadowOf(audioManager).lastAudioFocusRequest)
        request.listener.onAudioFocusChange(focusChange)
        return focusLost
    }

    private fun usageOf(requester: AudioFocusRequester): Int {
        val context = RuntimeEnvironment.getApplication()
        val audioManager = requireNotNull(context.getSystemService<AudioManager>())
        DefaultAudioFocus(context).requestAudioFocus(requester) {}
        val request = requireNotNull(shadowOf(audioManager).lastAudioFocusRequest)
        return requireNotNull(request.audioFocusRequest).audioAttributes.usage
    }
}

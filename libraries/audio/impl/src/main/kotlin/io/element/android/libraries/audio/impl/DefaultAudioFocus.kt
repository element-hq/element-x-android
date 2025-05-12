/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.audio.impl

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import androidx.core.content.getSystemService
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.audio.api.AudioFocus
import io.element.android.libraries.audio.api.AudioFocusRequester
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultAudioFocus @Inject constructor(
    @ApplicationContext private val context: Context,
) : AudioFocus {
    private val audioManager = requireNotNull(context.getSystemService<AudioManager>())

    private var audioFocusRequest: AudioFocusRequest? = null
    private var audioFocusChangeListener: AudioManager.OnAudioFocusChangeListener? = null

    @Suppress("DEPRECATION")
    override fun requestAudioFocus(mode: AudioFocusRequester) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(mode.toAudioUsage())
                .build()
            val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(audioAttributes)
                .build()
            audioManager.requestAudioFocus(request)
            audioFocusRequest = request
        } else {
            val listener = AudioManager.OnAudioFocusChangeListener { }
            audioManager.requestAudioFocus(
                listener,
                mode.toAudioStream(),
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE,
            )
            audioFocusChangeListener = listener
        }
    }

    @Suppress("DEPRECATION")
    override fun releaseAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            audioFocusChangeListener?.let { audioManager.abandonAudioFocus(it) }
        }
    }
}

private fun AudioFocusRequester.toAudioUsage(): Int {
    return when (this) {
        AudioFocusRequester.ElementCall,
        AudioFocusRequester.VoiceMessage -> AudioAttributes.USAGE_VOICE_COMMUNICATION
        AudioFocusRequester.MediaViewer -> AudioAttributes.USAGE_MEDIA
    }
}

private fun AudioFocusRequester.toAudioStream(): Int {
    return when (this) {
        AudioFocusRequester.ElementCall,
        AudioFocusRequester.VoiceMessage -> AudioManager.STREAM_VOICE_CALL
        AudioFocusRequester.MediaViewer -> AudioManager.STREAM_MUSIC
    }
}

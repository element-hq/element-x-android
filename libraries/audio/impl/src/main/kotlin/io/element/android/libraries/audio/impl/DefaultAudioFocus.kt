/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.audio.impl

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import androidx.core.content.getSystemService
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.audio.api.AudioFocus
import io.element.android.libraries.audio.api.AudioFocusRequester
import io.element.android.libraries.di.annotations.ApplicationContext

@ContributesBinding(AppScope::class)
class DefaultAudioFocus(
    @ApplicationContext private val context: Context,
) : AudioFocus {
    private val audioManager = requireNotNull(context.getSystemService<AudioManager>())

    private var audioFocusRequest: AudioFocusRequest? = null
    private var audioFocusChangeListener: AudioManager.OnAudioFocusChangeListener? = null

    @Suppress("DEPRECATION")
    override fun requestAudioFocus(
        requester: AudioFocusRequester,
        onFocusLost: () -> Unit,
    ) {
        val listener = AudioManager.OnAudioFocusChangeListener {
            when (it) {
                AudioManager.AUDIOFOCUS_GAIN -> {
                    // Do nothing
                }
                AudioManager.AUDIOFOCUS_LOSS,
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                    onFocusLost()
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(requester.toAudioUsage())
                .build()
            val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(audioAttributes)
                .setOnAudioFocusChangeListener(listener)
                .setWillPauseWhenDucked(requester.willPausedWhenDucked())
                .build()
            audioManager.requestAudioFocus(request)
            audioFocusRequest = request
        } else {
            audioManager.requestAudioFocus(
                listener,
                requester.toAudioStream(),
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

private fun AudioFocusRequester.willPausedWhenDucked(): Boolean {
    return when (this) {
        // (note that for Element Call, there is no action when the focus is lost)
        AudioFocusRequester.ElementCall,
        AudioFocusRequester.VoiceMessage -> true
        // For the MediaViewer, we let the system automatically handle the ducking
        // https://developer.android.com/media/optimize/audio-focus#automatic-ducking
        AudioFocusRequester.MediaViewer -> false
    }
}

/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ptt.impl.services

import android.content.Context
import android.content.Intent
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.view.KeyEvent
import androidx.core.content.IntentCompat
import io.element.android.libraries.core.log.logger.LoggerTag
import timber.log.Timber

private val loggerTag = LoggerTag("PttMediaButton")

/**
 * Routes Bluetooth / wired PTT-accessory media-button events to transmit control (Pryme, AINA, and
 * similar hold-to-talk buttons emulate media keys). A [MediaSession] marked active with a "playing"
 * [PlaybackState] becomes the system's media-button target, so the accessory's press/release
 * (KeyEvent down/up) drives [onKeyDown]/[onKeyUp] even while the app is backgrounded or locked.
 *
 * Owned by the foreground [PttSessionHostService] for the lifetime of a session. Vendor-specific
 * rugged-device PTT keys (Sonim/Kyocera broadcasts) are a separate, per-OEM follow-on.
 */
class PttMediaButtonController(
    context: Context,
    private val onKeyDown: () -> Unit,
    private val onKeyUp: () -> Unit,
) {
    private val session = MediaSession(context, "ElementPtt").apply {
        setCallback(object : MediaSession.Callback() {
            override fun onMediaButtonEvent(mediaButtonIntent: Intent): Boolean {
                val keyEvent = IntentCompat.getParcelableExtra(
                    mediaButtonIntent,
                    Intent.EXTRA_KEY_EVENT,
                    KeyEvent::class.java,
                ) ?: return super.onMediaButtonEvent(mediaButtonIntent)

                val handled = keyEvent.keyCode in PTT_KEYCODES
                // Log every media key so an unrecognised PTT accessory's keycode is easy to identify
                // (add it to PTT_KEYCODES). `adb logcat | grep PttMediaButton`.
                Timber.tag(loggerTag.value).d(
                    "media key: keyCode=%d (%s) action=%d handled=%b",
                    keyEvent.keyCode,
                    KeyEvent.keyCodeToString(keyEvent.keyCode),
                    keyEvent.action,
                    handled,
                )
                if (!handled) {
                    return super.onMediaButtonEvent(mediaButtonIntent)
                }
                when (keyEvent.action) {
                    KeyEvent.ACTION_DOWN -> if (keyEvent.repeatCount == 0) onKeyDown()
                    KeyEvent.ACTION_UP -> onKeyUp()
                }
                return true
            }
        })
        // A "playing" state makes this the preferred media-button target.
        setPlaybackState(
            PlaybackState.Builder()
                .setActions(PlaybackState.ACTION_PLAY_PAUSE)
                .setState(PlaybackState.STATE_PLAYING, PlaybackState.PLAYBACK_POSITION_UNKNOWN, 1f)
                .build()
        )
        isActive = true
    }

    fun release() {
        session.isActive = false
        session.release()
    }

    private companion object {
        val PTT_KEYCODES = setOf(
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
            KeyEvent.KEYCODE_MEDIA_PLAY,
            KeyEvent.KEYCODE_MEDIA_PAUSE,
            KeyEvent.KEYCODE_HEADSETHOOK,
            KeyEvent.KEYCODE_MEDIA_RECORD,
        )
    }
}

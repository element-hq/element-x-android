/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.voiceplayer.impl

import android.content.Context
import android.net.Uri
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.di.annotations.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import kotlin.coroutines.resume
import android.media.MediaPlayer as AndroidMediaPlayer

private const val PLAYBACK_TIMEOUT_MS = 1_000L
private const val NOTIFICATION_SOUND_RES_NAME = "message"

@ContributesBinding(RoomScope::class)
class DefaultTransitionSoundPlayer(
    @ApplicationContext private val context: Context,
) : TransitionSoundPlayer {
    override suspend fun playAndAwait() {
        try {
            val resId = context.resources.getIdentifier(NOTIFICATION_SOUND_RES_NAME, "raw", context.packageName)
            if (resId == 0) {
                Timber.tag(TAG).e("Notification sound resource not found")
                return
            }
            val completed = withTimeoutOrNull(PLAYBACK_TIMEOUT_MS) {
                suspendCancellableCoroutine { continuation ->
                    val soundUri = Uri.parse("android.resource://${context.packageName}/$resId")
                    val player = AndroidMediaPlayer.create(context, soundUri)
                    if (player == null) {
                        Timber.tag(TAG).e("Failed to create MediaPlayer for transition sound")
                        continuation.resume(Unit)
                        return@suspendCancellableCoroutine
                    }
                    Timber.tag(TAG).d("Playing transition sound (duration=%dms)", player.duration)
                    player.setOnCompletionListener { mp ->
                        Timber.tag(TAG).d("Transition sound completed")
                        mp.release()
                        continuation.resume(Unit)
                    }
                    player.setOnErrorListener { mp, what, extra ->
                        Timber.tag(TAG).e("Transition sound error: what=$what, extra=$extra")
                        mp.release()
                        continuation.resume(Unit)
                        true
                    }
                    continuation.invokeOnCancellation {
                        player.release()
                    }
                    player.start()
                }
            }
            if (completed == null) {
                Timber.tag(TAG).e("Transition sound timed out after %dms", PLAYBACK_TIMEOUT_MS)
            }
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to play transition sound")
        }
    }

    companion object {
        private const val TAG = "TransitionSound"
    }
}

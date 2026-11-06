/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.callnative.impl

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.call.api.ElementCallData
import io.element.android.call.api.rtc.id.RoomId
import io.element.android.call.ui.ElementCallActivity
import io.element.android.features.call.api.CallData
import io.element.android.features.callnative.api.NativeCallEntryPoint
import io.element.android.libraries.di.annotations.AppCoroutineScope
import io.element.android.libraries.di.annotations.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import kotlin.time.Duration.Companion.seconds

private val CALL_STATE_TIMEOUT = 5.seconds

/**
 * Starts a call on the native MatrixRTC stack.
 *
 * App-scoped, while the stack it drives is per session: the session comes from the call rather than
 * from whatever is on screen, so answering a notification for a second account reaches that account's
 * stack.
 */
@ContributesBinding(AppScope::class)
class DefaultNativeCallEntryPoint(
    @ApplicationContext private val context: Context,
    private val controllers: ElementCallControllers,
    @AppCoroutineScope private val appCoroutineScope: CoroutineScope,
) : NativeCallEntryPoint {
    override fun startCall(callData: CallData) {
        appCoroutineScope.launch {
            val controller = controllers.getOrBuild(callData.sessionId) ?: return@launch
            controller.startCall(
                ElementCallData(
                    roomId = RoomId(callData.roomId.value),
                    isAudioCall = callData.isAudioCall,
                    // Temporary, to be removed from the library soon: the core already skips the ring when anyone else is in the call.
                    isStartingCall = true,
                )
            )
            // The call screen asks for the microphone itself, and is where the answer normally comes
            // from. This covers the case it cannot reach: a call answered from a notification while
            // the app is in the background has no call screen composed yet, and a call waiting on a
            // permission nobody is there to grant would sit unanswered. Reporting a permission we
            // already hold is idempotent, so the screen asking again later costs nothing.
            if (hasMicrophonePermission()) {
                controller.setMicrophonePermissionGranted(true)
            }
            // The call screen is an Activity of its own, and it closes at once when it finds no call, so it
            // is opened only once the controller has one. From the app the overlay would open it as well;
            // answering from the ringing screen or its notification has no overlay in front, so this is
            // what shows the call there, over the lock screen if need be. It is `singleTask`, so the
            // overlay opening it too brings back the same screen.
            val hasCall = withTimeoutOrNull(CALL_STATE_TIMEOUT) { controller.state.first { it != null } } != null
            if (hasCall) {
                ElementCallActivity.start(context)
            } else {
                Timber.w("NativeCall: no call state after $CALL_STATE_TIMEOUT, not opening the call screen")
            }
        }
    }

    private fun hasMicrophonePermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
}

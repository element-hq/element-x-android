/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.callnative.impl

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.call.api.ElementCallData
import io.element.android.call.api.rtc.id.RoomId
import io.element.android.features.call.api.CallData
import io.element.android.features.callnative.api.NativeCallEntryPoint
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.di.annotations.AppCoroutineScope
import io.element.android.libraries.di.annotations.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

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
            // The call screen is an overlay of the main Activity, unlike the WebView which has its own.
            // A call answered from a notification or the ringing screen is started by
            // IncomingCallActivity, which then finishes: without this the call would run with nothing
            // on screen to show it. Not guarded on the app being in the foreground: the ringing screen
            // is what keeps it there, and it is about to finish.
            bringAppToFront()
        }
    }

    private fun hasMicrophonePermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

    /**
     * Uses the launcher intent so this module does not depend on the app's Activity. The main Activity
     * is single task, so this brings the existing task forward rather than starting a new one, and a
     * launcher intent carries no deep link, so it leaves the navigation where it is when the Activity
     * is already on top.
     */
    private fun bringAppToFront() {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        if (intent == null) {
            Timber.w("NativeCall: no launch intent, cannot show the call screen")
            return
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatchingExceptions { context.startActivity(intent) }
            .onFailure { Timber.w(it, "NativeCall: cannot bring the app to the front") }
    }
}

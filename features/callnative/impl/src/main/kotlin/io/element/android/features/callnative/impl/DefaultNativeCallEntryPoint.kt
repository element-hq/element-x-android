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
import io.element.android.features.call.api.CallData
import io.element.android.features.callnative.api.NativeCallEntryPoint
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
        // The call does nothing past "requested" until the microphone permission is answered, and
        // only an Activity can ask for one. Until the call has a UI of its own, the only answer
        // available is the permission the app already holds - so check it before starting rather
        // than after. Starting a call nobody can answer leaves it running and refusing every later
        // attempt for the rest of the session, with nothing on screen to hang it up.
        if (!hasMicrophonePermission()) {
            Timber.w("NativeCall: RECORD_AUDIO is not granted and there is no call UI to ask for it, not starting the call")
            return
        }
        appCoroutineScope.launch {
            val controller = controllers.getOrBuild(callData.sessionId) ?: return@launch
            controller.startCall(
                ElementCallData(
                    roomId = RoomId(callData.roomId.value),
                    isAudioCall = callData.isAudioCall,
                )
            )
            controller.setMicrophonePermissionGranted(true)
            // A call with no UI is never the screen the user is looking at, and the snapshot assumes
            // it is. Saying so matters for more than tidiness: the component holds a proximity wake
            // lock while a maximized audio call is on screen, so leaving the default in place blanks
            // the display whenever a hand goes near the top of the phone - including when reaching
            // for the notification shade, which is the only way to hang this call up.
            controller.setMaximized(false)
        }
    }

    private fun hasMicrophonePermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
}

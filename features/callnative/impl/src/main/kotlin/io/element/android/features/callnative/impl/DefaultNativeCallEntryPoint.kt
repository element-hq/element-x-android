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
        }
    }

    private fun hasMicrophonePermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
}

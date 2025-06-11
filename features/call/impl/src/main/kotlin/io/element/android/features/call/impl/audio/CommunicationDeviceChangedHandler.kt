/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.call.impl.audio

import android.media.AudioManager
import android.os.Build
import androidx.annotation.RequiresApi
import timber.log.Timber
import java.util.concurrent.Executors

open class CommunicationDeviceChangedHandler {
    open fun start(listener: CommunicationDeviceChangedHandlerListener) = Unit
    open fun stop() = Unit
}

interface CommunicationDeviceChangedHandlerListener {
    fun onCommunicationDeviceChange(deviceId: Int?)
}

@RequiresApi(Build.VERSION_CODES.S)
class CommunicationDeviceChangedHandlerImpl31(
    private val audioManager: AudioManager,
) : CommunicationDeviceChangedHandler() {
    private var listener: CommunicationDeviceChangedHandlerListener? = null

    /**
     * This listener tracks the current communication device and invokes the listener when it changes.
     */
    private val commsDeviceChangedListener by lazy {
        AudioManager.OnCommunicationDeviceChangedListener { device ->
            if (device != null) {
                Timber.d("Audio device changed, type: ${device.type}")
            } else {
                Timber.d("Audio device cleared")
            }
            listener?.onCommunicationDeviceChange(device?.id)
        }
    }

    override fun start(listener: CommunicationDeviceChangedHandlerListener) {
        this.listener = listener
        audioManager.addOnCommunicationDeviceChangedListener(Executors.newSingleThreadExecutor(), commsDeviceChangedListener)
    }

    override fun stop() {
        audioManager.clearCommunicationDevice()
        audioManager.removeOnCommunicationDeviceChangedListener(commsDeviceChangedListener)
        listener = null
    }
}

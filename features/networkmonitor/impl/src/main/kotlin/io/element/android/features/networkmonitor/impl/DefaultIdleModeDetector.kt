/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.networkmonitor.impl

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.PowerManager
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.networkmonitor.api.IdleModeDetector
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.di.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DefaultIdleModeDetector @Inject constructor(
    @ApplicationContext context: Context,
) : IdleModeDetector {
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager

    private val idleModeFlow = MutableStateFlow<Boolean>(isDeviceInIdleMode())

    init {
        context.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                idleModeFlow.value = isDeviceInIdleMode()
            }
        }, IntentFilter().apply {
            addAction(PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                addAction(PowerManager.ACTION_DEVICE_LIGHT_IDLE_MODE_CHANGED)
            }
        })
    }

    override fun isDeviceInIdleMode(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            powerManager.isDeviceIdleMode || powerManager.isDeviceLightIdleMode
        } else {
            powerManager.isDeviceIdleMode
        }
    }

    override fun subscribeToIdleMode(): Flow<Boolean> {
        return idleModeFlow
    }
}

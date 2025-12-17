/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rageshake.impl.rageshake

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.core.content.getSystemService
import com.squareup.seismic.ShakeDetector
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import io.element.android.libraries.di.annotations.ApplicationContext

@SingleIn(AppScope::class)
@ContributesBinding(scope = AppScope::class, binding = binding<RageShake>())
class DefaultRageShake(
    @ApplicationContext context: Context,
) : ShakeDetector.Listener, RageShake {
    private var sensorManager = context.getSystemService<SensorManager>()
    private var shakeDetector: ShakeDetector? = null
    private var interceptor: (() -> Unit)? = null

    override fun setInterceptor(interceptor: (() -> Unit)?) {
        this.interceptor = interceptor
    }

    /**
     * Check if the feature is available on this device.
     */
    override fun isAvailable(): Boolean {
        return sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null
    }

    override fun start(sensitivity: Float) {
        sensorManager?.let {
            shakeDetector = ShakeDetector(this).apply {
                start(it, SensorManager.SENSOR_DELAY_GAME)
            }
            setSensitivity(sensitivity)
        }
    }

    override fun stop() {
        shakeDetector?.stop()
    }

    /**
     * sensitivity will be {0, O.25, 0.5, 0.75, 1} and converted to
     * [ShakeDetector.SENSITIVITY_LIGHT (=11), ShakeDetector.SENSITIVITY_HARD (=15)].
     */
    override fun setSensitivity(sensitivity: Float) {
        shakeDetector?.setSensitivity(
            ShakeDetector.SENSITIVITY_LIGHT + (sensitivity * 4).toInt()
        )
    }

    override fun hearShake() {
        interceptor?.invoke()
    }
}

/*
 * Copyright (c) 2022 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.rageshake.impl.rageshake

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.core.content.getSystemService
import com.squareup.anvil.annotations.ContributesBinding
import com.squareup.seismic.ShakeDetector
import io.element.android.features.rageshake.api.rageshake.RageShake
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.di.SingleIn
import javax.inject.Inject

@SingleIn(AppScope::class)
@ContributesBinding(scope = AppScope::class, boundType = RageShake::class)
class DefaultRageShake @Inject constructor(
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

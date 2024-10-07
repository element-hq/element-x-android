/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.rageshake.api.rageshake

interface RageShake {
    /**
     * Check if the feature is available on this device.
     */
    fun isAvailable(): Boolean

    fun start(sensitivity: Float)

    fun stop()

    /**
     * sensitivity will be {0, O.25, 0.5, 0.75, 1} and converted to
     * [ShakeDetector.SENSITIVITY_LIGHT (=11), ShakeDetector.SENSITIVITY_HARD (=15)].
     */
    fun setSensitivity(sensitivity: Float)

    fun setInterceptor(interceptor: (() -> Unit)?)
}

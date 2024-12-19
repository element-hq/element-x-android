/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.networkmonitor.api

import kotlinx.coroutines.flow.Flow

interface IdleModeDetector {
    /** Returns true if the device is in idle mode (doze), false otherwise. */
    fun isDeviceInIdleMode(): Boolean

    /** Subscribes to the changes in idle mode (doze). */
    fun subscribeToIdleMode(): Flow<Boolean>
}

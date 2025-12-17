/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.lockscreen.impl.pin

import kotlinx.coroutines.flow.Flow

/**
 * This interface is the main interface to manage the pin code.
 * Implementation should take care of encrypting the pin code and storing it.
 */
interface PinCodeManager {
    /**
     * Callbacks for pin code management events.
     */
    interface Callback {
        /**
         * Called when the pin code is verified.
         */
        fun onPinCodeVerified()

        /**
         * Called when the pin code is created.
         */
        fun onPinCodeCreated()

        /**
         * Called when the pin code is removed.
         */
        fun onPinCodeRemoved()
    }

    /**
     * Register a callback to be notified of pin code management events.
     */
    fun addCallback(callback: Callback)

    /**
     * Unregister callback to be notified of pin code management events.
     */
    fun removeCallback(callback: Callback)

    /**
     * @return true if a pin code is available.
     */
    fun hasPinCode(): Flow<Boolean>

    /**
     * @return the size of the saved pin code.
     */
    suspend fun getPinCodeSize(): Int

    /**
     * Creates a new encrypted pin code.
     * @param pinCode the clear pin code to create
     */
    suspend fun createPinCode(pinCode: String)

    /**
     * @return true if the pin code is correct.
     */
    suspend fun verifyPinCode(pinCode: String): Boolean

    /**
     * Deletes the previously created pin code.
     */
    suspend fun deletePinCode()

    /**
     * @return the number of remaining attempts before the pin code is blocked.
     */
    suspend fun getRemainingPinCodeAttemptsNumber(): Int
}

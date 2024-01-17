/*
 * Copyright (c) 2023 New Vector Ltd
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

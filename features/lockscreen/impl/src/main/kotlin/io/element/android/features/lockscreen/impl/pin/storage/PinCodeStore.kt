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

package io.element.android.features.lockscreen.impl.pin.storage

interface PinCodeStore : EncryptedPinCodeStorage {

    /**
     * Returns the remaining PIN code attempts. When this reaches 0 the PIN code access won't be available for some time.
     */
    suspend fun getRemainingPinCodeAttemptsNumber(): Int

    /**
     * Should decrement the number of remaining PIN code attempts.
     */
    suspend fun onWrongPin()

    /**
     * Resets the counter of attempts for PIN code and biometric access.
     */
    suspend fun resetCounter()
}



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

package io.element.android.features.pin.impl.pin.storage

import android.content.SharedPreferences
import androidx.core.content.edit
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.SingleIn
import kotlinx.coroutines.withContext
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject

private const val ENCODED_PIN_CODE_KEY = "ENCODED_PIN_CODE_KEY"
private const val REMAINING_PIN_CODE_ATTEMPTS_KEY = "REMAINING_PIN_CODE_ATTEMPTS_KEY"
private const val MAX_PIN_CODE_ATTEMPTS_NUMBER_BEFORE_LOGOUT = 3

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class SharedPreferencesPinCodeStore @Inject constructor(
    private val dispatchers: CoroutineDispatchers,
    private val sharedPreferences: SharedPreferences,
) : PinCodeStore {

    private val listeners = CopyOnWriteArrayList<PinCodeStore.Listener>()

    override suspend fun getEncryptedCode(): String? = withContext(dispatchers.io) {
        sharedPreferences.getString(ENCODED_PIN_CODE_KEY, null)
    }

    override suspend fun saveEncryptedPinCode(pinCode: String) = withContext(dispatchers.io) {
        sharedPreferences.edit {
            putString(ENCODED_PIN_CODE_KEY, pinCode)
        }
        withContext(dispatchers.main) {
            listeners.forEach { it.onPinSetUpChange(isConfigured = true) }
        }
    }

    override suspend fun deleteEncryptedPinCode() = withContext(dispatchers.io) {
        // Also reset the counters
        resetCounter()
        sharedPreferences.edit {
            remove(ENCODED_PIN_CODE_KEY)
        }
        withContext(dispatchers.main) {
            listeners.forEach { it.onPinSetUpChange(isConfigured = false) }
        }
    }

    override suspend fun hasPinCode(): Boolean = withContext(dispatchers.io) {
        sharedPreferences.contains(ENCODED_PIN_CODE_KEY)
    }

    override suspend fun getRemainingPinCodeAttemptsNumber(): Int = withContext(dispatchers.io) {
        sharedPreferences.getInt(REMAINING_PIN_CODE_ATTEMPTS_KEY, MAX_PIN_CODE_ATTEMPTS_NUMBER_BEFORE_LOGOUT)
    }

    override suspend fun onWrongPin(): Int = withContext(dispatchers.io) {
        val remaining = getRemainingPinCodeAttemptsNumber() - 1
        sharedPreferences.edit {
            putInt(REMAINING_PIN_CODE_ATTEMPTS_KEY, remaining)
        }
        remaining
    }

    override suspend fun resetCounter() = withContext(dispatchers.io) {
        sharedPreferences.edit {
            remove(REMAINING_PIN_CODE_ATTEMPTS_KEY)
        }
    }

    override fun addListener(listener: PinCodeStore.Listener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: PinCodeStore.Listener) {
        listeners.remove(listener)
    }
}
